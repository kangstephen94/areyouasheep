import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import PageShell from '../components/layout/PageShell';
import RankingBoard from '../components/ranking/RankingBoard';
import Spinner from '../components/common/Spinner';
import ErrorBanner from '../components/common/ErrorBanner';
import { useAuth } from '../hooks/useAuth';
import { getActiveTopics } from '../services/topicService';
import { submitVote } from '../services/voteService';
import type { Topic } from '../types/api';

export default function RankPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [topic, setTopic] = useState<Topic | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    getActiveTopics()
      .then((topics) => {
        const found = topics.find((t) => t.id === Number(id));
        setTopic(found ?? null);
        if (!found) setError('Topic not found');
      })
      .catch(() => setError('Failed to load topic'))
      .finally(() => setLoading(false));
  }, [id]);

  const handleSubmit = async (ranked: string[]) => {
    if (!topic || !user) return;
    setSubmitting(true);
    setError('');
    try {
      // Convert ranked option names to indices in original options array
      const ranking = ranked.map((name) => topic.options.indexOf(name));
      await submitVote({ userId: user.id, topicId: topic.id, ranking });
      // Store user ranking in sessionStorage for results page
      sessionStorage.setItem(`rank-${topic.id}`, JSON.stringify(ranked));
      navigate(`/topic/${topic.id}/results`);
    } catch {
      setError('Failed to submit vote. You may have already voted on this topic.');
      setSubmitting(false);
    }
  };

  if (loading) return <PageShell><Spinner /></PageShell>;
  if (!topic) return <PageShell><ErrorBanner message={error || 'Topic not found'} /></PageShell>;

  return (
    <PageShell title={topic.question}>
      <ErrorBanner message={error} />
      <RankingBoard options={topic.options} onSubmit={handleSubmit} submitting={submitting} />
    </PageShell>
  );
}
