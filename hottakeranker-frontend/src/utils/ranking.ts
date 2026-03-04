/**
 * Compare user ranking to crowd ranking and return per-position match info.
 * Returns an array of { option, emoji } where emoji is:
 *   🟩 = exact position match
 *   🟨 = off by 1 position
 *   🟥 = off by 2+ positions
 */
export function compareRankings(
  userRanking: string[],
  crowdRanking: string[]
): { option: string; emoji: string; diff: number }[] {
  return userRanking.map((option, userIdx) => {
    const crowdIdx = crowdRanking.indexOf(option);
    const diff = Math.abs(userIdx - crowdIdx);
    let emoji: string;
    if (diff === 0) emoji = '🟩';
    else if (diff === 1) emoji = '🟨';
    else emoji = '🟥';
    return { option, emoji, diff };
  });
}

/**
 * Calculate a match percentage (0-100) between user and crowd rankings.
 * Uses inverse of normalized Kendall tau distance.
 */
export function matchPercentage(userRanking: string[], crowdRanking: string[]): number {
  const n = userRanking.length;
  if (n <= 1) return 100;
  const maxDisorder = (n * (n - 1)) / 2;
  let disorder = 0;
  for (let i = 0; i < n; i++) {
    for (let j = i + 1; j < n; j++) {
      const uA = userRanking.indexOf(userRanking[i]);
      const uB = userRanking.indexOf(userRanking[j]);
      const cA = crowdRanking.indexOf(userRanking[i]);
      const cB = crowdRanking.indexOf(userRanking[j]);
      if ((uA - uB) * (cA - cB) < 0) disorder++;
    }
  }
  return Math.round(((maxDisorder - disorder) / maxDisorder) * 100);
}

/** Generate emoji bar string from comparison results */
export function emojiBar(comparison: { emoji: string }[]): string {
  return comparison.map((c) => c.emoji).join('');
}
