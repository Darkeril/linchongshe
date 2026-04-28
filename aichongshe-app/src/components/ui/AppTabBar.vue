<!--
  AppTabBar — 全局底部 TabBar（爱宠社液态玻璃 + 悬浮发布按钮）
  设计稿来源：design/screens-part1.jsx 行 30-89 AppTabBar
  组件分层：ui/（uview-plus 无此控件，属设计稿特色组件）

  核心特性：
  - position: fixed，悬浮于底部（left/right 14、bottom 16），高 60、圆角 radius×1.4
  - 毛玻璃背景：rgba(255,255,255,0.72) + backdrop-filter blur(24px) saturate(180%)
  - 5 个 Tab：home / market / publish(悬浮按钮) / msg / me
  - publish 按钮 48×48、marginTop -18（向上突出）、135° 渐变 primary→primaryInk
  - active：图标 filled=true + label weight 700 + color=primary
  - inactive：图标 outline + label weight 500 + color=inkMuted
  - badge：warn 色圆角小徽章，top -4 right -8

  跨端兼容：
  - backdrop-filter 在微信/抖音小程序不支持，条件编译降级为半透明纯色（gotchas #14）
  - 全部类名加 acs- 前缀防止 uview-plus 全局污染（gotchas #12）
  - 组件 scoped 隔离
  - 事件全部 @tap（与移动端触摸语义对齐，gotchas #11）
  - 跳转走 switchTab / navigateSafe 封装（gotchas #13）
  - fixed 元素必须挂在页面根层，父链禁止 transform（gotchas #5）
-->
<template>
  <view class="acs-tabbar">
    <!-- 5 个 Tab；发布按钮居中悬浮 -->
    <view
      v-for="item in items"
      :key="item.id"
      class="acs-tabbar__item"
      :class="{ 'acs-tabbar__item--publish': item.id === 'publish' }"
      @tap="onItemTap(item.id)"
    >
      <!-- 发布按钮（特殊：悬浮向上突出） -->
      <template v-if="item.id === 'publish'">
        <view class="acs-tabbar__publish">
          <PlusIcon :size="24" color="#FFFFFF" />
        </view>
      </template>

      <!-- 普通 Tab：图标 + label，支持 badge -->
      <template v-else>
        <view class="acs-tabbar__icon-wrap">
          <HomeIcon
            v-if="item.id === 'home'"
            :size="22"
            :color="active === item.id ? primaryColor : inkMutedColor"
            :filled="active === item.id"
          />
          <BagIcon
            v-else-if="item.id === 'market'"
            :size="22"
            :color="active === item.id ? primaryColor : inkMutedColor"
            :filled="active === item.id"
          />
          <MsgIcon
            v-else-if="item.id === 'msg'"
            :size="22"
            :color="active === item.id ? primaryColor : inkMutedColor"
            :filled="active === item.id"
          />
          <MeIcon
            v-else-if="item.id === 'me'"
            :size="22"
            :color="active === item.id ? primaryColor : inkMutedColor"
            :filled="active === item.id"
          />
          <!-- badge：只在 msg 且传入 >0 时显示 -->
          <view
            v-if="item.id === 'msg' && badges.msg && badges.msg > 0"
            class="acs-tabbar__badge"
          >
            <text class="acs-tabbar__badge-text">{{ badges.msg > 99 ? '99+' : badges.msg }}</text>
          </view>
        </view>
        <text
          class="acs-tabbar__label"
          :class="{ 'acs-tabbar__label--active': active === item.id }"
        >
          {{ item.label }}
        </text>
      </template>
    </view>
  </view>
</template>

<script setup lang="ts">
import { COLOR } from '@/styles/tokens';
import { navigateSafe, switchTab } from '@/utils/navigate';
import { useAuthGuard } from '@/composables/useAuthGuard';
import HomeIcon from '@/components/ui/icons/HomeIcon.vue';
import BagIcon from '@/components/ui/icons/BagIcon.vue';
import PlusIcon from '@/components/ui/icons/PlusIcon.vue';
import MsgIcon from '@/components/ui/icons/MsgIcon.vue';
import MeIcon from '@/components/ui/icons/MeIcon.vue';

/** 5 个 tab 的逻辑 id（publish 不是 tab，仅占位渲染发布按钮） */
export type TabId = 'home' | 'market' | 'publish' | 'msg' | 'me';

interface Props {
  /** 当前 active 的 tab（publish 永远不 active） */
  active: TabId;
  /** badge 数量，按 tab id 为 key；目前仅 msg 显示 */
  badges?: { msg?: number };
}

const props = withDefaults(defineProps<Props>(), {
  badges: () => ({}),
});

const { requireAuth } = useAuthGuard();
const primaryColor = COLOR.primary;
const inkMutedColor = COLOR.inkMuted;

const items: Array<{ id: TabId; label: string }> = [
  { id: 'home', label: '首页' },
  { id: 'market', label: '市集' },
  { id: 'publish', label: '发布' },
  { id: 'msg', label: '消息' },
  { id: 'me', label: '我' },
];

/**
 * Tab 路由映射（pagePath；前面带斜杠给 switchTab 用）。
 * publish 不在 tabBar list 中，用 navigateSafe 走普通 push。
 */
