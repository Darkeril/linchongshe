<!--
  HomeIcon — TabBar 首页图标
  设计稿来源：design/design-system.jsx 行 148-156
  24×24 viewBox；filled=true 时实心填充，否则 1.8 描边
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
        d="M3 11l9-7.5L21 11v9a1.5 1.5 0 01-1.5 1.5h-4V15h-7v6.5h-4A1.5 1.5 0 013 20v-9z"
        :fill="filled ? color : 'none'"
        :stroke="color"
        stroke-width="1.8"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
    </svg>
  </view>
</template>

<script setup lang="ts">
interface Props {
  size?: number;
  color: string;
  filled?: boolean;
}
withDefaults(defineProps<Props>(), {
  size: 22,
  filled: false,
});
</script>

<style lang="scss" scoped>
.acs-tab-icon {
  display: inline-block;
}
</style>
