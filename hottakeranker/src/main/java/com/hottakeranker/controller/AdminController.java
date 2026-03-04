package com.hottakeranker.controller;

import com.hottakeranker.service.EloService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

	private final EloService eloService;

	public AdminController(EloService eloService) {
		this.eloService = eloService;
	}

	@PostMapping("/elo/{topicId}")
	public ResponseEntity<Map<String, String>> recalculateElo(@PathVariable Long topicId) {
		boolean calculated = eloService.calculateEloAdjustments(topicId);

		if (!calculated) {
			return ResponseEntity.badRequest()
				.body(Map.of("message", "Not enough votes to calculate Elo for topic " + topicId));
		}

		return ResponseEntity.ok(Map.of("message", "Elo recalculated for topic " + topicId));
	}
}
