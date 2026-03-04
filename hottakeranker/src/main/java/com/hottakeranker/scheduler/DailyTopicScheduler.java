package com.hottakeranker.scheduler;

import com.hottakeranker.entity.Topic;
import com.hottakeranker.enums.TopicStatus;
import com.hottakeranker.repository.TopicRepository;
import com.hottakeranker.service.EloService;
import com.hottakeranker.service.TopicSuggestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DailyTopicScheduler {

	private static final Logger log = LoggerFactory.getLogger(DailyTopicScheduler.class);

	private final TopicRepository topicRepository;
	private final EloService eloService;
	private final TopicSuggestionService suggestionService;

	public DailyTopicScheduler(TopicRepository topicRepository, EloService eloService,
			TopicSuggestionService suggestionService) {
		this.topicRepository = topicRepository;
		this.eloService = eloService;
		this.suggestionService = suggestionService;
	}

	@Scheduled(cron = "0 0 0 * * *", zone = "UTC")
	public void rotateDailyTopics() {
		log.info("Starting daily topic rotation");

		// 1. Archive all active topics
		List<Topic> activeTopics = topicRepository.findByStatus(TopicStatus.ACTIVE);
		for (Topic topic : activeTopics) {
			topic.setStatus(TopicStatus.ARCHIVED);
			topicRepository.save(topic);
			log.info("Archived topic: {}", topic.getQuestion());

			// 2. Trigger Elo recalculation for each archived topic
			boolean calculated = eloService.calculateEloAdjustments(topic.getId());
			if (calculated) {
				log.info("Elo recalculated for topic: {}", topic.getQuestion());
			} else {
				log.warn("Not enough votes for Elo on topic: {}", topic.getQuestion());
			}
		}

		// 3. Promote top 5 user-suggested topics
		suggestionService.promoteTopSuggestions();
		List<Topic> newActive = topicRepository.findByStatus(TopicStatus.ACTIVE);
		int activeCount = newActive.size();

		// 4. If fewer than 5 promoted, fill remaining slots from PENDING seeded topics
		if (activeCount < 5) {
			List<Topic> pendingTopics = topicRepository.findByStatus(TopicStatus.PENDING);
			int needed = 5 - activeCount;
			int available = Math.min(needed, pendingTopics.size());

			for (int i = 0; i < available; i++) {
				Topic topic = pendingTopics.get(i);
				topic.setStatus(TopicStatus.ACTIVE);
				topicRepository.save(topic);
				activeCount++;
			}
		}

		// 5. If still fewer than 5, keep yesterday's topics that haven't been replaced
		if (activeCount < 5 && !activeTopics.isEmpty()) {
			int needed = 5 - activeCount;
			int available = Math.min(needed, activeTopics.size());
			log.warn("Not enough new topics — keeping {} from yesterday", available);

			for (int i = 0; i < available; i++) {
				Topic topic = activeTopics.get(i);
				topic.setStatus(TopicStatus.ACTIVE);
				topicRepository.save(topic);
			}
		}

		log.info("Daily rotation complete — {} topics now active",
				topicRepository.findByStatus(TopicStatus.ACTIVE).size());
	}
}
