import type { RouteLocationNormalized } from 'vue-router';
import zhCN from '@/locale/zh-CN';

const zhMenuTitleToLocaleKey: Record<string, string> = (() => {
  const map: Record<string, string> = {};
  const menu = (zhCN as Record<string, unknown>).menu as Record<string, string> | undefined;
  if (menu) {
    Object.entries(menu).forEach(([key, val]) => {
      if (typeof val === 'string' && !map[val]) {
        map[val] = `menu.${key}`;
      }
    });
  }
  return map;
})();

const STORAGE_KEY = 'arco-admin-route-visit-v1';
const MAX_ENTRIES = 120;

export interface VisitEntry {
  path: string;
  title: string;
  localeKey?: string;
  count: number;
  lastVisit: number;
}

const SKIP_EXACT = new Set([
  '/login',
  '/404',
  '/403',
  '/500',
  '/register',
]);

function shouldRecord(to: RouteLocationNormalized): boolean {
  const p = to.path;
  if (SKIP_EXACT.has(p)) return false;
  if (p.startsWith('/redirect')) return false;
  const { matched } = to;
  if (!matched.length) return false;
  const leaf = matched[matched.length - 1];
  if (leaf.meta?.requiresAuth === false) return false;
  const anyAuth = matched.some((r) => r.meta?.requiresAuth === true);
  if (!anyAuth) return false;
  const { name } = leaf;
  if (
    name === 'login' ||
    name === 'notFound' ||
    name === 'redirectWrapper'
  ) {
    return false;
  }
  return true;
}

function pickTitle(to: RouteLocationNormalized): { title: string; localeKey?: string } {
  for (let i = to.matched.length - 1; i >= 0; i -= 1) {
    const { meta } = to.matched[i];
    const title = meta?.title as string | undefined;
    const localeKey = meta?.locale as string | undefined;
    if (title && String(title).trim()) {
      return { title: String(title).trim(), localeKey };
    }
  }
  const title = typeof to.name === 'string' ? to.name : to.path;
  return { title };
}

function loadEntries(): VisitEntry[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return [];
    const arr = JSON.parse(raw) as VisitEntry[];
    if (!Array.isArray(arr)) return [];
    let migrated = false;
    const result = arr.map((e) => {
      if (!e.localeKey && e.title) {
        const key = zhMenuTitleToLocaleKey[e.title];
        if (key) {
          migrated = true;
          return { ...e, localeKey: key };
        }
      }
      return e;
    });
    if (migrated) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(result));
    }
    return result;
  } catch {
    return [];
  }
}

function prune(entries: VisitEntry[]): VisitEntry[] {
  if (entries.length <= MAX_ENTRIES) return entries;
  return [...entries]
    .sort((a, b) => b.lastVisit - a.lastVisit)
    .slice(0, MAX_ENTRIES);
}

function saveEntries(entries: VisitEntry[]) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(prune(entries)));
}

/** 路由完成后写入本地（用于「最近访问」「快捷入口」统计） */
export function recordRouteVisit(to: RouteLocationNormalized): void {
  if (!shouldRecord(to)) return;
  const { path } = to;
  const { title, localeKey } = pickTitle(to);
  const now = Date.now();
  const list = loadEntries();
  const idx = list.findIndex((e) => e.path === path);
  if (idx >= 0) {
    const prev = list[idx];
    list[idx] = {
      ...prev,
      title,
      localeKey,
      count: prev.count + 1,
      lastVisit: now,
    };
  } else {
    list.push({ path, title, localeKey, count: 1, lastVisit: now });
  }
  saveEntries(list);
}

/** 按最近访问时间 */
export function getRecentVisits(limit = 9): VisitEntry[] {
  return [...loadEntries()]
    .sort((a, b) => b.lastVisit - a.lastVisit)
    .slice(0, limit);
}

/** 按访问次数（常用入口） */
export function getFrequentVisits(limit = 6): VisitEntry[] {
  return [...loadEntries()]
    .sort((a, b) => {
      if (b.count !== a.count) return b.count - a.count;
      return b.lastVisit - a.lastVisit;
    })
    .slice(0, limit);
}

export type VisitIconKey =
  | 'user'
  | 'file'
  | 'apps'
  | 'settings'
  | 'list'
  | 'message'
  | 'dashboard'
  | 'default';

/** 退出登录时清空，避免同机换账号看到上一用户的记录 */
export function clearRouteVisitHistory(): void {
  localStorage.removeItem(STORAGE_KEY);
}

export function pathToIconKey(path: string): VisitIconKey {
  if (path.includes('/member')) return 'user';
  if (path.includes('/note') || path.includes('/blog')) return 'file';
  if (path.includes('/idle') || path.includes('/product')) return 'apps';
  if (path.includes('/system') || path.includes('/config')) return 'settings';
  if (path.includes('/log') || path.includes('/monitor')) return 'list';
  if (
    path.includes('/im') ||
    path.includes('/chat') ||
    path.includes('/customer')
  ) {
    return 'message';
  }
  if (path.includes('/dashboard')) return 'dashboard';
  return 'default';
}
