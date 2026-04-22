<!--
  Home 首页（Phase 4 瀑布流接入）
  设计稿：design/screens-part2.jsx 行 15-148

  布局分层：
  - 顶部导航栏（菜单 + 三 Tab + 搜索）：flex-shrink:0，固定不滚
  - 话题胶囊横滑（TopicPillScroll）：同固定，不滚
  - swiper 占剩余：三个 swiper-item 分别接 WaterFall（WaterFall 自带 scroll-view）
  - 左右滑动 swiper = 切 tab；tap tab = 设置 swiper current

  数据：
  - 每个 feed 独立 state（list / hasMore / loading / page / loaded）
  - onShow 时加载当前 tab 第一页（gotchas #15 必用 onShow 不是 onActivated）
  - swiper 切 tab / tap tab 时若目标 feed 没加载过则 loadFirstPage
  - WaterFall @load-more 拉下一页
  - @like 走乐观更新，调 service 失败回滚
  - @tap 走 navigateSafe 跳 note-detail（gotchas #13）
-->
<template>
  <MainLayout active="home">
    <view class="home">
      <!-- 顶部：菜单 · 三 Tab · 搜索（固定，不跟着瀑布流滚动） -->
      <view class="home__top-bar">
        <view class="home__menu-btn" @tap="onMenuTap">
          <svg
            width="16"
            height="12"
            viewBox="0 0 16 12"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              d="M1 1.5h14M1 6h10M1 10.5h14"
              :stroke="inkColor"
              stroke-width="2"
              stroke-linecap="round"
              fill="none"
            />
          </svg>
        </view>

        <view class="home__tabs">
          <view
            v-for="t in tabs"
            :key="t.id"
            class="home__tab"
            :class="{ 'home__tab--active': activeTab === t.id }"
            @tap="onTabTap(t.id)"
          >
            <text class="home__tab-text">{{ t.label }}</text>
            <view v-if="activeTab === t.id" class="home__tab-underline" />
          </view>
        </view>

        <view class="home__search-btn" @tap="onSearchTap">
          <svg
            width="18"
            height="18"
            viewBox="0 0 24 24"
            xmlns="http://www.w3.org/2000/svg"
          >
            <circle
              cx="11"
              cy="11"
              r="7"
              :stroke="inkColor"
              stroke-width="2"
              fill="none"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
            <path
              d="M20 20l-3.5-3.5"
              :stroke="inkColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
              fill="none"
            />
          </svg>
        </view>
      </view>

      <!-- 话题胶囊横滑（固定在顶部 Tab 下方） -->
      <view class="home__topics">
        <TopicPillScroll :topics="topicChips" :active-index="0" />
      </view>

      <!-- swiper 左右切 tab；每个 item 内放 WaterFall（WaterFall 自带 scroll-view） -->
      <swiper
        class="home__swiper"
        :current="activeTabIndex"
        :duration="260"
        :disable-touch="false"
        @change="onSwiperChange"
      >
        <swiper-item
          v-for="t in tabs"
          :key="t.id"
          class="home__swiper-item"
        >
          <WaterFall
            :items="feedState[t.id].list"
            :has-more="feedState[t.id].hasMore"
            :loading="feedState[t.id].loading"
            @card-tap="onNoteTap"
            @card-like="onNoteLike"
            @load-more="onLoadMore(t.id)"
          />
        </swiper-item>
      </swiper>
    </view>
  </MainLayout>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import MainLayout from '@/components/ui/MainLayout.vue';
import TopicPillScroll from '@/components/ui/TopicPillScroll.vue';
import WaterFall from '@/components/ui/WaterFall.vue';
import { COLOR } from '@/styles/tokens';
import { getNoteList, likeNote } from '@/api/services/note';
import type { NoteFeedId, NoteListItem } from '@/types/note';
import { navigateSafe } from '@/utils/navigate';

const inkColor = COLOR.ink;

const tabs: Array<{ id: NoteFeedId; label: string }> = [
  { id: 'follow', label: '关注' },
  { id: 'discover', label: '发现' },
  { id: 'nearby', label: '附近' },
];

// 默认 active 'discover'（与设计稿 screens-part2.jsx 行 17 一致）
const activeTab = ref<NoteFeedId>('discover');

const topicChips = ['🔥 热榜', '🐕 狗狗', '🐈 猫咪', '🥘 爱宠食谱', '🏥 宠物医院', '✂️ 美容'];

const activeTabIndex = computed<number>(() =>
  tabs.findIndex((t) => t.id === activeTab.value),
);

// ── 每个 feed 的独立状态 ──────────────────────────────────
interface FeedState {
  list: NoteListItem[];
  hasMore: boolean;
  loading: boolean;
  page: number;
  /** 是否已经至少加载过一次（防止 onShow 重复拉第一页） */
  loaded: boolean;
}

function makeFeed(): FeedState {
  return { list: [], hasMore: true, loading: false, page: 0, loaded: false };
}

const feedState = reactive<Record<NoteFeedId, FeedState>>({
  follow: makeFeed(),
  discover: makeFeed(),
  nearby: makeFeed(),
});

const PAGE_SIZE = 10;

/** 加载指定 feed 下一页（含首屏） */
async function loadPage(feed: NoteFeedId): Promise<void> {
  const state = feedState[feed];
  if (state.loading || !state.hasMore) return;
  state.loading = true;
  try {
    const nextPage = state.page + 1;
    const res = await getNoteList({ feed, page: nextPage, size: PAGE_SIZE });
    state.list = [...state.list, ...res.list];
    state.hasMore = res.hasMore;
    state.page = nextPage;
    state.loaded = true;
  } catch (err) {
    uni.showToast({
      title: err instanceof Error ? err.message : '加载失败',
      icon: 'none',
    });
  } finally {
    state.loading = false;
  }
}

