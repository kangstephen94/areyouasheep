package com.hottakeranker.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EloHistoryEntryDto {
	private Long topicId;
	private String question;
	private int eloChange;
	private double similarityScore;
	private LocalDate date;
}
