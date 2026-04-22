<!--
  MoreDotsIcon — 三点横向
  设计稿来源：screens-part2.jsx L202-204
  - 三个实心圆，水平分布
  - viewBox 18×4，高宽比由 size 决定（宽度 = size，高度 = size * 4/18）
  - class 加 acs- 前缀（gotchas #12）
-->
<template>
  <view
    class="acs-more-dots-icon"
    :style="{
      width: size + 'px',
      height: size * 4 / 18 + 'px',
      lineHeight: 0,
    }"
  >
    <svg
      :width="size"
      :height="size * 4 / 18"
      viewBox="0 0 18 4"
      xmlns="http://www.w3.org/2000/svg"
      :style="{ display: 'block' }"
    >
      <circle cx="2" cy="2" r="1.6" :fill="color" />
      <circle cx="9" cy="2" r="1.6" :fill="color" />
      <circle cx="16" cy="2" r="1.6" :fill="color" />
    </svg>
  </view>
</template>

<script setup lang="ts">
interface Props {
  /** 水平宽度（px），设计稿默认 18 */
  size?: number;
  /** 圆点填充色 */
  color: string;
}

withDefaults(defineProps<Props>(), {
  size: 18,
});
</script>

<style lang="scss" scoped>
.acs-more-dots-icon {
  display: inline-block;
}
</style>
