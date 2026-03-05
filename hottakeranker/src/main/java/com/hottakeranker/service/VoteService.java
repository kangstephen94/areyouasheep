package com.hottakeranker.service;
import com.hottakeranker.dto.VoteHistoryDto;
import com.hottakeranker.dto.VoteRequest;
import com.hottakeranker.entity.Topic;
import com.hottakeranker.entity.Vote;
import com.hottakeranker.enums.TopicStatus;
import com.hottakeranker.repository.TopicRepository;
import com.hottakeranker.repository.UserRepository;
import com.hottakeranker.repository.VoteRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class VoteService {
	private final VoteRepository voteRepository;
	private final TopicRepository topicRepository;
	private final UserRepository userRepository;

	public VoteService(VoteRepository voteRepository, TopicRepository topicRepository, UserRepository userRepository) {
		this.voteRepository = voteRepository;
		this.topicRepository = topicRepository;
		this.userRepository = userRepository;
	}

	@Caching(evict = {
		@CacheEvict(value = "results", key = "#request.topicId"),
		@CacheEvict(value = "demographics", allEntries = true),
		@CacheEvict(value = "controversial", allEntries = true)
	})
	public void submitVote(VoteRequest request) {
		if (!userRepository.existsById(request.getUserId())) {
			throw new RuntimeException("User not found");
		}

		Topic topic = topicRepository.findById(request.getTopicId()).orElseThrow(() -> new RuntimeException("Topic not found"));

		if (topic.getStatus() != TopicStatus.ACTIVE) {
			throw new RuntimeException("Topic is not active");
		}

		if (voteRepository.existsByUserIdAndTopicId(request.getUserId(), request.getTopicId())) {
			throw new RuntimeException("User has already voted on this topic");
		}

		Set<Integer> unique = new HashSet<>(request.getRanking());

		if (unique.size() != 8) {
			throw new RuntimeException("Duplicate ranking values");
		}

		for (int index: unique) {
			if (index < 0 || index > 7) {
				throw new RuntimeException("Invalid ranking value");
			}
		}

		voteRepository.save(new Vote(request.getUserId(), request.getTopicId(), request.getRanking()));
	}

	public List<Long> getVotedTopicIds(Long userId) {
		return voteRepository.findTopicIdsByUserId(userId);
	}

	public List<VoteHistoryDto> getVoteHistory(Long userId) {
		List<Vote> votes = voteRepository.findByUserIdOrderByCreatedAtDesc(userId);
		List<VoteHistoryDto> history = new ArrayList<>();
		for (Vote vote : votes) {
			VoteHistoryDto dto = new VoteHistoryDto();
			dto.setTopicId(vote.getTopicId());
			dto.setVotedAt(vote.getCreatedAt());
			topicRepository.findById(vote.getTopicId()).ifPresent(topic -> {
				dto.setQuestion(topic.getQuestion());
				dto.setCategory(topic.getCategory());
			});
			history.add(dto);
		}
		return history;
	}
}