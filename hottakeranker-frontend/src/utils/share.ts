import { compareRankings, matchPercentage, emojiBar } from './ranking';
import { getNpcTier, NPC_TIER_EMOJIS, NPC_TIER_LABELS } from './npcScore';

export function generateShareText(
  question: string,
  userRanking: string[],
  crowdRanking: string[]
): string {
  const comparison = compareRankings(userRanking, crowdRanking);
  const pct = matchPercentage(userRanking, crowdRanking);
  const bar = emojiBar(comparison);
  const tier = getNpcTier(pct);

  return [
    `NPC Detector`,
    `"${question}"`,
    `${bar} ${pct}% match`,
    `${NPC_TIER_EMOJIS[tier]} ${NPC_TIER_LABELS[tier]}`,
    `npcdetector.com`,
  ].join('\n');
}

export async function copyToClipboard(text: string): Promise<boolean> {
  try {
    await navigator.clipboard.writeText(text);
    return true;
  } catch {
    return false;
  }
}
