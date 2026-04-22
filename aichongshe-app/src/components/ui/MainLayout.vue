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
    // Phase 4 修正：TabBar 是悬浮毛玻璃（fixed），内容区延伸到屏幕底部
    // 最后一屏卡片能滚到 TabBar 后面再露出（视觉连续感，小红书/抖音模式）
    // 页面自己的滚动容器末尾加空白占位保证最后一张卡不被 TabBar 永久遮挡
    box-sizing: border-box;
  }
}
</style>
