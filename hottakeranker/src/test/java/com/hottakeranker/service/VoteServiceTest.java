package com.hottakeranker.service;

import com.hottakeranker.dto.VoteRequest;
import com.hottakeranker.entity.Topic;
import com.hottakeranker.enums.TopicStatus;
import com.hottakeranker.repository.TopicRepository;
import com.hottakeranker.repository.UserRepository;
import com.hottakeranker.repository.VoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoteServiceTest {

	@Mock
	private VoteRepository voteRepository;

	@Mock
	private TopicRepository topicRepository;

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private VoteService voteService;

	private VoteRequest createValidRequest() {
		VoteRequest request = new VoteRequest();
		request.setUserId(1L);
		request.setTopicId(1L);
		request.setRanking(List.of(0, 1, 2, 3, 4, 5, 6, 7));
		return request;
	}

	private Topic createActiveTopic() {
		Topic topic = new Topic("Best pizza?", "Food",
				List.of("A", "B", "C", "D", "E", "F", "G", "H"), TopicStatus.ACTIVE);
		topic.setId(1L);
		return topic;
	}

	@Test
	void submitVote_valid_savesSuccessfully() {
		VoteRequest request = createValidRequest();
		when(userRepository.existsById(1L)).thenReturn(true);
		when(topicRepository.findById(1L)).thenReturn(Optional.of(createActiveTopic()));
		when(voteRepository.existsByUserIdAndTopicId(1L, 1L)).thenReturn(false);

		voteService.submitVote(request);

		verify(voteRepository).save(any());
	}

	@Test
	void submitVote_userNotFound_throwsException() {
		VoteRequest request = createValidRequest();
		when(userRepository.existsById(1L)).thenReturn(false);

		assertThatThrownBy(() -> voteService.submitVote(request))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("User not found");
	}

	@Test
	void submitVote_topicNotFound_throwsException() {
		VoteRequest request = createValidRequest();
		when(userRepository.existsById(1L)).thenReturn(true);
		when(topicRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> voteService.submitVote(request))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("Topic not found");
	}

	@Test
	void submitVote_topicNotActive_throwsException() {
		VoteRequest request = createValidRequest();
		Topic archivedTopic = createActiveTopic();
		archivedTopic.setStatus(TopicStatus.ARCHIVED);

		when(userRepository.existsById(1L)).thenReturn(true);
		when(topicRepository.findById(1L)).thenReturn(Optional.of(archivedTopic));

		assertThatThrownBy(() -> voteService.submitVote(request))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("Topic is not active");
	}

	@Test
	void submitVote_duplicateVote_throwsException() {
		VoteRequest request = createValidRequest();
		when(userRepository.existsById(1L)).thenReturn(true);
		when(topicRepository.findById(1L)).thenReturn(Optional.of(createActiveTopic()));
		when(voteRepository.existsByUserIdAndTopicId(1L, 1L)).thenReturn(true);

		assertThatThrownBy(() -> voteService.submitVote(request))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("User has already voted on this topic");
	}

	@Test
	void submitVote_duplicateRankingValues_throwsException() {
		VoteRequest request = createValidRequest();
		request.setRanking(List.of(0, 0, 2, 3, 4, 5, 6, 7));

		when(userRepository.existsById(1L)).thenReturn(true);
		when(topicRepository.findById(1L)).thenReturn(Optional.of(createActiveTopic()));
		when(voteRepository.existsByUserIdAndTopicId(1L, 1L)).thenReturn(false);

		assertThatThrownBy(() -> voteService.submitVote(request))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("Duplicate ranking values");
	}

	@Test
	void submitVote_outOfRangeIndex_throwsException() {
		VoteRequest request = createValidRequest();
		request.setRanking(List.of(0, 1, 2, 3, 4, 5, 6, 8));

		when(userRepository.existsById(1L)).thenReturn(true);
		when(topicRepository.findById(1L)).thenReturn(Optional.of(createActiveTopic()));
		when(voteRepository.existsByUserIdAndTopicId(1L, 1L)).thenReturn(false);

		assertThatThrownBy(() -> voteService.submitVote(request))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("Invalid ranking value");
	}

	@Test
	void submitVote_wrongLength_throwsException() {
		VoteRequest request = createValidRequest();
		request.setRanking(List.of(0, 1, 2, 3));

		when(userRepository.existsById(1L)).thenReturn(true);
		when(topicRepository.findById(1L)).thenReturn(Optional.of(createActiveTopic()));
		when(voteRepository.existsByUserIdAndTopicId(1L, 1L)).thenReturn(false);

		assertThatThrownBy(() -> voteService.submitVote(request))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("Duplicate ranking values");
	}
}
