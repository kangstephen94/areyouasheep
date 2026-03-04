package com.hottakeranker.repository;

import com.hottakeranker.entity.TopicSuggestion;
import com.hottakeranker.enums.SuggestionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TopicSuggestionRepository extends JpaRepository<TopicSuggestion, Long> {
	List<TopicSuggestion> findByStatusOrderByUpvotesDesc(SuggestionStatus status);
	List<TopicSuggestion> findTop5ByStatusOrderByUpvotesDesc(SuggestionStatus status);
}
