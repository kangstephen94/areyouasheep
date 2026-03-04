import { useState } from 'react';
import OptionTile from './OptionTile';
import RankedList from './RankedList';
import Button from '../common/Button';
import styles from './RankingBoard.module.css';

interface Props {
  options: string[];
  onSubmit: (ranking: string[]) => void;
  submitting: boolean;
}

export default function RankingBoard({ options, onSubmit, submitting }: Props) {
  const [ranked, setRanked] = useState<string[]>([]);

  const pick = (option: string) => {
    setRanked((prev) => [...prev, option]);
  };

  const remove = (index: number) => {
    setRanked((prev) => prev.filter((_, i) => i !== index));
  };

  const allRanked = ranked.length === options.length;

  return (
    <div className={styles.board}>
      <div className={styles.section}>
        <span className={styles.sectionTitle}>Your Ranking</span>
        <RankedList items={ranked} onRemove={remove} />
      </div>

      <div className={styles.section}>
        <span className={styles.sectionTitle}>
          {allRanked ? 'All ranked!' : `Tap #${ranked.length + 1}`}
        </span>
        <div className={styles.optionGrid}>
          {options.map((opt) => (
            <OptionTile
              key={opt}
              label={opt}
              picked={ranked.includes(opt)}
              onClick={() => pick(opt)}
            />
          ))}
        </div>
      </div>

      <Button
        size="lg"
        full
        disabled={!allRanked || submitting}
        onClick={() => onSubmit(ranked)}
      >
        {submitting ? 'Submitting...' : 'Lock It In'}
      </Button>
    </div>
  );
}
