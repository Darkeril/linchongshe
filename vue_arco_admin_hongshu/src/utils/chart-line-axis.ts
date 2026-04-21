/**
 * 折线图 / 面积图：纵轴从 0 起；全为 0 时给合理上限，避免 ECharts 出现 0.8~1 等异常刻度。
 */
function finiteNums(values: number[]): number[] {
  return values.filter((n) => typeof n === 'number' && Number.isFinite(n));
}

/** 计数类指标（订单数、新增数等） */
export function countValueYAxisPartial(
  values: number[],
  options?: { emptyMax?: number }
): Record<string, unknown> {
  const emptyMax = options?.emptyMax ?? 8;
  const nums = finiteNums(values);
  const maxVal = nums.length ? Math.max(...nums, 0) : 0;
  const minVal = nums.length ? Math.min(...nums, 0) : 0;
  if (maxVal === 0 && minVal >= 0) {
    return {
      min: 0,
      max: emptyMax,
      minInterval: 1,
      scale: false,
    };
  }
  return {
    min: minVal < 0 ? undefined : 0,
    scale: false,
  };
}

/** 金额类（元），单独一轴，避免与条数混用同一刻度 */
export function amountValueYAxisPartial(values: number[]): Record<string, unknown> {
  const nums = finiteNums(values);
  const maxVal = nums.length ? Math.max(...nums, 0) : 0;
  if (maxVal === 0) {
    return { min: 0, max: 100, scale: false };
  }
  return { min: 0, scale: false };
}

/** 百分比类（如互动率），数据可能超过 100% */
export function percentValueYAxisPartial(values: number[]): Record<string, unknown> {
  const nums = finiteNums(values);
  const maxVal = nums.length ? Math.max(...nums, 0) : 0;
  if (maxVal === 0) {
    return { min: 0, max: 5, scale: false };
  }
  const hi = Math.ceil(maxVal * 1.15);
  return { min: 0, max: hi, scale: false };
}
