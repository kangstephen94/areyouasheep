package com.hottakeranker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "suggestion_upvotes", uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "suggestionId"}))
public class SuggestionUpvote {
	public SuggestionUpvote() {}

	public SuggestionUpvote(Long userId, Long suggestionId) {
		this.userId = userId;
		this.suggestionId = suggestionId;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long userId;
	private Long suggestionId;

	@CreationTimestamp
	private LocalDateTime createdAt;
}
