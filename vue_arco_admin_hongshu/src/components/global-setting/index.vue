<template>
  <div v-if="!appStore.navbar" class="fixed-settings" @click="setVisible">
    <a-button type="primary">
      <template #icon>
        <icon-settings />
      </template>
    </a-button>
  </div>
  <a-drawer
    :width="300"
    unmount-on-close
    :visible="visible"
    :cancel-text="$t('common.cancel')"
    :ok-text="$t('common.confirm')"
    @ok="confirmSettings"
    @cancel="cancel"
  >
    <template #title> {{ $t('settings.title') }}</template>
    <Block :options="contentOpts" :title="$t('settings.contentArea')" />
    <Block :options="othersOpts" :title="$t('settings.otherSettings')" />
    <a-alert>{{ $t('settings.alertMessage') }}</a-alert>
  </a-drawer>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { Message } from '@arco-design/web-vue';
  import { useAppStore } from '@/store';
  import cache from '@/plugins/cache';
  import Block from './block.vue';

  const emit = defineEmits(['cancel']);

  const { t } = useI18n();
  const appStore = useAppStore();
  const visible = computed(() => appStore.globalSettings);
  const contentOpts = computed(() => [
    { name: t('settings.navbar'), key: 'navbar', defaultVal: appStore.navbar },
    {
      name: t('settings.menu'),
      key: 'menu',
      defaultVal: appStore.menu,
    },
    { name: t('settings.footer'), key: 'footer', defaultVal: appStore.footer },
    { name: t('settings.tabBar'), key: 'tabBar', defaultVal: appStore.tabBar },
    {
      name: t('settings.menuWidth'),
      key: 'menuWidth',
      defaultVal: appStore.menuWidth,
      type: 'number',
    },
  ]);
  const othersOpts = computed(() => [
    {
      name: t('settings.colorWeak'),
      key: 'colorWeak',
      defaultVal: appStore.colorWeak,
    },
  ]);

  const cancel = () => {
    appStore.updateSettings({ globalSettings: false });
    emit('cancel');
  };
  
  const confirmSettings = () => {
    // 保存当前配置到 localStorage
    const settings = {
      theme: appStore.theme,
      colorWeak: appStore.colorWeak,
      navbar: appStore.navbar,
      menu: appStore.menu,
      hideMenu: appStore.hideMenu,
      menuCollapse: appStore.menuCollapse,
      footer: appStore.footer,
      themeColor: appStore.themeColor,
      menuWidth: appStore.menuWidth,
      tabBar: appStore.tabBar,
      menuFromServer: appStore.menuFromServer,
      device: appStore.device,
    };
    
    cache.local.setJSON('app-settings', settings);
    Message.success(t('settings.saved'));
    
    // 关闭弹窗
    appStore.updateSettings({ globalSettings: false });
    emit('cancel');
  };
  
  const setVisible = () => {
    appStore.updateSettings({ globalSettings: true });
  };
</script>

<style scoped lang="less">
  .fixed-settings {
    position: fixed;
    top: 280px;
    right: 0;

    svg {
      font-size: 18px;
      vertical-align: -4px;
    }
  }
</style>
