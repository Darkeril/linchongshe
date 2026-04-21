import type { Component } from 'vue';
import { onActivated, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { useI18n } from 'vue-i18n';
import i18n from '@/locale';
import { useAppStore } from '@/store';
import {
  IconApps,
  IconDesktop,
  IconFile,
  IconList,
  IconMessage,
  IconSettings,
  IconUser,
} from '@arco-design/web-vue/es/icon';
import {
  getFrequentVisits,
  getRecentVisits,
  pathToIconKey,
  type VisitEntry,
  type VisitIconKey,
} from '@/utils/route-visit-history';

const VISIT_ICON_MAP: Record<VisitIconKey, Component> = {
  user: IconUser,
  file: IconFile,
  apps: IconApps,
  settings: IconSettings,
  list: IconList,
  message: IconMessage,
  dashboard: IconDesktop,
  default: IconApps,
};

export interface WorkbenchVisitLink {
  text: string;
  path: string;
  icon: Component;
}

function resolveMenuLocaleKey(localeKey: string, locale: string): string | undefined {
  if (localeKey.startsWith('menu.')) {
    const subKey = localeKey.slice(5);
    const msgs = i18n.global.messages.value[locale] as Record<string, unknown>;
    const menuMsgs = msgs?.menu as Record<string, string> | undefined;
    if (menuMsgs && typeof menuMsgs[subKey] === 'string') {
      return menuMsgs[subKey];
    }
  }
  return undefined;
}

function useVisitLinks(loader: () => VisitEntry[]) {
  const links = ref<WorkbenchVisitLink[]>([]);
  const route = useRoute();
  const { t, te, locale } = useI18n();
  const appStore = useAppStore();

  function toDisplay(items: VisitEntry[]): WorkbenchVisitLink[] {
    return items.map((e) => {
      let text = e.title;
      if (e.localeKey) {
        const direct = resolveMenuLocaleKey(e.localeKey, appStore.locale);
        if (direct) {
          text = direct;
        } else if (te(e.localeKey)) {
          text = t(e.localeKey);
        }
      }
      return { text, path: e.path, icon: VISIT_ICON_MAP[pathToIconKey(e.path)] };
    });
  }

  function refresh() {
    links.value = toDisplay(loader());
  }

  onMounted(refresh);
  onActivated(refresh);
  watch(() => route.fullPath, refresh);
  watch(() => locale.value, refresh);

  return { links, refresh };
}

/** 工作台右侧：最近访问（按时间） */
export function useRecentVisitLinks(limit = 9) {
  return useVisitLinks(() => getRecentVisits(limit));
}

/** 工作台右侧：快捷操作（按访问次数） */
export function useFrequentVisitLinks(limit = 6) {
  return useVisitLinks(() => getFrequentVisits(limit));
}

export type { VisitEntry };
