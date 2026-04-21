import { Message } from '@arco-design/web-vue';
import type { LocationQueryRaw, RouteRecordNormalized, Router } from 'vue-router';
import useAppStore from '@/store/modules/app';

/**
 * 服务端菜单模式下，Workplace 等路由在 getRouters 挂载前不存在；
 * 若直接 router.push({ name: 'Workplace' }) 会报 No match。
 * @returns 是否需要服务端菜单且已成功挂载（非服务端菜单恒为 true）
 */
export async function ensureServerMenusMounted(): Promise<boolean> {
  const appStore = useAppStore();
  if (!appStore.menuFromServer) return true;
  if (appStore.dynamicRoutesMounted) return true;
  return appStore.fetchServerMenuConfig();
}

/** 与 getRouters 常见工作台 path 对齐（route-manager 中 Workplace 多挂在此 path 下） */
export const FALLBACK_ADMIN_PATH = '/dashboard/dashboard';

/** 常见首页 path，按顺序尝试（后端菜单未固定为 dashboard/dashboard 时仍可能命中） */
const EXTRA_FALLBACK_PATHS = [
  '/dashboard/workplace',
  '/dashboard/index',
  '/index',
];

function parseRedirectName(raw: unknown): string {
  if (typeof raw === 'string') {
    return raw.trim();
  }
  if (Array.isArray(raw) && typeof raw[0] === 'string') {
    return raw[0].trim();
  }
  return '';
}

/**
 * 从已同步的 serverMenu 树算出第一个能 resolve 到的叶子 path（与动态路由一致）
 */
function firstNavigablePathFromServerMenu(
  router: Router,
  nodes: RouteRecordNormalized[] | undefined,
  parentAbs = '',
): string | null {
  if (!nodes?.length) return null;
  for (let i = 0; i < nodes.length; i += 1) {
    const n = nodes[i];
    const seg = String(n.path || '');
    let abs: string;
    if (seg.startsWith('/')) {
      abs = seg.replace(/\/+/g, '/');
    } else {
      const base = parentAbs.replace(/\/$/, '') || '';
      abs = base ? `${base}/${seg}`.replace(/\/+/g, '/') : `/${seg}`;
    }
    if (!abs.startsWith('/')) abs = `/${abs}`;
    const children = n.children as RouteRecordNormalized[] | undefined;
    if (children?.length) {
      const sub = firstNavigablePathFromServerMenu(router, children, abs);
      if (sub) return sub;
    } else if (router.resolve({ path: abs }).matched.length) {
      return abs;
    }
  }
  return null;
}

async function pushIfPathMatches(
  router: Router,
  path: string,
  query: LocationQueryRaw,
): Promise<boolean> {
  if (!path) return false;
  const resolved = router.resolve({ path, query });
  if (resolved.matched.length) {
    await router.push(resolved);
    return true;
  }
  return false;
}

/**
 * 登录成功后的跳转：先挂载动态路由，再按 name 或回退 path。
 */
export async function pushAfterLogin(
  router: Router,
  query: LocationQueryRaw,
): Promise<void> {
  const menusOk = await ensureServerMenusMounted();
  if (!menusOk) {
    const redirect = parseRedirectName(query.redirect);
    await router.replace({
      name: 'login',
      query: {
        ...query,
        ...(redirect ? { redirect } : {}),
      },
    });
    return;
  }

  const redirectRaw = parseRedirectName(query.redirect);

  const rest: LocationQueryRaw = { ...query };
  delete (rest as Record<string, unknown>).redirect;

  // redirect 可能是路由 name，也可能是 path（如 /system/user）
  if (redirectRaw) {
    if (router.hasRoute(redirectRaw)) {
      await router.push({ name: redirectRaw, query: rest });
      return;
    }
    const pathCandidate = redirectRaw.startsWith('/')
      ? redirectRaw
      : `/${redirectRaw}`;
    if (await pushIfPathMatches(router, pathCandidate, rest)) {
      return;
    }
  }

  if (router.hasRoute('Workplace')) {
    await router.push({ name: 'Workplace', query: rest });
    return;
  }

  if (await pushIfPathMatches(router, FALLBACK_ADMIN_PATH, rest)) {
    return;
  }

  const extraHit = EXTRA_FALLBACK_PATHS.find(
    (p) => router.resolve({ path: p, query: rest }).matched.length > 0,
  );
  if (extraHit && (await pushIfPathMatches(router, extraHit, rest))) {
    return;
  }

  const appStore = useAppStore();
  const fromMenu = firstNavigablePathFromServerMenu(
    router,
    appStore.serverMenu as RouteRecordNormalized[],
  );
  if (fromMenu && (await pushIfPathMatches(router, fromMenu, rest))) {
    return;
  }

  Message.warning({
    content:
      '未找到可进入的首页：请确认账号已分配菜单，且 getRouters 中组件路径与前端 views 一致（如工作台多为 dashboard/dashboard）。',
    duration: 8000,
  });
  await router.push({ name: 'notFound', query: rest });
}
