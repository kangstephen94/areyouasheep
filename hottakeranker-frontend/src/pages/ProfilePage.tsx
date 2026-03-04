import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import PageShell from '../components/layout/PageShell';
import ProfileHeader from '../components/profile/ProfileHeader';
import VotingHistory from '../components/profile/VotingHistory';
import EloChart from '../components/leaderboard/EloChart';
import Button from '../components/common/Button';
import Spinner from '../components/common/Spinner';
import ErrorBanner from '../components/common/ErrorBanner';
import { useAuth } from '../hooks/useAuth';
import { getEloHistory, getVoteHistory } from '../services/leaderboardService';
import type { EloHistoryResponse, VoteHistoryEntry } from '../types/api';

export default function ProfilePage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [eloData, setEloData] = useState<EloHistoryResponse | null>(null);
  const [voteHistory, setVoteHistory] = useState<VoteHistoryEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const load = async () => {
      try {
        const [elo, votes] = await Promise.all([
          getEloHistory(),
          getVoteHistory(),
        ]);
        setEloData(elo);
        setVoteHistory(votes);
      } catch {
        setError('Failed to load profile data');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  if (!user) return null;

  return (
    <PageShell>
      <ErrorBanner message={error} />
      {loading ? (
        <Spinner />
      ) : (
        <>
          <ProfileHeader
            displayName={user.displayName}
            elo={eloData?.currentElo ?? 1000}
          />
          {eloData && eloData.history.length > 0 && (
            <EloChart history={eloData.history} currentElo={eloData.currentElo} />
          )}
          <div style={{ marginTop: 'var(--space-xl)' }}>
            <VotingHistory voteHistory={voteHistory} eloHistory={eloData?.history ?? []} />
          </div>
          <div style={{ marginTop: 'var(--space-xl)' }}>
            <Button variant="ghost" full onClick={handleLogout}>
              Log Out
            </Button>
          </div>
        </>
      )}
    </PageShell>
  );
}
