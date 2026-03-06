package com.hottakeranker.controller;

import com.hottakeranker.dto.AdminTopicRequest;
import com.hottakeranker.entity.Topic;
import com.hottakeranker.entity.TopicSuggestion;
import com.hottakeranker.enums.SuggestionStatus;
import com.hottakeranker.enums.TopicStatus;
import com.hottakeranker.repository.TopicRepository;
import com.hottakeranker.repository.TopicSuggestionRepository;
import com.hottakeranker.service.EloService;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

	private final EloService eloService;
	private final TopicRepository topicRepository;
	private final TopicSuggestionRepository suggestionRepository;

	public AdminController(EloService eloService, TopicRepository topicRepository,
			TopicSuggestionRepository suggestionRepository) {
		this.eloService = eloService;
		this.topicRepository = topicRepository;
		this.suggestionRepository = suggestionRepository;
	}

	@PostMapping("/elo/{topicId}")
	public ResponseEntity<Map<String, String>> recalculateElo(@PathVariable Long topicId) {
		boolean calculated = eloService.calculateEloAdjustments(topicId);

		if (!calculated) {
			return ResponseEntity.badRequest()
				.body(Map.of("message", "Not enough votes to calculate Elo for topic " + topicId));
		}

		return ResponseEntity.ok(Map.of("message", "Elo recalculated for topic " + topicId));
	}

	@GetMapping("/topics")
	public ResponseEntity<List<Topic>> getAllTopics() {
		List<Topic> topics = topicRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
		return ResponseEntity.ok(topics);
	}

	@PostMapping("/topics")
	public ResponseEntity<?> createTopic(@RequestBody AdminTopicRequest request) {
		if (request.getOptions() == null || request.getOptions().size() != 8) {
			return ResponseEntity.badRequest().body(Map.of("message", "Exactly 8 options required"));
		}
		HashSet<String> unique = new HashSet<>(request.getOptions().stream()
				.map(o -> o.trim().toLowerCase()).toList());
		if (unique.size() != 8) {
			return ResponseEntity.badRequest().body(Map.of("message", "All 8 options must be unique"));
		}

		Topic topic = new Topic(
			request.getQuestion(),
			request.getCategory(),
			request.getOptions(),
			request.getStatus() != null ? request.getStatus() : TopicStatus.PENDING
		);
		topicRepository.save(topic);
		return ResponseEntity.ok(topic);
	}

	@PutMapping("/topics/{id}/status")
	public ResponseEntity<?> updateTopicStatus(@PathVariable Long id, @RequestParam TopicStatus status) {
		return topicRepository.findById(id).map(topic -> {
			topic.setStatus(status);
			topicRepository.save(topic);
			return ResponseEntity.ok(topic);
		}).orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/suggestions")
	public ResponseEntity<List<TopicSuggestion>> getPendingSuggestions() {
		List<TopicSuggestion> suggestions = suggestionRepository.findByStatusOrderByUpvotesDesc(SuggestionStatus.PENDING);
		return ResponseEntity.ok(suggestions);
	}

	@PostMapping("/suggestions/{id}/promote")
	public ResponseEntity<?> promoteSuggestion(@PathVariable Long id) {
		return suggestionRepository.findById(id).map(suggestion -> {
			Topic topic = new Topic(
				suggestion.getQuestion(), suggestion.getCategory(),
				suggestion.getOptions(), TopicStatus.ACTIVE);
			topicRepository.save(topic);

			suggestion.setStatus(SuggestionStatus.APPROVED);
			suggestionRepository.save(suggestion);

			return ResponseEntity.ok(topic);
		}).orElse(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/topics/{id}")
	public ResponseEntity<?> deleteTopic(@PathVariable Long id) {
		if (!topicRepository.existsById(id)) {
			return ResponseEntity.notFound().build();
		}
		topicRepository.deleteById(id);
		return ResponseEntity.ok(Map.of("message", "Topic deleted"));
	}
}
