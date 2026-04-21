import { defineStore } from 'pinia';
import { Notification } from '@arco-design/web-vue';
import type { RouteRecordNormalized } from 'vue-router';
import defaultSettings from '@/config/settings.json';
import { getMenuList } from '@/api/user';
import cache from '@/plugins/cache';
import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';
import 'dayjs/locale/en';
import i18n, { LOCALE_OPTIONS } from '@/locale';
import routeManager, { routesToMenuRecords } from '@/utils/route-manager';
import isMenuFromServer from '@/utils/menu-from-server';
import { AppState } from './types';

function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => {
    setTimeout(resolve, ms);
  });
}

/** 解析 getRouters 返回体（兼容 AjaxResult / 纯数组 / 网关包装） */
function extractRoutersList(body: unknown): unknown[] {
  if (Array.isArray(body)) {
    return body;
  }
  if (body && typeof body === 'object') {
    const b = body as Record<string, unknown>;
    if (Array.isArray(b.data)) {
      return b.data as unknown[];
    }
    if (Array.isArray(b.rows)) {
      return b.rows as unknown[];
    }
    const inner = b.data;
    if (inner && typeof inner === 'object') {
      const d = inner as Record<string, unknown>;
      if (Array.isArray(d.data)) {
        return d.data as unknown[];
      }
    }
  }
  return [];
}

const VALID_LOCALES = LOCALE_OPTIONS.map((o) => o.value);

const getInitialState = (): AppState => {
  const savedSettings = cache.local.getJSON('app-settings');
  const stored = localStorage.getItem('arco-locale');
  const locale = stored && VALID_LOCALES.includes(stored) ? stored : 'zh-CN';
  return {
    ...(defaultSettings as unknown as AppState),
    ...(savedSettings || {}),
    dynamicRoutesMounted: false,
    menuFromServer: isMenuFromServer(),
    locale,
  };
};

const useAppStore = defineStore('app', {
  state: (): AppState => getInitialState(),

  getters: {
    appCurrentSetting(state: AppState): AppState {
      return { ...state };
    },
    appDevice(state: AppState) {
      return state.device;
    },
    appAsyncMenus(state: AppState): RouteRecordNormalized[] {
      return state.serverMenu as unknown as RouteRecordNormalized[];
    },
  },

  actions: {
    // Update app settings
    updateSettings(partial: Partial<AppState>) {
      // @ts-ignore-next-line
      this.$patch(partial);
    },

    // Change theme color
    toggleTheme(dark: boolean) {
      if (dark) {
        this.theme = 'dark';
        document.body.setAttribute('arco-theme', 'dark');
      } else {
        this.theme = 'light';
        document.body.removeAttribute('arco-theme');
      }
    },
    toggleDevice(device: string) {
      this.device = device;
    },
    toggleMenu(value: boolean) {
      this.hideMenu = value;
    },
    switchLocale(lang: string) {
      this.locale = lang;
      localStorage.setItem('arco-locale', lang);
      (i18n.global.locale as unknown as { value: string }).value = lang;
      dayjs.locale(lang === 'zh-CN' ? 'zh-cn' : 'en');
    },
    /**
     * 拉取并挂载动态路由。
     * @returns 是否成功（空菜单也算成功）
     */
    async fetchServerMenuConfig(): Promise<boolean> {
      const { t } = i18n.global;
      const maxAttempts = 3;
      const retryDelaysMs = [0, 450, 900];

      Notification.info({
        id: 'menuLoading',
        content: t('settings.menuLoadingNotice'),
        closable: true,
      });

      const runAttempt = async (attemptIndex: number): Promise<boolean> => {
        if (retryDelaysMs[attemptIndex] > 0) {
          Notification.info({
            id: 'menuLoading',
            content: t('settings.menuRetryNotice', { attempt: attemptIndex + 1, max: maxAttempts }),
            closable: true,
          });
          await sleep(retryDelaysMs[attemptIndex]);
        }
        try {
          const body = await getMenuList();
          const raw = extractRoutersList(body);
          if (!raw.length) {
            Notification.warning({
              id: 'menuEmpty',
              content: t('settings.menuEmptyWarning'),
              closable: true,
              duration: 10000,
            });
          }
          const mounted = routeManager.transformAndMountServerMenus(raw as any);
          if (raw.length > 0 && !mounted.length) {
            Notification.warning({
              id: 'menuNoRoutes',
              content: t('settings.menuNoRoutesWarning'),
              closable: true,
              duration: 12000,
            });
          }
          this.serverMenu = JSON.parse(
            JSON.stringify(routesToMenuRecords(mounted)),
          ) as unknown as RouteRecordNormalized[];
          this.dynamicRoutesMounted = true;
          Notification.remove('menuLoading');
          return true;
        } catch (error: unknown) {
          this.dynamicRoutesMounted = false;
          this.serverMenu = [];
          if (attemptIndex + 1 >= maxAttempts) {
            Notification.remove('menuLoading');
            const ax = error as {
              message?: string;
              response?: { data?: { msg?: string } };
            };
            const msg = ax?.response?.data?.msg || ax?.message || '';
            Notification.error({
              id: 'menuLoadError',
              content: msg
                ? t('settings.menuLoadFailedWithMsg', { max: maxAttempts, msg })
                : t('settings.menuLoadFailed', { max: maxAttempts }),
              closable: true,
              duration: 8000,
            });
            return false;
          }
          return runAttempt(attemptIndex + 1);
        }
      };

      return runAttempt(0);
    },
    clearServerMenu() {
      routeManager.clearMountedRoutes();
      this.serverMenu = [];
      this.dynamicRoutesMounted = false;
    },
  },
});

export default useAppStore;
