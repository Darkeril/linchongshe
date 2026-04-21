/**
 * 发现页等接口的短时内存缓存（仅当前 SPA 生命周期内有效）
 *
 * - 浏览器整页刷新（F5 / 地址栏回车）会重建 JS 上下文，Map 清空 → 一定走网络拉最新数据
 * - 同一次打开站点、不刷新的前提下，路由切来切去仍可命中缓存（配合 TTL）
 * - 用户点浮动刷新、退出登录会主动失效推荐与视频 Tab 第一页缓存
 */
import { storage } from "@/utils/storage";

const PREFIX = "hongshu_ac_";

/** 分类树缓存（毫秒） */
export const CATEGORY_TREE_TTL_MS = 15 * 60 * 1000;
/** 推荐 / 视频发现流第一页缓存（毫秒），不宜过长以免列表陈旧 */
export const RECOMMEND_FIRST_PAGE_TTL_MS = 45 * 1000;

interface CacheRow<T> {
  exp: number;
  payload: T;
}

/** 仅内存：刷新页面即清空，符合「刷新就要新数据」的预期 */
const memory = new Map<string, CacheRow<unknown>>();

function readRow<T>(key: string): T | null {
  const fullKey = PREFIX + key;
  const row = memory.get(fullKey) as CacheRow<T> | undefined;
  if (!row || typeof row.exp !== "number") return null;
  if (Date.now() > row.exp) {
    memory.delete(fullKey);
    return null;
  }
  return row.payload;
}

function writeRow<T>(key: string, payload: T, ttlMs: number) {
  const fullKey = PREFIX + key;
  memory.set(fullKey, {
    exp: Date.now() + ttlMs,
    payload,
  } as CacheRow<T>);
}

/** 用于区分游客 / 不同账号的推荐缓存 */
export function cacheUserFingerprint(): string {
  const t = storage.get("accessToken");
  if (typeof t === "string" && t.length > 12) return t.slice(0, 16);
  return "guest";
}

export function getCachedCategoryTree<T>(): T | null {
  return readRow<T>("category_tree");
}

export function setCachedCategoryTree<T>(payload: T) {
  writeRow("category_tree", payload, CATEGORY_TREE_TTL_MS);
}

export function getCachedRecommendFirstPage<T>(pageSize: number): T | null {
  const key = `recommend_1_${pageSize}_${cacheUserFingerprint()}`;
  return readRow<T>(key);
}

export function setCachedRecommendFirstPage<T>(pageSize: number, payload: T) {
  const key = `recommend_1_${pageSize}_${cacheUserFingerprint()}`;
  writeRow(key, payload, RECOMMEND_FIRST_PAGE_TTL_MS);
}

/** 发现页「视频」Tab 第一页（与推荐相同 TTL / 用户指纹策略） */
export function getCachedVideoFirstPage<T>(pageSize: number): T | null {
  const key = `video_1_${pageSize}_${cacheUserFingerprint()}`;
  return readRow<T>(key);
}

export function setCachedVideoFirstPage<T>(pageSize: number, payload: T) {
  const key = `video_1_${pageSize}_${cacheUserFingerprint()}`;
  writeRow(key, payload, RECOMMEND_FIRST_PAGE_TTL_MS);
}

export function invalidateCategoryCache() {
  memory.delete(PREFIX + "category_tree");
}

/** 浮动刷新、退出登录等：清空推荐与视频发现流第一页缓存 */
export function invalidateRecommendCaches() {
  for (const k of memory.keys()) {
    if (k.startsWith(PREFIX + "recommend_") || k.startsWith(PREFIX + "video_")) {
      memory.delete(k);
    }
  }
}
