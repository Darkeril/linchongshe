/**
 * 工作台趋势图横轴：与「笔记互动效率」一致，按本地自然日生成连续区间（含今天共 n 天），标签为 MM-dd。
 */
export type RollingDay = { raw: string; label: string };

export function buildRollingDayAxis(dayCount: 7 | 30): RollingDay[] {
  const n = dayCount === 30 ? 30 : 7;
  const out: RollingDay[] = [];
  const end = new Date();
  end.setHours(0, 0, 0, 0);
  for (let i = n - 1; i >= 0; i -= 1) {
    const d = new Date(end.getTime());
    d.setDate(d.getDate() - i);
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    out.push({ raw: `${y}-${m}-${day}`, label: `${m}-${day}` });
  }
  return out;
}

function normDateKey(s: unknown): string {
  return String(s ?? '')
    .trim()
    .slice(0, 10);
}

/**
 * 将接口按「返回顺序」给出的序列，按 raw 日期对齐到连续轴上；缺日补 fallback。
 */
export function alignSeriesToAxis<T>(
  axis: RollingDay[],
  apiDates: unknown[],
  values: T[] | undefined,
  fallback: T
): T[] {
  const map = new Map<string, number>();
  apiDates.forEach((d, i) => {
    map.set(normDateKey(d), i);
  });
  return axis.map(({ raw }) => {
    const idx = map.get(raw);
    if (idx == null || !values || idx >= values.length) return fallback;
    const v = values[idx];
    return v === undefined || v === null ? fallback : v;
  });
}
