<!--
  HeartIcon — 心形（点赞）
  设计稿来源：design/design-system.jsx（HeartIcon）
  - filled=true 时填充 color；false 时只描边
  - 24×24 viewBox，可任意缩放（NoteCard 用 12px）
  - class 加 acs- 前缀（gotchas #12 uview-plus 全局 class 污染）
-->
<template>
  <view
    class="acs-heart-icon"
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
        d="M12 20.5 L3.5 12.5 A5 5 0 0 1 10.5 5.5 L12 7 L13.5 5.5 A5 5 0 0 1 20.5 12.5 Z"
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
  /** 图标尺寸（px），NoteCard 默认 12 */
  size?: number;
  /** 描边 / 填充色 */
  color: string;
  /** 是否填充（已点赞） */
  filled?: boolean;
}

withDefaults(defineProps<Props>(), {
  size: 12,
  filled: false,
});
</script>

<style lang="scss" scoped>
.acs-heart-icon {
  display: inline-block;
}
</style>
