import { useState, type FormEvent } from 'react';
import Button from '../common/Button';
import ErrorBanner from '../common/ErrorBanner';
import styles from './SuggestForm.module.css';

interface Props {
  onSubmit: (data: { question: string; category: string; options: string[] }) => Promise<void>;
}

const OPTION_COUNT = 8;

export default function SuggestForm({ onSubmit }: Props) {
  const [question, setQuestion] = useState('');
  const [category, setCategory] = useState('');
  const [options, setOptions] = useState<string[]>(Array(OPTION_COUNT).fill(''));
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

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
    setError('');
    setSubmitting(true);
    try {
      await onSubmit({
        question: question.trim(),
        category: category.trim(),
        options: options.map((o) => o.trim()),
      });
      setQuestion('');
      setCategory('');
      setOptions(Array(OPTION_COUNT).fill(''));
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to submit';
      setError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form className={styles.form} onSubmit={handleSubmit}>
      <ErrorBanner message={error} />

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

      <Button type="submit" full disabled={!isValid || submitting}>
        {submitting ? 'Submitting...' : 'Submit Suggestion'}
      </Button>
    </form>
  );
}
