import Card from '../common/Card';
import type { TopicSuggestion } from '../../types/api';
import styles from './SuggestionCard.module.css';

interface Props {
  suggestion: TopicSuggestion;
  onUpvote: (id: number) => void;
}

export default function SuggestionCard({ suggestion, onUpvote }: Props) {
  return (
    <Card>
      <div className={styles.card}>
        <span className={styles.category}>{suggestion.category}</span>
        <div className={styles.question}>{suggestion.question}</div>
        <div className={styles.options}>
          {suggestion.options.map((opt) => (
            <span key={opt} className={styles.optionChip}>
              {opt}
            </span>
          ))}
        </div>
        <div className={styles.footer}>
          <button className={styles.upvoteBtn} onClick={() => onUpvote(suggestion.id)}>
            &#9650; {suggestion.upvotes}
          </button>
          <span className={styles.status}>{suggestion.status}</span>
        </div>
      </div>
    </Card>
  );
}
