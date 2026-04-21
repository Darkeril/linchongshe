import {computed} from 'vue';
import {RouteRecordNormalized, RouteRecordRaw} from 'vue-router';
import usePermission from '@/hooks/permission';
import {useAppStore} from '@/store';
import appClientMenus from '@/router/app-menus';

export default function useMenuTree() {
  const permission = usePermission();
  const appStore = useAppStore();
  const appRoute = computed(() => {
    if (appStore.menuFromServer) {
      if (appStore.appAsyncMenus?.length) {
        return appStore.appAsyncMenus as RouteRecordRaw[];
      }
      return [];
    }
    return appClientMenus;
  });
  const menuTree = computed(() => {
    const copyRouter = JSON.parse(JSON.stringify(appRoute.value)).filter(Boolean);
    copyRouter.sort((a: RouteRecordNormalized, b: RouteRecordNormalized) => {
      const aOrder = (a as any)?.meta?.order ?? 0;
      const bOrder = (b as any)?.meta?.order ?? 0;
      return aOrder - bOrder;
    });

    function travel(_routes: RouteRecordRaw[], layer: number) {
      if (!_routes) return null;

      const collector: any = _routes.map((element) => {
        // no access
        if (!permission.accessRouter(element)) {
          return null;
        }

        // 检查 hidden 字段
        if ((element as any).hidden === true) {
          return null;
        }

        // 检查 hideInMenu 字段（父路由）
        if (element.meta?.hideInMenu === true) {
          return null;
        }

        // leaf node
        if (element.meta?.hideChildrenInMenu || !element.children) {
          element.children = [];
          return element;
        }

        // route filter hideInMenu true and hidden true
        element.children = element.children.filter(
            (x) => x.meta?.hideInMenu !== true && (x as any).hidden !== true
        );

        // Associated child node
        const subItem = travel(element.children, layer + 1);

        if (subItem.length) {
          element.children = subItem;
          return element;
        }
        // the else logic
        if (layer > 1) {
          element.children = subItem;
          return element;
        }

        if (element.meta?.hideInMenu === false) {
          return element;
        }

        return null;
      });
      return collector.filter(Boolean);
    }

    return travel(copyRouter, 0);
  });

  return {
    menuTree,
  };
}
