package com.hottakeranker.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class VoteHistoryDto {
	private Long topicId;
	private String question;
	private String category;
	private LocalDateTime votedAt;
}
