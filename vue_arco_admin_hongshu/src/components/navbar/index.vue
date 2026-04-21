<template>
  <div class="navbar">
    <div class="left-side">
      <a-space>
        <img
          v-if="logoUrl"
          alt="logo"
          :src="logoUrl"
          @error="handleLogoError"
        />
        <img v-else alt="logo" src="@/assets/logo/logo.png" />
        <a-typography-title
          :style="{ margin: 0, fontSize: '18px' }"
          :heading="5"
        >
          {{ $t('navbar.title') }}
        </a-typography-title>
        <icon-menu-fold
          v-if="appStore.device === 'mobile'"
          style="font-size: 22px; cursor: pointer"
          @click="toggleDrawerMenu"
        />
      </a-space>
    </div>
    <ul class="right-side">
      <li
        class="quick-links-cluster"
        :class="{ 'is-expanded': quickLinksExpanded }"
      >
        <a-tooltip
          :content="
            quickLinksExpanded
              ? $t('navbar.collapseLinks')
              : $t('navbar.expandLinks')
          "
        >
          <a-button
            class="nav-btn quick-links-trigger"
            type="outline"
            :shape="'circle'"
            @click="quickLinksExpanded = !quickLinksExpanded"
          >
            <template #icon>
              <icon-link />
            </template>
          </a-button>
        </a-tooltip>
        <transition name="quick-links-fade">
          <div v-show="quickLinksExpanded" class="quick-links-items">
            <a-tooltip :content="$t('navbar.wechat')">
              <a
                href="https://www.yuque.com/xiaomage-ippj7/kaoqwn/rqslw69egcbgx7v7?singleDoc#"
                target="_blank"
                rel="noopener noreferrer"
              >
                <a-button class="nav-btn" type="outline" :shape="'circle'">
                  <template #icon>
                    <svg
                      t="1774720176159"
                      class="icon"
                      viewBox="0 0 1024 1024"
                      version="1.1"
                      xmlns="http://www.w3.org/2000/svg"
                      p-id="11783"
                      width="16"
                      height="16"
                    >
                      <path
                        d="M337.387283 341.82659c-17.757225 0-35.514451 11.83815-35.514451 29.595375s17.757225 29.595376 35.514451 29.595376 29.595376-11.83815 29.595376-29.595376c0-18.49711-11.83815-29.595376-29.595376-29.595375zM577.849711 513.479769c-11.83815 0-22.936416 12.578035-22.936416 23.6763 0 12.578035 11.83815 23.676301 22.936416 23.676301 17.757225 0 29.595376-11.83815 29.595376-23.676301s-11.83815-23.676301-29.595376-23.6763zM501.641618 401.017341c17.757225 0 29.595376-12.578035 29.595376-29.595376 0-17.757225-11.83815-29.595376-29.595376-29.595375s-35.514451 11.83815-35.51445 29.595375 17.757225 29.595376 35.51445 29.595376zM706.589595 513.479769c-11.83815 0-22.936416 12.578035-22.936416 23.6763 0 12.578035 11.83815 23.676301 22.936416 23.676301 17.757225 0 29.595376-11.83815 29.595376-23.676301s-11.83815-23.676301-29.595376-23.6763z"
                        fill="#28C445"
                        p-id="11784"
                      ></path>
                      <path
                        d="M510.520231 2.959538C228.624277 2.959538 0 231.583815 0 513.479769s228.624277 510.520231 510.520231 510.520231 510.520231-228.624277 510.520231-510.520231-228.624277-510.520231-510.520231-510.520231zM413.595376 644.439306c-29.595376 0-53.271676-5.919075-81.387284-12.578034l-81.387283 41.433526 22.936416-71.768786c-58.450867-41.433526-93.965318-95.445087-93.965317-159.815029 0-113.202312 105.803468-201.988439 233.803468-201.98844 114.682081 0 216.046243 71.028902 236.023121 166.473989-7.398844-0.739884-14.797688-1.479769-22.196532-1.479769-110.982659 1.479769-198.289017 85.086705-198.289017 188.67052 0 17.017341 2.959538 33.294798 7.398844 49.572255-7.398844 0.739884-15.537572 1.479769-22.936416 1.479768z m346.265896 82.867052l17.757225 59.190752-63.630058-35.514451c-22.936416 5.919075-46.612717 11.83815-70.289017 11.83815-111.722543 0-199.768786-76.947977-199.768786-172.393063-0.739884-94.705202 87.306358-171.653179 198.289017-171.65318 105.803468 0 199.028902 77.687861 199.028902 172.393064 0 53.271676-34.774566 100.624277-81.387283 136.138728z"
                        fill="#28C445"
                        p-id="11785"
                      ></path>
                    </svg>
                  </template>
                </a-button>
              </a>
            </a-tooltip>
            <a-tooltip :content="$t('navbar.giteeSource')">
              <a
                href="https://gitee.com/Maverick_Ma/hongshu"
                target="_blank"
                rel="noopener noreferrer"
              >
                <a-button class="nav-btn" type="outline" :shape="'circle'">
                  <template #icon>
                    <svg
                      t="1774719703168"
                      class="icon"
                      viewBox="0 0 1024 1024"
                      version="1.1"
                      xmlns="http://www.w3.org/2000/svg"
                      p-id="9411"
                      width="16"
                      height="16"
                    >
                      <path
                        d="M512 1024C229.222 1024 0 794.778 0 512S229.222 0 512 0s512 229.222 512 512-229.222 512-512 512z m259.149-568.883h-290.74a25.293 25.293 0 0 0-25.292 25.293l-0.026 63.206c0 13.952 11.315 25.293 25.267 25.293h177.024c13.978 0 25.293 11.315 25.293 25.267v12.646a75.853 75.853 0 0 1-75.853 75.853h-240.23a25.293 25.293 0 0 1-25.267-25.293V417.203a75.853 75.853 0 0 1 75.827-75.853h353.946a25.293 25.293 0 0 0 25.267-25.292l0.077-63.207a25.293 25.293 0 0 0-25.268-25.293H417.152a189.62 189.62 0 0 0-189.62 189.645V771.15c0 13.977 11.316 25.293 25.294 25.293h372.94a170.65 170.65 0 0 0 170.65-170.65V480.384a25.293 25.293 0 0 0-25.293-25.267z"
                        fill="#C71D23"
                        p-id="9412"
                      ></path>
                    </svg>
                  </template>
                </a-button>
              </a>
            </a-tooltip>
            <a-tooltip :content="$t('navbar.githubSource')">
              <a
                href="https://github.com/Ma-YongJian/HongShu"
                target="_blank"
                rel="noopener noreferrer"
              >
                <a-button class="nav-btn" type="outline" :shape="'circle'">
                  <template #icon>
                    <icon-github />
                  </template>
                </a-button>
              </a>
            </a-tooltip>
            <a-tooltip :content="$t('navbar.docLink')">
              <a
                href="https://www.yuque.com/xiaomage-ippj7/kaoqwn/zdgtgrco82vftbot?singleDoc# 《项目文档-开源版（Lite）》"
                target="_blank"
                rel="noopener noreferrer"
              >
                <a-button class="nav-btn" type="outline" :shape="'circle'">
                  <template #icon>
                    <svg
                      t="1774719965462"
                      class="icon"
                      viewBox="0 0 1024 1024"
                      version="1.1"
                      xmlns="http://www.w3.org/2000/svg"
                      p-id="10587"
                      width="20"
                      height="20"
                    >
                      <path
                        d="M853.7 345.6L614.4 106.4c-5.3-5.3-12.4-8.2-19.9-8.2H173.1c-7.8 0-14.1 6.3-14.1 14.1v801.4c0 7.8 6.3 14.1 14.1 14.1H848c7.8 0 14.1-6.3 14.1-14.1V365.5c-0.1-7.4-3.1-14.6-8.4-19.9z m-52.9 514.6H220.2V165.7h376.4v204h204.2v490.5z"
                        p-id="10588"
                      ></path>
                      <path
                        d="M687 446.6l-58.6 268.5h-68.3L518.6 543c-2.4-8.9-4.1-19.1-4.9-30.5-0.8 13-2.4 23.2-4.9 30.5l-43.9 172.1h-70.8l-58.6-268.5h62.2L428.3 626c0.8 4.1 1.6 11.8 2.4 23.2 0-2.4 0.4 0.4 1.2 8.5h1.2c0.8-9.8 2.4-20.3 4.9-31.7l41.5-179.4h64.7l43.9 181.8c0 1.6 0.4 4.1 1.2 7.3 1.6 9 2.4 16.3 2.4 22h1.2c1.6-12.2 2.8-22.4 3.7-30.5L626 446.6h61z"
                        p-id="10589"
                      ></path>
                    </svg>
                  </template>
                </a-button>
              </a>
            </a-tooltip>
          </div>
        </transition>
      </li>
      <li>
        <header-notice />
      </li>
      <li>
        <a-tooltip
          :content="
            theme === 'light'
              ? $t('navbar.toggleDarkMode')
              : $t('navbar.toggleLightMode')
          "
        >
          <a-button
            class="nav-btn"
            type="outline"
            :shape="'circle'"
            @click="handleToggleTheme"
          >
            <template #icon>
              <icon-moon-fill v-if="theme === 'dark'" />
              <icon-sun-fill v-else />
            </template>
          </a-button>
        </a-tooltip>
      </li>
      <li>
        <a-tooltip :content="$t('navbar.language')">
          <a-dropdown trigger="click" @select="handleLocaleChange">
            <a-button class="nav-btn" type="outline" :shape="'circle'">
              <template #icon>
                <icon-language />
              </template>
            </a-button>
            <template #content>
              <a-doption
                v-for="item in localeOptions"
                :key="item.value"
                :value="item.value"
              >
                <span
                  :style="{
                    fontWeight:
                      appStore.locale === item.value ? 'bold' : 'normal',
                  }"
                >
                  {{ item.label }}
                </span>
              </a-doption>
            </template>
          </a-dropdown>
        </a-tooltip>
      </li>
      <li>
        <a-tooltip
          :content="
            isFullscreen
              ? $t('navbar.exitFullscreen')
              : $t('navbar.enterFullscreen')
          "
        >
          <a-button
            class="nav-btn"
            type="outline"
            :shape="'circle'"
            @click="toggleFullScreen"
          >
            <template #icon>
              <icon-fullscreen-exit v-if="isFullscreen" />
              <icon-fullscreen v-else />
            </template>
          </a-button>
        </a-tooltip>
      </li>
      <li>
        <a-tooltip :content="$t('navbar.pageConfig')">
          <a-button
            class="nav-btn"
            type="outline"
            :shape="'circle'"
            @click="setVisible"
          >
            <template #icon>
              <icon-settings />
            </template>
          </a-button>
        </a-tooltip>
      </li>
      <li>
        <a-dropdown trigger="click">
          <a-avatar
            :size="32"
            :style="{ marginRight: '8px', cursor: 'pointer' }"
          >
            <img
              alt="avatar"
              :src="avatar"
              @error="handleAvatarError"
              @load="handleAvatarLoad"
            />
          </a-avatar>
          <template #content>
            <!-- <a-doption>
              <a-space @click="$router.push({ name: 'Info' })">
                <icon-user />
                <span>
                  {{ '用户中心' }}
                </span>
              </a-space>
            </a-doption> -->
            <a-doption>
              <a-space @click="$router.push({ name: 'Setting' })">
                <icon-settings />
                <span>
                  {{ $t('navbar.userSettings') }}
                </span>
              </a-space>
            </a-doption>
            <a-doption>
              <a-space @click="handleLogout">
                <icon-export />
                <span>
                  {{ $t('navbar.logout') }}
                </span>
              </a-space>
            </a-doption>
          </template>
        </a-dropdown>
      </li>
    </ul>
  </div>
