<script lang="tsx">
import {compile, computed, defineComponent, h, ref, watch} from 'vue';
import {useI18n} from 'vue-i18n';
import type {RouteMeta} from 'vue-router';
import {RouteRecordRaw, useRoute, useRouter} from 'vue-router';
import {useAppStore} from '@/store';
import {listenerRouteChange} from '@/utils/route-listener';
import {openWindow, regexUrl} from '@/utils';
import i18n from '@/locale';
import useMenuTree from './use-menu-tree';

/** 从「根到当前路由」的 name 链中，只保留真正有子级的节点（子菜单），不把叶子放进 open-keys */
function submenuOpenKeysFromPath(tree: RouteRecordRaw[], pathNames: string[]): string[] {
  const { keys } = pathNames.reduce(
    (acc, name) => {
      const node = acc.level.find((n) => n.name === name);
      if (!node) {
        return {...acc, level: [] as RouteRecordRaw[]};
      }
      if (node.children && node.children.length > 0) {
        return {
          keys: [...acc.keys, node.name as string],
          level: node.children,
        };
      }
      return {...acc, level: [] as RouteRecordRaw[]};
    },
    { keys: [] as string[], level: tree },
  );
  return keys;
}

export default defineComponent({
  emit: ['collapse'],
  setup() {
    const {t, te} = useI18n();
    const appStore = useAppStore();
    const router = useRouter();
    const route = useRoute();
    const {menuTree} = useMenuTree();
    const collapsed = computed({
      get() {
        if (appStore.device === 'desktop') return appStore.menuCollapse;
        return false;
      },
      set(value: boolean) {
        appStore.updateSettings({menuCollapse: value});
      },
    });

    const openKeys = ref<string[]>([]);
    const selectedKey = ref<string[]>([]);

    // 监听 openKeys 变化，确保只有一个主菜单展开（静态路由用）。服务端菜单允许多个顶层分组同时展开
    watch(openKeys, (newKeys, oldKeys) => {
      if (appStore.menuFromServer) {
        return;
      }
      // 获取所有主菜单的名称
      const mainMenuNames = menuTree.value.map((menu: RouteRecordRaw) => menu.name);
      
      // 过滤出主菜单的展开状态
      const mainMenuOpenKeys = newKeys.filter(key => mainMenuNames.includes(key));
      
      if (mainMenuOpenKeys.length > 1) {
        // 如果同时展开多个主菜单，只保留最后一个
        const lastMainMenuKey = mainMenuOpenKeys[mainMenuOpenKeys.length - 1];
        // 保留所有子菜单的展开状态，但只保留一个主菜单
        const subMenuOpenKeys = newKeys.filter(key => !mainMenuNames.includes(key));
        openKeys.value = [lastMainMenuKey, ...subMenuOpenKeys];
        
        // 如果点击的是主菜单标题，更新选中状态
        if (oldKeys && !oldKeys.includes(lastMainMenuKey)) {
          selectedKey.value = [lastMainMenuKey];
        }
      }

      // 在当前主菜单下，强制只保留一个二级菜单展开
      const currentMain = (openKeys.value.find(k => mainMenuNames.includes(k)) || '') as string;
      if (currentMain) {
        const mainNode = menuTree.value.find((m: any) => m.name === currentMain);
        const secondLevelNames = (mainNode?.children || []).map((c: any) => c.name);
        const openedSeconds = openKeys.value.filter(k => secondLevelNames.includes(k));
        if (openedSeconds.length > 1) {
          // 取这次新增的那个作为保留的二级；否则取最后一个
          const added = newKeys.filter(k => !oldKeys.includes(k));
          const addedSecond = added.find(k => secondLevelNames.includes(k));
          const keepSecond = addedSecond || openedSeconds[openedSeconds.length - 1];
          openKeys.value = openKeys.value.filter(k => !secondLevelNames.includes(k) || k === keepSecond);
          selectedKey.value = [keepSecond];
        }
      }
    }, { immediate: false });

    const goto = (item: RouteRecordRaw) => {
      // Open external link
      if (regexUrl.test(item.path)) {
        openWindow(item.path);
        selectedKey.value = [item.name as string];
        return;
      }
      // Eliminate external link side effects
      const {hideInMenu, activeMenu} = item.meta as RouteMeta;
      if (route.name === item.name && !hideInMenu && !activeMenu) {
        selectedKey.value = [item.name as string];
        return;
      }
      // Trigger router change
      router.push({
        name: item.name,
      });
    };
    const findMenuOpenKeys = (name: string) => {
      const result: string[] = [];
      let isFind = false;
      const backtrack = (
          item: RouteRecordRaw,
          keys: string[],
          target: string
      ) => {
        // 将当前节点加入展开路径
        const currentKeys = [...keys, item.name as string];
        if (item.name === target) {
          isFind = true;
          result.push(...currentKeys);
          return;
        }
        if (item.children?.length) {
          item.children.forEach((el) => {
            backtrack(el, currentKeys, target);
          });
        }
      };
      menuTree.value.forEach((el: RouteRecordRaw) => {
        if (isFind) return; // Performance optimization
        backtrack(el, [], name);
      });
      return result;
    };
    listenerRouteChange((newRoute) => {
      const {requiresAuth, activeMenu, hideInMenu} = newRoute.meta;
      if (requiresAuth && (!hideInMenu || activeMenu)) {
        const targetName = (activeMenu || newRoute.name) as string;
        const menuOpenKeys = findMenuOpenKeys(targetName);

        if (appStore.menuFromServer) {
          const branchKeys = submenuOpenKeysFromPath(
            menuTree.value,
            menuOpenKeys,
          );
          // 仅展开「当前路由」所在分支，勿固定展开第一项（否则仪表盘会一直展开）
          openKeys.value = [...new Set(branchKeys)];
          const leaf =
            menuOpenKeys.length > 0
              ? menuOpenKeys[menuOpenKeys.length - 1]
              : targetName;
          selectedKey.value = [(activeMenu || leaf) as string];
          return;
        }

        // 仅保留祖先子菜单（不包含叶子本身），并确保同一级只展开当前二级
        const ancestors = menuOpenKeys.length >= 3 ? menuOpenKeys.slice(0, -1) : menuOpenKeys;
        const mainKey = ancestors[0];
        const secondKey = ancestors[1];
        // 目标：除当前一级(mainKey)与当前二级(secondKey)外，其他二级全部折叠
        if (mainKey && secondKey) {
          openKeys.value = [mainKey, secondKey];
          selectedKey.value = [secondKey];
        } else if (mainKey) {
          // 只有到第二级（无第三级）时，仅展开当前二级（即 ancestors 已是 [mainKey, secondKey] 或 [mainKey]）
          openKeys.value = ancestors;
          selectedKey.value = [ancestors[ancestors.length - 1]];
        } else {
          openKeys.value = [];
        }

        selectedKey.value = [
          activeMenu || menuOpenKeys[menuOpenKeys.length - 1],
        ];
      }
    }, true);
    const setCollapse = (val: boolean) => {
      if (appStore.device === 'desktop')
        appStore.updateSettings({menuCollapse: val});
    };

    const renderSubMenu = () => {
      function resolveTitle(meta?: RouteMeta): string {
        const localeKey = meta?.locale as string | undefined;
        if (localeKey) {
          // Direct flat-key lookup: bypasses vue-i18n path traversal for dotted flat keys
          // e.g. 'menu.web.note' → messages[locale].menu['web.note'] = 'Notes'
          if (localeKey.startsWith('menu.')) {
            const subKey = localeKey.slice(5);
            const msgs = i18n.global.messages.value[appStore.locale] as Record<string, unknown>;
            const menuMsgs = msgs?.menu as Record<string, string> | undefined;
            if (menuMsgs && typeof menuMsgs[subKey] === 'string') {
              return menuMsgs[subKey];
            }
          }
          if (te(localeKey)) return t(localeKey);
        }
        return (meta?.title as string) || '';
      }

      function travel(_route: RouteRecordRaw[], nodes = []) {
        if (_route) {
          _route.forEach((element) => {
            const icon = element?.meta?.icon
                ? () => h(compile(element?.meta?.icon?.includes('Icon') ? `<${element?.meta?.icon}/>` : '<icon-menu/>'))
                : null;
            const titleText = resolveTitle(element?.meta);
            const node =
                element?.children && element?.children.length !== 0 ? (
                    <a-sub-menu
                        key={element?.name}
                        v-slots={{
                          icon,
                          title: () => titleText,
                        }}
                    >
                      {travel(element?.children)}
                    </a-sub-menu>
                ) : (
                    <a-menu-item
                        key={element?.name}
                        v-slots={{icon}}
                        onClick={() => goto(element)}
                    >
                      {titleText}
                    </a-menu-item>
                );
            nodes.push(node as never);
          });
        }
        return nodes;
      }

      return travel(menuTree.value);
    };

    return () => (
        <a-menu
            v-model:collapsed={collapsed.value}
            v-model:open-keys={openKeys.value}
            show-collapse-button={appStore.device !== 'mobile'}
            auto-open={false}
            selected-keys={selectedKey.value}
            auto-open-selected={true}
            level-indent={34}
            style="height: 100%"
            onCollapse={setCollapse}
        >
          {renderSubMenu()}
        </a-menu>
    );
  },
});
</script>

<style lang="less" scoped>
:deep(.arco-menu-inner) {
  .arco-menu-inline-header {
    display: flex;
    align-items: center;
  }

  .arco-icon {
    &:not(.arco-icon-down) {
      font-size: 18px;
    }
  }
}
</style>
