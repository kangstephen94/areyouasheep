import type { Topic } from '../../types/api';
import TopicCard from './TopicCard';
import styles from './CategoryRow.module.css';

interface Props {
  category: string;
  topics: Topic[];
  votedTopicIds?: Set<number>;
}

export default function CategoryRow({ category, topics, votedTopicIds }: Props) {
  const limited = topics.slice(0, 6);

  return (
    <div className={styles.section}>
      <h2 className={styles.heading}>{category}</h2>
      <div className={styles.row}>
        {limited.map((topic) => (
          <TopicCard key={topic.id} topic={topic} voted={votedTopicIds?.has(topic.id)} />
        ))}
      </div>
    </div>
  );
}
