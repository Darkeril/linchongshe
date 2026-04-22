<!--
  CommentThread — 一级评论 + 回复折叠
  2 层拍平，无递归。

  gotcha 提醒：
    - #12 acs- 前缀
    - #18 父组件 in-place mutation，本组件靠 props 驱动
    - #21 emit 名：thread-like / thread-reply（避 DOM 原生）
-->
<template>
  <view class="acs-comment-thread">
    <!-- 一级评论 -->
    <CommentItem
      variant="root"
      :comment="thread.root"
      @comment-like="onRootLike"
      @comment-reply="onRootReply"
    />

    <!-- 回复区（缩进 68rpx） -->
    <view
      v-if="thread.replies.length > 0"
      class="acs-comment-thread__replies"
    >
      <view
        v-for="reply in visibleReplies"
        :key="reply.id"
        class="acs-comment-thread__reply-row"
      >
        <CommentItem
          variant="reply"
          :comment="reply"
          @comment-like="onReplyLike"
          @comment-reply="onReplyReply"
        />
      </view>

      <!-- 展开 N 条回复 -->
      <view
        v-if="!expanded && thread.replies.length > 2"
        class="acs-comment-thread__expand"
        @tap="onExpand"
      >
        <text class="acs-comment-thread__expand-text">展开 {{ thread.replies.length - 2 }} 条回复</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import CommentItem from '@/components/ui/CommentItem.vue';
import type { CommentItem as CommentItemData, CommentThread as CommentThreadData } from '@/types/comment';

interface Props {
  thread: CommentThreadData;
}

const props = defineProps<Props>();

// gotcha #21
const emit = defineEmits<{
  (e: 'thread-like', c: CommentItemData): void;
  (e: 'thread-reply', target: CommentItemData, thread: CommentThreadData): void;
}>();

const expanded = ref<boolean>(false);

const visibleReplies = computed<CommentItemData[]>(() => {
  const all = props.thread.replies;
  if (expanded.value) return all;
  if (all.length <= 2) return all;
  return all.slice(0, 2);
});

function onExpand(): void {
  expanded.value = true;
}

function onRootLike(c: CommentItemData): void {
  emit('thread-like', c);
}
function onRootReply(c: CommentItemData): void {
  emit('thread-reply', c, props.thread);
}
function onReplyLike(c: CommentItemData): void {
  emit('thread-like', c);
}
function onReplyReply(c: CommentItemData): void {
  emit('thread-reply', c, props.thread);
}
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.acs-comment-thread {
  width: 100%;
}

.acs-comment-thread__replies {
  // 缩进让出 root 头像 68rpx (34 avatar + 20 gap 对应 rpx: 68+40 ≈ 108；
  // 设计稿未明确，按 V1 取 68rpx 保证层级清晰)
  // TODO(design): 回复缩进量待设计师细化
  padding-left: 68rpx;
  margin-top: 20rpx; // 10 → 20rpx
}

.acs-comment-thread__reply-row {
  margin-bottom: 20rpx;
  &:last-child { margin-bottom: 0; }
}

.acs-comment-thread__expand {
  margin-top: 12rpx; // 6 → 12rpx
  padding: 4rpx 0;
  &:active { opacity: 0.6; }
}

.acs-comment-thread__expand-text {
  font-size: $font-size-sm; // 11px
  color: $color-primary;
  font-weight: $font-weight-semibold;
  line-height: 1;
}
</style>
