package com.hottakeranker.scheduler;

import com.hottakeranker.entity.Topic;
import com.hottakeranker.enums.TopicStatus;
import com.hottakeranker.repository.EloHistoryRepository;
import com.hottakeranker.repository.TopicRepository;
import com.hottakeranker.service.EloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EloScheduler {

	private static final Logger log = LoggerFactory.getLogger(EloScheduler.class);

	private final TopicRepository topicRepository;
	private final EloHistoryRepository eloHistoryRepository;
	private final EloService eloService;

	public EloScheduler(TopicRepository topicRepository, EloHistoryRepository eloHistoryRepository,
						EloService eloService) {
		this.topicRepository = topicRepository;
		this.eloHistoryRepository = eloHistoryRepository;
		this.eloService = eloService;
	}

	@Scheduled(cron = "0 0 0 * * *")
	public void calculateEloForActiveTopics() {
		log.info("Starting nightly Elo calculation");

		List<Topic> activeTopics = topicRepository.findByStatus(TopicStatus.ACTIVE);
		int processed = 0;

		for (Topic topic : activeTopics) {
			if (eloHistoryRepository.existsByTopicId(topic.getId())) {
				continue;
			}

			boolean result = eloService.calculateEloAdjustments(topic.getId());
			if (result) {
				processed++;
				log.info("Calculated Elo for topic {} ({})", topic.getId(), topic.getQuestion());
			}
		}

		log.info("Nightly Elo calculation complete. Processed {} topics", processed);
	}
}
