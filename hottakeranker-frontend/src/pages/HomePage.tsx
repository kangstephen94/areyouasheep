import { useState, useEffect } from 'react';
import PageShell from '../components/layout/PageShell';
import TopicGrid from '../components/topic/TopicGrid';
import Spinner from '../components/common/Spinner';
import ErrorBanner from '../components/common/ErrorBanner';
import { useAuth } from '../hooks/useAuth';
import { getActiveTopics, getVotedTopicIds } from '../services/topicService';
import type { Topic } from '../types/api';

export default function HomePage() {
  const { isAuthenticated } = useAuth();
  const [topics, setTopics] = useState<Topic[]>([]);
  const [votedIds, setVotedIds] = useState<Set<number>>(new Set());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const load = async () => {
      try {
        const t = await getActiveTopics();
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
    <PageShell title="Hot Takes">
      <ErrorBanner message={error} />
      {loading ? <Spinner /> : <TopicGrid topics={topics} votedTopicIds={votedIds} />}
    </PageShell>
  );
}
