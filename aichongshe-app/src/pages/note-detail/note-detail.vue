<!--
  note-detail — 笔记详情页（Phase 5a 主体 + Phase 5b 评论接入）
  设计稿来源：design/screens-part2.jsx L159-320（NoteDetailScreen）

  布局：
    - NoteDetailHeader（absolute top，悬浮）
    - scroll-view 滚动区：NoteGallery + 标题/正文/话题/时间 + 分隔条 + CommentList
    - NoteDetailActions（fixed bottom）
    - CommentInputSheet（fixed 半屏，Phase 5b 新增）

  注意事项（按陷阱编号）：
    - #4 scroll-view 显式高度；CommentList 不嵌 scroll-view，共用本页主 scroll
    - #5 fixed + transform：页面根节点不加 transform（Sheet fixed 也依赖此）
    - #13 返回按钮：navigateBack，栈深 1 时 reLaunch 到 home
    - #14 条件编译（小程序端毛玻璃降级由子组件处理；Sheet 键盘高度监听亦条件编译）
    - #16 swiper 显式高度（由 NoteGallery 内部处理）
    - #18 reactive 引用替换：点赞/收藏/关注/评论点赞/发评论全部 in-place mutation
    - #19 根节点不加 :active（防止冒泡）
    - #11 @tap + 按钮风格
    - #21 emit 名避 DOM 原生：CommentList 用 list-like/list-reply；Sheet 用 sheet-*
-->
<template>
  <view class="acs-detail">
    <!-- 顶部悬浮导航 -->
    <NoteDetailHeader
      v-if="detail"
      :author="detail.author"
      :followed="detail.author.followed"
      @back="onBack"
      @follow="onFollow"
      @more="onMore"
    />

    <!-- 加载中占位 -->
    <view v-if="loading && !detail" class="acs-detail__loading">
      <text class="acs-detail__loading-text">加载中...</text>
    </view>

    <!-- 内容滚动区 -->
    <scroll-view
      v-if="detail"
      class="acs-detail__scroll"
      scroll-y
      :enhanced="true"
      :show-scrollbar="false"
      @scrolltolower="onListLoadMore"
    >
      <!-- 图文轮播（顶格） -->
      <NoteGallery :images="detail.images" @change="onGalleryChange" />

      <!-- 标题 / 正文 / 话题 / 编辑时间 -->
      <view class="acs-detail__body">
        <text class="acs-detail__title">{{ detail.title }}</text>
        <text class="acs-detail__content">{{ detail.content }}</text>
        <view class="acs-detail__topics">
          <TopicTag
            v-for="t in detail.topics"
            :key="t"
            :text="t"
            @topic-tap="onTopicTap(t)"
          />
        </view>
        <text class="acs-detail__meta">
          编辑于 {{ detail.editedAt }} · 来自 {{ detail.fromCity }}
        </text>
      </view>

      <!-- 分隔条（设计稿 height 8 → 16rpx，背景 surfaceDim） -->
      <view class="acs-detail__divider" />

      <!-- 评论区（Phase 5b 接入；gotcha #4 不嵌 scroll-view，共用主 scroll） -->
      <view class="acs-detail__comments">
        <CommentList
          :threads="commentThreads"
          :has-more="commentsHasMore"
          :loading="commentsLoading"
          :total="commentsTotal"
          @list-like="onListLike"
          @list-reply="onListReply"
        />
      </view>

      <!-- 底部留白，防止内容被固定操作栏遮挡 -->
      <view class="acs-detail__scroll-bottom-gap" />
    </scroll-view>

    <!-- 底部固定操作栏 -->
    <NoteDetailActions
      v-if="detail"
      :likes="detail.likes"
      :liked="detail.liked"
      :saves="detail.saves"
      :saved="detail.saved"
      :comments="detail.comments"
      @like="onLike"
      @save="onSave"
      @comment="onInputTap"
      @input-tap="onInputTap"
    />

    <!-- 评论发送面板 -->
    <CommentInputSheet
      :open="sheetOpen"
      :reply-target="sheetReplyTarget"
      @sheet-close="onSheetClose"
      @sheet-submit="onSheetSubmit"
    />
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import NoteDetailHeader from '@/components/ui/NoteDetailHeader.vue';
import NoteGallery from '@/components/ui/NoteGallery.vue';
import TopicTag from '@/components/ui/TopicTag.vue';
import NoteDetailActions from '@/components/ui/NoteDetailActions.vue';
import CommentList from '@/components/ui/CommentList.vue';
import CommentInputSheet from '@/components/ui/CommentInputSheet.vue';
import {
  getNoteDetail,
  likeNote,
  toggleSave,
  toggleFollow,
} from '@/api/services/note';
import {
  getComments,
  likeComment,
  postComment,
} from '@/api/services/comment';
import { useAuthGuard } from '@/composables/useAuthGuard';
import type { NoteDetail } from '@/types/note';
import type { CommentItem, CommentThread, CommentUser } from '@/types/comment';

