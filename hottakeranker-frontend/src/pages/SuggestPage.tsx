import { useState, useEffect, useCallback } from 'react';
import PageShell from '../components/layout/PageShell';
import SuggestForm from '../components/suggest/SuggestForm';
import SuggestionCard from '../components/suggest/SuggestionCard';
import Spinner from '../components/common/Spinner';
import ErrorBanner from '../components/common/ErrorBanner';
import { suggest, getSuggestions, upvoteSuggestion } from '../services/suggestionService';
import type { TopicSuggestion } from '../types/api';
import styles from './SuggestPage.module.css';

export default function SuggestPage() {
  const [suggestions, setSuggestions] = useState<TopicSuggestion[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    try {
      const data = await getSuggestions();
      setSuggestions(data);
    } catch {
      setError('Failed to load suggestions');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  const handleSubmit = async (data: { question: string; category: string; options: string[] }) => {
    await suggest(data);
    await load();
  };

  const handleUpvote = async (id: number) => {
    try {
      await upvoteSuggestion(id);
      await load();
    } catch {
      // Silently ignore duplicate upvotes
    }
  };

  return (
    <PageShell title="Suggest a Topic">
      <div className={styles.sections}>
        <div>
          <SuggestForm onSubmit={handleSubmit} />
        </div>

        <div>
          <h2 className={styles.sectionTitle}>Community Suggestions</h2>
          <ErrorBanner message={error} />
          {loading ? (
            <Spinner />
          ) : (
            <div className={styles.list}>
              {suggestions.length === 0 ? (
                <p style={{ color: 'var(--color-text-dim)' }}>No suggestions yet. Be the first!</p>
              ) : (
                suggestions.map((s) => (
                  <SuggestionCard key={s.id} suggestion={s} onUpvote={handleUpvote} />
                ))
              )}
            </div>
          )}
        </div>
      </div>
    </PageShell>
  );
}
