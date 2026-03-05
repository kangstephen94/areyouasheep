package com.hottakeranker.service;

import com.hottakeranker.dto.EloHistoryEntryDto;
import com.hottakeranker.dto.EloHistoryResponse;
import com.hottakeranker.dto.LeaderboardEntryDto;
import com.hottakeranker.dto.LeaderboardResponse;
import com.hottakeranker.entity.EloHistory;
import com.hottakeranker.entity.Topic;
import com.hottakeranker.entity.User;
import com.hottakeranker.enums.AgeGroup;
import com.hottakeranker.enums.Ethnicity;
import com.hottakeranker.enums.Gender;
import com.hottakeranker.enums.PoliticalView;
import com.hottakeranker.enums.Region;
import com.hottakeranker.enums.RelationshipStatus;
import com.hottakeranker.enums.ReligiousView;
import com.hottakeranker.repository.EloHistoryRepository;
import com.hottakeranker.repository.TopicRepository;
import com.hottakeranker.repository.UserRepository;
import com.hottakeranker.repository.VoteRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LeaderboardService {

	private final UserRepository userRepository;
	private final VoteRepository voteRepository;
	private final EloHistoryRepository eloHistoryRepository;
	private final TopicRepository topicRepository;

	public LeaderboardService(UserRepository userRepository, VoteRepository voteRepository,
			EloHistoryRepository eloHistoryRepository, TopicRepository topicRepository) {
		this.userRepository = userRepository;
		this.voteRepository = voteRepository;
		this.eloHistoryRepository = eloHistoryRepository;
		this.topicRepository = topicRepository;
	}

	@Cacheable(value = "leaderboard", key = "#region + '-' + #gender + '-' + #ageGroup + '-' + #ethnicity + '-' + #religiousView + '-' + #politicalView + '-' + #relationshipStatus")
	public LeaderboardResponse getLeaderboard(Region region, Gender gender, AgeGroup ageGroup, Ethnicity ethnicity,
			ReligiousView religiousView, PoliticalView politicalView, RelationshipStatus relationshipStatus) {
		boolean hasFilters = region != null || gender != null || ageGroup != null || ethnicity != null
				|| religiousView != null || politicalView != null || relationshipStatus != null;

		List<User> users;
		if (!hasFilters) {
			users = userRepository.findTop50ByOrderByEloRatingDesc();
		} else {
			users = userRepository.findAll(org.springframework.data.domain.Sort.by(
					org.springframework.data.domain.Sort.Direction.DESC, "eloRating"))
				.stream()
				.filter(u -> region == null || u.getRegion() == region)
				.filter(u -> gender == null || u.getGender() == gender)
				.filter(u -> ageGroup == null || u.getAgeGroup() == ageGroup)
				.filter(u -> ethnicity == null || u.getEthnicity() == ethnicity)
				.filter(u -> religiousView == null || u.getReligiousView() == religiousView)
				.filter(u -> politicalView == null || u.getPoliticalView() == politicalView)
				.filter(u -> relationshipStatus == null || u.getRelationshipStatus() == relationshipStatus)
				.limit(50)
				.toList();
		}

		List<LeaderboardEntryDto> entries = new ArrayList<>();
		for (int i = 0; i < users.size(); i++) {
			User user = users.get(i);
			LeaderboardEntryDto entry = new LeaderboardEntryDto();
			entry.setRank(i + 1);
			entry.setDisplayName(user.getDisplayName());
			entry.setEloRating(user.getEloRating());
			entry.setTotalVotes(voteRepository.countByUserId(user.getId()));
			entries.add(entry);
		}

		LeaderboardResponse response = new LeaderboardResponse();
		response.setLeaderboard(entries);
		response.setTotalUsers((int) userRepository.count());
		return response;
	}

	public EloHistoryResponse getEloHistory(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("User not found"));

		List<EloHistory> records = eloHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);

		List<EloHistoryEntryDto> history = new ArrayList<>();
		for (EloHistory record : records) {
			EloHistoryEntryDto entry = new EloHistoryEntryDto();
			entry.setTopicId(record.getTopicId());
			entry.setEloChange(record.getNewElo() - record.getPreviousElo());
			entry.setSimilarityScore(record.getSimilarityScore());
			entry.setDate(record.getCreatedAt().toLocalDate());

			topicRepository.findById(record.getTopicId())
					.ifPresent(topic -> entry.setQuestion(topic.getQuestion()));

			history.add(entry);
		}

		EloHistoryResponse response = new EloHistoryResponse();
		response.setCurrentElo(user.getEloRating());
		response.setHistory(history);
		return response;
	}
}
