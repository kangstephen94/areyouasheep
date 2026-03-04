package com.hottakeranker.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LeaderboardResponse {
	private List<LeaderboardEntryDto> leaderboard;
	private int totalUsers;
}
