import { useState, useEffect, useMemo } from 'react';
import PageShell from '../components/layout/PageShell';
import FilterBar from '../components/common/FilterBar';
import TopicGrid from '../components/topic/TopicGrid';
import Spinner from '../components/common/Spinner';
import ErrorBanner from '../components/common/ErrorBanner';
import { useAuth } from '../hooks/useAuth';
import { getActiveTopics, getVotedTopicIds } from '../services/topicService';
import type { Topic } from '../types/api';
import styles from './HomePage.module.css';

const CATEGORY_ORDER = [
  'Food', 'Games', 'Sports', 'Entertainment', 'Culture', 'Tech', 'Spicy',
  'Politics', 'Films', 'Music', 'Lifestyle', 'Science',
];

export default function HomePage() {
  const { isAuthenticated } = useAuth();
  const [topics, setTopics] = useState<Topic[]>([]);
  const [votedIds, setVotedIds] = useState<Set<number>>(new Set());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null);

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

  const categories = useMemo(() => {
    const seen = new Set<string>();
    for (const topic of topics) seen.add(topic.category);
    return CATEGORY_ORDER.filter((c) => seen.has(c)).concat(
      [...seen].filter((c) => !CATEGORY_ORDER.includes(c))
    );
  }, [topics]);

  const filtered = useMemo(() => {
    const list = selectedCategory
      ? topics.filter((t) => t.category === selectedCategory)
      : topics;
    return list.slice(0, 9);
  }, [topics, selectedCategory]);

  return (
    <div className={styles.wide}>
      <PageShell title="NPC Detector">
        <ErrorBanner message={error} />
        {loading ? (
          <Spinner />
        ) : (
          <>
            <FilterBar
              options={categories.map((c) => ({ value: c, label: c }))}
              selected={selectedCategory}
              onSelect={setSelectedCategory}
            />
            <TopicGrid topics={filtered} votedTopicIds={votedIds} />
          </>
        )}
      </PageShell>
    </div>
  );
}
