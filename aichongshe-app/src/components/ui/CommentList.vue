<!--
  CommentList — 评论列表容器
  gotcha #4：不嵌 scroll-view，沿用 note-detail 主 scroll（避免双重滚动）

  emit 名避 DOM 原生（gotcha #21）：list-load-more / list-like / list-reply
-->
<template>
  <view class="acs-comment-list">
    <!-- 标题 -->
    <text class="acs-comment-list__title">共 {{ total }} 条评论</text>

    <!-- 空态 -->
    <view
      v-if="!loading && threads.length === 0"
      class="acs-comment-list__empty"
    >
      <text class="acs-comment-list__empty-text">暂无评论，抢个沙发吧～</text>
    </view>

    <!-- 列表 -->
    <view
      v-for="t in threads"
      :key="t.root.id"
      class="acs-comment-list__thread"
    >
      <CommentThread
        :thread="t"
        @thread-like="onThreadLike"
        @thread-reply="onThreadReply"
      />
    </view>

    <!-- 底部状态 -->
    <view v-if="threads.length > 0" class="acs-comment-list__status">
      <text v-if="loading" class="acs-comment-list__status-text">加载中...</text>
      <text v-else-if="!hasMore" class="acs-comment-list__status-text">到底了</text>
      <text v-else class="acs-comment-list__status-text">上拉加载更多</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import CommentThread from '@/components/ui/CommentThread.vue';
import type { CommentItem, CommentThread as CommentThreadData } from '@/types/comment';

interface Props {
  threads: CommentThreadData[];
  hasMore: boolean;
  loading: boolean;
  total: number;
}

defineProps<Props>();

// gotcha #21：emit 名避 DOM 原生（list-* 前缀）
// load-more 由父的主 scroll-view @scrolltolower 触发（不在本组件内 emit）
const emit = defineEmits<{
  (e: 'list-like', c: CommentItem): void;
  (e: 'list-reply', target: CommentItem, thread: CommentThreadData): void;
}>();

function onThreadLike(c: CommentItem): void {
  emit('list-like', c);
}
function onThreadReply(target: CommentItem, thread: CommentThreadData): void {
  emit('list-reply', target, thread);
}
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.acs-comment-list {
  width: 100%;
}

.acs-comment-list__title {
  display: block;
  font-size: $font-size-lg; // 14px
  font-weight: $font-weight-bold;
  color: $color-ink;
  margin-bottom: 24rpx; // 12 → 24rpx
  line-height: 1;
}

.acs-comment-list__empty {
  padding: 80rpx 24rpx;
  border-radius: $radius-md;
  background: $color-surface;
  display: flex;
  align-items: center;
  justify-content: center;
}

.acs-comment-list__empty-text {
  font-size: $font-size-base; // 12px
  color: $color-ink-muted;
}

.acs-comment-list__thread {
  margin-bottom: 32rpx; // 16 → 32rpx
  &:last-child { margin-bottom: 0; }
}

.acs-comment-list__status {
  margin-top: 24rpx;
  padding: 20rpx 0;
  display: flex;
  justify-content: center;
}

.acs-comment-list__status-text {
  font-size: $font-size-sm;
  color: $color-ink-muted;
  line-height: 1;
}
</style>
