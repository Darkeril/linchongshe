<!--
  BookmarkIcon — 书签（收藏）
  设计稿来源：screens-part2.jsx L310-311 + 对标 HeartIcon 结构
  - filled=true 时填充 color；false 时只描边
  - 24×24 viewBox，可任意缩放
  - class 加 acs- 前缀（gotchas #12）
-->
<template>
  <view
    class="acs-bookmark-icon"
    :style="{ width: size + 'px', height: size + 'px', lineHeight: 0 }"
  >
    <svg
      :width="size"
      :height="size"
      viewBox="0 0 24 24"
      xmlns="http://www.w3.org/2000/svg"
      :style="{ display: 'block' }"
    >
      <path
        d="M6 3.5 L18 3.5 A1 1 0 0 1 19 4.5 L19 20.5 L12 16 L5 20.5 L5 4.5 A1 1 0 0 1 6 3.5 Z"
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
  /** 图标尺寸（px） */
  size?: number;
  /** 描边 / 填充色 */
  color: string;
  /** 是否填充（已收藏） */
  filled?: boolean;
}

withDefaults(defineProps<Props>(), {
  size: 22,
  filled: false,
});
</script>

<style lang="scss" scoped>
.acs-bookmark-icon {
  display: inline-block;
}
</style>
