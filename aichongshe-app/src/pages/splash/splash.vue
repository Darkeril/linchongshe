<template>
  <view class="splash">
    <view class="splash__brand">爱宠社</view>
    <view class="splash__tip">Splash — 待像素级还原</view>
    <view class="splash__mock" v-if="profileText">{{ profileText }}</view>
    <view class="splash__mock splash__mock--loading" v-else>正在读取 Mock 用户…</view>
    <u-button
      type="primary"
      shape="circle"
      text="进入首页（占位）"
      class="splash__btn"
      @click="goIndex"
    />
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getProfile } from '@/api/services/user';

const profileText = ref('');

onMounted(async () => {
  // 验证 Mock 数据通路（VITE_USE_MOCK=true 时走 mock）
  const profile = await getProfile();
  if (profile) {
    profileText.value = `Mock OK · ${profile.nickname}（fans=${profile.fansCount}）`;
  }
});

function goIndex(): void {
  uni.redirectTo({ url: '/pages/index/index' });
}
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.splash {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: $color-bg;
  padding: $spacing-2xl;

  &__brand {
    font-family: $font-family-brand;
    font-size: $font-size-4xl;
    font-weight: $font-weight-bold;
    color: $color-primary-ink;
    margin-bottom: $spacing-md;
  }

  &__tip {
    font-size: $font-size-base;
    color: $color-ink-muted;
    margin-bottom: $spacing-2xl;
  }

  &__mock {
    font-size: $font-size-sm;
    color: $color-ink-soft;
    margin-bottom: $spacing-xl;

    &--loading {
      color: $color-ink-faint;
    }
  }

  &__btn {
    width: 80%;
  }
}
</style>