const TAB_ROUTES: Record<Exclude<TabId, 'publish'>, string> = {
  home: '/pages/home/home',
  market: '/pages/market/market',
  msg: '/pages/message/message',
  me: '/pages/mine/mine',
};

async function onItemTap(id: TabId): Promise<void> {
  if (id === 'publish') {
    // 发布按钮：先过登录拦截，再 navigate（独立路由）
    try {
      await requireAuth(() => navigateSafe({ url: '/pages/publish/publish' }));
    } catch (err) {
      const code = (err as { code?: string })?.code;
      if (code === 'AUTH_CANCELLED' || code === 'AUTH_REPLACED') return;
      console.error('[AppTabBar] publish navigate failed:', err);
    }
    return;
  }
  // 点击当前 tab 不做跳转（小程序对相同 tab 的 switchTab 也是 no-op）
  if (id === props.active) return;
  try {
    await switchTab(TAB_ROUTES[id]);
  } catch (err) {
    console.error('[AppTabBar] switchTab failed:', err);
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

// ── 容器：悬浮毛玻璃 ────────────────────────────────────────
// 设计稿：left/right 14、bottom 16、height 60、borderRadius radius*1.4(≈22)
// 映射：1 design-px = 2rpx（与 splash/login 一致）
.acs-tabbar {
  position: fixed;
  left: 28rpx;
  right: 28rpx;
  // bottom 16 + safe-area，iPhone 刘海屏也能踩稳
  bottom: calc(32rpx + env(safe-area-inset-bottom));
  height: 120rpx; // 60px → 120rpx
  // radius md(16)*1.4 ≈ 22 → 44rpx；与 $radius-lg 一致
  border-radius: $radius-lg * 2;
  // 毛玻璃底：白色 0.72 alpha
  background: rgba(255, 255, 255, 0.72);
  // H5 / App-vue / iOS 小程序支持 backdrop-filter
  backdrop-filter: blur(24px) saturate(180%);
  -webkit-backdrop-filter: blur(24px) saturate(180%);
  // 阴影：primary 主色外扩 + 轻底阴影 + inset 高光
  box-shadow:
    0 16rpx 56rpx rgba($color-primary, 0.13),
    0 4rpx 12rpx rgba(0, 0, 0, 0.05),
    inset 0 2rpx 0 rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 0 16rpx;
  z-index: 30;
  // 允许发布按钮向上突出
  overflow: visible;

  /* #ifdef MP-WEIXIN || MP-TOUTIAO */
  // 小程序端 backdrop-filter 支持度低，降级为半透明纯色
  background: rgba(255, 255, 255, 0.92);
  /* #endif */
}

.acs-tabbar__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4rpx;
  position: relative;
  // 点击整个区域可响应
  height: 100%;

  // 按下轻微反馈（view 不支持 hover-class，用 :active）
  &:active {
    opacity: 0.7;
  }
}

// 发布按钮占位 item：宽度维持 flex:1 但内部是悬浮按钮
.acs-tabbar__item--publish {
  // 发布项不做按压透明度反馈（独立按钮自己做）
  &:active {
    opacity: 1;
  }
}

// ── 发布按钮（悬浮 48×48） ──────────────────────────────────
.acs-tabbar__publish {
  width: 96rpx;   // 48px → 96rpx
  height: 96rpx;
  // 向上突出 18px → -36rpx
  margin-top: -36rpx;
  border-radius: 32rpx; // 16px → 32rpx
  background: linear-gradient(135deg, $color-primary, $color-primary-ink);
  box-shadow:
    0 20rpx 44rpx rgba($color-primary, 0.33),
    inset 0 2rpx 0 rgba(255, 255, 255, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.12s ease, box-shadow 0.12s ease;

  &:active {
    transform: scale(0.94);
    box-shadow:
      0 12rpx 28rpx rgba($color-primary, 0.4),
      inset 0 2rpx 0 rgba(255, 255, 255, 0.4);
  }
}

// ── 普通 Tab 图标包装（用来挂 badge） ──────────────────────
.acs-tabbar__icon-wrap {
  position: relative;
  line-height: 0;
}

// ── Label ──────────────────────────────────────────────────
.acs-tabbar__label {
  font-size: 20rpx; // 10px → 20rpx
  color: $color-ink-muted;
  font-weight: $font-weight-medium; // 500
  line-height: $line-height-tight;
  letter-spacing: 0;
}

.acs-tabbar__label--active {
  color: $color-primary;
  font-weight: $font-weight-bold; // 700
}

// ── Badge ──────────────────────────────────────────────────
// 设计稿：minWidth 16 height 16 padding 0 4px radius 8
//         top -4 right -8；warn 底白字 10px weight 700
.acs-tabbar__badge {
  position: absolute;
  top: -8rpx;
  right: -16rpx;
  min-width: 32rpx;  // 16px → 32rpx
  height: 32rpx;
  padding: 0 8rpx;   // 4px → 8rpx
  border-radius: 16rpx;
  background: $color-warn;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 8rpx rgba(0, 0, 0, 0.15);
  box-sizing: border-box;
}

.acs-tabbar__badge-text {
  font-size: 20rpx; // 10px
  color: #ffffff;
  font-weight: $font-weight-bold;
  line-height: 1;
}
</style>
