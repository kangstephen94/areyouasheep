import { getNpcTier, NPC_TIER_LABELS, NPC_TIER_DESCRIPTIONS, NPC_TIER_EMOJIS, NPC_TIER_COLORS } from '../../utils/npcScore';
import styles from './NpcBadge.module.css';

interface Props {
  matchPercentage: number;
}

export default function NpcBadge({ matchPercentage }: Props) {
  const tier = getNpcTier(matchPercentage);

  return (
    <div className={styles.wrapper}>
      <div className={styles.emoji}>{NPC_TIER_EMOJIS[tier]}</div>
      <div className={styles.tier} style={{ color: NPC_TIER_COLORS[tier] }}>
        {NPC_TIER_LABELS[tier]}
      </div>
      <div className={styles.description}>{NPC_TIER_DESCRIPTIONS[tier]}</div>
    </div>
  );
}
