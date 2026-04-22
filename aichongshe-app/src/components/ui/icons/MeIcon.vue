<!--
  MeIcon — TabBar "我" 图标
  设计稿来源：design/design-system.jsx 行 179-187
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
      <circle
        cx="12"
        cy="8"
        r="4"
        :fill="filled ? color : 'none'"
        :stroke="color"
        stroke-width="1.8"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
      <path
        d="M4 21c0-4.4 3.6-8 8-8s8 3.6 8 8"
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
