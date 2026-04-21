<template>
  <a-config-provider :locale="arcoLocale">
    <router-view/>
    <global-setting/>
  </a-config-provider>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import arcoZhCN from '@arco-design/web-vue/es/locale/lang/zh-cn';
import arcoEnUS from '@arco-design/web-vue/es/locale/lang/en-us';
import { useAppStore } from '@/store';
import GlobalSetting from '@/components/global-setting/index.vue';

const appStore = useAppStore();

const arcoLocale = computed(() => {
  return appStore.locale === 'en-US' ? arcoEnUS : arcoZhCN;
});
</script>
