<template>
  <view class="index">
    <view class="index__title">首页占位</view>
    <view class="index__desc">这里将承载关注 / 发现 / 附近 三 Tab，见 docs/plans。</view>
    <u-button type="primary" text="uview-plus 主按钮测试" @click="showToast" />
  </view>
</template>

<script setup lang="ts">
function showToast(): void {
  uni.showToast({ title: 'latte 主题生效', icon: 'none' });
}
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.index {
  min-height: 100vh;
  padding: $spacing-2xl $spacing-lg;
  background-color: $color-bg;

  &__title {
    font-size: $font-size-2xl;
    font-weight: $font-weight-bold;
    color: $color-ink;
    margin-bottom: $spacing-sm;
  }

  &__desc {
    font-size: $font-size-base;
    color: $color-ink-soft;
    margin-bottom: $spacing-2xl;
  }
}
</style>
