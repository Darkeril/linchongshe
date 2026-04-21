import {createRouter, createWebHistory} from 'vue-router';
import NProgress from 'nprogress'; // progress bar
import 'nprogress/nprogress.css';

import routeManager from '@/utils/route-manager';
import isMenuFromServer from '@/utils/menu-from-server';
import {appDynamicRoutes, appRoutes} from './routes';
import {NOT_FOUND_ROUTE, REDIRECT_MAIN, EXCEPTION_403, EXCEPTION_404, EXCEPTION_500} from './routes/base';
import createRouteGuard from './guard';

NProgress.configure({showSpinner: false}); // NProgress Configuration

// 根据配置决定是否使用静态路由
const getRoutes = () => {
  const baseRoutes = [
    {
      path: '/',
      redirect: 'login',
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/login/index.vue'),
      meta: {
        requiresAuth: false,
      },
    },
    // 重定向和未知页面路由
    REDIRECT_MAIN,
    NOT_FOUND_ROUTE,
    // 异常页面的路由
    EXCEPTION_403,
    EXCEPTION_404,
    EXCEPTION_500,
  ];

  if (isMenuFromServer()) {
    // 业务菜单由 getRouters + addRoute 注入，仅保留固定页与隐藏功能路由
    return [...baseRoutes, ...(appDynamicRoutes as any)];
  }
  // 使用静态菜单时，注册所有路由
  return [
    ...baseRoutes,
    ...(appRoutes as any),
    ...(appDynamicRoutes as any),
  ];
};

const router = createRouter({
  history: createWebHistory('/arco-admin/'),
  routes: getRoutes(),
  scrollBehavior() {
    return {top: 0};
  },
});

createRouteGuard(router);

// 设置路由管理器，避免循环依赖
routeManager.setRouter(router);

export default router;
