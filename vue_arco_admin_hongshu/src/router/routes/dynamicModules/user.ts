import { DEFAULT_LAYOUT } from '../base';
import { AppRouteRecordRaw } from '../types';

/**
 * 个人中心 / 用户设置：服务端菜单模式下不加载 appRoutes，须放在 dynamicModules 与 gen-edit 等一致，
 * 否则顶栏「用户设置」router.push({ name: 'Setting' }) 会报 No match。
 */
const USER: AppRouteRecordRaw = {
  path: '/user',
  name: 'user',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.user',
    title: '个人中心',
    icon: 'IconStar',
    requiresAuth: true,
    order: 9,
    hideInMenu: true,
  },
  children: [
    {
      path: 'setting',
      name: 'Setting',
      component: () => import('@/views/user/setting/index.vue'),
      meta: {
        locale: 'menu.user.setting',
        title: '用户设置',
        requiresAuth: true,
        roles: ['*'],
        icon: 'IconTool',
      },
    },
  ],
};

export default USER;