</template>

<script lang="ts" setup>
import { computed, inject, ref, watch, onBeforeUnmount, onMounted } from 'vue';
import { useDark, useFullscreen, useToggle } from '@vueuse/core';
import { useAppStore, useUserStore } from '@/store';
import useUser from '@/hooks/user';
import { LOCALE_OPTIONS } from '@/locale';
import axios from '@/api/interceptor';
import { getWebsiteConfig } from '@/api/system/config';
import HeaderNotice from '../header-notice/index.vue';

const appStore = useAppStore();
const userStore = useUserStore();
const { logout } = useUser();

/** 顶部项目外链（微信 / Gitee / GitHub / 文档）折叠，默认收起 */
const quickLinksExpanded = ref(false);
/** 展开后自动收起延迟（毫秒） */
const QUICK_LINKS_AUTO_COLLAPSE_MS = 5000;
let quickLinksAutoCollapseTimer: ReturnType<typeof setTimeout> | null = null;

function clearQuickLinksAutoCollapseTimer() {
  if (quickLinksAutoCollapseTimer !== null) {
    clearTimeout(quickLinksAutoCollapseTimer);
    quickLinksAutoCollapseTimer = null;
  }
}

watch(quickLinksExpanded, (expanded) => {
  clearQuickLinksAutoCollapseTimer();
  if (!expanded) return;
  quickLinksAutoCollapseTimer = setTimeout(() => {
    quickLinksExpanded.value = false;
    quickLinksAutoCollapseTimer = null;
  }, QUICK_LINKS_AUTO_COLLAPSE_MS);
});
const { isFullscreen, toggle: toggleFullScreen } = useFullscreen();
const rawAvatar = computed(() => {
  const raw = (userStore.user.avatar || '').trim();
  return raw.replace(/["“”]/g, '');
});

function buildAvatarCandidates(url: string): string[] {
  if (!url) return [];
  try {
    const u = new URL(url, window.location.origin);
    const path = u.pathname;
    const lastSlash = path.lastIndexOf('/');
    const dir = path.substring(0, lastSlash + 1);
    const file = path.substring(lastSlash + 1);
    const encodedFile = encodeURIComponent(file);
    const spaceEncoded = url.replace(/\s/g, '%20');
    const filenameEncoded = `${u.origin}${dir}${encodedFile}${u.search}${u.hash}`;
    const fullyEncoded = encodeURI(url);
    return [url, spaceEncoded, filenameEncoded, fullyEncoded];
  } catch (e) {
    const spaceEncoded = url.replace(/\s/g, '%20');
    try {
      return [url, spaceEncoded, encodeURI(url)];
    } catch (err) {
      return [url, spaceEncoded];
    }
  }
}

const avatarCandidates = ref<string[]>([]);
const avatarIndex = ref(0);
const avatarBlobUrl = ref<string>('');
const isFetchingAvatar = ref(false);
const avatar = computed(
  () => avatarBlobUrl.value || avatarCandidates.value[avatarIndex.value] || ''
);

watch(
  rawAvatar,
  (val) => {
    // cleanup previous blob url if any
    if (avatarBlobUrl.value) {
      URL.revokeObjectURL(avatarBlobUrl.value);
      avatarBlobUrl.value = '';
    }
    avatarCandidates.value = buildAvatarCandidates(val);
    avatarIndex.value = 0;
  },
  { immediate: true }
);

onBeforeUnmount(() => {
  clearQuickLinksAutoCollapseTimer();
  if (avatarBlobUrl.value) URL.revokeObjectURL(avatarBlobUrl.value);
});

function tryFetchBlob(url: string): Promise<string> {
  return axios
    .get(url, { responseType: 'blob' })
    .then((response: any) => {
      const blob = response as unknown as Blob;
      if (
        blob &&
        typeof (blob as any).type === 'string' &&
        ((blob as any).type.startsWith('image/') ||
          (blob as any).type === 'application/octet-stream')
      ) {
        return URL.createObjectURL(blob);
      }
      return '';
    })
    .catch(() => '');
}

function fetchAvatarAsBlob(urls: string[]) {
  if (isFetchingAvatar.value) return;
  isFetchingAvatar.value = true;
  urls
    .reduce<Promise<string>>((promise, url) => {
      return promise.then((result) => result || tryFetchBlob(url));
    }, Promise.resolve(''))
    .then((result) => {
      if (result) {
        avatarBlobUrl.value = result;
      }
    })
    .finally(() => {
      isFetchingAvatar.value = false;
    });
}
const theme = computed(() => {
  return appStore.theme;
});
const isDark = useDark({
  selector: 'body',
  attribute: 'arco-theme',
  valueDark: 'dark',
  valueLight: 'light',
  storageKey: 'arco-theme',
  onChanged(dark: boolean) {
    // overridden default behavior
    appStore.toggleTheme(dark);
  },
});
const toggleTheme = useToggle(isDark);
const handleToggleTheme = () => {
  toggleTheme();
};
const setVisible = () => {
  appStore.updateSettings({ globalSettings: true });
};
const handleLogout = () => {
  logout();
};

const localeOptions = LOCALE_OPTIONS;
const handleLocaleChange = (
  value: string | number | Record<string, any> | undefined
) => {
  appStore.switchLocale(value as string);
};

const handleAvatarError = (e: Event) => {
  // 静默处理头像加载错误，避免在控制台打印错误信息
  // 只在开发环境下打印调试信息
  if (import.meta.env.DEV) {
    console.log('Avatar load error (silent):', avatar.value);
  }
  if (avatarBlobUrl.value) return; // already using blob
  if (avatarIndex.value < avatarCandidates.value.length - 1) {
    avatarIndex.value += 1;
  } else {
    // All candidates failed, try fetching as blob with auth headers
    fetchAvatarAsBlob(avatarCandidates.value);
  }
};

const handleAvatarLoad = () => {
  // 只在开发环境下打印调试信息
  if (import.meta.env.DEV) {
    console.log('Avatar loaded successfully:', avatar.value);
  }
};

// Logo相关
const logoUrl = ref<string>('');

// 获取网站配置中的logo
const fetchWebsiteLogo = async () => {
  try {
    const response = await getWebsiteConfig();
    if (response.code === 200 && response.data?.logo) {
      logoUrl.value = response.data.logo;
    }
  } catch (error) {
    console.error('获取网站logo失败:', error);
  }
};

// 处理logo加载错误
const handleLogoError = () => {
  logoUrl.value = ''; // 清空URL，使用默认logo
};

// 组件挂载时获取logo
onMounted(() => {
  fetchWebsiteLogo();
});

const toggleDrawerMenu = inject('toggleDrawerMenu') as () => void;
</script>

<style scoped lang="less">
.navbar {
  display: flex;
  justify-content: space-between;
  height: 100%;
  background-color: var(--color-bg-2);
  border-bottom: 1px solid var(--color-border);
}

.left-side {
  display: flex;
  align-items: center;
  padding-left: 20px;
}

.left-side img {
  width: 60px;
  height: 30px;
}

.right-side {
  display: flex;
  padding-right: 20px;
  list-style: none;

  :deep(.locale-select) {
    border-radius: 20px;
  }

  li {
    display: flex;
    align-items: center;
    padding: 0 10px;
  }

  a {
    color: var(--color-text-1);
    text-decoration: none;
  }

  /* 含子组件 header-notice 内按钮，需 :deep；并补全 hover，避免覆盖 Arco 后无悬浮态 */
  :deep(.nav-btn) {
    border-color: rgb(var(--gray-2));
    color: rgb(var(--gray-8));
    font-size: 16px;
    transition: border-color 0.2s ease, color 0.2s ease,
      background-color 0.2s ease;

    &:hover,
    &:focus-visible {
      border-color: rgb(var(--primary-6));
      color: rgb(var(--primary-6));
    }
  }

  .quick-links-cluster {
    display: flex;
    align-items: center;
    flex-wrap: nowrap;
    gap: 4px;
    padding-right: 4px;
  }

  .quick-links-items {
    display: flex;
    align-items: center;
    gap: 20px;
    margin-left: 10px;
    padding-left: 14px;
    border-left: 1px solid var(--color-border-2);
  }

  .quick-links-cluster.is-expanded .quick-links-trigger {
    border-color: rgb(var(--primary-6));
    color: rgb(var(--primary-6));
  }
}

.quick-links-fade-enter-active,
.quick-links-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.quick-links-fade-enter-from,
.quick-links-fade-leave-to {
  opacity: 0;
  transform: translateX(6px);
}
</style>

