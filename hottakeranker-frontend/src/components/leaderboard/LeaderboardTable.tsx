import type { LeaderboardEntryDto } from '../../types/api';
import Badge from '../common/Badge';
import styles from './LeaderboardTable.module.css';

interface Props {
  entries: LeaderboardEntryDto[];
  totalUsers: number;
}

export default function LeaderboardTable({ entries, totalUsers }: Props) {
  if (entries.length === 0) {
    return <div className={styles.empty}>No entries yet</div>;
  }

  return (
    <>
      <table className={styles.table}>
        <thead>
          <tr>
            <th>#</th>
            <th>Player</th>
            <th>Elo</th>
            <th>Tier</th>
            <th>Votes</th>
          </tr>
        </thead>
        <tbody>
          {entries.map((e) => (
            <tr key={e.rank} className={styles.row}>
              <td className={styles.rank}>{e.rank}</td>
              <td className={styles.name}>{e.displayName}</td>
              <td className={styles.elo}>{e.eloRating}</td>
              <td>
                <Badge elo={e.eloRating} />
              </td>
              <td className={styles.votes}>{e.totalVotes}</td>
            </tr>
          ))}
        </tbody>
      </table>
      <div className={styles.total}>{totalUsers} total players</div>
    </>
  );
}
