import { useNavigate } from 'react-router-dom';
import Card from '../common/Card';
import type { ControversialTopicDto } from '../../types/api';
import styles from './ControversialCard.module.css';

interface Props {
  topic: ControversialTopicDto;
  voted?: boolean;
}

export default function ControversialCard({ topic, voted }: Props) {
  const navigate = useNavigate();

  const handleClick = () => {
    if (voted) {
      navigate(`/topic/${topic.topicId}/results`);
    } else {
      navigate(`/topic/${topic.topicId}/rank`);
    }
  };

  const displayOptions = topic.options.slice(0, 4);
  const remaining = topic.options.length - 4;

  return (
    <Card clickable onClick={handleClick}>
      <div className={styles.card}>
        <div className={styles.header}>
          <span className={styles.category}>{topic.category}</span>
          <span className={styles.controversyBadge}>{topic.controversyScore}% split</span>
        </div>
        <h3 className={styles.question}>{topic.question}</h3>
        <div className={styles.meta}>
          <span>{topic.totalVotes} votes</span>
          {voted && <span>Voted</span>}
        </div>
        <div className={styles.options}>
          {displayOptions.map((opt) => (
            <span key={opt} className={styles.optionChip}>{opt}</span>
          ))}
          {remaining > 0 && (
            <span className={styles.moreChip}>+{remaining} more</span>
          )}
        </div>
      </div>
    </Card>
  );
}
