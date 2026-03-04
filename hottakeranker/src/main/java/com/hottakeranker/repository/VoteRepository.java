package com.hottakeranker.repository;
import com.hottakeranker.entity.Vote;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VoteRepository extends JpaRepository<Vote, Long> {
	List<Vote> findByTopicId(Long topicId);
	boolean existsByUserIdAndTopicId(Long userId, Long topicId);
	int countByUserId(Long userId);
	List<Vote> findByUserIdOrderByCreatedAtDesc(Long userId);

	@Query("SELECT v.topicId FROM Vote v WHERE v.userId = :userId")
	List<Long> findTopicIdsByUserId(Long userId);
}

