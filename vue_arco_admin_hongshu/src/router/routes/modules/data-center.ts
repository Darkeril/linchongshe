import { DEFAULT_LAYOUT } from '../base';
import type { AppRouteRecordRaw } from '../types';

/**
 * 数据中心
 */
const DataCenter: AppRouteRecordRaw = {
  path: '/data-center',
  name: 'DataCenter',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.dataCenter',
    title: '数据中心',
    requiresAuth: true,
    icon: 'IconStorage',
    order: 1,
  },
  children: [
    {
      path: 'analytics',
      name: 'DataCenterAnalytics',
      component: () => import('@/views/data-center/analytics/index.vue'),
      meta: {
        locale: 'menu.dataCenter.analytics',
        title: '数据分析',
        requiresAuth: true,
        roles: ['*'],
        icon: 'IconMindMapping',
      },
    },
    {
      path: 'fan-profile',
      name: 'DataCenterFanProfile',
      component: () => import('@/views/data-center/fan-profile/index.vue'),
      meta: {
        locale: 'menu.dataCenter.fanProfile',
        title: '粉丝画像',
        requiresAuth: true,
        roles: ['*'],
        icon: 'IconUserGroup',
      },
    },
    {
      path: 'popular-content',
      name: 'DataCenterPopularContent',
      component: () => import('@/views/data-center/popular-content/index.vue'),
      meta: {
        locale: 'menu.dataCenter.popularContent',
        title: '内容分析',
        requiresAuth: true,
        roles: ['*'],
        icon: 'IconBulb',
      },
    },
    {
      path: 'popular-content/note-detail/:id',
      name: 'DataCenterNoteDetail',
      component: () => import('@/views/data-center/note-detail/index.vue'),
      meta: {
        title: '笔记数据详情',
        requiresAuth: true,
        roles: ['*'],
        hideInMenu: true,
      },
    },
  ],
};

export default DataCenter;
