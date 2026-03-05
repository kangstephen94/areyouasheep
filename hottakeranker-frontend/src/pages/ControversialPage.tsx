import { useState, useEffect } from 'react';
import PageShell from '../components/layout/PageShell';
import ControversialCard from '../components/topic/ControversialCard';
import Spinner from '../components/common/Spinner';
import ErrorBanner from '../components/common/ErrorBanner';
import { useAuth } from '../hooks/useAuth';
import { getControversialTopics, getVotedTopicIds } from '../services/topicService';
import type { ControversialTopicDto } from '../types/api';

export default function ControversialPage() {
  const { isAuthenticated } = useAuth();
  const [topics, setTopics] = useState<ControversialTopicDto[]>([]);
  const [votedIds, setVotedIds] = useState<Set<number>>(new Set());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const load = async () => {
      try {
        const t = await getControversialTopics();
        setTopics(t);
        if (isAuthenticated) {
          try {
            const ids = await getVotedTopicIds();
            setVotedIds(new Set(ids));
          } catch {
            // non-critical
          }
        }
      } catch {
        setError('Failed to load topics');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [isAuthenticated]);

  return (
    <PageShell title="Most Controversial">
      <ErrorBanner message={error} />
      {loading ? (
        <Spinner />
      ) : topics.length === 0 ? (
        <div style={{ textAlign: 'center', color: 'var(--color-text-dim)', padding: 'var(--space-2xl)', fontSize: '1.1rem' }}>
          No controversial topics yet. Check back when more votes come in!
        </div>
      ) : (
        topics.map((topic) => (
          <ControversialCard
            key={topic.topicId}
            topic={topic}
            voted={votedIds.has(topic.topicId)}
          />
        ))
      )}
    </PageShell>
  );
}
