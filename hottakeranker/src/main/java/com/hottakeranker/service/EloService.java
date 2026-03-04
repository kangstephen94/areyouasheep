package com.hottakeranker.service;

import com.hottakeranker.dto.TopicResultResponse;
import com.hottakeranker.entity.EloHistory;
import com.hottakeranker.entity.Topic;
import com.hottakeranker.entity.User;
import com.hottakeranker.entity.Vote;
import com.hottakeranker.repository.EloHistoryRepository;
import com.hottakeranker.repository.TopicRepository;
import com.hottakeranker.repository.UserRepository;
import com.hottakeranker.repository.VoteRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class EloService {

	private static final int K = 32;
	private static final int MIN_VOTES = 10;

	private final VoteRepository voteRepository;
	private final UserRepository userRepository;
	private final TopicRepository topicRepository;
	private final EloHistoryRepository eloHistoryRepository;
	private final RankingAggregationService rankingAggregationService;

	public EloService(VoteRepository voteRepository, UserRepository userRepository,
					  TopicRepository topicRepository, EloHistoryRepository eloHistoryRepository,
					  RankingAggregationService rankingAggregationService) {
		this.voteRepository = voteRepository;
		this.userRepository = userRepository;
		this.topicRepository = topicRepository;
		this.eloHistoryRepository = eloHistoryRepository;
		this.rankingAggregationService = rankingAggregationService;
	}

	@Transactional
	@CacheEvict(value = "leaderboard", allEntries = true)
	public boolean calculateEloAdjustments(Long topicId) {
		Topic topic = topicRepository.findById(topicId)
			.orElseThrow(() -> new RuntimeException("Topic not found"));

		List<Vote> votes = voteRepository.findByTopicId(topicId);

		if (votes.size() < MIN_VOTES) {
			return false;
		}

		// Get crowd ranking via aggregation
		TopicResultResponse results = rankingAggregationService.aggregateVotes(topic, votes);
		List<String> crowdRanking = results.getCrowdRanking();
		List<String> options = topic.getOptions();

		// Convert crowd ranking to index-based: crowdRankByIndex[i] = position of option index i
		int n = options.size();
		int[] crowdRankByIndex = new int[n];
		for (int pos = 0; pos < crowdRanking.size(); pos++) {
			int optionIndex = options.indexOf(crowdRanking.get(pos));
			crowdRankByIndex[optionIndex] = pos;
		}

		List<User> updatedUsers = new ArrayList<>();
		List<EloHistory> histories = new ArrayList<>();

		for (Vote vote : votes) {
			User user = userRepository.findById(vote.getUserId()).orElse(null);
			if (user == null) {
				continue;
			}

			// Convert user ranking to index-based: userRankByIndex[i] = position of option index i
			List<Integer> rankings = vote.getRankings();
			int[] userRankByIndex = new int[n];
			for (int pos = 0; pos < rankings.size(); pos++) {
				userRankByIndex[rankings.get(pos)] = pos;
			}

			// Compute Spearman correlation and normalize to 0-1
			double spearman = computeSpearmanCorrelation(userRankByIndex, crowdRankByIndex);
			double actualScore = (spearman + 1.0) / 2.0;

			// Compute expected score based on current Elo
			double expectedScore = 1.0 / (1.0 + Math.pow(10.0, (1500.0 - user.getEloRating()) / 400.0));

			// Calculate Elo change
			int eloChange = (int) Math.round(K * (actualScore - expectedScore));

			int previousElo = user.getEloRating();
			int newElo = previousElo + eloChange;
			user.setEloRating(newElo);

			updatedUsers.add(user);
			histories.add(new EloHistory(user.getId(), topicId, previousElo, newElo, actualScore));
		}

		userRepository.saveAll(updatedUsers);
		eloHistoryRepository.saveAll(histories);

		return true;
	}

	private double computeSpearmanCorrelation(int[] rankA, int[] rankB) {
		int n = rankA.length;
		int sumD2 = 0;
		for (int i = 0; i < n; i++) {
			int d = rankA[i] - rankB[i];
			sumD2 += d * d;
		}
		return 1.0 - (6.0 * sumD2) / (n * ((long) n * n - 1));
	}
}
