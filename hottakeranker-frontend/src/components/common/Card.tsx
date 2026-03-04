import type { ReactNode, HTMLAttributes } from 'react';
import styles from './Card.module.css';

interface Props extends HTMLAttributes<HTMLDivElement> {
  clickable?: boolean;
  children: ReactNode;
}

export default function Card({ clickable = false, className = '', children, ...rest }: Props) {
  return (
    <div
      className={`${styles.card} ${clickable ? styles.clickable : ''} ${className}`}
      {...rest}
    >
      {children}
    </div>
  );
}
