import { useNavigate } from 'react-router-dom';
import type { EloHistoryEntryDto, VoteHistoryEntry } from '../../types/api';
import styles from './VotingHistory.module.css';

interface Props {
  voteHistory: VoteHistoryEntry[];
  eloHistory: EloHistoryEntryDto[];
}

export default function VotingHistory({ voteHistory, eloHistory }: Props) {
  const navigate = useNavigate();

  if (voteHistory.length === 0) {
    return <div className={styles.empty}>No votes yet. Go rank some takes!</div>;
  }

  // Build a map of topicId -> elo data for quick lookup
  const eloMap = new Map<number, EloHistoryEntryDto>();
  for (const entry of eloHistory) {
    eloMap.set(entry.topicId, entry);
  }

  return (
    <div>
      <div className={styles.title}>Voting History</div>
      <div className={styles.list}>
        {voteHistory.map((vote) => {
          const elo = eloMap.get(vote.topicId);
          return (
            <div
              key={vote.topicId}
              className={styles.entry}
              onClick={() => navigate(`/topic/${vote.topicId}/results`)}
            >
              <div className={styles.entryMain}>
                <span className={styles.category}>{vote.category}</span>
                <span className={styles.question}>{vote.question}</span>
              </div>
              <div className={styles.entryMeta}>
                {elo ? (
                  <>
                    <span
                      className={`${styles.change} ${elo.eloChange >= 0 ? styles.positive : styles.negative}`}
                    >
                      {elo.eloChange >= 0 ? '+' : ''}{elo.eloChange}
                    </span>
                    <span className={styles.similarity}>
                      {Math.round(elo.similarityScore * 100)}%
                    </span>
                  </>
                ) : (
                  <span className={styles.pending}>Pending</span>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
