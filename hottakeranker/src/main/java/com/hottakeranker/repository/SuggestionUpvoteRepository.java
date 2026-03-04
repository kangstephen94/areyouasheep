package com.hottakeranker.repository;

import com.hottakeranker.entity.SuggestionUpvote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuggestionUpvoteRepository extends JpaRepository<SuggestionUpvote, Long> {
	boolean existsByUserIdAndSuggestionId(Long userId, Long suggestionId);
}
