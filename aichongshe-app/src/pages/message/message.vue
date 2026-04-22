<!-- TODO(design): 待设计师细化 消息页 -->
<!--
  Message 消息（Tab 壳占位）
  说明：设计师暂未交付；V1 占位。TabBar msg 徽章 mock=3，验证 badge 渲染。
-->
<template>
  <MainLayout active="msg" :badges="{ msg: 3 }">
    <view class="msg">
      <view class="msg__header">
        <text class="msg__title">消息</text>
        <text class="msg__sub">通知 · 评论 · 私信</text>
      </view>
      <view class="msg__placeholder">
        <text class="msg__placeholder-text">待设计师细化</text>
      </view>
    </view>
  </MainLayout>
</template>

<script setup lang="ts">
import MainLayout from '@/components/ui/MainLayout.vue';
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.msg {
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
