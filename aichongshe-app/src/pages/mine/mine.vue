<!-- TODO(design): 待设计师细化 我的页 -->
<!--
  Mine "我" Tab 壳占位
  说明：设计师暂未交付；V1 占位。登录态 + 爪币 + 任务 等入口待真实设计。
-->
<template>
  <MainLayout active="me">
    <view class="mine">
      <view class="mine__header">
        <text class="mine__title">我</text>
        <text class="mine__sub">个人主页 · 爪币 · 订单 · 设置</text>
      </view>
      <view class="mine__placeholder">
        <text class="mine__placeholder-text">待设计师细化</text>
      </view>
    </view>
  </MainLayout>
</template>

<script setup lang="ts">
import MainLayout from '@/components/ui/MainLayout.vue';
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.mine {
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
    font-size: $font-size-4xl;
    font-weight: $font-weight-bold;
    color: $color-ink;
    font-family: $font-family-brand;
  }

  &__sub {
    font-size: $font-size-base;
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
    font-size: $font-size-lg;
    color: $color-ink-soft;
  }
}
</style>
