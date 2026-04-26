<!--
  WechatIcon — 微信品牌图标（Phase 6 新增）
  路径来源：login.vue 微信按钮内 SVG（保持视觉一致）
-->
<template>
  <svg
    :width="size"
    :height="size"
    viewBox="0 0 24 24"
    :fill="color"
    xmlns="http://www.w3.org/2000/svg"
  >
    <path
      d="M8.7 13.8c-0.4 0-0.8-0.3-0.8-0.8s0.3-0.8 0.8-0.8 0.8 0.3 0.8 0.8-0.3 0.8-0.8 0.8zm7.2 0c-0.4 0-0.8-0.3-0.8-0.8s0.3-0.8 0.8-0.8c0.4 0 0.8 0.3 0.8 0.8s-0.3 0.8-0.8 0.8zm-3.6-8.8c-5.2 0-9.4 3.5-9.4 7.9 0 2.5 1.4 4.7 3.5 6.1-0.2 0.6-0.6 2-0.6 2.3 0 0.2 0.1 0.3 0.3 0.3 0.1 0 2.3-1.5 2.7-1.8 1.1 0.3 2.2 0.5 3.5 0.5 5.2 0 9.4-3.5 9.4-7.9S17.5 5 12.3 5z"
    />
  </svg>
</template>

<script setup lang="ts">
interface Props {
  size?: number;
  color?: string;
}
withDefaults(defineProps<Props>(), {
  size: 22,
  color: '#FFFFFF',
});
</script>
