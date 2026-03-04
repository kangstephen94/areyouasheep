package com.hottakeranker.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "elo_history")
public class EloHistory {

	public EloHistory() {}

	public EloHistory(Long userId, Long topicId, int previousElo, int newElo, double similarityScore) {
		this.userId = userId;
		this.topicId = topicId;
		this.previousElo = previousElo;
		this.newElo = newElo;
		this.similarityScore = similarityScore;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long userId;
	private Long topicId;
	private int previousElo;
	private int newElo;
	private double similarityScore;

	@CreationTimestamp
	private LocalDateTime createdAt;
}
