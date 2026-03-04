package com.hottakeranker.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TopicSuggestionRequest {
	private String question;
	private String category;
	private List<String> options;
}
