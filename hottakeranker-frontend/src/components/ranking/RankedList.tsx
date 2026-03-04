import styles from './RankedList.module.css';

interface Props {
  items: string[];
  onRemove: (index: number) => void;
}

export default function RankedList({ items, onRemove }: Props) {
  if (items.length === 0) {
    return <div className={styles.placeholder}>Tap options below to rank them</div>;
  }

  return (
    <div className={styles.list}>
      {items.map((item, i) => (
        <div key={item} className={styles.item}>
          <span className={styles.rank}>#{i + 1}</span>
          <span className={styles.label}>{item}</span>
          <button className={styles.remove} onClick={() => onRemove(i)}>
            &times;
          </button>
        </div>
      ))}
    </div>
  );
}
