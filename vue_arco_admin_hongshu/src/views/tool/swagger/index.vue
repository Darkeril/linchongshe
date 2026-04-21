<template>
  <div class="container">
    <!--面包屑-->
    <Breadcrumb :items="['系统工具', '系统接口']"></Breadcrumb>

    <i-frame :src="state.url"></i-frame>

  </div>
</template>

<script lang="ts" setup>
import IFrame from '@/components/iframe/index.vue';

/**
 * 网关在浏览器中的路径前缀（与 SpringDoc public base、Nginx 转发一致）。
 * - 开发：Vite 代理 VITE_APP_BASE_API（如 /dev-api）
 * - 生产：未配置 VITE_APP_SWAGGER_BASE 时，若 BASE_URL 为 /arco-admin/ 则用该前缀
 */
function resolveSwaggerGatewayPrefix(): string {
  const explicit = (import.meta.env.VITE_APP_SWAGGER_BASE || '').trim();
  if (explicit) {
    return explicit.replace(/\/$/, '');
  }
  if (import.meta.env.DEV) {
    return (import.meta.env.VITE_APP_BASE_API || '').replace(/\/$/, '');
  }
  const fromBase = (import.meta.env.BASE_URL || '/').replace(/\/$/, '');
  if (fromBase && fromBase !== '/') {
    return fromBase;
  }
  return (import.meta.env.VITE_APP_BASE_API || '').replace(/\/$/, '');
}

/**
 * SpringDoc 聚合配置；显式 configUrl 可避免 Swagger UI 未拉到配置时退回 Petstore 示例。
 * 需保证网关白名单放行 /v3/api-docs/**、/swagger-ui/**（或对应前缀下的路径）。
 */
function resolveSwaggerUiUrl(): string {
  const prefix = resolveSwaggerGatewayPrefix();
  const uiPath = `${prefix}/swagger-ui/index.html`;
  const configPath = `${prefix}/v3/api-docs/swagger-config`;
  const origin =
    typeof window !== 'undefined' ? window.location.origin : '';
  if (origin) {
    const absUi = `${origin}${uiPath}`;
    const absConfig = `${origin}${configPath}`;
    const q = new URLSearchParams({ configUrl: absConfig });
    return `${absUi}?${q.toString()}`;
  }
  return `${uiPath}?configUrl=${encodeURIComponent(configPath)}`;
}

const state = {
  url: resolveSwaggerUiUrl(),
};
</script>

<style scoped>
.container {
  padding: 0 20px 20px 20px;
}
</style>
