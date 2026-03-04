import { useNavigate } from 'react-router-dom';
import Card from '../common/Card';
import type { Topic } from '../../types/api';
import styles from './TopicCard.module.css';

interface Props {
  topic: Topic;
  voted?: boolean;
}

export default function TopicCard({ topic, voted }: Props) {
  const navigate = useNavigate();

  const handleClick = () => {
    if (voted) {
      navigate(`/topic/${topic.id}/results`);
    } else {
      navigate(`/topic/${topic.id}/rank`);
    }
  };

  return (
    <Card clickable onClick={handleClick}>
      <div className={styles.card}>
        <div className={styles.header}>
          <span className={styles.category}>{topic.category}</span>
          {voted && <span className={styles.votedBadge}>Voted</span>}
        </div>
        <h3 className={styles.question}>{topic.question}</h3>
        <div className={styles.options}>
          {topic.options.map((opt) => (
            <span key={opt} className={styles.optionChip}>
              {opt}
            </span>
          ))}
        </div>
      </div>
    </Card>
  );
}
