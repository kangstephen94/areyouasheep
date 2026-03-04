package com.hottakeranker.repository;

import com.hottakeranker.entity.EloHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EloHistoryRepository extends JpaRepository<EloHistory, Long> {
	List<EloHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
	boolean existsByTopicId(Long topicId);
}
