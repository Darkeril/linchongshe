<!--
  CommentItem — 单条评论（root 或 reply 变体）
  设计稿来源：design/screens-part2.jsx L266-287（root 样式）
  子回复（reply variant）无设计稿，按原则 1 V1：
    TODO(design): 子回复样式待设计师细化（头像 28 + @用户名 着色）

  gotcha 提醒：
    - #12 class 加 acs- 前缀
    - #18 点赞 in-place mutation（组件内不持 state，全靠 props 驱动）
    - #21 emit 名避 DOM 原生：comment-like / comment-reply
-->
<template>
  <view class="acs-comment-item" :class="['acs-comment-item--' + variant]">
    <!-- 左侧头像（root 34 / reply 28） -->
    <view class="acs-comment-item__avatar">
      <PetAvatar
        :variant="comment.author.avatarVariant"
        :size="variant === 'reply' ? 28 : 34"
      />
    </view>

    <!-- 右侧正文 -->
    <view class="acs-comment-item__body">
      <text class="acs-comment-item__name">{{ comment.author.name }}</text>

      <!-- 正文：有 replyTo 时前缀着色的 @用户名 -->
      <view class="acs-comment-item__text">
        <text
          v-if="comment.replyTo"
          class="acs-comment-item__mention"
        >@{{ comment.replyTo.name }} </text>
        <text class="acs-comment-item__text-body">{{ comment.text }}</text>
      </view>

      <!-- 底部：时间 + 回复 + 右侧点赞 -->
      <view class="acs-comment-item__bottom">
        <text class="acs-comment-item__time">{{ comment.createdAt }}</text>
        <text
          class="acs-comment-item__reply-btn"
          @tap="onReply"
        >回复</text>
        <view
          class="acs-comment-item__like"
          @tap="onLike"
        >
          <HeartIcon
            :size="12"
            :color="comment.liked ? warnColor : inkMutedColor"
            :filled="comment.liked"
          />
          <text
            class="acs-comment-item__like-count"
            :class="{ 'acs-comment-item__like-count--active': comment.liked }"
          >{{ comment.likes }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import PetAvatar from '@/components/ui/PetAvatar.vue';
import HeartIcon from '@/components/ui/icons/HeartIcon.vue';
import { COLOR } from '@/styles/tokens';
import type { CommentItem as CommentItemData } from '@/types/comment';

interface Props {
  comment: CommentItemData;
  variant?: 'root' | 'reply';
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'root',
});

// gotcha #21：emit 名避 DOM 原生事件（不用 like / reply / tap）
const emit = defineEmits<{
  (e: 'comment-like', c: CommentItemData): void;
  (e: 'comment-reply', c: CommentItemData): void;
}>();

const warnColor = COLOR.warn;
const inkMutedColor = COLOR.inkMuted;

function onLike(): void {
  emit('comment-like', props.comment);
}

function onReply(): void {
  emit('comment-reply', props.comment);
}
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

// gotcha #19：本组件不加 :active 到根（防冒泡）；按钮子元素自己有反馈
.acs-comment-item {
  display: flex;
  gap: 20rpx; // 10 → 20rpx
  width: 100%;
  box-sizing: border-box;

  // reply 变体：头像稍小，整体字号/留白不变（缩进由父组件处理）
  &--reply {
    // TODO(design): 子回复样式待设计师细化
    gap: 16rpx;
  }
}

.acs-comment-item__avatar {
  flex-shrink: 0;
  line-height: 0;
}

.acs-comment-item__body {
  flex: 1;
  min-width: 0;
}

.acs-comment-item__name {
  display: block;
  font-size: $font-size-base; // 12px
  color: $color-ink-muted;
  margin-bottom: 6rpx; // 3 → 6rpx
  line-height: 1;
}

.acs-comment-item__text {
  display: block;
  font-size: $font-size-md; // 13px
  color: $color-ink;
  line-height: 1.5;
  margin-bottom: 12rpx; // 6 → 12rpx
  word-break: break-word;
}

.acs-comment-item__mention {
  color: $color-primary;
  font-weight: $font-weight-semibold;
}

.acs-comment-item__text-body {
  color: inherit;
}

.acs-comment-item__bottom {
  display: flex;
  align-items: center;
  gap: 28rpx; // 14 → 28rpx
  font-size: $font-size-sm; // 11px
  color: $color-ink-muted;
  line-height: 1;
}

.acs-comment-item__time {
  font-size: $font-size-sm;
  color: $color-ink-muted;
  line-height: 1;
}

.acs-comment-item__reply-btn {
  font-size: $font-size-sm;
  color: $color-ink-muted;
  line-height: 1;
  padding: 6rpx 4rpx;
  &:active { opacity: 0.6; }
}

.acs-comment-item__like {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 6rpx; // 3 → 6rpx
  padding: 6rpx 4rpx;
  &:active { opacity: 0.65; }
}

.acs-comment-item__like-count {
  font-size: $font-size-sm;
  color: $color-ink-muted;
  font-family: $font-family-num;
  line-height: 1;

  &--active {
    color: $color-warn;
  }
}
</style>
