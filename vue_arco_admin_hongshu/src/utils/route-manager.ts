import type { RouteRecordRaw, Router } from 'vue-router';
import zhCN from '@/locale/zh-CN';

const zhTitleToLocaleKey: Record<string, string> = (() => {
  const map: Record<string, string> = {};
  const menuMessages = (zhCN as Record<string, unknown>).menu as Record<string, string> | undefined;
  if (menuMessages) {
    Object.entries(menuMessages).forEach(([key, val]) => {
      if (typeof val === 'string' && !map[val]) {
        map[val] = `menu.${key}`;
      }
    });
  }
  return map;
})();

/** 后端 getRouters 单条菜单（RuoYi） */
export type ServerMenuItem = {
  path: string;
  name?: string;
  hidden?: boolean;
  component?: string;
  redirect?: string;
  alwaysShow?: boolean;
  children?: ServerMenuItem[];
  meta?: {
    title?: string;
    icon?: string;
    noCache?: boolean;
    link?: string | null;
    [key: string]: unknown;
  };
};

/** 后端 component 与 Arco 工程 views 路径不一致时的别名 */
const COMPONENT_ALIASES: Record<string, string> = {
  index: 'dashboard/index/index',
  // RuoYi 默认「工作台」组件名；本 Arco 项目实际页面在 dashboard/workplace
  'dashboard/index': 'dashboard/workplace/index',
  'monitor/cache/index': 'monitor/cache/info/index',
};

function joinPath(parent: string, segment: string): string {
  if (segment.startsWith('/')) {
    return segment.replace(/\/+/g, '/');
  }
  const base = parent && parent !== '/' ? parent.replace(/\/$/, '') : '';
  if (!base) {
    return `/${segment}`.replace(/\/+/g, '/');
  }
  return `${base}/${segment}`.replace(/\/+/g, '/');
}

class RouteManager {
  private router: Router | null = null;

  private mountedRootNames: string[] = [];

  setRouter(router: Router) {
    this.router = router;
  }

  clearMountedRoutes() {
    if (!this.router) return;
    const { router } = this;
    [...this.mountedRootNames].reverse().forEach((name) => {
      if (name && router.hasRoute(name)) {
        router.removeRoute(name);
      }
    });
    this.mountedRootNames = [];
  }

