import { useState, useEffect, useCallback, type FormEvent } from 'react';
import PageShell from '../components/layout/PageShell';
import Button from '../components/common/Button';
import Spinner from '../components/common/Spinner';
import ErrorBanner from '../components/common/ErrorBanner';
import { getAllTopics, createTopic, updateTopicStatus, deleteTopic, getPendingSuggestions, promoteSuggestion } from '../services/adminService';
import type { Topic, TopicSuggestion } from '../types/api';
import { TopicStatus } from '../types/enums';
import styles from './AdminPage.module.css';

const OPTION_COUNT = 8;

export default function AdminPage() {
  const [topics, setTopics] = useState<Topic[]>([]);
  const [suggestions, setSuggestions] = useState<TopicSuggestion[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Form state
  const [question, setQuestion] = useState('');
  const [category, setCategory] = useState('');
  const [options, setOptions] = useState<string[]>(Array(OPTION_COUNT).fill(''));
  const [status, setStatus] = useState<TopicStatus>(TopicStatus.PENDING);
  const [formError, setFormError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const load = useCallback(async () => {
    try {
      const [topicData, suggestionData] = await Promise.all([
        getAllTopics(),
        getPendingSuggestions(),
      ]);
      setTopics(topicData);
      setSuggestions(suggestionData);
    } catch {
      setError('Failed to load data');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  const setOption = (index: number, value: string) => {
    setOptions((prev) => prev.map((v, i) => (i === index ? value : v)));
  };

  const isValid =
    question.trim() &&
    category.trim() &&
    options.every((o) => o.trim()) &&
    new Set(options.map((o) => o.trim().toLowerCase())).size === OPTION_COUNT;

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setFormError('');
    setSubmitting(true);
    try {
      await createTopic({
        question: question.trim(),
        category: category.trim(),
        options: options.map((o) => o.trim()),
        status,
      });
      setQuestion('');
      setCategory('');
      setOptions(Array(OPTION_COUNT).fill(''));
      setStatus(TopicStatus.PENDING);
      await load();
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to create topic';
      setFormError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  const handleSetStatus = async (id: number, newStatus: TopicStatus) => {
    try {
      await updateTopicStatus(id, newStatus);
      await load();
    } catch {
      setError('Failed to update status');
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('Delete this topic? This cannot be undone.')) return;
    try {
      await deleteTopic(id);
      await load();
    } catch {
      setError('Failed to delete topic');
    }
  };

  const handlePromote = async (id: number) => {
    try {
      await promoteSuggestion(id);
      await load();
    } catch {
      setError('Failed to promote suggestion');
    }
  };

  const statusClass = (s: TopicStatus) => {
    if (s === TopicStatus.ACTIVE) return styles.statusActive;
    if (s === TopicStatus.PENDING) return styles.statusPending;
    return styles.statusArchived;
  };

  const statusActions = (topic: Topic) => {
    const buttons = [];
    if (topic.status !== TopicStatus.ACTIVE) {
      buttons.push(
        <button key="activate" className={styles.btnSmall} onClick={() => handleSetStatus(topic.id, TopicStatus.ACTIVE)}>
          Activate
        </button>
      );
    }
    if (topic.status !== TopicStatus.PENDING) {
      buttons.push(
        <button key="pending" className={styles.btnSmall} onClick={() => handleSetStatus(topic.id, TopicStatus.PENDING)}>
          Set Pending
        </button>
      );
    }
    if (topic.status !== TopicStatus.ARCHIVED) {
      buttons.push(
        <button key="archive" className={styles.btnSmall} onClick={() => handleSetStatus(topic.id, TopicStatus.ARCHIVED)}>
          Archive
        </button>
      );
    }
    return buttons;
  };

  return (
    <PageShell title="Admin Panel">
      <div className={styles.sections}>
        {/* Create Topic Form */}
        <div>
          <h2 className={styles.sectionTitle}>Create Topic</h2>
          <form className={styles.form} onSubmit={handleSubmit}>
            <ErrorBanner message={formError} />

            <div className={styles.field}>
              <label>Question</label>
              <input
                placeholder="What's the best...?"
                value={question}
                onChange={(e) => setQuestion(e.target.value)}
              />
            </div>

            <div className={styles.field}>
              <label>Category</label>
              <input
                placeholder="Food, Music, Sports..."
                value={category}
                onChange={(e) => setCategory(e.target.value)}
              />
            </div>

            <div className={styles.field}>
              <label>Options ({OPTION_COUNT} required)</label>
              <div className={styles.optionsGrid}>
                {options.map((opt, i) => (
                  <input
                    key={i}
                    placeholder={`Option ${i + 1}`}
                    value={opt}
                    onChange={(e) => setOption(i, e.target.value)}
                  />
                ))}
              </div>
            </div>

            <div className={styles.field}>
              <label>Status</label>
              <select value={status} onChange={(e) => setStatus(e.target.value as TopicStatus)}>
                <option value={TopicStatus.PENDING}>Pending</option>
                <option value={TopicStatus.ACTIVE}>Active</option>
                <option value={TopicStatus.ARCHIVED}>Archived</option>
              </select>
            </div>

            <Button type="submit" full disabled={!isValid || submitting}>
              {submitting ? 'Creating...' : 'Create Topic'}
            </Button>
          </form>
        </div>

        {/* Community Suggestions */}
        <div>
          <h2 className={styles.sectionTitle}>Community Suggestions</h2>
          {loading ? (
            <Spinner />
          ) : suggestions.length === 0 ? (
            <p style={{ color: 'var(--color-text-dim)' }}>No pending suggestions.</p>
          ) : (
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>Question</th>
                  <th>Category</th>
                  <th>Upvotes</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {suggestions.map((s) => (
                  <tr key={s.id}>
                    <td>{s.question}</td>
                    <td>{s.category}</td>
                    <td>{s.upvotes}</td>
                    <td>
                      <button className={styles.btnSmall} onClick={() => handlePromote(s.id)}>
                        Promote to Active
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        {/* Topic Table */}
        <div>
          <h2 className={styles.sectionTitle}>All Topics</h2>
          <ErrorBanner message={error} />
          {loading ? (
            <Spinner />
          ) : topics.length === 0 ? (
            <p style={{ color: 'var(--color-text-dim)' }}>No topics yet.</p>
          ) : (
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>Question</th>
                  <th>Category</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {topics.map((t) => (
                  <tr key={t.id}>
                    <td>{t.question}</td>
                    <td>{t.category}</td>
                    <td>
                      <span className={`${styles.statusBadge} ${statusClass(t.status)}`}>
                        {t.status}
                      </span>
                    </td>
                    <td>
                      <div className={styles.actions}>
                        {statusActions(t)}
                        <button
                          className={styles.btnDanger}
                          onClick={() => handleDelete(t.id)}
                        >
                          Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </PageShell>
  );
}
