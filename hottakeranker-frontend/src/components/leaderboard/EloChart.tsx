import type { EloHistoryEntryDto } from '../../types/api';
import styles from './EloChart.module.css';

interface Props {
  history: EloHistoryEntryDto[];
  currentElo: number;
}

export default function EloChart({ history, currentElo }: Props) {
  if (history.length === 0) {
    return null;
  }

  // Build cumulative elo points from history
  const points: number[] = [];
  let elo = currentElo;
  // history is chronological, so compute backwards to get starting elo
  for (let i = history.length - 1; i >= 0; i--) {
    elo -= history[i].eloChange;
  }
  // Now walk forward
  for (const entry of history) {
    elo += entry.eloChange;
    points.push(elo);
  }

  const minElo = Math.min(...points) - 20;
  const maxElo = Math.max(...points) + 20;
  const range = maxElo - minElo || 1;

  const w = 600;
  const h = 180;
  const padX = 40;
  const padY = 10;
  const chartW = w - padX * 2;
  const chartH = h - padY * 2;

  const coords = points.map((val, i) => ({
    x: padX + (i / Math.max(points.length - 1, 1)) * chartW,
    y: padY + chartH - ((val - minElo) / range) * chartH,
  }));

  const pathD = coords
    .map((c, i) => `${i === 0 ? 'M' : 'L'} ${c.x} ${c.y}`)
    .join(' ');

  // Grid lines
  const gridLines = 4;
  const gridVals = Array.from({ length: gridLines }, (_, i) =>
    Math.round(minElo + (range * i) / (gridLines - 1))
  );

  return (
    <div className={styles.wrapper}>
      <div className={styles.chartTitle}>Elo History</div>
      <div className={styles.chart}>
        <svg viewBox={`0 0 ${w} ${h}`} preserveAspectRatio="none">
          {/* Grid lines */}
          {gridVals.map((v) => {
            const y = padY + chartH - ((v - minElo) / range) * chartH;
            return (
              <g key={v}>
                <line
                  x1={padX}
                  y1={y}
                  x2={w - padX}
                  y2={y}
                  stroke="var(--color-bg-hover)"
                  strokeWidth="1"
                />
                <text
                  x={padX - 6}
                  y={y + 4}
                  textAnchor="end"
                  fill="var(--color-text-dim)"
                  fontSize="10"
                >
                  {v}
                </text>
              </g>
            );
          })}

          {/* Line */}
          <path d={pathD} fill="none" stroke="var(--color-brand)" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" />

          {/* Dots */}
          {coords.map((c, i) => (
            <circle
              key={i}
              cx={c.x}
              cy={c.y}
              r="4"
              fill="var(--color-brand)"
              stroke="var(--color-bg-raised)"
              strokeWidth="2"
            />
          ))}

          {/* 1000 baseline */}
          {1000 >= minElo && 1000 <= maxElo && (
            <line
              x1={padX}
              y1={padY + chartH - ((1000 - minElo) / range) * chartH}
              x2={w - padX}
              y2={padY + chartH - ((1000 - minElo) / range) * chartH}
              stroke="var(--color-text-dim)"
              strokeWidth="1"
              strokeDasharray="4,4"
            />
          )}
        </svg>
      </div>
    </div>
  );
}
