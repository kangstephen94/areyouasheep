package com.hottakeranker.repository;
import com.hottakeranker.entity.Topic;
import com.hottakeranker.enums.TopicStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    List<Topic> findByStatus(TopicStatus status);
}
