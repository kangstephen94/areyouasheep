package com.hottakeranker.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EloHistoryResponse {
	private int currentElo;
	private List<EloHistoryEntryDto> history;
}
