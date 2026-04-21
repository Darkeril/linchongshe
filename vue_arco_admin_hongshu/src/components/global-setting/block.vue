<template>
  <div class="block">
    <h5 class="title">{{ title }}</h5>
    <div v-for="option in options" :key="option.name" class="switch-wrapper">
      <span>{{ option.name }}</span>
      <form-wrapper
          :type="option.type || 'switch'"
          :name="option.key"
          :default-value="option.defaultVal"
          @input-change="handleChange"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
import {PropType} from 'vue';
import {useI18n} from 'vue-i18n';
import {Message} from '@arco-design/web-vue';
import {useAppStore} from '@/store';
import cache from '@/plugins/cache';
import getAdminReloadHref from '@/utils/admin-entry-href';
import FormWrapper from './form-wrapper.vue';

interface OptionsProps {
  name: string;
  key: string;
  type?: string;
  defaultVal?: boolean | string | number;
}

defineProps({
  title: {
    type: String,
    default: '',
  },
  options: {
    type: Array as PropType<OptionsProps[]>,
    default() {
      return [];
    },
  },
});
const { t } = useI18n();
const appStore = useAppStore();
const handleChange = async ({key, value,}: {
  key: string;
  value: unknown;
}) => {
  if (key === 'colorWeak') {
    document.body.style.filter = value ? 'invert(80%)' : 'none';
  }
  if (key === 'menuFromServer') {
    const v = Boolean(value);
    appStore.updateSettings({menuFromServer: v});
    const prev = (cache.local.getJSON('app-settings') as Record<string, unknown>) || {};
    cache.local.setJSON('app-settings', {...prev, menuFromServer: v});
    if (v) {
      const ok = await appStore.fetchServerMenuConfig();
      if (!ok) {
        appStore.updateSettings({menuFromServer: false});
        cache.local.setJSON('app-settings', {...prev, menuFromServer: false});
        return;
      }
      Message.success(t('settings.menuLoaded'));
      // reload 会保留当前 URL；静态工作台多为 /dashboard/workplace，后端多为 /dashboard/dashboard，不跳转必 404
      window.setTimeout(() => {
        window.location.replace(getAdminReloadHref(true));
      }, 200);
      return;
    }
    appStore.clearServerMenu();
    Message.success(t('settings.menuRestored'));
    window.setTimeout(() => {
      window.location.replace(getAdminReloadHref(false));
    }, 200);
    return;
  }
  appStore.updateSettings({[key]: value});
  const prev = (cache.local.getJSON('app-settings') as Record<string, unknown>) || {};
  cache.local.setJSON('app-settings', {...prev, [key]: value});
};
</script>

<style scoped lang="less">
.block {
  margin-bottom: 24px;
}

.title {
  margin: 10px 0;
  padding: 0;
  font-size: 14px;
}

.switch-wrapper {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 32px;
}
</style>
