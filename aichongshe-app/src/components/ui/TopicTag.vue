<!--
  TopicTag — 笔记详情页的话题标签（小号）
  设计稿来源：design/screens-part2.jsx L248-255
    padding 3 9  → 6 18 rpx
    radius 10    → 20rpx
    primarySoft 背景 / primaryInk 字
    font 11/500
  - 与首页 TopicPill（12/500、padding 12/24rpx、radius 28rpx）明确不同
  - 组件内部前加 # 前缀，传入 text 不带 #
  - class 加 acs- 前缀（gotchas #12）
  - @tap（gotchas #11 项目约定）
-->
<template>
  <view class="acs-topic-tag" @tap="onTap">
    <text class="acs-topic-tag__text">#{{ text }}</text>
  </view>
</template>

<script setup lang="ts">
interface Props {
  /** 话题文本（不带 # 前缀） */
  text: string;
}

defineProps<Props>();

// emit 名避开 DOM 原生事件 tap（gotcha #21）
const emit = defineEmits<{
  (e: 'topic-tap'): void;
}>();

function onTap(): void {
  emit('topic-tap');
}
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.acs-topic-tag {
  // 设计稿 3/9 → 6/18 rpx
  padding: 6rpx 18rpx;
  border-radius: 20rpx;
  background: $color-primary-soft;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  white-space: nowrap;

  &:active {
    opacity: 0.7;
  }

  &__text {
    font-size: $font-size-sm; // 11px
    font-weight: $font-weight-medium;
    color: $color-primary-ink;
    line-height: 1;
    white-space: nowrap;
  }
}
</style>
