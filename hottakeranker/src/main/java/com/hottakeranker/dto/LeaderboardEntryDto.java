package com.hottakeranker.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeaderboardEntryDto {
	private int rank;
	private String displayName;
	private int eloRating;
	private int totalVotes;
}
