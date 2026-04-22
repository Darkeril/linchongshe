<!--
  NoteDetailActions — 笔记详情底部固定操作栏
  设计稿来源：design/screens-part2.jsx L291-317
  结构：
    - fixed bottom, height 62 → 124rpx
    - 毛玻璃 rgba(255,255,255,0.95) + blur 20（条件编译降级）
    - 顶部 divider 1rpx（0.5px）
    - 左：输入框占位 flex:1，height 38 → 76rpx，radius 19 → 38rpx，surfaceDim 背景，"说点什么吧..." 12/inkMuted
    - 右：3 组图标+数字（每组 width 40 → 80rpx，垂直 gap 1 → 2rpx）
      - Heart 22: active warn filled / inactive inkSoft，数字 9/font-family-num
      - Bookmark 22: active accent filled / inactive inkSoft，数字 9
      - Chat 22: inkSoft，数字 9
  - safe-area-inset-bottom 处理
  - 所有按钮 @tap（gotchas #11）
  - class 加 acs- 前缀（gotchas #12）
-->
<template>
  <view class="acs-detail-actions">
    <!-- 输入框占位 -->
    <view class="acs-detail-actions__input" @tap="onInputTap">
      <text class="acs-detail-actions__input-placeholder">说点什么吧...</text>
    </view>

    <!-- 点赞 -->
    <view class="acs-detail-actions__btn" @tap="onLike">
      <HeartIcon
        :size="22"
        :color="liked ? warnColor : inkSoftColor"
        :filled="liked"
      />
      <text
        class="acs-detail-actions__btn-count"
        :class="{ 'acs-detail-actions__btn-count--active-warn': liked }"
      >{{ formatCount(likes) }}</text>
    </view>

    <!-- 收藏 -->
    <view class="acs-detail-actions__btn" @tap="onSave">
      <BookmarkIcon
        :size="22"
        :color="saved ? accentColor : inkSoftColor"
        :filled="saved"
      />
      <text
        class="acs-detail-actions__btn-count"
        :class="{ 'acs-detail-actions__btn-count--active-accent': saved }"
      >{{ formatCount(saves) }}</text>
    </view>

    <!-- 评论 -->
    <view class="acs-detail-actions__btn" @tap="onComment">
      <ChatIcon :size="22" :color="inkSoftColor" />
      <text class="acs-detail-actions__btn-count">{{ formatCount(comments) }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import HeartIcon from '@/components/ui/icons/HeartIcon.vue';
import BookmarkIcon from '@/components/ui/icons/BookmarkIcon.vue';
import ChatIcon from '@/components/ui/icons/ChatIcon.vue';
import { COLOR } from '@/styles/tokens';

interface Props {
  likes: number;
  liked: boolean;
  saves: number;
  saved: boolean;
  comments: number;
}

defineProps<Props>();

const emit = defineEmits<{
  (e: 'like'): void;
  (e: 'save'): void;
  (e: 'comment'): void;
  (e: 'input-tap'): void;
}>();

const warnColor = COLOR.warn;
const accentColor = COLOR.accent;
const inkSoftColor = COLOR.inkSoft;

/** 数字 > 999 显示 N.Nk */
function formatCount(n: number): string {
  if (n > 999) return (n / 1000).toFixed(1) + 'k';
  return String(n);
}

function onLike(): void {
  emit('like');
}

function onSave(): void {
  emit('save');
}

function onComment(): void {
  emit('comment');
}

function onInputTap(): void {
  emit('input-tap');
}
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

// fixed 底部栏（gotchas #5：确保祖先链无 transform）
.acs-detail-actions {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  // 设计稿 62 → 124rpx + safe-area 兜底
  height: 124rpx;
  padding: 0 24rpx; // 12 → 24rpx
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-top: 1rpx solid $color-divider;
  display: flex;
  align-items: center;
  gap: 20rpx; // 10 → 20rpx
  z-index: 10;

  /* #ifdef MP-WEIXIN || MP-TOUTIAO */
  // 小程序端 backdrop-filter 支持差，降级为不透明白
  background: rgba(255, 255, 255, 0.98);
  /* #endif */

  // ── 输入框占位 ───────────────────────────────────────
  &__input {
    flex: 1;
    height: 76rpx; // 38 → 76rpx
    border-radius: 38rpx; // 19 → 38rpx
    background: $color-surface-dim;
    display: flex;
    align-items: center;
    padding: 0 28rpx; // 14 → 28rpx
    min-width: 0;
    &:active { opacity: 0.85; }
  }

  &__input-placeholder {
    font-size: $font-size-base; // 12px
    color: $color-ink-muted;
    line-height: 1;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  // ── 图标按钮（3 组）─────────────────────────────────
  &__btn {
    // 每组宽 40 → 80rpx，垂直 gap 1 → 2rpx
    width: 80rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 2rpx;
    flex-shrink: 0;
    &:active { opacity: 0.65; }
  }

  &__btn-count {
    font-size: 18rpx; // 9 → 18rpx（设计稿极小字）
    font-weight: $font-weight-semibold;
    color: $color-ink-muted;
    font-family: $font-family-num;
    line-height: 1;

    &--active-warn {
      color: $color-warn;
    }
    &--active-accent {
      color: $color-accent;
    }
  }
}
</style>
