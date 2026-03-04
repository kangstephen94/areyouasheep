import type { ButtonHTMLAttributes } from 'react';
import styles from './Button.module.css';

interface Props extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'ghost';
  full?: boolean;
  size?: 'md' | 'lg';
}

export default function Button({
  variant = 'primary',
  full = false,
  size = 'md',
  className = '',
  children,
  ...rest
}: Props) {
  const cls = [
    styles.btn,
    styles[variant],
    full && styles.full,
    size === 'lg' && styles.lg,
    className,
  ]
    .filter(Boolean)
    .join(' ');

  return (
    <button className={cls} {...rest}>
      {children}
    </button>
  );
}
