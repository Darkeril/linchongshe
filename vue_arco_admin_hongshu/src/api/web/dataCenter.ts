import axios from 'axios';

/** 数据中心-创作者/用户下拉选项（用于数据分析、粉丝画像、内容分析按用户筛选） */
export interface CreatorOption {
  id: string;
  username: string;
}

/** 获取创作者列表（用于下拉选择用户，仅返回 id、username） */
export function getCreatorOptions(params?: { pageNum?: number; pageSize?: number; keyword?: string }) {
  return axios({
    url: '/web/user/getUserList',
    method: 'get',
    params: { pageNum: 1, pageSize: 500, ...params },
  });
}

/** 粉丝画像-概览与趋势 */
export interface FanProfileOverview {
  totalFans: number;
  newFans: number;
  lostFans: number | null;
  dates: string[];
  total: number[];
  newFansTrend: number[];
  lostFansTrend: number[];
}

/** 粉丝画像-分布项 */
export interface FanProfileNameValue {
  name: string;
  value: number;
}

/** 粉丝画像-分布 */
export interface FanProfileDistribution {
  gender: FanProfileNameValue[];
  age: FanProfileNameValue[];
  region: FanProfileNameValue[];
  interest: FanProfileNameValue[];
}

/** 粉丝画像概览与趋势 */
export function getFanProfileOverview(params?: { uid?: string; days?: number }) {
  return axios({
    url: '/web/dataCenter/fanProfile/overview',
    method: 'get',
    params: params || {},
  });
}

/** 粉丝画像分布 */
export function getFanProfileDistribution(params?: { uid?: string }) {
  return axios({
    url: '/web/dataCenter/fanProfile/distribution',
    method: 'get',
    params: params || {},
  });
}

// ========== 数据分析 ==========

/** 概览单项（总览卡片） */
export interface AnalyticsOverviewItem {
  label: string;
  value: number | string;
  ring: number;
  ringText: string;
}

/** 名称数值（饼图等） */
export interface AnalyticsNameValue {
  name: string;
  value: number;
}

/** 观看数据 */
export interface AnalyticsView {
  overview: AnalyticsOverviewItem[];
  trend: number[];
  source: AnalyticsNameValue[];
  timeSlot: number[];
}

/** 单个 Tab（互动/涨粉/发布） */
export interface AnalyticsTab {
  overview: AnalyticsOverviewItem[];
  trend: number[];
}

/** 数据分析聚合 */
export interface AnalyticsData {
  view: AnalyticsView;
  interact: AnalyticsTab;
  fans: AnalyticsTab;
  publish: AnalyticsTab;
}

/** 数据分析（观看、互动、涨粉、发布） */
export function getAnalytics(params?: { uid?: string; days?: number }) {
  return axios({
    url: '/web/dataCenter/analytics',
    method: 'get',
    params: params || {},
  });
}
