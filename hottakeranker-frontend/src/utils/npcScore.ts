export type NpcTier = 'NPC' | 'NORMIE' | 'FREE_THINKER' | 'CONTRARIAN' | 'UNHINGED';

export function getNpcTier(pct: number): NpcTier {
  if (pct >= 95) return 'NPC';
  if (pct >= 80) return 'NORMIE';
  if (pct >= 60) return 'FREE_THINKER';
  if (pct >= 40) return 'CONTRARIAN';
  return 'UNHINGED';
}

export const NPC_TIER_LABELS: Record<NpcTier, string> = {
  NPC: 'NPC',
  NORMIE: 'Normie',
  FREE_THINKER: 'Free Thinker',
  CONTRARIAN: 'Contrarian',
  UNHINGED: 'Unhinged',
};

export const NPC_TIER_DESCRIPTIONS: Record<NpcTier, string> = {
  NPC: "You're an NPC. Basic.",
  NORMIE: 'Pretty mainstream.',
  FREE_THINKER: "You've got your own mind.",
  CONTRARIAN: 'You enjoy disagreeing.',
  UNHINGED: "You're built different.",
};

export const NPC_TIER_EMOJIS: Record<NpcTier, string> = {
  NPC: '🤖',
  NORMIE: '😐',
  FREE_THINKER: '🧠',
  CONTRARIAN: '😈',
  UNHINGED: '🔥',
};

export const NPC_TIER_COLORS: Record<NpcTier, string> = {
  NPC: 'var(--color-npc)',
  NORMIE: 'var(--color-normie)',
  FREE_THINKER: 'var(--color-freethinker)',
  CONTRARIAN: 'var(--color-contrarian)',
  UNHINGED: 'var(--color-unhinged)',
};
