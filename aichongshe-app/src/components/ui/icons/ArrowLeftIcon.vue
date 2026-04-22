<!--
  ArrowLeftIcon — 返回箭头 `<`
  设计稿来源：screens-part2.jsx L180-182
  - path "M10 3L5 8l5 5" 描边，viewBox 16×16
  - class 加 acs- 前缀（gotchas #12）
-->
<template>
  <view
    class="acs-arrow-left-icon"
    :style="{ width: size + 'px', height: size + 'px', lineHeight: 0 }"
  >
    <svg
      :width="size"
      :height="size"
      viewBox="0 0 16 16"
      xmlns="http://www.w3.org/2000/svg"
      :style="{ display: 'block' }"
    >
      <path
        d="M10 3L5 8l5 5"
        fill="none"
        :stroke="color"
        stroke-width="2"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
    </svg>
  </view>
</template>

<script setup lang="ts">
interface Props {
  /** 图标尺寸（px），设计稿默认 16 */
  size?: number;
  /** 描边色 */
  color: string;
}

withDefaults(defineProps<Props>(), {
  size: 16,
});
</script>

<style lang="scss" scoped>
.acs-arrow-left-icon {
  display: inline-block;
}
</style>
