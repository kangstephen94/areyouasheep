import { EloTier } from '../../types/enums';
import { getEloTier, TIER_COLORS, TIER_LABELS } from '../../utils/elo';
import styles from './Badge.module.css';

interface Props {
  elo: number;
}

export default function Badge({ elo }: Props) {
  const tier = getEloTier(elo);
  const color = TIER_COLORS[tier];
  const icon: Record<EloTier, string> = {
    [EloTier.BRONZE]: '\u25C6',
    [EloTier.SILVER]: '\u25C6',
    [EloTier.GOLD]: '\u2605',
    [EloTier.PLATINUM]: '\u2605',
    [EloTier.DIAMOND]: '\u2666',
  };

  return (
    <span className={styles.badge} style={{ color }}>
      {icon[tier]} {TIER_LABELS[tier]}
    </span>
  );
}
