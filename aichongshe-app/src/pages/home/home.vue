<!--
  Home 首页（Tab 壳 + 内部三 Tab 骨架）
  设计稿：design/screens-part2.jsx 行 15-148
  Phase 3：顶部导航固定 + swiper 左右滑动切 tab + 每个 tab 独立上下滚动
  Phase 4 待做：瀑布流 WaterFall + NoteCard + TopicPill 替换占位

  布局分层（用户反馈修正）：
  - 顶部导航栏（菜单 + 三 Tab + 搜索）：fixed 在 home 顶部，永远可见
  - swiper 占剩余高度：三个 swiper-item 对应三个 tab
  - 每个 swiper-item 内 scroll-view 独立 scroll-y
  - 左右滑动 swiper = 切 tab；tap tab = 设置 swiper current
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

      <!-- Tab 内容区：swiper 左右滑动切 tab + 每个 item 内部独立上下滚 -->
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
          <scroll-view
            scroll-y
            class="home__scroll"
            :show-scrollbar="false"
          >
            <!-- 话题胶囊（当前每 tab 都有，Phase 4 可根据 tab 分别加载） -->
            <view class="home__topics">
              <view
                v-for="(c, i) in topicChips"
                :key="c"
                class="home__topic"
                :class="{ 'home__topic--hot': i === 0 }"
              >
                <text class="home__topic-text">{{ c }}</text>
              </view>
            </view>

            <!-- 瀑布流占位 -->
            <view class="home__feed-placeholder">
              <text class="home__placeholder-text">{{ t.label }} Feed</text>
              <text class="home__placeholder-sub">Phase 4 实现 WaterFall + NoteCard</text>
            </view>

            <!-- 额外占位内容，确认上下滚动生效 -->
            <view
              v-for="i in 12"
              :key="`row-${t.id}-${i}`"
              class="home__stub-row"
            >
              <text class="home__stub-text">{{ t.label }} · 占位行 {{ i }}</text>
            </view>
          </scroll-view>
        </swiper-item>
      </swiper>
    </view>
  </MainLayout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import MainLayout from '@/components/ui/MainLayout.vue';
import { COLOR } from '@/styles/tokens';

const inkColor = COLOR.ink;

type HomeTabId = 'follow' | 'discover' | 'nearby';

const tabs: Array<{ id: HomeTabId; label: string }> = [
  { id: 'follow', label: '关注' },
  { id: 'discover', label: '发现' },
  { id: 'nearby', label: '附近' },
];

// 默认 active 'discover'（与设计稿 screens-part2.jsx 行 17 一致）
const activeTab = ref<HomeTabId>('discover');

// 话题胶囊占位；Phase 4 从 API 拉真实话题
const topicChips = ['🔥 热榜', '🐕 狗狗', '🐈 猫咪', '🥘 爱宠食谱', '🏥 宠物医院', '✂️ 美容'];

/** swiper 的 current 来自 activeTab 在 tabs 中的索引 */
const activeTabIndex = computed<number>(() =>
  tabs.findIndex(t => t.id === activeTab.value),
);

function onTabTap(id: HomeTabId): void {
  if (activeTab.value !== id) activeTab.value = id;
}

/** swiper 手势滑动 / current 变化回调 */
function onSwiperChange(e: { detail: { current: number } }): void {
  const idx = e.detail.current;
  if (idx >= 0 && idx < tabs.length) {
    const nextId = tabs[idx].id;
    if (activeTab.value !== nextId) activeTab.value = nextId;
  }
}

function onMenuTap(): void {
  uni.showToast({ title: '菜单待接入', icon: 'none' });
}

function onSearchTap(): void {
  uni.showToast({ title: '搜索待接入', icon: 'none' });
}
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

// ⚠️ gotchas #4（scroll-view 必须显式高度） + uni-app swiper 默认 150px
// 整体用 flex column；顶部固定，swiper 占剩余；swiper-item 内 scroll-view 独立滚动
.home {
  display: flex;
  flex-direction: column;
  // MainLayout 的 content 已经留了 padding-bottom 216rpx + safe-area
  // 这里 home 高度 = content 可用高度
  height: calc(100vh - 216rpx - env(safe-area-inset-bottom));
  width: 100%;
  background: $color-bg;
  font-family: $font-family-body;
  overflow: hidden;

  // ── 顶部栏（固定，不随内容滚动） ────────────────────────
  // 设计稿：padding 6 14 10，gap 10（1px→2rpx）
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
    font-size: 30rpx; // 15px inactive
    font-weight: $font-weight-medium;
    color: $color-ink-muted;
    transition: all 0.2s;
    line-height: $line-height-tight;
  }

  &__tab--active .home__tab-text {
    font-size: 36rpx; // 18px active
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

  // ── Swiper 内容区（独立滚动） ──────────────────────────
  &__swiper {
    // uni-app swiper 默认 150px；flex:1 配合 height:0 兜底撑开剩余空间
    flex: 1;
    width: 100%;
    height: 0;
  }

  &__swiper-item {
    width: 100%;
    height: 100%;
  }

  // ── 每个 tab 的独立滚动区 ──────────────────────────────
  // gotchas #4：scroll-view 必须显式高度 + 禁止嵌套上下 scroll
  &__scroll {
    width: 100%;
    height: 100%;
    box-sizing: border-box;
  }

  // ── 话题胶囊（后续改 scroll-view 横滑 + TopicPill 组件） ─
  &__topics {
    display: flex;
    gap: 16rpx;
    padding: 0 32rpx 20rpx;
    overflow-x: hidden;
    white-space: nowrap;
  }

  &__topic {
    padding: 12rpx 24rpx;
    border-radius: 28rpx;
    background: $color-surface;
    box-shadow: 0 2rpx 6rpx rgba(80, 50, 30, 0.04);
    flex-shrink: 0;
  }

  &__topic-text {
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    color: $color-ink-soft;
    line-height: 1;
  }

  &__topic--hot {
    background: $color-primary;
    box-shadow: 0 8rpx 20rpx rgba($color-primary, 0.27);

    .home__topic-text { color: #ffffff; }
  }

  // ── 瀑布流占位（Phase 4 替换为真实 WaterFall 组件） ─────
  &__feed-placeholder {
    margin: 24rpx 28rpx;
    padding: 64rpx 32rpx;
    background: $color-surface;
    border-radius: $radius-lg;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16rpx;
    box-shadow: $shadow-sm;
  }

  &__placeholder-text {
    font-size: $font-size-xl;
    font-weight: $font-weight-bold;
    color: $color-ink;
  }

  &__placeholder-sub {
    font-size: $font-size-sm;
    color: $color-ink-muted;
  }

  // ── 占位行（验证上下滚动生效，Phase 4 会替换为瀑布流卡片） ─
  &__stub-row {
    margin: 16rpx 28rpx;
    padding: 40rpx 32rpx;
    background: $color-surface;
    border-radius: $radius-md;
    box-shadow: $shadow-sm;
  }

  &__stub-text {
    font-size: $font-size-md;
    color: $color-ink-soft;
  }
}
</style>
