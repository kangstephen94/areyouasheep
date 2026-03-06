package com.hottakeranker.dto;

import com.hottakeranker.enums.TopicStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AdminTopicRequest {
	private String question;
	private String category;
	private List<String> options;
	private TopicStatus status;
}
