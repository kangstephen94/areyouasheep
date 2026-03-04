import { useState, useEffect, useCallback } from 'react';
import PageShell from '../components/layout/PageShell';
import LeaderboardTable from '../components/leaderboard/LeaderboardTable';
import EloChart from '../components/leaderboard/EloChart';
import FilterBar from '../components/common/FilterBar';
import Spinner from '../components/common/Spinner';
import ErrorBanner from '../components/common/ErrorBanner';
import { useAuth } from '../hooks/useAuth';
import { getLeaderboard, getEloHistory } from '../services/leaderboardService';
import { Gender, AgeGroup, Region, Ethnicity, GENDER_LABELS, AGE_GROUP_LABELS, REGION_LABELS, ETHNICITY_LABELS } from '../types/enums';
import type { LeaderboardResponse, EloHistoryResponse, DemographicFilter } from '../types/api';

export default function LeaderboardPage() {
  const { isAuthenticated } = useAuth();
  const [data, setData] = useState<LeaderboardResponse | null>(null);
  const [eloData, setEloData] = useState<EloHistoryResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filter, setFilter] = useState<DemographicFilter>({});

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const lb = await getLeaderboard(filter);
      setData(lb);
    } catch {
      setError('Failed to load leaderboard');
    }
    if (isAuthenticated) {
      try {
        const elo = await getEloHistory();
        setEloData(elo);
      } catch {
        // elo history is optional, don't block the page
      }
    }
    setLoading(false);
  }, [filter, isAuthenticated]);

  useEffect(() => { load(); }, [load]);

  return (
    <PageShell title="Leaderboard">
      <ErrorBanner message={error} />

      <FilterBar
        options={[
          ...Object.entries(GENDER_LABELS).map(([v, l]) => ({ value: `g:${v}`, label: l })),
          ...Object.entries(AGE_GROUP_LABELS).map(([v, l]) => ({ value: `a:${v}`, label: l })),
          ...Object.entries(REGION_LABELS).map(([v, l]) => ({ value: `r:${v}`, label: l })),
          ...Object.entries(ETHNICITY_LABELS).map(([v, l]) => ({ value: `e:${v}`, label: l })),
        ]}
        selected={
          filter.gender ? `g:${filter.gender}` :
          filter.ageGroup ? `a:${filter.ageGroup}` :
          filter.region ? `r:${filter.region}` :
          filter.ethnicity ? `e:${filter.ethnicity}` :
          null
        }
        onSelect={(v) => {
          if (!v) {
            setFilter({});
            return;
          }
          const [type, val] = v.split(':');
          setFilter(
            type === 'g' ? { gender: val as Gender } :
            type === 'a' ? { ageGroup: val as AgeGroup } :
            type === 'r' ? { region: val as Region } :
            { ethnicity: val as Ethnicity }
          );
        }}
      />

      {loading ? (
        <Spinner />
      ) : data ? (
        <>
          <LeaderboardTable entries={data.leaderboard} totalUsers={data.totalUsers} />
          {eloData && (
            <EloChart history={eloData.history} currentElo={eloData.currentElo} />
          )}
        </>
      ) : null}
    </PageShell>
  );
}
