<!--
  MainLayout — Tab 页统一布局容器
  来源：docs/plans/phase-3-tabbar.md（方案 D：MainLayout 包裹，TabBar 全局一致）

  职责：
  - 接受 active prop（home / market / msg / me），传给 AppTabBar
  - 用 slot 承载页面内容
  - 统一处理底部安全距离（content 预留 TabBar 高度 + 安全区，防内容被挡）
  - AppTabBar 与 content 平级（避免 fixed 在 transform 祖先上失效，gotchas #5）

  使用（MainLayout active=home 包裹主体）

  注意：
  - 不要在 MainLayout 外层套 transform 容器，否则 TabBar 会脱离视口定位（gotchas #5）
  - badges 目前仅 msg 使用，后续扩展再加 props
-->
<template>
  <view class="acs-layout">
    <view class="acs-layout__content">
      <slot />
    </view>
    <AppTabBar :active="active" :badges="badges" />
  </view>
</template>

<script setup lang="ts">
import AppTabBar, { type TabId } from '@/components/ui/AppTabBar.vue';

interface Props {
  /** 当前 active 的 tab，publish 不作为 active 目标 */
  active: Exclude<TabId, 'publish'>;
  /** tab 徽章数量 */
  badges?: { msg?: number };
}

withDefaults(defineProps<Props>(), {
  badges: () => ({}),
});
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.acs-layout {
  position: relative;
  width: 100%;
  min-height: 100vh;
  background: $color-bg;
  // ⚠️ 禁止在此层/外层加 transform / filter / perspective（gotchas #5）

  &__content {
    width: 100%;
    min-height: 100vh;
    // 底部预留 TabBar 高 120rpx + bottom 32rpx + 额外 32rpx 安全距 + safe-area
    padding-bottom: calc(216rpx + env(safe-area-inset-bottom));
    box-sizing: border-box;
  }
}
</style>
