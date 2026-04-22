<!-- TODO(design): 待设计师细化 发布页 -->
<!--
  Publish 发布页（独立路由，不在 tabBar）
  说明：
  - 发布按钮走 navigateSafe push，不是 switchTab
  - 不使用 MainLayout（非 tab 页）
  - V1 占位；Phase 5+ 接入真实发布流程（图/文/视频 + 话题 + 定位）
-->
<template>
  <view class="publish">
    <view class="publish__header">
      <view class="publish__back" @tap="onBack">
        <svg
          width="22"
          height="22"
          viewBox="0 0 24 24"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            d="M15 5 L8 12 L15 19"
            :stroke="inkColor"
            stroke-width="2"
            fill="none"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
      </view>
      <text class="publish__title">发布</text>
      <view class="publish__placeholder-spacer" />
    </view>
    <view class="publish__body">
      <text class="publish__tip">发布流程待细化</text>
      <text class="publish__sub">Phase 5+ 接入图文 / 视频 / 话题 / 定位</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { COLOR } from '@/styles/tokens';

const inkColor = COLOR.ink;

function onBack(): void {
  // 走 uni.navigateBack 单次返回；栈内无页面时降级到首页
  const pages = getCurrentPages();
  if (pages.length > 1) {
    uni.navigateBack({ delta: 1 });
  } else {
    uni.switchTab({ url: '/pages/home/home' });
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.publish {
  position: relative;
  width: 100%;
  min-height: 100vh;
  background: $color-bg;
  font-family: $font-family-body;

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 48rpx 28rpx 24rpx;
  }

  &__back {
    width: 64rpx;
    height: 64rpx;
    border-radius: 32rpx;
    background: $color-surface;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: $shadow-sm;

    &:active { opacity: 0.7; }
  }

  &__title {
    font-size: $font-size-2xl; // 18px
    font-weight: $font-weight-bold;
    color: $color-ink;
    line-height: 1;
  }

  &__placeholder-spacer {
    width: 64rpx;
    height: 64rpx;
  }

  &__body {
    margin: 96rpx 28rpx 0;
    padding: 96rpx 32rpx;
    background: $color-surface;
    border-radius: $radius-lg;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16rpx;
    box-shadow: $shadow-sm;
  }

  &__tip {
    font-size: $font-size-xl; // 16px
    font-weight: $font-weight-bold;
    color: $color-ink;
  }

  &__sub {
    font-size: $font-size-sm; // 11px
    color: $color-ink-muted;
  }
}
</style>
