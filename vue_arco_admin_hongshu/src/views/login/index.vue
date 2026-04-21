<template>
  <div class="container" :class="{ 'is-dark': theme === 'dark' }">
    <div class="logo">
      <img 
        v-if="logoUrl" 
        alt="logo" 
        :src="logoUrl" 
        @error="handleLogoError"
      />
      <img 
        v-else 
        alt="logo" 
        src="@/assets/logo/logo.png" 
      />
      <div class="logo-text"></div>
    </div>
    <div class="theme-toggle">
      <a-button
        class="theme-btn"
        type="outline"
        :shape="'circle'"
        @click="handleToggleTheme"
      >
        <template #icon>
          <icon-moon-fill v-if="theme === 'dark'" />
          <icon-sun-fill v-else />
        </template>
      </a-button>
    </div>
    <LoginBanner />
    <div class="content">
      <video class="bg-video" autoplay muted loop playsinline aria-hidden="true">
        <source :src="loginBgVideo" type="video/mp4" />
      </video>
      <div class="bg-overlay" />
      <div class="content-inner">
        <LoginForm />
      </div>
      <div class="footer">
        <Footer />
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, ref, onMounted } from 'vue';
import { useDark, useToggle } from '@vueuse/core';
import { useAppStore } from '@/store';
import { getWebsiteConfig } from '@/api/system/config';
// eslint-disable-next-line import/no-unresolved
import loginBgVideo from '@/assets/images/index-back.mp4';
import Footer from '@/components/footer/index.vue';
import LoginBanner from './components/banner.vue';
import LoginForm from './components/login-form.vue';

const appStore = useAppStore();
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
    appStore.toggleTheme(dark);
  },
});
const toggleTheme = useToggle(isDark);
const handleToggleTheme = () => {
  toggleTheme();
};

const logoUrl = ref<string>('');

const fetchWebsiteLogo = async () => {
  try {
    const response = await getWebsiteConfig();
    if (response.code === 200 && response.data?.logo) {
      logoUrl.value = response.data.logo;
    }
  } catch (error) {
    // fallback to default logo
  }
};

const handleLogoError = () => {
  logoUrl.value = '';
};

onMounted(() => {
  fetchWebsiteLogo();
});
</script>

<style lang="less" scoped>
.container {
  display: flex;
  height: 100vh;

  .banner {
    width: 60%;
    background: linear-gradient(163.85deg, #1d2129 0%, #00308f 100%);
  }

  .content {
    position: relative;
    z-index: 2;
    display: flex;
    flex: 1;
    align-items: center;
    justify-content: center;
    padding: 40px 24px 56px;
    box-sizing: border-box;
  }

  .bg-video {
    position: absolute;
    inset: 0;
    z-index: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .bg-overlay {
    position: absolute;
    inset: 0;
    z-index: 1;
    background: transparent;
    transition: background 0.3s;
    pointer-events: none;
  }

  &.is-dark .bg-overlay {
    background: rgba(0, 0, 0, 0.55);
  }

  .content-inner {
    position: relative;
    z-index: 2;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    max-width: 400px;
    padding: 40px 36px 32px;
    background: rgba(255, 255, 255, 0.88);
    border-radius: 12px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
    backdrop-filter: blur(12px);
    transition: background 0.3s, box-shadow 0.3s;
  }

  &.is-dark .content-inner {
    background: rgba(36, 36, 36, 0.88);
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
  }

  .footer {
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 2;
    width: 100%;

    :deep(.arco-layout-footer),
    :deep(.footer) {
      color: rgba(0, 0, 0, 0.45);
      background: transparent !important;
    }
  }

  &.is-dark .footer {
    :deep(.arco-layout-footer),
    :deep(.footer) {
      color: rgba(255, 255, 255, 0.45);
    }
  }
}

.logo {
  position: fixed;
  top: 24px;
  left: 22px;
  z-index: 3;
  display: inline-flex;
  align-items: center;

  img {
    width: 100px;
    height: 50px;
  }

  &-text {
    margin-right: 4px;
    margin-left: 4px;
    color: var(--color-fill-1);
    font-size: 20px;
  }
}

.theme-toggle {
  position: fixed;
  top: 24px;
  right: 24px;
  z-index: 3;
}

.theme-toggle :deep(.theme-btn) {
  background-color: rgba(0, 0, 0, 0.06);
  border-color: rgba(0, 0, 0, 0.12);
  color: #333;

  &:hover {
    background-color: rgba(0, 0, 0, 0.1);
    border-color: rgba(0, 0, 0, 0.18);
  }
}

.container.is-dark .theme-toggle :deep(.theme-btn) {
  background-color: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.25);
  color: #fff;

  &:hover {
    background-color: rgba(255, 255, 255, 0.25);
    border-color: rgba(255, 255, 255, 0.35);
  }
}
</style>

<style lang="less" scoped>
@media (max-width: @screen-lg) {
  .container {
    .banner {
      width: 40%;
    }
  }
}

@media (max-width: @screen-md) {
  .container {
    .banner {
      display: none;
    }
  }
}
</style>
