package com.hottakeranker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hottakeranker.dto.TopicResultResponse;
import com.hottakeranker.entity.Topic;
import com.hottakeranker.enums.TopicStatus;
import com.hottakeranker.repository.TopicRepository;
import com.hottakeranker.security.JwtAuthFilter;
import com.hottakeranker.security.JwtTokenProvider;
import com.hottakeranker.service.ControversyService;
import com.hottakeranker.service.DemographicService;
import com.hottakeranker.service.RankingAggregationService;
import com.hottakeranker.service.TopicSuggestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TopicController.class)
@AutoConfigureMockMvc(addFilters = false)
class TopicControllerTest {

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@MockitoBean
	private TopicRepository topicRepository;

	@MockitoBean
	private RankingAggregationService rankingService;

	@MockitoBean
	private DemographicService demographicService;

	@MockitoBean
	private TopicSuggestionService suggestionService;

	@MockitoBean
	private ControversyService controversyService;

	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;

	@MockitoBean
	private JwtAuthFilter jwtAuthFilter;

	@Test
	void getActiveTopics_returns200WithTopics() throws Exception {
		Topic topic = new Topic("Best pizza?", "Food",
				List.of("A","B","C","D","E","F","G","H"), TopicStatus.ACTIVE);
		topic.setId(1L);

		when(topicRepository.findByStatus(TopicStatus.ACTIVE)).thenReturn(List.of(topic));

		mockMvc.perform(get("/api/topics"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].question").value("Best pizza?"));
	}

	@Test
	void getResults_returns200WithResults() throws Exception {
		TopicResultResponse response = new TopicResultResponse();
		response.setTopicId(1L);
		response.setQuestion("Best pizza?");
		response.setCrowdRanking(List.of("A","B","C","D","E","F","G","H"));
		response.setTotalVotes(10);
		response.setScores(Map.of("A", 80, "B", 70));

		when(rankingService.getResults(1L)).thenReturn(response);

		mockMvc.perform(get("/api/topics/1/results"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.topicId").value(1))
				.andExpect(jsonPath("$.totalVotes").value(10));
	}

	@Test
	void suggest_optionsNotEight_returns400() throws Exception {
		Map<String, Object> request = Map.of(
				"question", "Best pizza?",
				"category", "Food",
				"options", List.of("A", "B", "C", "D")
		);

		mockMvc.perform(post("/api/topics/suggest")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void suggest_duplicateOptions_returns400() throws Exception {
		Map<String, Object> request = Map.of(
				"question", "Best pizza?",
				"category", "Food",
				"options", List.of("A", "A", "C", "D", "E", "F", "G", "H")
		);

		mockMvc.perform(post("/api/topics/suggest")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}
}
