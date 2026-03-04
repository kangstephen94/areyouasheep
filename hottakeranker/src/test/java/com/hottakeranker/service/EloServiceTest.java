package com.hottakeranker.service;

import com.hottakeranker.dto.TopicResultResponse;
import com.hottakeranker.entity.Topic;
import com.hottakeranker.entity.User;
import com.hottakeranker.entity.Vote;
import com.hottakeranker.enums.TopicStatus;
import com.hottakeranker.repository.EloHistoryRepository;
import com.hottakeranker.repository.TopicRepository;
import com.hottakeranker.repository.UserRepository;
import com.hottakeranker.repository.VoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EloServiceTest {

	@Mock
	private VoteRepository voteRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private TopicRepository topicRepository;

	@Mock
	private EloHistoryRepository eloHistoryRepository;

	@Mock
	private RankingAggregationService rankingAggregationService;

	@InjectMocks
	private EloService eloService;

	private Topic createTopic() {
		Topic topic = new Topic("Best pizza?", "Food",
				List.of("Pepperoni", "Margherita", "Hawaiian", "BBQ", "Veggie", "Buffalo", "Meat Lovers", "Supreme"),
				TopicStatus.ACTIVE);
		topic.setId(1L);
		return topic;
	}

	private double callComputeSpearman(int[] rankA, int[] rankB) throws Exception {
		Method method = EloService.class.getDeclaredMethod("computeSpearmanCorrelation", int[].class, int[].class);
		method.setAccessible(true);
		return (double) method.invoke(eloService, rankA, rankB);
	}

	@Test
	void spearmanCorrelation_perfectMatch_returnsOne() throws Exception {
		int[] rankA = {0, 1, 2, 3, 4, 5, 6, 7};
		int[] rankB = {0, 1, 2, 3, 4, 5, 6, 7};

		double result = callComputeSpearman(rankA, rankB);

		assertThat(result).isCloseTo(1.0, within(0.001));
	}

	@Test
	void spearmanCorrelation_perfectlyOpposite_returnsNegativeOne() throws Exception {
		int[] rankA = {0, 1, 2, 3, 4, 5, 6, 7};
		int[] rankB = {7, 6, 5, 4, 3, 2, 1, 0};

		double result = callComputeSpearman(rankA, rankB);

		assertThat(result).isCloseTo(-1.0, within(0.001));
	}

	@Test
	void spearmanCorrelation_knownPartialMatch_correctValue() throws Exception {
		int[] rankA = {0, 1, 2, 3, 4, 5, 6, 7};
		int[] rankB = {1, 0, 3, 2, 5, 4, 7, 6};
		// Each pair swapped: d=1 for each, sumD2 = 8
		// 1 - 6*8/(8*63) = 1 - 48/504 = 1 - 0.0952... ≈ 0.905

		double result = callComputeSpearman(rankA, rankB);

		assertThat(result).isCloseTo(0.905, within(0.01));
	}

	@Test
	void calculateEloAdjustments_fewerThanTenVotes_returnsFalse() {
		Topic topic = createTopic();
		when(topicRepository.findById(1L)).thenReturn(Optional.of(topic));
		when(voteRepository.findByTopicId(1L)).thenReturn(List.of(
				new Vote(1L, 1L, List.of(0,1,2,3,4,5,6,7)),
				new Vote(2L, 1L, List.of(0,1,2,3,4,5,6,7))
		));

		boolean result = eloService.calculateEloAdjustments(1L);

		assertThat(result).isFalse();
		verify(userRepository, never()).saveAll(anyList());
	}

	@Test
	void calculateEloAdjustments_userMatchesCrowd_gainsElo() {
		Topic topic = createTopic();
		List<String> options = topic.getOptions();

		// Create 10 votes all with the same ranking
		List<Vote> votes = new ArrayList<>();
		for (int i = 1; i <= 10; i++) {
			Vote v = new Vote((long) i, 1L, List.of(0, 1, 2, 3, 4, 5, 6, 7));
			votes.add(v);
		}

		// Crowd ranking matches this order
		TopicResultResponse crowdResult = new TopicResultResponse();
		crowdResult.setCrowdRanking(List.of(
				options.get(0), options.get(1), options.get(2), options.get(3),
				options.get(4), options.get(5), options.get(6), options.get(7)));

		when(topicRepository.findById(1L)).thenReturn(Optional.of(topic));
		when(voteRepository.findByTopicId(1L)).thenReturn(votes);
		when(rankingAggregationService.aggregateVotes(eq(topic), eq(votes))).thenReturn(crowdResult);

		// User 1 has default 1000 Elo and matches crowd perfectly
		User user = new User();
		user.setId(1L);
		user.setEloRating(1000);
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		// Return empty for other users
		for (long i = 2; i <= 10; i++) {
			when(userRepository.findById(i)).thenReturn(Optional.empty());
		}

		boolean result = eloService.calculateEloAdjustments(1L);

		assertThat(result).isTrue();
		// Perfect match → Spearman = 1.0, actualScore = 1.0
		// expectedScore for 1000 Elo ≈ 0.24
		// eloChange = 32 * (1.0 - 0.24) ≈ +24
		assertThat(user.getEloRating()).isGreaterThan(1000);
	}

	@Test
	void calculateEloAdjustments_userRanksOpposite_losesElo() {
		Topic topic = createTopic();
		List<String> options = topic.getOptions();

		// Create 10 votes: user 1 ranks opposite, rest rank normally
		List<Vote> votes = new ArrayList<>();
		Vote oppositeVote = new Vote(1L, 1L, List.of(7, 6, 5, 4, 3, 2, 1, 0));
		votes.add(oppositeVote);
		for (int i = 2; i <= 10; i++) {
			votes.add(new Vote((long) i, 1L, List.of(0, 1, 2, 3, 4, 5, 6, 7)));
		}

		TopicResultResponse crowdResult = new TopicResultResponse();
		crowdResult.setCrowdRanking(List.of(
				options.get(0), options.get(1), options.get(2), options.get(3),
				options.get(4), options.get(5), options.get(6), options.get(7)));

		when(topicRepository.findById(1L)).thenReturn(Optional.of(topic));
		when(voteRepository.findByTopicId(1L)).thenReturn(votes);
		when(rankingAggregationService.aggregateVotes(eq(topic), eq(votes))).thenReturn(crowdResult);

		User user = new User();
		user.setId(1L);
		user.setEloRating(1000);
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		for (long i = 2; i <= 10; i++) {
			when(userRepository.findById(i)).thenReturn(Optional.empty());
		}

		eloService.calculateEloAdjustments(1L);

		// Opposite ranking → Spearman = -1.0, actualScore = 0.0
		// eloChange = 32 * (0.0 - expectedScore) → negative
		assertThat(user.getEloRating()).isLessThan(1000);
	}

	@Test
	void calculateEloAdjustments_highEloUserScoringAverage_losesMoreThanLowEloUser() {
		Topic topic = createTopic();
		List<String> options = topic.getOptions();

		// Both users have a partial match ranking: swap adjacent pairs
		List<Integer> partialRanking = List.of(1, 0, 3, 2, 5, 4, 7, 6);

		List<Vote> votes = new ArrayList<>();
		votes.add(new Vote(1L, 1L, partialRanking)); // high Elo user
		votes.add(new Vote(2L, 1L, partialRanking)); // low Elo user
		for (int i = 3; i <= 12; i++) {
			votes.add(new Vote((long) i, 1L, List.of(0, 1, 2, 3, 4, 5, 6, 7)));
		}

		TopicResultResponse crowdResult = new TopicResultResponse();
		crowdResult.setCrowdRanking(List.of(
				options.get(0), options.get(1), options.get(2), options.get(3),
				options.get(4), options.get(5), options.get(6), options.get(7)));

		when(topicRepository.findById(1L)).thenReturn(Optional.of(topic));
		when(voteRepository.findByTopicId(1L)).thenReturn(votes);
		when(rankingAggregationService.aggregateVotes(eq(topic), eq(votes))).thenReturn(crowdResult);

		User highEloUser = new User();
		highEloUser.setId(1L);
		highEloUser.setEloRating(1800);

		User lowEloUser = new User();
		lowEloUser.setId(2L);
		lowEloUser.setEloRating(1000);

		when(userRepository.findById(1L)).thenReturn(Optional.of(highEloUser));
		when(userRepository.findById(2L)).thenReturn(Optional.of(lowEloUser));
		for (long i = 3; i <= 12; i++) {
			when(userRepository.findById(i)).thenReturn(Optional.empty());
		}

		eloService.calculateEloAdjustments(1L);

		// Both have the same actualScore, but high Elo user has higher expectedScore
		// So high Elo user's eloChange is more negative (or less positive)
		int highEloChange = highEloUser.getEloRating() - 1800;
		int lowEloChange = lowEloUser.getEloRating() - 1000;
		assertThat(highEloChange).isLessThan(lowEloChange);
	}
}