  /**
   * 将 getRouters 数据转为顶层 RouteRecordRaw 并 addRoute。
   * hidden 路由也会注册（仅侧边栏通过 meta.hideInMenu 隐藏），避免详情页无法直达。
   */
  transformAndMountServerMenus(menus: ServerMenuItem[]): RouteRecordRaw[] {
    this.clearMountedRoutes();
    if (!this.router || !menus?.length) {
      return [];
    }

    const viewModules = import.meta.glob('@/views/**/*.vue');

    const loadView = (view: string) => {
      const normalized = view.replace(/^\/+/, '').replace(/\.vue$/, '');
      const candidate = COMPONENT_ALIASES[normalized] || normalized;
      const matchPath = Object.keys(viewModules).find((p) =>
        p.endsWith(`${candidate}.vue`),
      );
      return matchPath ? viewModules[matchPath] : undefined;
    };

    const mapComponent = (component?: string) => {
      if (!component) return undefined;
      if (component === 'Layout') {
        return () => import('@/layout/default-layout.vue');
      }
      if (component === 'ParentView') {
        return () => import('@/components/parent-view/index.vue');
      }
      return loadView(component);
    };

    const usedNames = new Set<string>();
    const allocateName = (raw: string | undefined, fallback: string): string => {
      let base = (raw && String(raw).trim()) || fallback;
      base = base.replace(/[^\w\u4e00-\u9fa5]/g, '_').replace(/^_+|_+$/g, '') || 'Route';
      let name = base;
      let i = 0;
      while (usedNames.has(name)) {
        i += 1;
        name = `${base}_${i}`;
      }
      usedNames.add(name);
      return name;
    };

    const traverse = (
      items: ServerMenuItem[],
      parentFullPath: string,
    ): RouteRecordRaw[] => {
      return (items || [])
        .map((item) => {
          if (/^https?:\/\//i.test(item.path)) {
            return null as unknown as RouteRecordRaw;
          }

          // 不可把「/ + Layout + 单子」提升为顶层页面路由：否则会绕过 default-layout，首页全宽且无侧栏
          const fullPath = joinPath(parentFullPath, item.path);
          // 顶层 addRoute 的记录 path 必须以 "/" 开头；嵌套在 Layout 下的子路由保持相对 path
          const normalizedPath =
            parentFullPath === '' && item.path && !item.path.startsWith('/')
              ? `/${item.path}`.replace(/\/+/g, '/')
              : item.path;
          const childParentPrefix = normalizedPath.startsWith('/')
            ? normalizedPath
            : joinPath(parentFullPath, normalizedPath);

          const fallbackName = fullPath.replace(/\//g, '_').replace(/^_/, '') || 'root';

          const isDashboardChild =
            parentFullPath === '/dashboard' &&
            (item.path === 'dashboard' || item.component === 'dashboard/index');

          let routeName: string;
          if (isDashboardChild) {
            if (!usedNames.has('Workplace')) {
              routeName = 'Workplace';
              usedNames.add('Workplace');
            } else {
              routeName = allocateName(item.name, fallbackName);
            }
          } else {
            routeName = allocateName(item.name, fallbackName);
          }

          const rawMeta = item.meta || {};
          const meta: Record<string, unknown> = {
            ...rawMeta,
            requiresAuth: true,
            hideInMenu: item.hidden === true,
          };
          if (!meta.locale && typeof rawMeta.title === 'string') {
            const localeKey = zhTitleToLocaleKey[rawMeta.title];
            if (localeKey) {
              meta.locale = localeKey;
            }
          }

          let redirect: RouteRecordRaw['redirect'] = item.redirect as RouteRecordRaw['redirect'];
          if (redirect === ('noRedirect' as any) || !redirect) {
            redirect = undefined;
          }

          const comp = mapComponent(item.component);
          const route: RouteRecordRaw = {
            path: normalizedPath,
            name: routeName,
            meta,
            redirect,
            component: comp as RouteRecordRaw['component'],
          };

          if (item.children?.length) {
            route.children = traverse(item.children, childParentPrefix).filter(
              (r): r is RouteRecordRaw => !!r,
            );
          }

          if (!route.component) {
            if (route.children?.length) {
              route.component = () => import('@/components/parent-view/index.vue');
            } else {
              return null as unknown as RouteRecordRaw;
            }
          }

          if (
            (item.component === 'Layout' || item.component === 'ParentView') &&
            route.children?.length &&
            !route.redirect
          ) {
            const first = route.children[0];
            if (first?.name) {
              route.redirect = { name: first.name as string };
            }
          }

          return route;
        })
        .filter((r): r is RouteRecordRaw => !!r);
    };

    const routes = traverse(menus, '');
    const { router } = this;
    routes.forEach((r) => {
      if (r.name) {
        router.addRoute(r);
        this.mountedRootNames.push(r.name as string);
      }
    });

    return routes;
  }
}

/**
 * 侧边栏菜单数据：与 transformAndMountServerMenus 产出的路由使用相同的 name/path/meta/children，
 * 去掉 component。若仍用后端 raw 菜单，route.name 与菜单项 name 不一致会导致无法高亮、openKeys 被清空。
 */
export function routesToMenuRecords(routes: RouteRecordRaw[]): RouteRecordRaw[] {
  return (routes || []).map((r) => {
    const children = r.children as RouteRecordRaw[] | undefined;
    const node: RouteRecordRaw = {
      path: r.path,
      name: r.name,
      meta: r.meta ? { ...(r.meta as Record<string, unknown>) } : {},
      redirect: r.redirect,
    } as RouteRecordRaw;
    if (children?.length) {
      node.children = routesToMenuRecords(children);
    }
    return node;
  });
}

const routeManager = new RouteManager();
export default routeManager;
