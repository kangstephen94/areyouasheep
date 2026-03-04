package com.hottakeranker.service;

import com.hottakeranker.dto.TopicResultResponse;
import com.hottakeranker.entity.Topic;
import com.hottakeranker.entity.Vote;
import com.hottakeranker.repository.TopicRepository;
import com.hottakeranker.repository.VoteRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RankingAggregationService {
	private final TopicRepository topicRepository;
	private final VoteRepository voteRepository;

	public RankingAggregationService(TopicRepository topicRepository, VoteRepository voteRepository) {
		this.topicRepository = topicRepository;
		this.voteRepository = voteRepository;
	}

	@Cacheable(value = "results", key = "#topicId")
	public TopicResultResponse getResults(Long topicId) {
		Topic topic = topicRepository.findById(topicId)
			.orElseThrow(() -> new RuntimeException("Topic not found"));

		List<Vote> votes = voteRepository.findByTopicId(topicId);

		return aggregateVotes(topic, votes);
	}

	public TopicResultResponse aggregateVotes(Topic topic, List<Vote> votes) {
		// Calculate Borda points per option index
		int[] points = new int[8];
		for (Vote vote : votes) {
			List<Integer> ranking = vote.getRankings();
			for (int position = 0; position < 8; position++) {
				int optionIndex = ranking.get(position);
				points[optionIndex] += 8 - position;
			}
		}

		// Map option names to their scores
		List<String> options = topic.getOptions();
		Map<String, Integer> scores = new HashMap<>();
		for (int i = 0; i < 8; i++) {
			scores.put(options.get(i), points[i]);
		}

		// Sort option names by score descending
		List<String> crowdRanking = new ArrayList<>(scores.keySet());
		crowdRanking.sort((a, b) -> scores.get(b) - scores.get(a));

		// Build response
		TopicResultResponse response = new TopicResultResponse();
		response.setTopicId(topic.getId());
		response.setQuestion(topic.getQuestion());
		response.setCrowdRanking(crowdRanking);
		response.setTotalVotes(votes.size());
		response.setScores(scores);

		return response;
	}
}
