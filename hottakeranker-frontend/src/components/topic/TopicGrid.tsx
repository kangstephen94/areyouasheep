import type { Topic } from '../../types/api';
import TopicCard from './TopicCard';
import styles from './TopicGrid.module.css';

interface Props {
  topics: Topic[];
  votedTopicIds?: Set<number>;
}

export default function TopicGrid({ topics, votedTopicIds }: Props) {
  if (topics.length === 0) {
    return <div className={styles.empty}>No active topics right now. Check back soon!</div>;
  }
  return (
    <div className={styles.grid}>
      {topics.map((topic) => (
        <TopicCard key={topic.id} topic={topic} voted={votedTopicIds?.has(topic.id)} />
      ))}
    </div>
  );
}
