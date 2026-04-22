<!--
  NoteDetailHeader — 笔记详情顶部悬浮导航栏
  设计稿来源：design/screens-part2.jsx L168-206
  结构：
    - 左：返回按钮（36×36 圆 → 72rpx，毛玻璃 rgba(255,255,255,0.9) + blur 16）
    - 中：作者胶囊（毛玻璃 + PetAvatar 28 + 名字 13/600 + "+ 关注" / "已关注" 按钮）
    - 右：更多操作（36×36 圆 + 三点）
  - absolute top 40 → 80rpx + safe-area top，height 52 → 104rpx，z-index 10
  - 毛玻璃条件编译降级（gotchas #14：小程序端用纯色 0.95）
  - 所有按钮 @tap（gotchas #11）
  - class 加 acs- 前缀（gotchas #12）
-->
<template>
  <view class="acs-detail-header">
    <!-- 返回按钮 -->
    <view class="acs-detail-header__btn" @tap="onBack">
      <ArrowLeftIcon :size="16" :color="inkColor" />
    </view>

    <!-- 作者胶囊 -->
    <view class="acs-detail-header__author">
      <PetAvatar :variant="author.avatarVariant" :size="28" />
      <text class="acs-detail-header__author-name">{{ author.name }}</text>
      <view
        class="acs-detail-header__follow"
        :class="{ 'acs-detail-header__follow--followed': followed }"
        @tap="onFollow"
      >
        <text class="acs-detail-header__follow-text">{{ followed ? '已关注' : '+ 关注' }}</text>
      </view>
    </view>

    <!-- 更多 -->
    <view class="acs-detail-header__btn" @tap="onMore">
      <MoreDotsIcon :size="18" :color="inkColor" />
    </view>
  </view>
</template>

<script setup lang="ts">
import PetAvatar from '@/components/ui/PetAvatar.vue';
import ArrowLeftIcon from '@/components/ui/icons/ArrowLeftIcon.vue';
import MoreDotsIcon from '@/components/ui/icons/MoreDotsIcon.vue';
import { COLOR } from '@/styles/tokens';
import type { NoteAuthor } from '@/types/note';

interface Props {
  author: NoteAuthor;
  /** 当前登录用户是否已关注（从父组件读，便于统一与其它位置同步） */
  followed: boolean;
}

defineProps<Props>();

const emit = defineEmits<{
  (e: 'back'): void;
  (e: 'follow'): void;
  (e: 'more'): void;
}>();

const inkColor = COLOR.ink;

function onBack(): void {
  emit('back');
}

function onFollow(): void {
  emit('follow');
}

function onMore(): void {
  emit('more');
}
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.acs-detail-header {
  position: absolute;
  // 设计稿 top 40 → 80rpx；叠加 iOS 状态栏安全区（H5 忽略）
  top: calc(80rpx + constant(safe-area-inset-top));
  top: calc(80rpx + env(safe-area-inset-top));
  left: 0;
  right: 0;
  height: 104rpx; // 52 → 104rpx
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28rpx; // 14 → 28rpx
  z-index: 10;

  // ── 左右圆形按钮（返回 / 更多） ─────────────────────
  &__btn {
    width: 72rpx; // 36 → 72rpx
    height: 72rpx;
    border-radius: 36rpx;
    background: rgba(255, 255, 255, 0.9);
    backdrop-filter: blur(16px);
    -webkit-backdrop-filter: blur(16px);
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4rpx 16rpx rgba(80, 50, 30, 0.08);
    flex-shrink: 0;
    &:active { opacity: 0.75; }

    /* #ifdef MP-WEIXIN || MP-TOUTIAO */
    // 小程序端 backdrop-filter 支持度低，降级为不透明白
    background: rgba(255, 255, 255, 0.95);
    /* #endif */
  }

  // ── 中间作者胶囊 ───────────────────────────────────
  &__author {
    display: flex;
    align-items: center;
    gap: 16rpx; // 8 → 16rpx
    background: rgba(255, 255, 255, 0.9);
    backdrop-filter: blur(16px);
    -webkit-backdrop-filter: blur(16px);
    padding: 8rpx 24rpx 8rpx 8rpx; // 4 12 4 4 → 8 24 8 8 rpx
    border-radius: 40rpx;
    box-shadow: 0 4rpx 16rpx rgba(80, 50, 30, 0.08);
    min-width: 0;

    /* #ifdef MP-WEIXIN || MP-TOUTIAO */
    background: rgba(255, 255, 255, 0.95);
    /* #endif */
  }

  &__author-name {
    font-size: $font-size-md; // 13px
    font-weight: $font-weight-semibold; // 600
    color: $color-ink;
    line-height: 1;
    max-width: 180rpx;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  // ── 关注按钮（primary 背景 / outline 样式） ───────
  &__follow {
    padding: 8rpx 20rpx; // 4 10 → 8 20 rpx
    border-radius: 24rpx; // 12 → 24rpx
    background: $color-primary;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    line-height: 1;
    &:active { opacity: 0.85; }
  }

  &__follow-text {
    font-size: $font-size-sm; // 11px
    font-weight: $font-weight-semibold; // 600
    color: #ffffff;
    line-height: 1;
    white-space: nowrap;
  }

  // 已关注：outline 样式（白底 + primary 描边 + primary 字）
  &__follow--followed {
    background: rgba(255, 255, 255, 0.6);
    border: 1rpx solid $color-primary-soft;

    .acs-detail-header__follow-text {
      color: $color-primary-ink;
    }
  }
}
</style>
