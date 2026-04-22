<!--
  note-detail — 笔记详情页（Phase 5a 主体）
  设计稿来源：design/screens-part2.jsx L159-320（NoteDetailScreen）

  布局：
    - NoteDetailHeader（absolute top，悬浮）
    - scroll-view 滚动区：NoteGallery + 标题/正文/话题/时间 + 分隔条 + 评论占位
    - NoteDetailActions（fixed bottom）

  注意事项（按陷阱编号）：
    - #4 scroll-view 显式高度（calc 计算 viewport - safe-area）
    - #5 fixed + transform：页面根节点不加 transform
    - #13 返回按钮：navigateBack，栈深 1 时 reLaunch 到 home
    - #14 条件编译（小程序端毛玻璃降级由子组件处理）
    - #16 swiper 显式高度（由 NoteGallery 内部处理）
    - #18 reactive 引用替换：点赞/收藏/关注统一 in-place mutation
    - #19 根节点不加 :active（防止冒泡）
    - #11 @tap + 按钮风格
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
            @tap="onTopicTap(t)"
          />
        </view>
        <text class="acs-detail__meta">
          编辑于 {{ detail.editedAt }} · 来自 {{ detail.fromCity }}
        </text>
      </view>

      <!-- 分隔条（设计稿 height 8 → 16rpx，背景 surfaceDim） -->
      <view class="acs-detail__divider" />

      <!-- 评论区占位 -->
      <!-- TODO(phase5b): 评论列表 + 回复 + 子楼 -->
      <view class="acs-detail__comments">
        <text class="acs-detail__comments-count">共 {{ detail.comments }} 条评论</text>
        <view class="acs-detail__comments-todo">
          <text class="acs-detail__comments-todo-text">评论功能 Phase 5b 实现</text>
        </view>
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
      @comment="onComment"
      @input-tap="onComment"
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
import {
  getNoteDetail,
  likeNote,
  toggleSave,
  toggleFollow,
} from '@/api/services/note';
import type { NoteDetail } from '@/types/note';

const detail = ref<NoteDetail | null>(null);
const loading = ref<boolean>(false);

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

function onComment(): void {
  // Phase 5b 替换为打开评论面板
  uni.showToast({ title: '评论功能 Phase 5b 实现', icon: 'none' });
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

.acs-detail__comments-count {
  display: block;
  font-size: $font-size-lg; // 14px
  font-weight: $font-weight-bold;
  color: $color-ink;
  margin-bottom: 24rpx; // 12 → 24rpx
}

.acs-detail__comments-todo {
  padding: 80rpx 24rpx;
  border-radius: $radius-md;
  background: $color-surface;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2rpx 6rpx rgba(80, 50, 30, 0.04);
}

.acs-detail__comments-todo-text {
  font-size: $font-size-base; // 12px
  color: $color-ink-muted;
}
</style>
