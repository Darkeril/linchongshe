<!--
  TopicPillScroll — 话题胶囊横滑容器
  设计稿来源：design/screens-part2.jsx 行 115-129
  - gap 8 → 16rpx；左右 padding 16 → 32rpx
  - 容器用 <scroll-view scroll-x>；必须显式高度（gotchas #4 scroll-view）
  - show-scrollbar=false 去掉滚动条
-->
<template>
  <scroll-view
    class="acs-topic-scroll"
    scroll-x
    :show-scrollbar="false"
    :enhanced="true"
  >
    <view class="acs-topic-scroll__inner">
      <TopicPill
        v-for="(t, i) in topics"
        :key="t + '-' + i"
        :text="t"
        :active="i === activeIndex"
      />
    </view>
  </scroll-view>
</template>

<script setup lang="ts">
import TopicPill from '@/components/ui/TopicPill.vue';

interface Props {
  topics: string[];
  /** 高亮索引，默认第一个 */
  activeIndex?: number;
}

withDefaults(defineProps<Props>(), {
  activeIndex: 0,
});
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.acs-topic-scroll {
  // gotchas #4：scroll-view 必须显式高度。
  // 胶囊 padding 12rpx 上下 + 内容 12px(=24rpx) + 阴影溢出 ≈ 72rpx
  width: 100%;
  height: 72rpx;
  // 允许阴影外溢（scroll-view 默认 overflow: hidden）
  white-space: nowrap;
  box-sizing: border-box;

  &__inner {
    display: inline-flex; // 横排，scroll-x 需要 inline-block 或 inline-flex
    align-items: center;
    gap: 16rpx;
    padding: 0 32rpx;
    height: 100%;
  }
}
</style>
