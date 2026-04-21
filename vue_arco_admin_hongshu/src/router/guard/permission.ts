import type {Router} from 'vue-router';
import NProgress from 'nprogress';
import usePermission from '@/hooks/permission';
import {FALLBACK_ADMIN_PATH} from '@/utils/post-login-navigation';
import {appDynamicRoutes} from '../routes';
import {NOT_FOUND} from '../constants';

function collectRouteNames(routes: any[]): string[] {
  const names: string[] = [];
  routes.forEach((r) => {
    if (r?.name) names.push(r.name);
    if (r?.children?.length) names.push(...collectRouteNames(r.children));
  });
  return names;
}

/** 不参与动态菜单但仍已注册的路由名（如分配角色、代码生成编辑） */
const STATIC_EXTRA_NAMES = new Set(collectRouteNames(appDynamicRoutes as any[]));

export default function setupPermissionGuard(router: Router) {
  router.beforeEach(async (to, from, next) => {
    const {useAppStore, useUserStore} = await import('@/store');
    const appStore = useAppStore();
    const userStore = useUserStore();
    const Permission = usePermission();

    if (to.meta?.requiresAuth === false) {
      next();
      NProgress.done();
      return;
    }

    // 以 Pinia 为准，避免「界面已打开关但 localStorage 未点确定保存」时与 isMenuFromServer() 不一致
    if (!appStore.menuFromServer) {
      const permissionsAllow = Permission.accessRouter(to);
      const {appRoutes} = await import('../routes');
      if (permissionsAllow) {
        next();
      } else {
        const destination =
            Permission.findFirstPermissionRoute(appRoutes, userStore.roles) ||
            NOT_FOUND;
        next(destination);
      }
      NProgress.done();
      return;
    }

    // ——— 服务端菜单模式 ———
    const permissionsAllow = Permission.accessRouter(to);

    if (!appStore.dynamicRoutesMounted) {
      const ok = await appStore.fetchServerMenuConfig();
      if (!ok) {
        // 菜单接口偶发失败时勿落到「假 404」，回登录页可再次拉菜单（与 pushAfterLogin 一致）
        const redirectPath =
          to.path && to.path !== '/' ? to.fullPath : undefined;
        next({
          name: 'login',
          query: redirectPath ? { redirect: redirectPath } : {},
          replace: true,
        });
        NProgress.done();
        return;
      }
      // 刷新时 URL 往往已被通配路由匹配成 name=notFound，展开 ...to 会沿用该 name，永远进 404
      // 必须按 path 重新解析，才能命中刚 addRoute 的业务路由
      next({
        path: to.path,
        query: to.query,
        hash: to.hash,
        replace: true,
      });
      NProgress.done();
      return;
    }

    const allowByName =
      STATIC_EXTRA_NAMES.has(to.name as string) ||
      to.name === 'notFound' ||
      to.name === 'redirectWrapper';

    if (allowByName && permissionsAllow) {
      next();
      NProgress.done();
      return;
    }

    if (to.matched.length === 0) {
      // 书签或从静态切换后残留的 path 在后端菜单中不存在
      if (router.hasRoute('Workplace')) {
        next({ name: 'Workplace', replace: true });
      } else if (router.resolve(FALLBACK_ADMIN_PATH).matched.length) {
        next({ path: FALLBACK_ADMIN_PATH, replace: true });
      } else {
        next(NOT_FOUND);
      }
      NProgress.done();
      return;
    }

    if (permissionsAllow) {
      next();
    } else {
      next(NOT_FOUND);
    }
    NProgress.done();
  });
}
