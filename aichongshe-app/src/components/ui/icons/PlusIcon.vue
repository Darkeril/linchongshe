<!--
  PlusIcon — TabBar 发布按钮图标
  设计稿来源：design/design-system.jsx 行 190-197
  只有实心/描边线（+）两种笔画；默认白色
-->
<template>
  <view class="acs-tab-icon" :style="{ width: size + 'px', height: size + 'px', lineHeight: 0 }">
    <svg
      :width="size"
      :height="size"
      viewBox="0 0 24 24"
      xmlns="http://www.w3.org/2000/svg"
      :style="{ display: 'block' }"
    >
      <path
        d="M12 5v14M5 12h14"
        fill="none"
        :stroke="color"
        stroke-width="2.6"
        stroke-linecap="round"
      />
    </svg>
  </view>
</template>

<script setup lang="ts">
interface Props {
  size?: number;
  color?: string;
}
withDefaults(defineProps<Props>(), {
  size: 24,
  color: '#FFFFFF',
});
</script>

<style lang="scss" scoped>
.acs-tab-icon {
  display: inline-block;
}
</style>
