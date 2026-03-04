package com.hottakeranker.service;

import com.hottakeranker.dto.TopicSuggestionRequest;
import com.hottakeranker.entity.SuggestionUpvote;
import com.hottakeranker.entity.Topic;
import com.hottakeranker.entity.TopicSuggestion;
import com.hottakeranker.enums.SuggestionStatus;
import com.hottakeranker.enums.TopicStatus;
import com.hottakeranker.repository.SuggestionUpvoteRepository;
import com.hottakeranker.repository.TopicRepository;
import com.hottakeranker.repository.TopicSuggestionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TopicSuggestionService {

	private final TopicSuggestionRepository suggestionRepository;
	private final SuggestionUpvoteRepository upvoteRepository;
	private final TopicRepository topicRepository;

	public TopicSuggestionService(TopicSuggestionRepository suggestionRepository,
			SuggestionUpvoteRepository upvoteRepository, TopicRepository topicRepository) {
		this.suggestionRepository = suggestionRepository;
		this.upvoteRepository = upvoteRepository;
		this.topicRepository = topicRepository;
	}

	public TopicSuggestion submitSuggestion(Long userId, TopicSuggestionRequest request) {
		TopicSuggestion suggestion = new TopicSuggestion(
				userId, request.getQuestion(), request.getCategory(), request.getOptions());
		return suggestionRepository.save(suggestion);
	}

	public List<TopicSuggestion> getPendingSuggestions() {
		return suggestionRepository.findByStatusOrderByUpvotesDesc(SuggestionStatus.PENDING);
	}

	public void upvote(Long userId, Long suggestionId) {
		TopicSuggestion suggestion = suggestionRepository.findById(suggestionId)
				.orElseThrow(() -> new RuntimeException("Suggestion not found"));

		if (suggestion.getStatus() != SuggestionStatus.PENDING) {
			throw new RuntimeException("Suggestion is no longer accepting votes");
		}

		if (upvoteRepository.existsByUserIdAndSuggestionId(userId, suggestionId)) {
			throw new RuntimeException("Already upvoted this suggestion");
		}

		upvoteRepository.save(new SuggestionUpvote(userId, suggestionId));
		suggestion.setUpvotes(suggestion.getUpvotes() + 1);
		suggestionRepository.save(suggestion);
	}

	public void promoteTopSuggestions() {
		List<TopicSuggestion> top5 = suggestionRepository.findTop5ByStatusOrderByUpvotesDesc(SuggestionStatus.PENDING);

		for (TopicSuggestion suggestion : top5) {
			Topic topic = new Topic(
					suggestion.getQuestion(), suggestion.getCategory(),
					suggestion.getOptions(), TopicStatus.ACTIVE);
			topicRepository.save(topic);

			suggestion.setStatus(SuggestionStatus.APPROVED);
			suggestionRepository.save(suggestion);
		}
	}
}
