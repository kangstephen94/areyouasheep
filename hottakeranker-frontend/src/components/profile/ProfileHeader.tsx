import Badge from '../common/Badge';
import styles from './ProfileHeader.module.css';

interface Props {
  displayName: string;
  elo: number;
}

export default function ProfileHeader({ displayName, elo }: Props) {
  return (
    <div className={styles.header}>
      <div className={styles.avatar}>{displayName.charAt(0).toUpperCase()}</div>
      <div className={styles.name}>{displayName}</div>
      <div className={styles.eloRow}>
        <span className={styles.elo}>{elo}</span>
        <Badge elo={elo} />
      </div>
    </div>
  );
}
