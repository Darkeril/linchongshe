<!--
  DouyinIcon — 抖音品牌图标（Phase 6 新增）
  简化版抖音 logo（音符），不严格还原品牌商标，仅作占位识别
  TODO(design): 待设计师确认是否使用官方品牌素材
-->
<template>
  <svg
    :width="size"
    :height="size"
    viewBox="0 0 24 24"
    xmlns="http://www.w3.org/2000/svg"
  >
    <path
      d="M16.6 5.8c-1.2-0.8-2-2.1-2.1-3.6h-3v13.4c-0.1 1.5-1.3 2.7-2.8 2.7-1.6 0-2.8-1.3-2.8-2.8 0-1.6 1.3-2.8 2.8-2.8 0.3 0 0.6 0.1 0.9 0.2v-3.1c-0.3 0-0.6-0.1-0.9-0.1-3.2 0-5.8 2.6-5.8 5.8s2.6 5.8 5.8 5.8 5.8-2.6 5.8-5.8V8.7c1.2 0.9 2.7 1.4 4.3 1.4v-3c-0.7 0-1.4-0.2-2.2-1.3z"
      :fill="color"
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
