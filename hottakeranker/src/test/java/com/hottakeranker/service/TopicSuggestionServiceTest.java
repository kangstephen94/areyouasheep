package com.hottakeranker.service;

import com.hottakeranker.dto.TopicSuggestionRequest;
import com.hottakeranker.entity.TopicSuggestion;
import com.hottakeranker.enums.SuggestionStatus;
import com.hottakeranker.repository.SuggestionUpvoteRepository;
import com.hottakeranker.repository.TopicRepository;
import com.hottakeranker.repository.TopicSuggestionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TopicSuggestionServiceTest {

	@Mock
	private TopicSuggestionRepository suggestionRepository;

	@Mock
	private SuggestionUpvoteRepository upvoteRepository;

	@Mock
	private TopicRepository topicRepository;

	@InjectMocks
	private TopicSuggestionService suggestionService;

	@Test
	void submitSuggestion_valid_savedWithPendingStatusAndZeroUpvotes() {
		TopicSuggestionRequest request = new TopicSuggestionRequest();
		request.setQuestion("Best pizza?");
		request.setCategory("Food");
		request.setOptions(List.of("A", "B", "C", "D", "E", "F", "G", "H"));

		when(suggestionRepository.save(any(TopicSuggestion.class))).thenAnswer(inv -> inv.getArgument(0));

		TopicSuggestion result = suggestionService.submitSuggestion(1L, request);

		assertThat(result.getStatus()).isEqualTo(SuggestionStatus.PENDING);
		assertThat(result.getUpvotes()).isEqualTo(0);
		assertThat(result.getQuestion()).isEqualTo("Best pizza?");
		verify(suggestionRepository).save(any(TopicSuggestion.class));
	}

	@Test
	void getPendingSuggestions_returnsList() {
		TopicSuggestion s1 = new TopicSuggestion(1L, "Q1", "Cat", List.of("A","B","C","D","E","F","G","H"));
		TopicSuggestion s2 = new TopicSuggestion(2L, "Q2", "Cat", List.of("A","B","C","D","E","F","G","H"));
		when(suggestionRepository.findByStatusOrderByUpvotesDesc(SuggestionStatus.PENDING))
				.thenReturn(List.of(s1, s2));

		List<TopicSuggestion> results = suggestionService.getPendingSuggestions();

		assertThat(results).hasSize(2);
	}

	@Test
	void upvote_valid_savesUpvoteAndIncrementsCount() {
		TopicSuggestion suggestion = new TopicSuggestion(1L, "Q", "Cat", List.of("A","B","C","D","E","F","G","H"));
		suggestion.setId(10L);
		suggestion.setUpvotes(5);

		when(suggestionRepository.findById(10L)).thenReturn(Optional.of(suggestion));
		when(upvoteRepository.existsByUserIdAndSuggestionId(2L, 10L)).thenReturn(false);

		suggestionService.upvote(2L, 10L);

		verify(upvoteRepository).save(any());
		assertThat(suggestion.getUpvotes()).isEqualTo(6);
		verify(suggestionRepository).save(suggestion);
	}

	@Test
	void upvote_suggestionNotFound_throwsException() {
		when(suggestionRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> suggestionService.upvote(1L, 99L))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("Suggestion not found");
	}

	@Test
	void upvote_suggestionNotPending_throwsException() {
		TopicSuggestion suggestion = new TopicSuggestion(1L, "Q", "Cat", List.of("A","B","C","D","E","F","G","H"));
		suggestion.setId(10L);
		suggestion.setStatus(SuggestionStatus.APPROVED);

		when(suggestionRepository.findById(10L)).thenReturn(Optional.of(suggestion));

		assertThatThrownBy(() -> suggestionService.upvote(2L, 10L))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("Suggestion is no longer accepting votes");
	}

	@Test
	void upvote_alreadyUpvoted_throwsException() {
		TopicSuggestion suggestion = new TopicSuggestion(1L, "Q", "Cat", List.of("A","B","C","D","E","F","G","H"));
		suggestion.setId(10L);

		when(suggestionRepository.findById(10L)).thenReturn(Optional.of(suggestion));
		when(upvoteRepository.existsByUserIdAndSuggestionId(2L, 10L)).thenReturn(true);

		assertThatThrownBy(() -> suggestionService.upvote(2L, 10L))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("Already upvoted this suggestion");
	}

	@Test
	void promoteTopSuggestions_createsActiveTopicsAndApprovessuggestions() {
		TopicSuggestion s1 = new TopicSuggestion(1L, "Q1", "Cat", List.of("A","B","C","D","E","F","G","H"));
		s1.setId(1L);
		TopicSuggestion s2 = new TopicSuggestion(2L, "Q2", "Cat", List.of("A","B","C","D","E","F","G","H"));
		s2.setId(2L);

		when(suggestionRepository.findTop5ByStatusOrderByUpvotesDesc(SuggestionStatus.PENDING))
				.thenReturn(List.of(s1, s2));

		suggestionService.promoteTopSuggestions();

		verify(topicRepository, times(2)).save(any());
		assertThat(s1.getStatus()).isEqualTo(SuggestionStatus.APPROVED);
		assertThat(s2.getStatus()).isEqualTo(SuggestionStatus.APPROVED);
		verify(suggestionRepository, times(2)).save(any());
	}
}
