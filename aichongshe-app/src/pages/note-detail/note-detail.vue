<!-- TODO(design): 待设计师细化 笔记详情页（Phase 5 实现） -->
<!--
  note-detail — 笔记详情页（Phase 4 占位）
  Phase 5 才实现像素级还原（设计稿 screens-part2.jsx 行 159 起）。
  这里只做：
  - 顶部返回按钮（navigateBack）
  - 显示传入的 noteId
  - 使用 latte 主题 + tokens；不包 MainLayout（详情页无底部 TabBar）
-->
<template>
  <view class="acs-detail">
    <view class="acs-detail__bar">
      <view class="acs-detail__back" @tap="onBack">
        <text class="acs-detail__back-text">‹ 返回</text>
      </view>
      <text class="acs-detail__title">笔记详情</text>
      <view class="acs-detail__spacer" />
    </view>

    <view class="acs-detail__body">
      <text class="acs-detail__hint">笔记详情 Phase 5 实现</text>
      <text class="acs-detail__id">noteId = {{ noteId }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';

const noteId = ref<string>('');

// 初次加载读路由参数（gotchas #15：用 onLoad 读 query）
onLoad((query) => {
  const q = query as { id?: string } | undefined;
  noteId.value = q?.id ?? '';
});

function onBack(): void {
  if (getCurrentPages().length > 1) {
    uni.navigateBack();
  } else {
    uni.reLaunch({ url: '/pages/home/home' });
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.acs-detail {
  min-height: 100vh;
  background: $color-bg;
  font-family: $font-family-body;
  display: flex;
  flex-direction: column;

  &__bar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 24rpx 32rpx;
    background: $color-surface;
    box-shadow: $shadow-sm;
  }

  &__back {
    padding: 8rpx 16rpx;
    &:active { opacity: 0.6; }
  }

  &__back-text {
    font-size: $font-size-lg;
    color: $color-primary;
    font-weight: $font-weight-medium;
  }

  &__title {
    font-size: $font-size-xl;
    font-weight: $font-weight-bold;
    color: $color-ink;
  }

  &__spacer {
    width: 80rpx; // 平衡左右
  }

  &__body {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 24rpx;
    padding: 48rpx;
  }

  &__hint {
    font-size: $font-size-2xl;
    color: $color-ink;
    font-weight: $font-weight-bold;
  }

  &__id {
    font-size: $font-size-md;
    color: $color-ink-muted;
    font-family: $font-family-num;
  }
}
</style>
