import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import PageShell from '../components/layout/PageShell';
import MatchScore from '../components/results/MatchScore';
import NpcBadge from '../components/results/NpcBadge';
import ComparisonTable from '../components/results/ComparisonRow';
import ScoreBreakdown from '../components/results/ScoreBreakdown';
import FilterBar from '../components/common/FilterBar';
import Button from '../components/common/Button';
import Spinner from '../components/common/Spinner';
import ErrorBanner from '../components/common/ErrorBanner';
import { getResults, getDemographicResults } from '../services/topicService';
import { compareRankings, matchPercentage, emojiBar } from '../utils/ranking';
import { generateShareText, copyToClipboard } from '../utils/share';
import {
  Gender, AgeGroup, Region, Ethnicity, ReligiousView, PoliticalView, RelationshipStatus,
  GENDER_LABELS, AGE_GROUP_LABELS, REGION_LABELS, ETHNICITY_LABELS,
  RELIGIOUS_VIEW_LABELS, POLITICAL_VIEW_LABELS, RELATIONSHIP_STATUS_LABELS,
} from '../types/enums';
import type { TopicResultResponse, DemographicFilter } from '../types/api';
import styles from './ResultsPage.module.css';

export default function ResultsPage() {
  const { id } = useParams<{ id: string }>();
  const topicId = Number(id);
  const [results, setResults] = useState<TopicResultResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [copied, setCopied] = useState(false);
  const [filter, setFilter] = useState<DemographicFilter>({});

  // Load user ranking from session if available
  const stored = sessionStorage.getItem(`rank-${topicId}`);
  const userRanking: string[] | null = stored ? JSON.parse(stored) : null;

  useEffect(() => {
    setLoading(true);
    const hasFilter = filter.gender || filter.ageGroup || filter.region || filter.ethnicity
      || filter.religiousView || filter.politicalView || filter.relationshipStatus;
    const promise = hasFilter
      ? getDemographicResults(topicId, filter)
      : getResults(topicId);

    promise
      .then(setResults)
      .catch(() => setError('Failed to load results'))
      .finally(() => setLoading(false));
  }, [topicId, filter]);

  const handleShare = async () => {
    if (!results || !userRanking) return;
    const text = generateShareText(results.question, userRanking, results.crowdRanking);
    const ok = await copyToClipboard(text);
    if (ok) {
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  if (loading) return <PageShell><Spinner /></PageShell>;
  if (!results) return <PageShell><ErrorBanner message={error || 'No results'} /></PageShell>;

  const comparison = userRanking
    ? compareRankings(userRanking, results.crowdRanking)
    : null;
  const pct = userRanking
    ? matchPercentage(userRanking, results.crowdRanking)
    : null;
  const emojis = comparison ? emojiBar(comparison) : '';

  const comparisonItems = comparison
    ? comparison.map((c, i) => ({
        option: c.option,
        emoji: c.emoji,
        userRank: i + 1,
        crowdRank: results.crowdRanking.indexOf(c.option) + 1,
      }))
    : null;

  return (
    <PageShell>
      <div className={styles.sections}>
        <div>
          <div className={styles.question}>{results.question}</div>
          <div className={styles.votes}>{results.totalVotes} votes</div>
        </div>

        {pct !== null && comparison && (
          <>
            <MatchScore percentage={pct} emojis={emojis} />
            <NpcBadge matchPercentage={pct} />
          </>
        )}

        {comparisonItems && (
          <div>
            <h2 className={styles.sectionTitle}>Your Ranking vs The Crowd</h2>
            <ComparisonTable items={comparisonItems} />
          </div>
        )}

        <div>
          <h2 className={styles.sectionTitle}>Borda Scores</h2>
          <ScoreBreakdown scores={results.scores} crowdRanking={results.crowdRanking} />
        </div>

        <div className={styles.filterGroup}>
          <h2 className={styles.sectionTitle}>Demographic Breakdown</h2>
          <span className={styles.filterLabel}>Gender</span>
          <FilterBar
            options={Object.entries(GENDER_LABELS).map(([v, l]) => ({ value: v, label: l }))}
            selected={filter.gender ?? null}
            onSelect={(v) => setFilter((f) => ({ ...f, gender: v as Gender | undefined ?? undefined }))}
          />
          <span className={styles.filterLabel}>Age</span>
          <FilterBar
            options={Object.entries(AGE_GROUP_LABELS).map(([v, l]) => ({ value: v, label: l }))}
            selected={filter.ageGroup ?? null}
            onSelect={(v) => setFilter((f) => ({ ...f, ageGroup: v as AgeGroup | undefined ?? undefined }))}
          />
          <span className={styles.filterLabel}>Region</span>
          <FilterBar
            options={Object.entries(REGION_LABELS).map(([v, l]) => ({ value: v, label: l }))}
            selected={filter.region ?? null}
            onSelect={(v) => setFilter((f) => ({ ...f, region: v as Region | undefined ?? undefined }))}
          />
          <span className={styles.filterLabel}>Ethnicity</span>
          <FilterBar
            options={Object.entries(ETHNICITY_LABELS).map(([v, l]) => ({ value: v, label: l }))}
            selected={filter.ethnicity ?? null}
            onSelect={(v) => setFilter((f) => ({ ...f, ethnicity: v as Ethnicity | undefined ?? undefined }))}
          />
          <span className={styles.filterLabel}>Religion</span>
          <FilterBar
            options={Object.entries(RELIGIOUS_VIEW_LABELS).map(([v, l]) => ({ value: v, label: l }))}
            selected={filter.religiousView ?? null}
            onSelect={(v) => setFilter((f) => ({ ...f, religiousView: v as ReligiousView | undefined ?? undefined }))}
          />
          <span className={styles.filterLabel}>Politics</span>
          <FilterBar
            options={Object.entries(POLITICAL_VIEW_LABELS).map(([v, l]) => ({ value: v, label: l }))}
            selected={filter.politicalView ?? null}
            onSelect={(v) => setFilter((f) => ({ ...f, politicalView: v as PoliticalView | undefined ?? undefined }))}
          />
          <span className={styles.filterLabel}>Relationship</span>
          <FilterBar
            options={Object.entries(RELATIONSHIP_STATUS_LABELS).map(([v, l]) => ({ value: v, label: l }))}
            selected={filter.relationshipStatus ?? null}
            onSelect={(v) => setFilter((f) => ({ ...f, relationshipStatus: v as RelationshipStatus | undefined ?? undefined }))}
          />
        </div>

        {userRanking && (
          <div className={styles.shareRow}>
            <Button onClick={handleShare}>Share Results</Button>
            {copied && <span className={styles.copied}>Copied!</span>}
          </div>
        )}
      </div>
    </PageShell>
  );
}
