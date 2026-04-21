import type {LocationQueryRaw, Router} from 'vue-router';
import NProgress from 'nprogress'; // progress bar
import {isLogin} from '@/utils/auth';
import {FALLBACK_ADMIN_PATH} from '@/utils/post-login-navigation';

export default function setupUserLoginInfoGuard(router: Router) {
  router.beforeEach(async (to, from, next) => {
    NProgress.start();
    // 延迟导入 useUserStore 避免循环依赖
    const {useUserStore, useAppStore} = await import('@/store');
    const userStore = useUserStore();
    const appStore = useAppStore();
    
    // 如果已经在登录页面，直接放行，避免循环
    if (to.name === 'login') {
      // 如果已经登录但 token 过期，清除 token
      if (isLogin()) {
        try {
          await userStore.info();
          // 动态菜单需先拉 getRouters，否则无「工作台」等路由
          if (appStore.menuFromServer && !appStore.dynamicRoutesMounted) {
            const ok = await appStore.fetchServerMenuConfig();
            if (!ok) {
              next();
              NProgress.done();
              return;
            }
          }
          if (router.hasRoute('Workplace')) {
            next({name: 'Workplace', replace: true});
          } else if (router.resolve(FALLBACK_ADMIN_PATH).matched.length) {
            next({path: FALLBACK_ADMIN_PATH, replace: true});
          } else {
            next();
          }
        } catch (error) {
          // token 过期，静默清除 token 但停留在登录页
          // 不要调用 logout() API，避免触发拦截器的循环重定向
          userStore.logoutCallBack();
          next();
        }
      } else {
        next();
      }
      NProgress.done();
      return;
    }
    
    if (isLogin()) {
      try {
        await userStore.info();
        next();
      } catch (error) {
        await userStore.logout();
        next({
          name: 'login',
          query: {
            redirect: to.name,
            ...to.query,
          } as LocationQueryRaw,
        });
      }
    } else {
      next({
        name: 'login',
        query: {
          redirect: to.name,
          ...to.query,
        } as LocationQueryRaw,
      });
    }
  });
}
