import type { ReactNode } from 'react';
import styles from './PageShell.module.css';

interface Props {
  title?: string;
  children: ReactNode;
}

export default function PageShell({ title, children }: Props) {
  return (
    <main className={styles.shell}>
      {title && <h1 className={styles.title}>{title}</h1>}
      {children}
    </main>
  );
}
