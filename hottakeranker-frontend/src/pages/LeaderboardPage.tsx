import { useState, useEffect, useCallback } from 'react';
import PageShell from '../components/layout/PageShell';
import LeaderboardTable from '../components/leaderboard/LeaderboardTable';
import EloChart from '../components/leaderboard/EloChart';
import Spinner from '../components/common/Spinner';
import ErrorBanner from '../components/common/ErrorBanner';
import { useAuth } from '../hooks/useAuth';
import { getLeaderboard, getEloHistory } from '../services/leaderboardService';
import {
  Gender, AgeGroup, Region, Ethnicity, ReligiousView, PoliticalView, RelationshipStatus,
  GENDER_LABELS, AGE_GROUP_LABELS, REGION_LABELS, ETHNICITY_LABELS,
  RELIGIOUS_VIEW_LABELS, POLITICAL_VIEW_LABELS, RELATIONSHIP_STATUS_LABELS,
} from '../types/enums';
import type { LeaderboardResponse, EloHistoryResponse, DemographicFilter } from '../types/api';
import styles from './LeaderboardPage.module.css';

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

  const hasFilters = Object.values(filter).some(Boolean);

  return (
    <PageShell title="Leaderboard">
      <ErrorBanner message={error} />

      <div className={styles.filters}>
        <select
          value={filter.gender ?? ''}
          onChange={(e) => setFilter((f) => ({ ...f, gender: e.target.value as Gender || undefined }))}
        >
          <option value="">Gender</option>
          {Object.entries(GENDER_LABELS).map(([v, l]) => (
            <option key={v} value={v}>{l}</option>
          ))}
        </select>

        <select
          value={filter.ageGroup ?? ''}
          onChange={(e) => setFilter((f) => ({ ...f, ageGroup: e.target.value as AgeGroup || undefined }))}
        >
          <option value="">Age</option>
          {Object.entries(AGE_GROUP_LABELS).map(([v, l]) => (
            <option key={v} value={v}>{l}</option>
          ))}
        </select>

        <select
          value={filter.region ?? ''}
          onChange={(e) => setFilter((f) => ({ ...f, region: e.target.value as Region || undefined }))}
        >
          <option value="">Region</option>
          {Object.entries(REGION_LABELS).map(([v, l]) => (
            <option key={v} value={v}>{l}</option>
          ))}
        </select>

        <select
          value={filter.ethnicity ?? ''}
          onChange={(e) => setFilter((f) => ({ ...f, ethnicity: e.target.value as Ethnicity || undefined }))}
        >
          <option value="">Ethnicity</option>
          {Object.entries(ETHNICITY_LABELS).map(([v, l]) => (
            <option key={v} value={v}>{l}</option>
          ))}
        </select>

        <select
          value={filter.religiousView ?? ''}
          onChange={(e) => setFilter((f) => ({ ...f, religiousView: e.target.value as ReligiousView || undefined }))}
        >
          <option value="">Religion</option>
          {Object.entries(RELIGIOUS_VIEW_LABELS).map(([v, l]) => (
            <option key={v} value={v}>{l}</option>
          ))}
        </select>

        <select
          value={filter.politicalView ?? ''}
          onChange={(e) => setFilter((f) => ({ ...f, politicalView: e.target.value as PoliticalView || undefined }))}
        >
          <option value="">Politics</option>
          {Object.entries(POLITICAL_VIEW_LABELS).map(([v, l]) => (
            <option key={v} value={v}>{l}</option>
          ))}
        </select>

        <select
          value={filter.relationshipStatus ?? ''}
          onChange={(e) => setFilter((f) => ({ ...f, relationshipStatus: e.target.value as RelationshipStatus || undefined }))}
        >
          <option value="">Relationship</option>
          {Object.entries(RELATIONSHIP_STATUS_LABELS).map(([v, l]) => (
            <option key={v} value={v}>{l}</option>
          ))}
        </select>

        {hasFilters && (
          <button className={styles.clearBtn} onClick={() => setFilter({})}>
            Clear All
          </button>
        )}
      </div>

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
