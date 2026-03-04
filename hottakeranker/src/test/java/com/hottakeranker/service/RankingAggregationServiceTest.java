package com.hottakeranker.service;

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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RankingAggregationServiceTest {

	@Mock
	private TopicRepository topicRepository;

	@Mock
	private VoteRepository voteRepository;

	@InjectMocks
	private RankingAggregationService rankingAggregationService;

	private Topic createTopic() {
		Topic topic = new Topic("Best pizza?", "Food",
				List.of("Pepperoni", "Margherita", "Hawaiian", "BBQ", "Veggie", "Buffalo", "Meat Lovers", "Supreme"),
				TopicStatus.ACTIVE);
		topic.setId(1L);
		return topic;
	}

	@Test
	void bordaCount_threeVotes_correctScoresAndOrder() {
		Topic topic = createTopic();
		// Vote 1: ranks options in order 0,1,2,3,4,5,6,7 (option 0 gets 8pts, option 7 gets 1pt)
		Vote vote1 = new Vote(1L, 1L, List.of(0, 1, 2, 3, 4, 5, 6, 7));
		// Vote 2: ranks options in order 0,1,2,3,4,5,6,7 (same)
		Vote vote2 = new Vote(2L, 1L, List.of(0, 1, 2, 3, 4, 5, 6, 7));
		// Vote 3: ranks options in reverse 7,6,5,4,3,2,1,0
		Vote vote3 = new Vote(3L, 1L, List.of(7, 6, 5, 4, 3, 2, 1, 0));

		TopicResultResponse result = rankingAggregationService.aggregateVotes(topic, List.of(vote1, vote2, vote3));

		assertThat(result.getTotalVotes()).isEqualTo(3);
		// Option 0 (Pepperoni): 8+8+1 = 17
		assertThat(result.getScores().get("Pepperoni")).isEqualTo(17);
		// Option 7 (Supreme): 1+1+8 = 10
		assertThat(result.getScores().get("Supreme")).isEqualTo(10);
		// First place should be Pepperoni (17 pts)
		assertThat(result.getCrowdRanking().get(0)).isEqualTo("Pepperoni");
	}

	@Test
	void bordaCount_singleVote_correctPointAssignment() {
		Topic topic = createTopic();
		Vote vote = new Vote(1L, 1L, List.of(3, 0, 7, 1, 5, 2, 6, 4));

		TopicResultResponse result = rankingAggregationService.aggregateVotes(topic, List.of(vote));

		assertThat(result.getTotalVotes()).isEqualTo(1);
		// Position 0 → option 3 (BBQ) gets 8 pts
		assertThat(result.getScores().get("BBQ")).isEqualTo(8);
		// Position 1 → option 0 (Pepperoni) gets 7 pts
		assertThat(result.getScores().get("Pepperoni")).isEqualTo(7);
		// Position 2 → option 7 (Supreme) gets 6 pts
		assertThat(result.getScores().get("Supreme")).isEqualTo(6);
		// Position 3 → option 1 (Margherita) gets 5 pts
		assertThat(result.getScores().get("Margherita")).isEqualTo(5);
		// Position 4 → option 5 (Buffalo) gets 4 pts
		assertThat(result.getScores().get("Buffalo")).isEqualTo(4);
		// Position 5 → option 2 (Hawaiian) gets 3 pts
		assertThat(result.getScores().get("Hawaiian")).isEqualTo(3);
		// Position 6 → option 6 (Meat Lovers) gets 2 pts
		assertThat(result.getScores().get("Meat Lovers")).isEqualTo(2);
		// Position 7 → option 4 (Veggie) gets 1 pt
		assertThat(result.getScores().get("Veggie")).isEqualTo(1);
		assertThat(result.getCrowdRanking().get(0)).isEqualTo("BBQ");
	}

	@Test
	void bordaCount_zeroVotes_emptyResults() {
		Topic topic = createTopic();

		TopicResultResponse result = rankingAggregationService.aggregateVotes(topic, List.of());

		assertThat(result.getTotalVotes()).isEqualTo(0);
		assertThat(result.getScores().values()).allMatch(score -> score == 0);
	}

	@Test
	void getResults_topicNotFound_throwsException() {
		when(topicRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> rankingAggregationService.getResults(99L))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("Topic not found");
	}

	@Test
	void getResults_validTopic_delegatesToAggregation() {
		Topic topic = createTopic();
		Vote vote = new Vote(1L, 1L, List.of(0, 1, 2, 3, 4, 5, 6, 7));
		when(topicRepository.findById(1L)).thenReturn(Optional.of(topic));
		when(voteRepository.findByTopicId(1L)).thenReturn(List.of(vote));

		TopicResultResponse result = rankingAggregationService.getResults(1L);

		assertThat(result.getTopicId()).isEqualTo(1L);
		assertThat(result.getTotalVotes()).isEqualTo(1);
	}
}
