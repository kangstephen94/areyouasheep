import styles from './MatchScore.module.css';

interface Props {
  percentage: number;
  emojis: string;
}

export default function MatchScore({ percentage, emojis }: Props) {
  return (
    <div className={styles.wrapper}>
      <div className={styles.score}>{percentage}%</div>
      <div className={styles.label}>match with the crowd</div>
      <div className={styles.emojiBar}>{emojis}</div>
    </div>
  );
}
