import type {RouteRecordNormalized} from 'vue-router';

export interface AppState {
  theme: string;
  colorWeak: boolean;
  navbar: boolean;
  menu: boolean;
  hideMenu: boolean;
  menuCollapse: boolean;
  footer: boolean;
  themeColor: string;
  menuWidth: number;
  globalSettings: boolean;
  device: string;
  tabBar: boolean;
  menuFromServer: boolean;
  serverMenu: RouteRecordNormalized[];
  /** 是否已根据 getRouters 执行过 addRoute（登出时需重置） */
  dynamicRoutesMounted: boolean;
  locale: string;

  [key: string]: unknown;
}
