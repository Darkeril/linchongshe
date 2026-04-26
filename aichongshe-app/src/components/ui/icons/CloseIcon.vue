<!--
  CloseIcon — X 关闭图标（Phase 6 新增）
  用于 LoginSheet 顶部关闭按钮
-->
<template>
  <svg
    :width="size"
    :height="size"
    viewBox="0 0 24 24"
    xmlns="http://www.w3.org/2000/svg"
  >
    <path
      d="M6 6 L18 18 M18 6 L6 18"
      :stroke="color"
      stroke-width="2"
      stroke-linecap="round"
      fill="none"
    />
  </svg>
</template>

<script setup lang="ts">
interface Props {
  size?: number;
  color?: string;
}
withDefaults(defineProps<Props>(), {
  size: 20,
  color: '#231710',
});
</script>
