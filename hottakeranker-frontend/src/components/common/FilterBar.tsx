import styles from './FilterBar.module.css';

interface FilterOption {
  label: string;
  value: string;
}

interface Props {
  options: FilterOption[];
  selected: string | null;
  onSelect: (value: string | null) => void;
}

export default function FilterBar({ options, selected, onSelect }: Props) {
  return (
    <div className={styles.bar}>
      <button
        className={`${styles.pill} ${selected === null ? styles.active : ''}`}
        onClick={() => onSelect(null)}
      >
        All
      </button>
      {options.map((opt) => (
        <button
          key={opt.value}
          className={`${styles.pill} ${selected === opt.value ? styles.active : ''}`}
          onClick={() => onSelect(selected === opt.value ? null : opt.value)}
        >
          {opt.label}
        </button>
      ))}
    </div>
  );
}
