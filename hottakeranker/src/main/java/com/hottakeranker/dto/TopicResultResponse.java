package com.hottakeranker.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class TopicResultResponse {
	private Long topicId;
	private String question;
	private List<String> crowdRanking;
	private int totalVotes;
	private Map<String, Integer> scores;
}
