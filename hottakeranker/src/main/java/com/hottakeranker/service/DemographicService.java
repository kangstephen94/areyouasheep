package com.hottakeranker.service;

import com.hottakeranker.dto.DemographicFilterRequest;
import com.hottakeranker.dto.TopicResultResponse;
import com.hottakeranker.entity.Topic;
import com.hottakeranker.entity.User;
import com.hottakeranker.entity.Vote;
import com.hottakeranker.repository.TopicRepository;
import com.hottakeranker.repository.UserRepository;
import com.hottakeranker.repository.VoteRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DemographicService {

	private final VoteRepository voteRepository;
	private final UserRepository userRepository;
	private final TopicRepository topicRepository;
	private final RankingAggregationService rankingAggregationService;

	public DemographicService(VoteRepository voteRepository, UserRepository userRepository,
							  TopicRepository topicRepository, RankingAggregationService rankingAggregationService) {
		this.voteRepository = voteRepository;
		this.userRepository = userRepository;
		this.topicRepository = topicRepository;
		this.rankingAggregationService = rankingAggregationService;
	}

	@Cacheable(value = "demographics", key = "#topicId + '-' + #filter.gender + '-' + #filter.ageGroup + '-' + #filter.region + '-' + #filter.ethnicity + '-' + #filter.religiousView + '-' + #filter.politicalView + '-' + #filter.relationshipStatus")
	public TopicResultResponse getFilteredResults(Long topicId, DemographicFilterRequest filter) {
		Topic topic = topicRepository.findById(topicId)
			.orElseThrow(() -> new RuntimeException("Topic not found"));

		List<Vote> votes = voteRepository.findByTopicId(topicId);

		if (!filter.hasFilters()) {
			return rankingAggregationService.aggregateVotes(topic, votes);
		}

		// Collect all voter user IDs
		Set<Long> voterUserIds = votes.stream()
			.map(Vote::getUserId)
			.collect(Collectors.toSet());

		// Fetch users and filter by demographics
		List<User> users = userRepository.findAllById(voterUserIds);
		Set<Long> matchingUserIds = users.stream()
			.filter(user -> matchesDemographics(user, filter))
			.map(User::getId)
			.collect(Collectors.toSet());

		// Filter votes to only matching users
		List<Vote> filteredVotes = votes.stream()
			.filter(vote -> matchingUserIds.contains(vote.getUserId()))
			.toList();

		return rankingAggregationService.aggregateVotes(topic, filteredVotes);
	}

	private boolean matchesDemographics(User user, DemographicFilterRequest filter) {
		if (filter.getGender() != null && user.getGender() != filter.getGender()) {
			return false;
		}
		if (filter.getAgeGroup() != null && user.getAgeGroup() != filter.getAgeGroup()) {
			return false;
		}
		if (filter.getRegion() != null && user.getRegion() != filter.getRegion()) {
			return false;
		}
		if (filter.getEthnicity() != null && user.getEthnicity() != filter.getEthnicity()) {
			return false;
		}
		if (filter.getReligiousView() != null && user.getReligiousView() != filter.getReligiousView()) {
			return false;
		}
		if (filter.getPoliticalView() != null && user.getPoliticalView() != filter.getPoliticalView()) {
			return false;
		}
		if (filter.getRelationshipStatus() != null && user.getRelationshipStatus() != filter.getRelationshipStatus()) {
			return false;
		}
		return true;
	}
}
