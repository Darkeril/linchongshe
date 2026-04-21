import type {RouteLocationNormalized} from 'vue-router';
import {defineStore} from 'pinia';
import {DEFAULT_ROUTE, DEFAULT_ROUTE_NAME, REDIRECT_ROUTE_NAME,} from '@/router/constants';
import {isString} from '@/utils/is';
import {TabBarState, TagProps} from './types';

const formatTag = (route: RouteLocationNormalized): TagProps => {
  const {name, meta, fullPath, query, params} = route;
  return {
      title: meta.title || '',
      name: String(name),
      fullPath,
      query,
      params,
      ignoreCache: meta.ignoreCache,
      locale: (meta.locale as string) || '',
  };
};

const BAN_LIST = [REDIRECT_ROUTE_NAME];

const useAppStore = defineStore('tabBar', {
  state: (): TabBarState => ({
    cacheTabList: new Set([DEFAULT_ROUTE_NAME]),
    tagList: [DEFAULT_ROUTE],
  }),

  getters: {
    getTabList(): TagProps[] {
      return this.tagList;
    },
    getCacheList(): string[] {
      return Array.from(this.cacheTabList);
    },
  },

  actions: {
    updateTabList(route: RouteLocationNormalized) {
      if (BAN_LIST.includes(route.name as string)) return;
      const tag = formatTag(route);
      // 预置首页标签的 fullPath 多为 /dashboard/workplace，服务端菜单常为 /dashboard/dashboard 等，
      // 仅 fullPath 不同会再 push 一条，出现两个「工作台」。同路由 name 只保留一条并同步当前 URL。
      if (route.name) {
        const sameNameIdx = this.tagList.findIndex(
          (t) => t.name === String(route.name),
        );
        if (sameNameIdx >= 0) {
          this.tagList.splice(sameNameIdx, 1, tag);
          if (!route.meta.ignoreCache) {
            this.cacheTabList.add(route.name as string);
          }
          return;
        }
      }
      this.tagList.push(tag);
      if (!route.meta.ignoreCache) {
        this.cacheTabList.add(route.name as string);
      }
    },
    deleteTag(idx: number, tag: TagProps) {
      this.tagList.splice(idx, 1);
      this.cacheTabList.delete(tag.name);
    },
    addCache(name: string) {
      if (isString(name) && name !== '') this.cacheTabList.add(name);
    },
    deleteCache(tag: TagProps) {
      this.cacheTabList.delete(tag.name);
    },
    freshTabList(tags: TagProps[]) {
      this.tagList = tags;
      this.cacheTabList.clear();
      // 要先判断ignoreCache
      this.tagList
          .filter((el) => !el.ignoreCache)
          .map((el) => el.name)
          .forEach((x) => this.cacheTabList.add(x));
    },
    resetTabList() {
      this.tagList = [DEFAULT_ROUTE];
      this.cacheTabList.clear();
      this.cacheTabList.add(DEFAULT_ROUTE_NAME);
    },
  },
});

export default useAppStore;
