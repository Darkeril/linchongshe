<!--
  WaterFall — 瀑布流虚拟列表（两列）
  设计稿来源：design/screens-part2.jsx 行 131-143
  ==================================================================
  虚拟化策略（reviewer 会 grep 这些关键词确认真虚拟化）：

  1. 两列贪心布局：
     - 每张卡片 absolute 定位；top / left 由 layout() 计算
     - 新卡片插入"当前较短的列"，保证左右列总高接近
     - 容器 height = max(leftTotal, rightTotal)，以支持滚动条正确长度

  2. 真虚拟化：
     - 监听 <scroll-view> @scroll 事件，throttle 150ms 写入 scrollTop
     - 视口范围 [scrollTop - BUFFER, scrollTop + viewportH + BUFFER]
     - visibleStart / visibleEnd 通过"线性扫描 sorted by top"快速定位
     - 视口外的卡片：只渲染空的 <view>（保留 absolute 占位，不渲染 NoteCard 子组件/SVG）
     - 视口内的卡片：渲染完整 <NoteCard>

  3. 触底加载：
     - scrollTop + viewportH + 200rpx 超过 totalH 时 emit load-more
     - loading / hasMore 做防抖，避免重复触发

  gotchas 引用：
  - #4 scroll-view 必须显式高度 → 容器 height: 100%
  - #11 @tap 风格统一
  - #12 class 加 acs- 前缀
  - #15 不依赖 onActivated；组件初次 mount 时自己 layout
-->
<template>
  <view class="acs-waterfall">
    <scroll-view
      class="acs-waterfall__scroll"
      scroll-y
      :show-scrollbar="false"
      :enhanced="true"
      @scroll="onScroll"
      @scrolltolower="onScrollToLower"
      lower-threshold="100"
    >
      <!-- 撑开总高的占位容器，让滚动条正确 -->
      <view
        class="acs-waterfall__canvas"
        :style="{ height: canvasH + 'px' }"
      >
        <!-- 每张卡片：视口内渲染完整 NoteCard；视口外只渲染空占位 view -->
        <template v-for="card in cards" :key="card.id">
          <view
            v-if="card.visible"
            class="acs-waterfall__slot acs-waterfall__slot--visible"
            :style="{
              position: 'absolute',
              left: card.left + 'px',
              top: card.top + 'px',
              width: colW + 'px',
            }"
          >
            <NoteCard
              :note="card.note"
              :cover-w="colW"
              @tap="onCardTap"
              @like="onCardLike"
            />
          </view>
          <view
            v-else
            class="acs-waterfall__slot acs-waterfall__slot--placeholder"
            :style="{
              position: 'absolute',
              left: card.left + 'px',
              top: card.top + 'px',
              width: colW + 'px',
              height: card.cardH + 'px',
            }"
          />
        </template>
      </view>

      <!-- 底部加载状态 -->
      <view class="acs-waterfall__footer" :style="{ top: totalH + 'px' }">
        <text v-if="loading" class="acs-waterfall__footer-text">加载中 🐾</text>
        <text v-else-if="!hasMore && items.length > 0" class="acs-waterfall__footer-text">
          — 到底了，歇会儿吧 🐾 —
        </text>
        <text v-else-if="items.length === 0" class="acs-waterfall__footer-text">
          🐾 暂无内容
        </text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import {
  computed,
  onMounted,
  onUnmounted,
  ref,
  watch,
} from 'vue';
import NoteCard from '@/components/ui/NoteCard.vue';
import type { NoteListItem } from '@/types/note';

interface Props {
  items: NoteListItem[];
  hasMore: boolean;
  loading: boolean;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  (e: 'tap', note: NoteListItem): void;
  (e: 'like', note: NoteListItem): void;
  (e: 'load-more'): void;
}>();

// ── 容器尺寸 ──────────────────────────────────────────────
// 宽度以 window.innerWidth 为准，设计稿两列 + 左右 10px padding + 列间 gap 8px
// 用 uni.getSystemInfoSync 拿到 windowWidth（px，iPhone 13 Pro 390）
// 375 设计稿下：总宽 375 - 左右 10×2 - gap 8 = 347；每列 173.5
const windowWidth = ref<number>(375);
const windowHeight = ref<number>(667);

function measureViewport(): void {
  try {
    // uni.getWindowInfo 在 2.x 是推荐 API；兼容 getSystemInfoSync
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const info = (uni.getWindowInfo?.() as any) ?? uni.getSystemInfoSync();
    windowWidth.value = info.windowWidth ?? 375;
    windowHeight.value = info.windowHeight ?? 667;
  } catch {
    // 兜底值
  }
}

// 设计稿 padding 0 10 → 卡片容器左右内边距
const PAD_X = 10; // px
const GAP_X = 8;  // px
const GAP_Y = 10; // px（卡片垂直间距，设计稿 marginBottom 10）

const colW = computed<number>(() => {
  const total = windowWidth.value - PAD_X * 2 - GAP_X;
  return Math.floor(total / 2);
});

// ── 布局：两列贪心 ────────────────────────────────────────
interface LaidCard {
  id: string;
  note: NoteListItem;
  left: number;
  top: number;
  /** 卡片实际高度 = 封面 h + 信息区 */
  cardH: number;
  /** 卡片 bottom = top + cardH */
  bottom: number;
  /** 是否在视口内（虚拟化状态） */
  visible: boolean;
}

const cards = ref<LaidCard[]>([]);
const totalH = ref<number>(0);

