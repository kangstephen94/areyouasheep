import styles from './ScoreBreakdown.module.css';

interface Props {
  scores: Record<string, number>;
  crowdRanking: string[];
}

export default function ScoreBreakdown({ scores, crowdRanking }: Props) {
  const maxScore = Math.max(...Object.values(scores), 1);

  return (
    <div className={styles.breakdown}>
      {crowdRanking.map((option) => (
        <div key={option} className={styles.row}>
          <span className={styles.label} title={option}>
            {option}
          </span>
          <div className={styles.barBg}>
            <div
              className={styles.barFill}
              style={{ width: `${(scores[option] / maxScore) * 100}%` }}
            />
          </div>
          <span className={styles.value}>{scores[option]}</span>
        </div>
      ))}
    </div>
  );
}
