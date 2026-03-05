package com.hottakeranker.service;

import com.hottakeranker.dto.ControversialTopicDto;
import com.hottakeranker.dto.TopicResultResponse;
import com.hottakeranker.entity.Topic;
import com.hottakeranker.entity.Vote;
import com.hottakeranker.enums.TopicStatus;
import com.hottakeranker.repository.TopicRepository;
import com.hottakeranker.repository.VoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ControversyServiceTest {

	@Mock
	private TopicRepository topicRepository;

	@Mock
	private VoteRepository voteRepository;

	@Mock
	private RankingAggregationService rankingService;

	@InjectMocks
	private ControversyService controversyService;

	private Topic createTopic(Long id, String question) {
		Topic topic = new Topic(question, "Food",
				List.of("A", "B", "C", "D", "E", "F", "G", "H"),
				TopicStatus.ACTIVE);
		topic.setId(id);
		return topic;
	}

	private List<Vote> createVotes(Long topicId, int count, List<Integer> ranking) {
		List<Vote> votes = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			votes.add(new Vote((long) (i + 1), topicId, ranking));
		}
		return votes;
	}

	@Test
	void unanimousVotes_lowControversyScore() {
		Topic topic = createTopic(1L, "Easy question");
		// All 10 voters agree on the same ranking
		List<Vote> votes = createVotes(1L, 10, List.of(0, 1, 2, 3, 4, 5, 6, 7));

		when(topicRepository.findByStatus(TopicStatus.ACTIVE)).thenReturn(List.of(topic));
		when(voteRepository.findByTopicId(1L)).thenReturn(votes);

		// Unanimous: scores will be {80, 70, 60, 50, 40, 30, 20, 10} — high stddev
		TopicResultResponse response = new TopicResultResponse();
		response.setScores(java.util.Map.of(
				"A", 80, "B", 70, "C", 60, "D", 50,
				"E", 40, "F", 30, "G", 20, "H", 10
		));
		when(rankingService.aggregateVotes(eq(topic), any())).thenReturn(response);

		List<ControversialTopicDto> result = controversyService.getControversialTopics();

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getControversyScore()).isLessThan(20.0);
	}

	@Test
	void evenlySpitVotes_highControversyScore() {
		Topic topic = createTopic(1L, "Divisive question");
		List<Vote> votes = createVotes(1L, 10, List.of(0, 1, 2, 3, 4, 5, 6, 7));

		when(topicRepository.findByStatus(TopicStatus.ACTIVE)).thenReturn(List.of(topic));
		when(voteRepository.findByTopicId(1L)).thenReturn(votes);

		// Evenly split: all options have the same score
		TopicResultResponse response = new TopicResultResponse();
		response.setScores(java.util.Map.of(
				"A", 45, "B", 45, "C", 45, "D", 45,
				"E", 45, "F", 45, "G", 45, "H", 45
		));
		when(rankingService.aggregateVotes(eq(topic), any())).thenReturn(response);

		List<ControversialTopicDto> result = controversyService.getControversialTopics();

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getControversyScore()).isEqualTo(100.0);
	}

	@Test
	void topicsWithFewerThan10Votes_filtered() {
		Topic topic = createTopic(1L, "Unpopular question");
		List<Vote> votes = createVotes(1L, 5, List.of(0, 1, 2, 3, 4, 5, 6, 7));

		when(topicRepository.findByStatus(TopicStatus.ACTIVE)).thenReturn(List.of(topic));
		when(voteRepository.findByTopicId(1L)).thenReturn(votes);

		List<ControversialTopicDto> result = controversyService.getControversialTopics();

		assertThat(result).isEmpty();
	}

	@Test
	void multipleTopics_sortedByControversyDescending() {
		Topic topic1 = createTopic(1L, "Consensus topic");
		Topic topic2 = createTopic(2L, "Controversial topic");
		List<Vote> votes = createVotes(1L, 10, List.of(0, 1, 2, 3, 4, 5, 6, 7));

		when(topicRepository.findByStatus(TopicStatus.ACTIVE)).thenReturn(List.of(topic1, topic2));
		when(voteRepository.findByTopicId(1L)).thenReturn(votes);
		when(voteRepository.findByTopicId(2L)).thenReturn(createVotes(2L, 10, List.of(0, 1, 2, 3, 4, 5, 6, 7)));

		// Topic 1: unanimous (low controversy)
		TopicResultResponse response1 = new TopicResultResponse();
		response1.setScores(java.util.Map.of(
				"A", 80, "B", 70, "C", 60, "D", 50,
				"E", 40, "F", 30, "G", 20, "H", 10
		));
		when(rankingService.aggregateVotes(eq(topic1), any())).thenReturn(response1);

		// Topic 2: evenly split (high controversy)
		TopicResultResponse response2 = new TopicResultResponse();
		response2.setScores(java.util.Map.of(
				"A", 45, "B", 45, "C", 45, "D", 45,
				"E", 45, "F", 45, "G", 45, "H", 45
		));
		when(rankingService.aggregateVotes(eq(topic2), any())).thenReturn(response2);

		List<ControversialTopicDto> result = controversyService.getControversialTopics();

		assertThat(result).hasSize(2);
		assertThat(result.get(0).getQuestion()).isEqualTo("Controversial topic");
		assertThat(result.get(1).getQuestion()).isEqualTo("Consensus topic");
		assertThat(result.get(0).getControversyScore()).isGreaterThan(result.get(1).getControversyScore());
	}
}