const detail = ref<NoteDetail | null>(null);
const loading = ref<boolean>(false);

// ── 评论状态（Phase 5b） ────────────────────────────
const commentThreads = ref<CommentThread[]>([]);
const commentsHasMore = ref<boolean>(false);
const commentsLoading = ref<boolean>(false);
const commentsTotal = ref<number>(0);
const commentPage = ref<number>(1);
const COMMENT_PAGE_SIZE = 10;

const { requireAuth } = useAuthGuard();

// Sheet 状态
const sheetOpen = ref<boolean>(false);
const sheetReplyTarget = ref<CommentUser | null>(null);
// 回复目标 rootId：null 表示发一级评论
const sheetReplyRootId = ref<string | null>(null);

// gotchas #15：初次加载读 query 用 onLoad
onLoad((query) => {
  const q = query as { id?: string } | undefined;
  const id = q?.id ?? '';
  if (!id) {
    uni.showToast({ title: '笔记不存在', icon: 'none' });
    setTimeout(() => onBack(), 500);
    return;
  }
  void loadDetail(id);
});

async function loadDetail(id: string): Promise<void> {
  loading.value = true;
  try {
    detail.value = await getNoteDetail(id);
    // 详情拉到后串联加载首页评论
    await loadComments(1);
  } catch (err) {
    uni.showToast({
      title: err instanceof Error ? err.message : '加载失败',
      icon: 'none',
    });
    setTimeout(() => onBack(), 500);
  } finally {
    loading.value = false;
  }
}

// ── 评论加载 ────────────────────────────────────────

async function loadComments(page: number): Promise<void> {
  const d = detail.value;
  if (!d) return;
  if (commentsLoading.value) return;
  commentsLoading.value = true;
  try {
    const res = await getComments({ noteId: d.id, page, size: COMMENT_PAGE_SIZE });
    if (page === 1) {
      commentThreads.value = res.threads;
    } else {
      // append（gotcha #18：append 不替换已有对象引用）
      for (const t of res.threads) {
        commentThreads.value.push(t);
      }
    }
    commentsHasMore.value = res.hasMore;
    commentsTotal.value = res.total;
    commentPage.value = res.page;
  } catch (err) {
    uni.showToast({
      title: err instanceof Error ? err.message : '加载评论失败',
      icon: 'none',
    });
  } finally {
    commentsLoading.value = false;
  }
}

function onListLoadMore(): void {
  if (!commentsHasMore.value || commentsLoading.value) return;
  void loadComments(commentPage.value + 1);
}

// ── 评论点赞（in-place mutation，gotcha #18） ──────

