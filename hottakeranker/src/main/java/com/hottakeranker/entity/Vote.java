package com.hottakeranker.entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.Column;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "votes", uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "topicId"}))
public class Vote {
	public Vote() {}

	public Vote(Long userId, Long topicId, List<Integer> rankings) {
		this.userId = userId;
		this.topicId = topicId;
		this.rankings = rankings;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Long userId;
	private Long topicId;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private List<Integer> rankings;
	@CreationTimestamp
	private LocalDateTime createdAt;
}