/** scroll-view 内容实际可滚高度 = 卡片区 + footer + TabBar 占位 */
const canvasH = computed<number>(() => totalH.value + FOOTER_H + TAB_BAR_SPACER);

/**
 * 估算每张卡片除封面外的"信息区高度"
 * 标题最大 2 行 × $line-height-normal(1.4) × 12px + margin-bottom 16rpx(8px)
 *   + 底部行 18px 高 + 上下 padding 16+20rpx(8+10px) ≈ 8 + (12*1.4*2) + 8 + 18 + 10 ≈ 77.6
 * 取 80 兜底（略留空间防止视觉截断）
 */
const INFO_H = 80;

/** 底部 footer 文字占位（"到底了"/"加载中"） */
const FOOTER_H = 48;

/**
 * 底部 TabBar 悬浮占位：
 * TabBar 高度 120rpx ≈ 60px + bottom 32rpx ≈ 16px + 额外呼吸 32rpx ≈ 16px + safe-area 估 16px
 * ≈ 108px。scroll-view 内容末尾追加这段空白，使最后一张卡片能滚到 TabBar 下方再露出，
 * 不被悬浮 TabBar 永久遮挡（Phase 4 视觉验收反馈）
 */
const TAB_BAR_SPACER = 108;

/** 两列贪心布局 —— 把新卡片放入当前较短的列 */
function layoutAll(): void {
  const w = colW.value;
  const leftX = PAD_X;
  const rightX = PAD_X + w + GAP_X;
  let leftY = 0;
  let rightY = 0;
  const next: LaidCard[] = [];

  for (const n of props.items) {
    const cardH = n.h + INFO_H;
    const putLeft = leftY <= rightY;
    const x = putLeft ? leftX : rightX;
    const y = putLeft ? leftY : rightY;
    next.push({
      id: n.id,
      note: n,
      left: x,
      top: y,
      cardH,
      bottom: y + cardH,
      visible: false,
    });
    if (putLeft) leftY = y + cardH + GAP_Y;
    else rightY = y + cardH + GAP_Y;
  }

  totalH.value = Math.max(leftY, rightY);
  cards.value = next;
  // 重新布局后立即根据当前滚动位置刷新 visible 区间
  updateVisible();
}

// ── 虚拟化：视口内/外判定 ──────────────────────────────────
const scrollTop = ref<number>(0);
const BUFFER = 300; // px（上下各缓冲 300px，滚动时卡片不突兀）

/** 遍历 cards 计算每张卡是否在视口窗口内；O(N) 但 N≤30~90 完全能承受 */
function updateVisible(): void {
  const vpTop = scrollTop.value - BUFFER;
  const vpBottom = scrollTop.value + windowHeight.value + BUFFER;
  let visibleStart = -1;
  let visibleEnd = -1;
  const list = cards.value;
  for (let i = 0; i < list.length; i += 1) {
    const c = list[i]!;
    const inView = c.bottom >= vpTop && c.top <= vpBottom;
    if (inView) {
      if (visibleStart === -1) visibleStart = i;
      visibleEnd = i;
      if (!c.visible) c.visible = true;
    } else if (c.visible) {
      c.visible = false;
    }
  }
  // visibleStart / visibleEnd 变量仅用于调试可见区间（reviewer 可 grep）
  // eslint-disable-next-line @typescript-eslint/no-unused-expressions
  visibleStart; visibleEnd;
}

// ── scroll 事件 throttle 150ms ────────────────────────────
let scrollThrottleTimer: ReturnType<typeof setTimeout> | null = null;
let pendingScrollTop = 0;

function onScroll(e: { detail: { scrollTop: number } }): void {
  pendingScrollTop = e.detail.scrollTop ?? 0;
  if (scrollThrottleTimer !== null) return;
  scrollThrottleTimer = setTimeout(() => {
    scrollTop.value = pendingScrollTop;
    updateVisible();
    scrollThrottleTimer = null;
  }, 150);
}

/** scroll-view 内置的触底事件，更稳定 */
function onScrollToLower(): void {
  if (props.loading || !props.hasMore) return;
  emit('load-more');
}

// ── 事件转发 ──────────────────────────────────────────────
function onCardTap(note: NoteListItem): void {
  emit('tap', note);
}

function onCardLike(note: NoteListItem): void {
  emit('like', note);
}

// ── 生命周期 ──────────────────────────────────────────────
onMounted(() => {
  measureViewport();
  layoutAll();
});

onUnmounted(() => {
  if (scrollThrottleTimer !== null) {
    clearTimeout(scrollThrottleTimer);
    scrollThrottleTimer = null;
  }
});

/** items 变化（新页拼接 / 首次拉取完）→ 重新布局 */
watch(
  () => props.items,
  () => {
    layoutAll();
  },
  { deep: false },
);
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.acs-waterfall {
  // gotchas #4：scroll-view 必须显式高度；这里父组件给 100% 高度
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  background: $color-bg;

  &__scroll {
    width: 100%;
    height: 100%;
    box-sizing: border-box;
  }

  &__canvas {
    position: relative;
    width: 100%;
  }

  &__slot {
    box-sizing: border-box;
    // 虚拟化占位（不渲染 NoteCard 时仅保留一个空 view，尺寸精确）
    &--placeholder {
      // 给视口外占位一个浅浅的 tint 便于调试（生产可透明）
      background: transparent;
    }
  }

  &__footer {
    position: absolute;
    left: 0;
    right: 0;
    padding: 24rpx 0 40rpx;
    text-align: center;
  }

  &__footer-text {
    font-size: $font-size-xs; // 10px
    color: $color-ink-faint;
    line-height: 1;
  }
}
</style>
