import styles from './ComparisonRow.module.css';

interface ComparisonItem {
  option: string;
  emoji: string;
  userRank: number;
  crowdRank: number;
}

interface Props {
  items: ComparisonItem[];
}

export default function ComparisonTable({ items }: Props) {
  return (
    <table className={styles.table}>
      <thead>
        <tr>
          <th className={styles.header}>You</th>
          <th className={styles.header}>Option</th>
          <th className={styles.header}></th>
          <th className={styles.header}>Crowd</th>
        </tr>
      </thead>
      <tbody>
        {items.map((item) => (
          <tr key={item.option} className={styles.row}>
            <td className={styles.rank}>#{item.userRank}</td>
            <td className={styles.option}>{item.option}</td>
            <td className={styles.emoji}>{item.emoji}</td>
            <td className={styles.crowdRank}>#{item.crowdRank}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
