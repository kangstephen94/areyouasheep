package com.hottakeranker.controller;

import com.hottakeranker.dto.ControversialTopicDto;
import com.hottakeranker.dto.DemographicFilterRequest;
import com.hottakeranker.dto.TopicResultResponse;
import com.hottakeranker.dto.TopicSuggestionRequest;
import com.hottakeranker.entity.Topic;
import com.hottakeranker.entity.TopicSuggestion;
import com.hottakeranker.enums.AgeGroup;
import com.hottakeranker.enums.Ethnicity;
import com.hottakeranker.enums.Gender;
import com.hottakeranker.enums.PoliticalView;
import com.hottakeranker.enums.Region;
import com.hottakeranker.enums.RelationshipStatus;
import com.hottakeranker.enums.ReligiousView;
import com.hottakeranker.enums.TopicStatus;
import com.hottakeranker.repository.TopicRepository;
import com.hottakeranker.service.ControversyService;
import com.hottakeranker.service.DemographicService;
import com.hottakeranker.service.RankingAggregationService;
import com.hottakeranker.service.TopicSuggestionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping("/api/topics")
public class TopicController {
	private final TopicRepository topicRepository;
	private final RankingAggregationService rankingService;
	private final DemographicService demographicService;
	private final TopicSuggestionService suggestionService;
	private final ControversyService controversyService;

	public TopicController(TopicRepository topicRepository, RankingAggregationService rankingService,
						   DemographicService demographicService, TopicSuggestionService suggestionService,
						   ControversyService controversyService) {
		this.topicRepository = topicRepository;
		this.rankingService = rankingService;
		this.demographicService = demographicService;
		this.suggestionService = suggestionService;
		this.controversyService = controversyService;
	}

	@GetMapping
	public ResponseEntity<List<Topic>> getActiveTopics() {
		List<Topic> topics = topicRepository.findByStatus(TopicStatus.ACTIVE);
		return ResponseEntity.ok(topics);
	}

	@GetMapping("/controversial")
	public ResponseEntity<List<ControversialTopicDto>> getControversialTopics() {
		return ResponseEntity.ok(controversyService.getControversialTopics());
	}

	@GetMapping("/{id}/results")
	public ResponseEntity<TopicResultResponse> getResults(@PathVariable Long id) {
		TopicResultResponse results = rankingService.getResults(id);
		return ResponseEntity.ok(results);
	}

	@GetMapping("/{id}/results/demographics")
	public ResponseEntity<TopicResultResponse> getDemographicResults(
			@PathVariable Long id,
			@RequestParam(required = false) Gender gender,
			@RequestParam(required = false) AgeGroup ageGroup,
			@RequestParam(required = false) Region region,
			@RequestParam(required = false) Ethnicity ethnicity,
			@RequestParam(required = false) ReligiousView religiousView,
			@RequestParam(required = false) PoliticalView politicalView,
			@RequestParam(required = false) RelationshipStatus relationshipStatus) {

		DemographicFilterRequest filter = new DemographicFilterRequest();
		filter.setGender(gender);
		filter.setAgeGroup(ageGroup);
		filter.setRegion(region);
		filter.setEthnicity(ethnicity);
		filter.setReligiousView(religiousView);
		filter.setPoliticalView(politicalView);
		filter.setRelationshipStatus(relationshipStatus);

		TopicResultResponse results = demographicService.getFilteredResults(id, filter);
		return ResponseEntity.ok(results);
	}

	@PreAuthorize("isAuthenticated()")
	@PostMapping("/suggest")
	public ResponseEntity<?> suggest(@RequestBody TopicSuggestionRequest request) {
		if (request.getOptions() == null || request.getOptions().size() != 8) {
			return ResponseEntity.badRequest().body("Options must be exactly 8 items");
		}
		if (new HashSet<>(request.getOptions()).size() != 8) {
			return ResponseEntity.badRequest().body("Options must not contain duplicates");
		}
		Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
		TopicSuggestion suggestion = suggestionService.submitSuggestion(userId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(suggestion);
	}

	@GetMapping("/suggestions")
	public ResponseEntity<List<TopicSuggestion>> getSuggestions() {
		// TODO: Call suggestionService.getPendingSuggestions()
		List<TopicSuggestion> suggestions = suggestionService.getPendingSuggestions();
		return ResponseEntity.ok(suggestions);
	}

	@PreAuthorize("isAuthenticated()")
	@PostMapping("/suggestions/{id}/upvote")
	public ResponseEntity<Void> upvote(@PathVariable Long id) {
		// TODO: Pull userId from SecurityContext, call suggestionService.upvote()
		Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
		suggestionService.upvote(userId, id);
		return ResponseEntity.ok().build();
	}
}
