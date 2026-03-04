import styles from './OptionTile.module.css';

interface Props {
  label: string;
  picked: boolean;
  onClick: () => void;
}

export default function OptionTile({ label, picked, onClick }: Props) {
  return (
    <button
      className={`${styles.tile} ${picked ? styles.picked : ''}`}
      onClick={onClick}
      disabled={picked}
    >
      {label}
    </button>
  );
}
