import type { LocationQueryRaw, Router } from 'vue-router';
import { Message } from '@arco-design/web-vue';

/**
 * 服务端 getRouters 里笔记审核菜单 path 多为 noteAudit（全路径 /note/noteAudit），
 * 与静态路由 hongshu.ts 中 path: audit（/note/audit）不一致，需兼容两者。
 */
const NOTE_AUDIT_PATHS = ['/note/noteAudit', '/note/audit'];

function isResolvable(router: Router, path: string): boolean {
  return router.resolve(path).name !== 'notFound';
}

function pushFirstMatch(
  router: Router,
  paths: string[],
  warnMsg?: string,
  query?: LocationQueryRaw,
): void {
  const hit = paths.find((p) => isResolvable(router, p));
  if (hit) {
    router.push({ path: hit, query }).catch(() => {});
    return;
  }
  if (warnMsg) {
    Message.warning(warnMsg);
  }
}

export function pushNoteAudit(router: Router): void {
  pushFirstMatch(
    router,
    NOTE_AUDIT_PATHS,
    '未找到笔记审核页面，请确认菜单已分配且路径为 noteAudit 或 audit',
  );
}

export function pushIdleAudit(router: Router): void {
  if (router.hasRoute('IdleAudit')) {
    router.push({ name: 'IdleAudit' }).catch(() => {});
    return;
  }
  pushFirstMatch(router, ['/idle/audit'], '未找到商品审核页面，请检查菜单配置');
}

export function pushIfResolvable(
  router: Router,
  paths: string[],
  warnMsg?: string,
  query?: LocationQueryRaw,
): void {
  pushFirstMatch(router, paths, warnMsg, query);
}

/** 获取今天 00:00:00 ~ 23:59:59 的字符串对，供 a-range-picker 使用 */
export function getTodayRange(): [string, string] {
  const d = new Date();
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  const day = `${yyyy}-${mm}-${dd}`;
  return [`${day} 00:00:00`, `${day} 23:59:59`];
}
