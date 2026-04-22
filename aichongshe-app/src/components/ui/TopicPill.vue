<!--
  TopicPill — 话题胶囊
  设计稿来源：design/screens-part2.jsx 行 115-129
  - active（i===0 在设计稿里）：primary 背景 + 白字 + primary@44 大阴影
  - 非 active：white 背景 + inkSoft 字 + 极浅阴影
  - padding 6 12 → 12 24 rpx；radius 14 → 28rpx
  - class 加 acs- 前缀（gotchas #12）
-->
<template>
  <view class="acs-topic-pill" :class="{ 'acs-topic-pill--active': active }">
    <text class="acs-topic-pill__text">{{ text }}</text>
  </view>
</template>

<script setup lang="ts">
interface Props {
  text: string;
  active?: boolean;
}

withDefaults(defineProps<Props>(), {
  active: false,
});
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.acs-topic-pill {
  // 设计稿 6/12 → 12/24 rpx
  padding: 12rpx 24rpx;
  border-radius: 28rpx;
  background: $color-surface;
  box-shadow: 0 2rpx 6rpx rgba(80, 50, 30, 0.03);
  flex-shrink: 0;
  // 设计稿是 inline flex，小程序 inline-block 更稳
  display: inline-flex;
  align-items: center;
  justify-content: center;
  white-space: nowrap;
  line-height: 1;

  &__text {
    font-size: $font-size-base; // 12px
    font-weight: $font-weight-medium;
    color: $color-ink-soft;
    line-height: 1;
    white-space: nowrap;
  }

  &--active {
    background: $color-primary;
    // 设计稿 primary@44（hex 44 ≈ 0.267）
    box-shadow: 0 8rpx 20rpx rgba($color-primary, 0.27);

    .acs-topic-pill__text {
      color: #ffffff;
    }
  }
}
</style>
