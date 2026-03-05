package com.hottakeranker.service;

import com.hottakeranker.dto.ControversialTopicDto;
import com.hottakeranker.dto.TopicResultResponse;
import com.hottakeranker.entity.Topic;
import com.hottakeranker.entity.Vote;
import com.hottakeranker.enums.TopicStatus;
import com.hottakeranker.repository.TopicRepository;
import com.hottakeranker.repository.VoteRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class ControversyService {
	private final TopicRepository topicRepository;
	private final VoteRepository voteRepository;
	private final RankingAggregationService rankingService;

	public ControversyService(TopicRepository topicRepository, VoteRepository voteRepository,
							  RankingAggregationService rankingService) {
		this.topicRepository = topicRepository;
		this.voteRepository = voteRepository;
		this.rankingService = rankingService;
	}

	@Cacheable(value = "controversial")
	public List<ControversialTopicDto> getControversialTopics() {
		List<Topic> activeTopics = topicRepository.findByStatus(TopicStatus.ACTIVE);
		List<ControversialTopicDto> results = new ArrayList<>();

		for (Topic topic : activeTopics) {
			List<Vote> votes = voteRepository.findByTopicId(topic.getId());
			if (votes.size() < 10) continue;

			TopicResultResponse aggregated = rankingService.aggregateVotes(topic, votes);
			double score = computeControversyScore(aggregated.getScores(), votes.size());

			ControversialTopicDto dto = new ControversialTopicDto();
			dto.setTopicId(topic.getId());
			dto.setQuestion(topic.getQuestion());
			dto.setCategory(topic.getCategory());
			dto.setOptions(topic.getOptions());
			dto.setTotalVotes(votes.size());
			dto.setControversyScore(Math.round(score * 10.0) / 10.0);

			results.add(dto);
		}

		results.sort(Comparator.comparingDouble(ControversialTopicDto::getControversyScore).reversed());
		return results;
	}

	private double computeControversyScore(Map<String, Integer> scores, int numVotes) {
		double[] values = scores.values().stream().mapToDouble(Integer::doubleValue).toArray();
		double mean = 0;
		for (double v : values) mean += v;
		mean /= values.length;

		double variance = 0;
		for (double v : values) variance += (v - mean) * (v - mean);
		variance /= values.length;
		double stddev = Math.sqrt(variance);

		// maxStddev for 8 options: numVotes * stddev({1,2,...,8})
		// stddev of {1..8} = sqrt(5.25)
		double maxStddev = numVotes * Math.sqrt(5.25);

		return (1.0 - stddev / maxStddev) * 100.0;
	}
}
