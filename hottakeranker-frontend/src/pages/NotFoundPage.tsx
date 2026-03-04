import { Link } from 'react-router-dom';
import PageShell from '../components/layout/PageShell';

export default function NotFoundPage() {
  return (
    <PageShell>
      <div style={{ textAlign: 'center', paddingTop: '20vh' }}>
        <h1 style={{ fontSize: '4rem', fontWeight: 800, color: 'var(--color-brand)' }}>404</h1>
        <p style={{ color: 'var(--color-text-muted)', marginBottom: 'var(--space-lg)' }}>
          This page doesn't exist
        </p>
        <Link to="/" style={{ fontWeight: 600 }}>
          Back to Hot Takes
        </Link>
      </div>
    </PageShell>
  );
}