/** tab 首次进入拉第一页 */
function loadFeedIfNeeded(feed: NoteFeedId): void {
  if (!feedState[feed].loaded && !feedState[feed].loading) {
    void loadPage(feed);
  }
}

function onTabTap(id: NoteFeedId): void {
  if (activeTab.value !== id) activeTab.value = id;
  loadFeedIfNeeded(id);
}

function onSwiperChange(e: { detail: { current: number } }): void {
  const idx = e.detail.current;
  if (idx >= 0 && idx < tabs.length) {
    const nextId = tabs[idx]!.id;
    if (activeTab.value !== nextId) activeTab.value = nextId;
    loadFeedIfNeeded(nextId);
  }
}

function onLoadMore(feed: NoteFeedId): void {
  void loadPage(feed);
}

// ── 卡片交互 ──────────────────────────────────────────────
function onNoteTap(note: NoteListItem): void {
  void navigateSafe({
    url: `/pages/note-detail/note-detail?id=${encodeURIComponent(note.id)}`,
  });
}

/** 乐观更新：in-place mutation（Vue3 reactive 能追踪属性变化，
 *   且 WaterFall 的 cards 持有同一个对象引用，修改属性会直接反映到渲染。
 *   如用替换对象 `list[idx] = newObj`，WaterFall 的 cards[i].note 会指向旧对象，视图不更新。） */
async function onNoteLike(note: NoteListItem): Promise<void> {
  const feedId = activeTab.value;
  const list = feedState[feedId].list;
  const idx = list.findIndex((n) => n.id === note.id);
  if (idx === -1) return;
  const target = list[idx]!;
  // 记录旧状态以便失败回滚
  const prevLiked = target.liked;
  const prevLikes = target.likes;
  // in-place 切换
  target.liked = !prevLiked;
  target.likes = Math.max(0, prevLikes + (prevLiked ? -1 : 1));

  try {
    const res = await likeNote(note.id);
    // 用后端返回的权威值对齐
    target.liked = res.liked;
    target.likes = res.likes;
  } catch (err) {
    // 回滚
    target.liked = prevLiked;
    target.likes = prevLikes;
    uni.showToast({
      title: err instanceof Error ? err.message : '点赞失败',
      icon: 'none',
    });
  }
}

function onMenuTap(): void {
  uni.showToast({ title: '菜单待接入', icon: 'none' });
}

function onSearchTap(): void {
  uni.showToast({ title: '搜索待接入', icon: 'none' });
}

// ── 生命周期 ──────────────────────────────────────────────
// gotchas #15：TabBar 切回需刷新 / 首次可见时拉取 → onShow（不是 onActivated）
onShow(() => {
  loadFeedIfNeeded(activeTab.value);
});
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

// ⚠️ gotchas #4（scroll-view 必须显式高度） + #16（swiper 默认 150px，flex:1 + height:0）
// Phase 4 修正：TabBar 悬浮，内容区延伸到屏幕最底（高度 = 100vh），
// 末尾卡片滚到底时能越过 TabBar 露出；WaterFall 内部 scroll 末尾已加 TabBar 高度占位
.home {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 100%;
  background: $color-bg;
  font-family: $font-family-body;
  overflow: hidden;

  // ── 顶部栏 ─────────────────────────────────────────────
  &__top-bar {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    gap: 20rpx;
    padding: 12rpx 28rpx 20rpx;
    background: $color-bg;
    z-index: 2;
  }

  &__menu-btn {
    width: 68rpx;
    height: 68rpx;
    border-radius: 34rpx;
    background: $color-surface;
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4rpx 12rpx rgba(80, 50, 30, 0.06);
    &:active { opacity: 0.7; }
  }

  &__tabs {
    display: flex;
    gap: 32rpx;
    flex: 1;
  }

  &__tab {
    position: relative;
    padding: 8rpx 4rpx;
    line-height: $line-height-tight;
    display: flex;
    flex-direction: column;
    align-items: center;
    transition: all 0.2s;
  }

  &__tab-text {
    font-size: 30rpx;
    font-weight: $font-weight-medium;
    color: $color-ink-muted;
    transition: all 0.2s;
    line-height: $line-height-tight;
  }

  &__tab--active .home__tab-text {
    font-size: 36rpx;
    font-weight: $font-weight-bold;
    color: $color-ink;
  }

  &__tab-underline {
    position: absolute;
    bottom: -4rpx;
    left: 50%;
    transform: translateX(-50%);
    width: 32rpx;
    height: 6rpx;
    border-radius: 4rpx;
    background: $color-primary;
  }

  &__search-btn {
    width: 72rpx;
    height: 72rpx;
    border-radius: 36rpx;
    background: $color-surface;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4rpx 12rpx rgba(80, 50, 30, 0.06);
    flex-shrink: 0;
    &:active { opacity: 0.7; }
  }

  // ── 话题胶囊 ───────────────────────────────────────────
  &__topics {
    flex-shrink: 0;
    padding-bottom: 16rpx;
  }

  // ── Swiper 内容区（瀑布流占剩余）───────────────────────
  &__swiper {
    // uni-app swiper 默认 150px；flex:1 + height:0 撑开剩余空间（gotchas #16）
    flex: 1;
    width: 100%;
    height: 0;
  }

  &__swiper-item {
    width: 100%;
    height: 100%;
  }
}
</style>
