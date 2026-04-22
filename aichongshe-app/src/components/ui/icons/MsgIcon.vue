<!--
  MsgIcon — TabBar 消息图标
  设计稿来源：design/design-system.jsx 行 169-177
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
        d="M21 11.5c0 4.1-4 7.5-9 7.5a10 10 0 01-4-.8L3 20l1.5-4A7.1 7.1 0 013 11.5C3 7.4 7 4 12 4s9 3.4 9 7.5z"
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
