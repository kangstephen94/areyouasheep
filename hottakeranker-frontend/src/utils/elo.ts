import { EloTier } from '../types/enums';

export function getEloTier(elo: number): EloTier {
  if (elo >= 1400) return EloTier.DIAMOND;
  if (elo >= 1300) return EloTier.PLATINUM;
  if (elo >= 1200) return EloTier.GOLD;
  if (elo >= 1100) return EloTier.SILVER;
  return EloTier.BRONZE;
}

export const TIER_COLORS: Record<EloTier, string> = {
  [EloTier.BRONZE]: 'var(--color-bronze)',
  [EloTier.SILVER]: 'var(--color-silver)',
  [EloTier.GOLD]: 'var(--color-gold)',
  [EloTier.PLATINUM]: 'var(--color-platinum)',
  [EloTier.DIAMOND]: 'var(--color-diamond)',
};

export const TIER_LABELS: Record<EloTier, string> = {
  [EloTier.BRONZE]: 'Bronze',
  [EloTier.SILVER]: 'Silver',
  [EloTier.GOLD]: 'Gold',
  [EloTier.PLATINUM]: 'Platinum',
  [EloTier.DIAMOND]: 'Diamond',
};