function onListLike(c: CommentItem): void {
  requireAuth(() => {
    // 直接改 comment 对象属性（不替换引用）
    const prevLiked = c.liked;
    const prevLikes = c.likes;
    c.liked = !prevLiked;
    c.likes = Math.max(0, prevLikes + (prevLiked ? -1 : 1));
    likeComment(c.id)
      .then((res) => {
        c.liked = res.liked;
        c.likes = res.likes;
      })
      .catch((err: unknown) => {
        c.liked = prevLiked;
        c.likes = prevLikes;
        uni.showToast({
          title: err instanceof Error ? err.message : '点赞失败',
          icon: 'none',
        });
      });
  });
}

// ── 评论回复 ────────────────────────────────────────

function onListReply(target: CommentItem, thread: CommentThread): void {
  requireAuth(() => {
    sheetReplyTarget.value = target.author;
    sheetReplyRootId.value = thread.root.id;
    sheetOpen.value = true;
  });
}

// ── 发一级评论入口（底部 Actions 输入框/评论图标） ──

function onInputTap(): void {
  requireAuth(() => {
    sheetReplyTarget.value = null;
    sheetReplyRootId.value = null;
    sheetOpen.value = true;
  });
}

// ── Sheet 提交 ──────────────────────────────────────

function onSheetSubmit(text: string): void {
  const d = detail.value;
  if (!d) return;
  const rootId = sheetReplyRootId.value;
  const replyTarget = sheetReplyTarget.value;
  postComment({
    noteId: d.id,
    text,
    ...(rootId ? { rootId } : {}),
    ...(replyTarget ? { replyTo: replyTarget } : {}),
  })
    .then((created) => {
      // gotcha #18：in-place 更新数组（unshift / push，不替换 threads 引用）
      if (!rootId) {
        // 一级评论：插到顶部
        commentThreads.value.unshift({ root: created, replies: [] });
      } else {
        const thread = commentThreads.value.find((t) => t.root.id === rootId);
        if (thread) {
          thread.replies.push(created);
        }
      }
      commentsTotal.value += 1;
      // 同步详情页评论数徽章（gotcha #18 in-place）
      if (d) {
        d.comments += 1;
      }
      sheetOpen.value = false;
      sheetReplyTarget.value = null;
      sheetReplyRootId.value = null;
      uni.showToast({ title: '发送成功', icon: 'none' });
    })
    .catch((err: unknown) => {
      uni.showToast({
        title: err instanceof Error ? err.message : '发送失败',
        icon: 'none',
      });
    });
}

function onSheetClose(): void {
  sheetOpen.value = false;
  sheetReplyTarget.value = null;
  sheetReplyRootId.value = null;
}

// ── 顶部导航 ────────────────────────────────────────

// gotchas #13：返回按钮统一走 navigateBack；栈深 1 则兜底 reLaunch 到 home
function onBack(): void {
  if (getCurrentPages().length > 1) {
    uni.navigateBack();
  } else {
    uni.reLaunch({ url: '/pages/home/home' });
  }
}

// gotchas #18：关注状态 in-place mutation，避免子组件持有旧引用
async function onFollow(): Promise<void> {
  const d = detail.value;
  if (!d) return;
  const prev = d.author.followed;
  d.author.followed = !prev;
  try {
    const res = await toggleFollow(d.author.id);
    d.author.followed = res.followed;
  } catch (err) {
    d.author.followed = prev; // 回滚
    uni.showToast({
      title: err instanceof Error ? err.message : '操作失败',
      icon: 'none',
    });
  }
}

function onMore(): void {
  uni.showToast({ title: '更多菜单待接入', icon: 'none' });
}

// ── 图文轮播 ────────────────────────────────────────

function onGalleryChange(_index: number): void {
  // 目前不需要副作用；预留埋点 / 图片预加载
}

// ── 正文 ────────────────────────────────────────────

function onTopicTap(topic: string): void {
  uni.showToast({ title: `话题：${topic}（待接入）`, icon: 'none' });
}

// ── 底部操作 ────────────────────────────────────────

