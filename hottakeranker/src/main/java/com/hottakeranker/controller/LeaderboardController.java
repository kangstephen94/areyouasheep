package com.hottakeranker.controller;

import com.hottakeranker.dto.EloHistoryResponse;
import com.hottakeranker.dto.LeaderboardResponse;
import com.hottakeranker.dto.VoteHistoryDto;
import com.hottakeranker.enums.AgeGroup;
import com.hottakeranker.enums.Ethnicity;
import com.hottakeranker.enums.Gender;
import com.hottakeranker.enums.PoliticalView;
import com.hottakeranker.enums.Region;
import com.hottakeranker.enums.RelationshipStatus;
import com.hottakeranker.enums.ReligiousView;
import com.hottakeranker.service.LeaderboardService;
import com.hottakeranker.service.VoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LeaderboardController {

	private final LeaderboardService leaderboardService;
	private final VoteService voteService;

	public LeaderboardController(LeaderboardService leaderboardService, VoteService voteService) {
		this.leaderboardService = leaderboardService;
		this.voteService = voteService;
	}

	@GetMapping("/leaderboard")
	public ResponseEntity<LeaderboardResponse> getLeaderboard(
			@RequestParam(required = false) Region region,
			@RequestParam(required = false) Gender gender,
			@RequestParam(required = false) AgeGroup ageGroup,
			@RequestParam(required = false) Ethnicity ethnicity,
			@RequestParam(required = false) ReligiousView religiousView,
			@RequestParam(required = false) PoliticalView politicalView,
			@RequestParam(required = false) RelationshipStatus relationshipStatus) {
		LeaderboardResponse response = leaderboardService.getLeaderboard(region, gender, ageGroup, ethnicity,
				religiousView, politicalView, relationshipStatus);
		return ResponseEntity.ok(response);
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/users/me/elo-history")
	public ResponseEntity<EloHistoryResponse> getEloHistory() {
		Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
		EloHistoryResponse response = leaderboardService.getEloHistory(userId);
		return ResponseEntity.ok(response);
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/users/me/voted-topics")
	public ResponseEntity<List<Long>> getVotedTopics() {
		Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
		List<Long> topicIds = voteService.getVotedTopicIds(userId);
		return ResponseEntity.ok(topicIds);
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/users/me/votes")
	public ResponseEntity<List<VoteHistoryDto>> getVoteHistory() {
		Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
		List<VoteHistoryDto> history = voteService.getVoteHistory(userId);
		return ResponseEntity.ok(history);
	}
}
