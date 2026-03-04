package com.hottakeranker.entity;

import com.hottakeranker.enums.SuggestionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "topic_suggestions")
public class TopicSuggestion {
	public TopicSuggestion() {}

	public TopicSuggestion(Long userId, String question, String category, List<String> options) {
		this.userId = userId;
		this.question = question;
		this.category = category;
		this.options = options;
		this.status = SuggestionStatus.PENDING;
		this.upvotes = 0;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long userId;

	@Column(nullable = false)
	private String question;

	private String category;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private List<String> options;

	@Enumerated(EnumType.STRING)
	private SuggestionStatus status;

	private int upvotes;

	@CreationTimestamp
	private LocalDateTime createdAt;
}
