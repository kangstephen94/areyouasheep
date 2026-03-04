package com.hottakeranker.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class VoteRequest {
    private Long userId;
    private Long topicId;
    private List<Integer> ranking;
}
