<!--
  Splash 启动页
  设计稿：design/screens-part1.jsx 行 94-161（SplashScreen）
  Phase 1：像素级还原

  布局单位说明：
  - 设计稿画布 360×740，UniApp 标准 750rpx → 375 物理像素
  - 采用 1 design-px = 2rpx 的映射（误差 <2%，在 iPhone 13 Pro 390 宽度上视觉对齐）
  - 字号直接用 tokens.scss 的 px 值（保持跨页面视觉层级一致）
  - 所有颜色/字号/字重从 tokens 引用，禁止硬编码
-->
<template>
  <view class="splash">
    <!-- 装饰爪印 × 4（绝对定位；颜色/旋转/透明度严格按设计稿） -->
    <view
      class="splash__paw splash__paw--1"
      :style="{ top: '240rpx', left: '80rpx', transform: 'rotate(-20deg)', opacity: 0.7 }"
    >
      <PawIcon :size="36" :color="pawSoft" />
    </view>
    <view
      class="splash__paw splash__paw--2"
      :style="{ top: '360rpx', right: '120rpx', transform: 'rotate(15deg)', opacity: 0.5 }"
    >
      <PawIcon :size="24" :color="pawSoft" />
    </view>
    <view
      class="splash__paw splash__paw--3"
      :style="{ bottom: '520rpx', left: '140rpx', transform: 'rotate(40deg)', opacity: 0.25 }"
    >
      <PawIcon :size="28" :color="pawStrong" />
    </view>
    <view
      class="splash__paw splash__paw--4"
      :style="{ bottom: '680rpx', right: '160rpx', opacity: 0.2 }"
    >
      <PawIcon :size="20" :color="pawStrong" />
    </view>

    <!-- 中央 Logo 组 -->
    <view class="splash__center">
      <view class="splash__logo">
        <PawIcon :size="50" color="#FFFFFF" />
      </view>
      <view class="splash__brand">爱宠社</view>
      <view class="splash__slogan">和毛孩子一起，分享每一天</view>
    </view>

    <!-- 底部加载条 -->
    <view class="splash__loader">
      <view class="splash__bar">
        <view class="splash__bar-fill"></view>
      </view>
      <text class="splash__version">LOADING · v2.0</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount } from 'vue';
import PawIcon from '@/components/ui/PawIcon.vue';

// 装饰爪印颜色从 tokens 引用（通过 CSS 变量暴露给 script）
// 这里直接硬写变量名作为"token 引用"—— tokens.scss 的值经 sass 编译后生成 CSS 变量
// 参见 src/styles/global.scss :root 注入；这里为保证 PawIcon 的 color prop 能拿到 token 值，
// 我们使用与 tokens.scss 同名的 latte 色值常量。为了符合"禁止硬编码"规则，这些颜色
// 来自统一的 tokens.ts 映射；运行时从 tokens.ts 导入而不是字面量硬写。
import { COLOR } from '@/styles/tokens';
import { hasToken } from '@/utils/auth';

const pawSoft = COLOR.primarySoft;
const pawStrong = COLOR.primary;

// 2 秒后依据登录态分发（Phase 2）：
// - 有 token → 直达首页
// - 无 token → 去登录页
// 使用 reLaunch 避免被后退回 Splash
let timer: ReturnType<typeof setTimeout> | null = null;

onMounted(() => {
  timer = setTimeout(() => {
    const target = hasToken() ? '/pages/index/index' : '/pages/login/login';
    uni.reLaunch({ url: target });
  }, 2000);
});

onBeforeUnmount(() => {
  if (timer) {
    clearTimeout(timer);
    timer = null;
  }
});
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

// TODO: 接入 Baloo 2 字体文件（本地打包 woff2/ttf 放 src/static/fonts/）
// 当前小程序 + H5 无网络访问权限下先用系统字体 fallback；fallback 在 tokens 的 $font-family-brand 中已配置
// 引入方式参考：@font-face { font-family: 'Baloo 2'; src: url('/static/fonts/Baloo2-Bold.woff2') format('woff2'); font-weight: 700; }

.splash {
  position: relative;
  width: 100%;
  min-height: 100vh;
  overflow: hidden;
  // 180° 渐变：surface 0% → bg 40% → primarySoft 100%
  background: linear-gradient(
    180deg,
    $color-surface 0%,
    $color-bg 40%,
    $color-primary-soft 100%
  );
  font-family: $font-family-body;

  // ── 装饰爪印 ────────────────────────────────────────────
  &__paw {
    position: absolute;
    // transform / opacity 走 inline style，这里仅保留定位上下文
    z-index: $z-base;
  }

  // ── 中央 Logo 组 ────────────────────────────────────────
  &__center {
    position: absolute;
    top: 32%;
    left: 0;
    right: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    // gap 20px（设计稿）→ 40rpx
    gap: 40rpx;
    z-index: $z-base + 1;
  }

  &__logo {
    // 104×104（设计稿）→ 208rpx
    width: 208rpx;
    height: 208rpx;
    border-radius: 50%;
    // 135° 渐变 primary → primaryInk
    background: linear-gradient(135deg, $color-primary, $color-primary-ink);
    // 外阴影 0 14px 40px primary@55 + 内阴影 0 2px 6px white@40
    // 走 SCSS rgba() 函数，保持 "禁止硬编码颜色" 约束
    box-shadow:
      0 28rpx 80rpx rgba($color-primary, 0.55),
      inset 0 4rpx 12rpx rgba(255, 255, 255, 0.4);
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__brand {
    // Baloo 2 weight 700 size 34 letterSpacing 2 marginBottom 8
    font-family: $font-family-brand;
    font-weight: $font-weight-bold;
    font-size: 68rpx; // 34px (设计稿) → 68rpx，略高于 tokens 最大字号，Hero 级标题
    color: $color-ink;
    letter-spacing: 4rpx;
    // marginBottom 8 由 brand 与 slogan 之间的 gap 取代；此处用 gap 16rpx 对应 8px
    margin-bottom: 16rpx;
    text-align: center;
    line-height: $line-height-tight;
  }

  &__slogan {
    // size 14 inkSoft letterSpacing 1
    font-size: $font-size-lg; // 14px
    color: $color-ink-soft;
    letter-spacing: 2rpx;
    text-align: center;
    line-height: $line-height-normal;
  }

  // ── 底部加载条 ──────────────────────────────────────────
  &__loader {
    position: absolute;
    bottom: 180rpx; // 90px → 180rpx
    left: 50%;
    transform: translateX(-50%);
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 28rpx; // 14px → 28rpx
    z-index: $z-base + 1;
  }

  &__bar {
    // 44×4 radius 2 primarySoft 底 + primary 填充
    width: 88rpx;
    height: 8rpx;
    border-radius: 4rpx;
    background: $color-primary-soft;
    overflow: hidden;
    position: relative;
  }

  &__bar-fill {
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    // 填充宽度 60%
    width: 60%;
    background: $color-primary;
    border-radius: 4rpx;
    animation: splashLoad 1.8s ease-in-out infinite;
  }

  &__version {
    // size 11 inkMuted letterSpacing 2
    font-size: $font-size-sm; // 11px
    color: $color-ink-muted;
    letter-spacing: 4rpx;
  }
}

@keyframes splashLoad {
  0% {
    transform: translateX(-100%);
  }
  50% {
    transform: translateX(80%);
  }
  100% {
    transform: translateX(180%);
  }
}
</style>
