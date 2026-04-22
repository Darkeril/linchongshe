<!-- TODO(design): 待设计师细化 市集首页 -->
<!--
  Market 市集（Tab 壳占位）
  说明：设计师暂未交付；按 CLAUDE.md 原则 1 无设计稿分支，出 V1 占位：
  - 整体走 latte 主题 tokens
  - 复用 MainLayout + AppTabBar，保证全局一致
-->
<template>
  <MainLayout active="market">
    <view class="market">
      <view class="market__header">
        <text class="market__title">市集</text>
        <text class="market__sub">宠物好物 · 寄养闲置 · 商家联动</text>
      </view>
      <view class="market__placeholder">
        <text class="market__placeholder-text">待设计师细化</text>
      </view>
    </view>
  </MainLayout>
</template>

<script setup lang="ts">
import MainLayout from '@/components/ui/MainLayout.vue';
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.market {
  width: 100%;
  min-height: 100vh;
  padding: 48rpx 32rpx 0;
  background: $color-bg;
  font-family: $font-family-body;

  &__header {
    display: flex;
    flex-direction: column;
    gap: 12rpx;
    margin-bottom: 48rpx;
  }

  &__title {
    font-size: $font-size-4xl; // 26px
    font-weight: $font-weight-bold;
    color: $color-ink;
    font-family: $font-family-brand;
  }

  &__sub {
    font-size: $font-size-base; // 12px
    color: $color-ink-muted;
  }

  &__placeholder {
    margin-top: 64rpx;
    padding: 80rpx 32rpx;
    background: $color-surface;
    border-radius: $radius-lg;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: $shadow-sm;
  }

  &__placeholder-text {
    font-size: $font-size-lg; // 14px
    color: $color-ink-soft;
  }
}
</style>
