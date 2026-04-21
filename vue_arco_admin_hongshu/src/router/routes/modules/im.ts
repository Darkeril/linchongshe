import { DEFAULT_LAYOUT } from '../base';
import type { AppRouteRecordRaw } from '../types';

/**
 * IM管理
 * 参考 im-admin-ui：数据管理、用户管理、群聊管理、敏感词管理、消息管理
 */
const IM: AppRouteRecordRaw = {
  path: '/im',
  name: 'Im',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.im',
    title: 'IM管理',
    requiresAuth: true,
    icon: 'IconMessage',
    order: 5,
  },
  children: [
    {
      path: 'index',
      name: 'ImIndex',
      component: () => import('@/views/im/index.vue'),
      meta: {
        locale: 'menu.im.index',
        title: '数据管理',
        requiresAuth: true,
        roles: ['*'],
        icon: 'IconDashboard',
      },
    },
    {
      path: 'user',
      name: 'ImUser',
      component: () => import('@/views/im/user/index.vue'),
      meta: {
        locale: 'menu.im.user',
        title: '用户管理',
        requiresAuth: true,
        roles: ['*'],
        icon: 'IconUser',
      },
    },
    {
      path: 'group',
      name: 'ImGroup',
      component: () => import('@/views/im/group/index.vue'),
      meta: {
        locale: 'menu.im.group',
        title: '群聊管理',
        requiresAuth: true,
        roles: ['*'],
        icon: 'IconMessage',
      },
    },
    {
      path: 'sensitiveWord',
      name: 'ImSensitiveWord',
      component: () => import('@/views/im/sensitiveWord/index.vue'),
      meta: {
        locale: 'menu.im.sensitiveWord',
        title: '敏感词管理',
        requiresAuth: true,
        roles: ['*'],
        icon: 'IconSafe',
      },
    },
    {
      path: 'notice',
      name: 'ImNotice',
      component: () => import('@/views/im/notice/index.vue'),
      meta: {
        locale: 'menu.im.notice',
        title: '通知管理',
        requiresAuth: true,
        roles: ['*'],
        icon: 'IconNotification',
      },
    },
    {
      path: 'message',
      name: 'ImMessage',
      component: () => import('@/views/im/message/index.vue'),
      redirect: '/im/message/private',
      meta: {
        locale: 'menu.im.message',
        title: '消息管理',
        requiresAuth: true,
        roles: ['*'],
        icon: 'IconNotification',
      },
      children: [
        {
          path: 'private',
          name: 'ImMessagePrivate',
          component: () => import('@/views/im/message/private.vue'),
          meta: {
            locale: 'menu.im.message.private',
            title: '私聊消息',
            requiresAuth: true,
            roles: ['*'],
            icon: 'IconUser',
          },
        },
        {
          path: 'group',
          name: 'ImMessageGroup',
          component: () => import('@/views/im/message/group.vue'),
          meta: {
            locale: 'menu.im.message.group',
            title: '群聊消息',
            requiresAuth: true,
            roles: ['*'],
            icon: 'IconMessage',
          },
        },
      ],
    },
  ],
};

export default IM;
