/**
 * 环形图：合并长尾分类，减少扇区与引导线拥挤
 */
export type PieDatum = { name: string; value: number };

export function foldPieOthers(
  data: PieDatum[],
  options?: { maxItems?: number; othersLabel?: string }
): PieDatum[] {
  const maxItems = options?.maxItems ?? 8;
  const othersLabel = options?.othersLabel ?? '其他';
  const list = [...data]
    .map((x) => ({ name: x.name, value: Number(x.value) || 0 }))
    .filter((x) => x.value > 0)
    .sort((a, b) => b.value - a.value);
  if (list.length <= maxItems) return list;
  const head = list.slice(0, maxItems - 1);
  const tail = list.slice(maxItems - 1);
  const otherVal = tail.reduce((s, x) => s + x.value, 0);
  if (otherVal <= 0) return head;
  return [...head, { name: othersLabel, value: otherVal }];
}

/** ECharts tooltip item 回调入参 */
export function pieItemTooltipFormatter(params: unknown): string {
  const p = (Array.isArray(params) ? params[0] : params) as {
    name?: string;
    value?: number;
    percent?: number;
  };
  const name = p?.name ?? '';
  const val = p?.value ?? 0;
  const pct =
    p?.percent != null ? Number(p.percent).toFixed(2) : '';
  return `${name}<br/>数量：${val}<br/>占比：${pct}%`;
}

/** 饼图扇区外侧百分比：过小扇区不显示文字，避免挤在一起 */
export function pieOutsidePercentFormatter(params: unknown): string {
  const p = params as { percent?: number };
  const pct = p?.percent;
  if (pct == null || Number.isNaN(pct)) return '';
  if (pct < 2) return '';
  return `${pct.toFixed(1)}%`;
}
