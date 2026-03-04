package com.hottakeranker.controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import com.hottakeranker.dto.VoteRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.hottakeranker.service.VoteService;

@RestController
@RequestMapping("/api")
public class VoteController {
	private final VoteService voteService;

	public VoteController(VoteService voteService) {
		this.voteService = voteService;
	}

	@PreAuthorize("isAuthenticated()")
	@PostMapping("/votes")
	public ResponseEntity<String> submitVote(@RequestBody VoteRequest request) {
		try {
			voteService.submitVote(request);
			return ResponseEntity.status(HttpStatus.CREATED).build();
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}
}