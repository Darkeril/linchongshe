<template>
  <div class="container">
    <Breadcrumb :items="['系统工具', 'Nacos']"></Breadcrumb>

    <a-alert v-if="!nacosUrl" type="warning" style="margin-bottom: 16px">
      未配置 Nacos 地址：请在构建环境的
      <code>.env.production</code>
      或对应部署变量中设置
      <code>VITE_APP_NACOS_URL</code>
      （例如 <code>http://服务器IP:8848/nacos</code>）。
    </a-alert>

    <template v-else>
      <!-- <a-alert
        v-if="!embedIframe"
        type="info"
        show-icon
        style="margin-bottom: 16px"
      >
        Nacos 控制台与当前管理端<strong>不同源</strong>时，无法在页面内嵌
        iframe（控制台脚本会访问父页
        <code>location</code>，浏览器会报 SecurityError）。请在新标签页中打开。
      </a-alert> -->

      <a-card title="Nacos 控制台" :bordered="false">
        <a-space direction="vertical" size="large" fill>
          <a-typography-paragraph type="secondary" :margin="0">
            地址：<code class="url-code">{{ nacosUrl }}</code>
          </a-typography-paragraph>
          <a-space wrap>
            <a-button type="primary" @click="openInNewTab">
              在新标签页打开
            </a-button>
            <a-button @click="openSameTab"> 当前窗口跳转 </a-button>
            <a-link :href="nacosUrl" target="_blank" rel="noopener noreferrer">
              或点此链接
            </a-link>
          </a-space>
        </a-space>
      </a-card>

      <i-frame v-if="embedIframe" :src="nacosUrl" class="iframe-wrap"></i-frame>
    </template>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import IFrame from '@/components/iframe/index.vue';

function resolveNacosUrl(): string {
  const raw = (import.meta.env.VITE_APP_NACOS_URL || '').trim();
  if (raw) {
    return raw.replace(/\/$/, '');
  }
  if (import.meta.env.DEV) {
    return 'http://localhost:8848/nacos';
  }
  return '';
}

const nacosUrl = resolveNacosUrl();

/** 仅当与当前页同源时才内嵌（例如把 Nacos 反代到与管理端同一域名下） */
const embedIframe = computed(() => {
  if (!nacosUrl || typeof window === 'undefined') return false;
  try {
    return (
      new URL(nacosUrl, window.location.href).origin === window.location.origin
    );
  } catch {
    return false;
  }
});

function openInNewTab() {
  if (!nacosUrl) return;
  window.open(nacosUrl, '_blank', 'noopener,noreferrer');
}

function openSameTab() {
  if (!nacosUrl) return;
  window.location.href = nacosUrl;
}
</script>

<style scoped>
.container {
  padding: 0 20px 20px 20px;
}

.url-code {
  word-break: break-all;
}

.iframe-wrap {
  margin-top: 16px;
}
</style>