// gotchas #18：in-place mutation 乐观更新
async function onLike(): Promise<void> {
  const d = detail.value;
  if (!d) return;
  const prevLiked = d.liked;
  const prevLikes = d.likes;
  d.liked = !prevLiked;
  d.likes = Math.max(0, prevLikes + (prevLiked ? -1 : 1));
  try {
    const res = await likeNote(d.id);
    d.liked = res.liked;
    d.likes = res.likes;
  } catch (err) {
    d.liked = prevLiked;
    d.likes = prevLikes;
    uni.showToast({
      title: err instanceof Error ? err.message : '点赞失败',
      icon: 'none',
    });
  }
}

async function onSave(): Promise<void> {
  const d = detail.value;
  if (!d) return;
  const prevSaved = d.saved;
  const prevSaves = d.saves;
  d.saved = !prevSaved;
  d.saves = Math.max(0, prevSaves + (prevSaved ? -1 : 1));
  try {
    const res = await toggleSave(d.id);
    d.saved = res.saved;
    d.saves = res.saves;
  } catch (err) {
    d.saved = prevSaved;
    d.saves = prevSaves;
    uni.showToast({
      title: err instanceof Error ? err.message : '收藏失败',
      icon: 'none',
    });
  }
}

</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

// gotchas #19：页面根容器不加 :active，避免子元素 :active 冒泡
.acs-detail {
  position: relative;
  width: 100%;
  height: 100vh;
  background: $color-bg;
  font-family: $font-family-body;
  overflow: hidden;
}

// 加载占位（简单文字，完整骨架屏 Phase 后续优化）
.acs-detail__loading {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}
.acs-detail__loading-text {
  font-size: $font-size-lg;
  color: $color-ink-muted;
}

// gotchas #4：scroll-view 必须显式高度
// 整页 100vh；底部操作栏 124rpx + safe-area 已 fixed 覆盖，这里给足内容滚动空间
.acs-detail__scroll {
  width: 100%;
  height: 100vh;
  box-sizing: border-box;
}

// 防内容被底部固定栏遮挡的占位（高度 = actions 栏高度 + 额外余量）
.acs-detail__scroll-bottom-gap {
  height: 160rpx;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

// ── 正文区 ────────────────────────────────────────────
.acs-detail__body {
  // 设计稿 padding 16 18 → 32rpx 36rpx
  padding: 32rpx 36rpx 0;
}

.acs-detail__title {
  display: block;
  font-size: 38rpx; // 19 → 38rpx
  font-weight: $font-weight-bold;
  color: $color-ink;
  line-height: $line-height-normal;
  margin-bottom: 20rpx; // 10 → 20rpx
}

.acs-detail__content {
  display: block;
  font-size: $font-size-lg; // 14px
  color: $color-ink-soft;
  // 设计稿 1.75
  line-height: 1.75;
  margin-bottom: 24rpx; // 12 → 24rpx
  // 允许换行
  white-space: pre-wrap;
  word-break: break-word;
}

.acs-detail__topics {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx; // 6 → 12rpx
  margin-bottom: 28rpx; // 14 → 28rpx
}

.acs-detail__meta {
  display: block;
  font-size: $font-size-sm; // 11px
  color: $color-ink-muted;
  line-height: 1.4;
}

// ── 分隔条 ────────────────────────────────────────────
.acs-detail__divider {
  // 设计稿 height 8 → 16rpx，margin 4 0 → 8rpx 0
  height: 16rpx;
  margin: 8rpx 0;
  background: $color-surface-dim;
}

// ── 评论区占位 ────────────────────────────────────────
.acs-detail__comments {
  // 设计稿 padding 14 18 20 → 28rpx 36rpx 40rpx
  padding: 28rpx 36rpx 40rpx;
}

// Phase 5b：评论占位 CSS（__comments-count / __comments-todo / __comments-todo-text）
// 已被 CommentList 组件内置样式替换，移除孤儿样式
</style>
