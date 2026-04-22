<!--
  Login 登录方式选择页
  设计稿：design/screens-part1.jsx 行 166-275（LoginScreen）
  Phase 2：像素级还原

  布局单位（与 Splash 对齐）：
  - 设计稿画布 360×740，1 design-px = 2rpx
  - 字号保留 tokens px 值（保持跨页面视觉层级一致）
  - 颜色 / 阴影 / 圆角从 tokens 引用；阴影 alpha 走 SCSS rgba() 函数
-->
<template>
  <view class="login">
    <!-- 顶部柔和背景块 (absolute, top -60 left/right -40, height 340) -->
    <view class="login__hero-bg" />

    <!-- 浮动宠物头像群 (absolute, top 70, height 280) -->
    <view class="login__pets">
      <!-- dog: size 56, top 30 left 40, rotate -10° -->
      <view class="login__pet login__pet--dog">
        <PetAvatar variant="dog" :size="56" />
      </view>
      <!-- cat: size 64, top 10 right 50, rotate 12° -->
      <view class="login__pet login__pet--cat">
        <PetAvatar variant="cat" :size="64" />
      </view>
      <!-- mochi: size 44, top 140 left 20, rotate -5° -->
      <view class="login__pet login__pet--mochi">
        <PetAvatar variant="mochi" :size="44" />
      </view>
      <!-- puppy: size 48, top 150 right 30, rotate 8° -->
      <view class="login__pet login__pet--puppy">
        <PetAvatar variant="puppy" :size="48" />
      </view>
      <!-- PawIcon 22 primary opacity 0.35 rotate -20° top 110 left 40% -->
      <view class="login__pet login__pet--paw">
        <PawIcon :size="22" :color="primaryColor" />
      </view>
    </view>

    <!-- Logo + 品牌 (absolute, top 210) -->
    <view class="login__brand-group">
      <view class="login__logo">
        <PawIcon :size="34" color="#FFFFFF" />
      </view>
      <view class="login__brand">爱宠社</view>
    </view>

    <!-- 欢迎文案 (absolute, top 360) -->
    <view class="login__welcome">
      <view class="login__welcome-title">欢迎回来 🐾</view>
      <view class="login__welcome-sub">
        宠友都在这里，等你一起分享{{ '\n' }}萌瞬间、闲置好物和日常治愈
      </view>
    </view>

    <!-- 按钮组 (absolute, top 470) -->
    <view class="login__actions">
      <button
        class="login__btn login__btn--primary"
        :disabled="submitting"
        hover-class="login__btn--hover"
        :hover-stay-time="120"
        @click="onWechatLogin"
      >
        <svg
          class="login__btn-icon"
          width="18"
          height="18"
          viewBox="0 0 24 24"
          fill="#FFFFFF"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            d="M8.7 13.8c-0.4 0-0.8-0.3-0.8-0.8s0.3-0.8 0.8-0.8 0.8 0.3 0.8 0.8-0.3 0.8-0.8 0.8zm7.2 0c-0.4 0-0.8-0.3-0.8-0.8s0.3-0.8 0.8-0.8c0.4 0 0.8 0.3 0.8 0.8s-0.3 0.8-0.8 0.8zm-3.6-8.8c-5.2 0-9.4 3.5-9.4 7.9 0 2.5 1.4 4.7 3.5 6.1-0.2 0.6-0.6 2-0.6 2.3 0 0.2 0.1 0.3 0.3 0.3 0.1 0 2.3-1.5 2.7-1.8 1.1 0.3 2.2 0.5 3.5 0.5 5.2 0 9.4-3.5 9.4-7.9S17.5 5 12.3 5z"
          />
        </svg>
        <text>微信一键登录</text>
      </button>
      <button
        class="login__btn login__btn--outline"
        hover-class="login__btn--outline-hover"
        :hover-stay-time="120"
        @click="onPhoneLogin"
      >
        <text>手机号登录 / 注册</text>
      </button>
    </view>

    <!-- 底部协议 (absolute, bottom 40) -->
    <view class="login__agreement">
      <view
        class="login__checkbox"
        :class="{ 'login__checkbox--checked': agreed }"
        @click="agreed = !agreed"
      >
        <svg
          v-if="agreed"
          width="8"
          height="8"
          viewBox="0 0 10 10"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            d="M1 5l3 3 5-6"
            stroke="#fff"
            stroke-width="1.8"
            fill="none"
            stroke-linecap="round"
          />
        </svg>
      </view>
      <view class="login__agreement-text">
        已阅读并同意
        <text class="login__agreement-link" @click.stop="onOpenDoc('user')">
          《用户协议》
        </text>
        和
        <text class="login__agreement-link" @click.stop="onOpenDoc('privacy')">
          《隐私政策》
        </text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import PawIcon from '@/components/ui/PawIcon.vue';
import PetAvatar from '@/components/ui/PetAvatar.vue';
import { useUserStore } from '@/stores/user';
import { COLOR } from '@/styles/tokens';

const primaryColor = COLOR.primary;

const agreed = ref(false);
const submitting = ref(false);

const userStore = useUserStore();

/** 点击微信一键登录：未勾选协议先提示，否则 mock 登录 */
async function onWechatLogin(): Promise<void> {
  if (!agreed.value) {
    uni.showToast({ title: '请先同意用户协议和隐私政策', icon: 'none' });
    return;
  }
  if (submitting.value) return;
  submitting.value = true;
  try {
    // Phase 2：mock 直接返回成功；真实环境需先 wx.login 拿 code
    await userStore.loginWithWechat({ code: 'mock_wx_code' });
    uni.showToast({ title: '登录成功', icon: 'success' });
    setTimeout(() => {
      // home 是 tabBar 页，必须用 switchTab 而非 reLaunch
      uni.switchTab({ url: '/pages/home/home' });
    }, 400);
  } catch (err) {
    const message = err instanceof Error ? err.message : '登录失败，请稍后重试';
    uni.showToast({ title: message, icon: 'none' });
  } finally {
    submitting.value = false;
  }
}

/** 点击手机号登录：协议校验后跳转二级页 */
function onPhoneLogin(): void {
  if (!agreed.value) {
    uni.showToast({ title: '请先同意用户协议和隐私政策', icon: 'none' });
    return;
  }
  uni.navigateTo({ url: '/pages/login/phone-login' });
}

function onOpenDoc(kind: 'user' | 'privacy'): void {
  // Phase 2 协议页未实现；仅提示，后续 Phase 补 WebView / 静态页
  uni.showToast({
    title: kind === 'user' ? '用户协议待接入' : '隐私政策待接入',
    icon: 'none',
  });
}
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

// 设计稿 1 design-px → 2rpx
.login {
  position: relative;
  width: 100%;
  min-height: 100vh;
  background: $color-bg;
  font-family: $font-family-body;
  overflow: hidden;

  // ── 顶部柔和背景块 ────────────────────────────────────────
  // top:-60 left:-40 right:-40 height:340 →
  &__hero-bg {
    position: absolute;
    top: -120rpx;
    left: -80rpx;
    right: -80rpx;
    height: 680rpx;
    // radial-gradient(ellipse at 50% 60%, primarySoft 0%, bg 70%)
    background: radial-gradient(
      ellipse at 50% 60%,
      $color-primary-soft 0%,
      $color-bg 70%
    );
    z-index: $z-base;
  }

  // ── 浮动宠物头像群 ────────────────────────────────────────
  // top:70 height:280
  &__pets {
    position: absolute;
    top: 140rpx;
    left: 0;
    right: 0;
    height: 560rpx;
    pointer-events: none;
    z-index: $z-base + 1;
  }

  &__pet {
    position: absolute;
  }

  // dog: top 30 left 40 → 60rpx / 80rpx; rotate -10
  &__pet--dog {
    top: 60rpx;
    left: 80rpx;
    transform: rotate(-10deg);
  }

  // cat: top 10 right 50 → 20rpx / 100rpx; rotate 12
  &__pet--cat {
    top: 20rpx;
    right: 100rpx;
    transform: rotate(12deg);
  }

  // mochi: top 140 left 20 → 280rpx / 40rpx; rotate -5
  &__pet--mochi {
    top: 280rpx;
    left: 40rpx;
    transform: rotate(-5deg);
  }

  // puppy: top 150 right 30 → 300rpx / 60rpx; rotate 8
  &__pet--puppy {
    top: 300rpx;
    right: 60rpx;
    transform: rotate(8deg);
  }

  // PawIcon 装饰: top 110 left 40% opacity 0.35 rotate -20
  &__pet--paw {
    top: 220rpx;
    left: 40%;
    opacity: 0.35;
    transform: rotate(-20deg);
  }

  // ── Logo + 品牌 ──────────────────────────────────────────
  // top 210 gap 12
  &__brand-group {
    position: absolute;
    top: 420rpx;
    left: 0;
    right: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 24rpx;
    z-index: $z-base + 2;
  }

  // 72×72 radius 50%
  &__logo {
    width: 144rpx;
    height: 144rpx;
    border-radius: 50%;
    background: linear-gradient(135deg, $color-primary, $color-primary-ink);
    // 0 10px 28px primary@55
    box-shadow: 0 20rpx 56rpx rgba($color-primary, 0.55);
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__brand {
    font-family: $font-family-brand;
    font-weight: $font-weight-bold;
    font-size: 28px;
    color: $color-ink;
    letter-spacing: 3rpx; // 1.5px → 3rpx
    line-height: $line-height-tight;
  }

  // ── 欢迎文案 ─────────────────────────────────────────────
  // top 360 left/right 32
  &__welcome {
    position: absolute;
    top: 720rpx;
    left: 64rpx;
    right: 64rpx;
    text-align: center;
    z-index: $z-base + 2;
  }

  &__welcome-title {
    font-size: $font-size-3xl; // 22px
    font-weight: $font-weight-bold;
    color: $color-ink;
    margin-bottom: 16rpx; // 8px → 16rpx
    letter-spacing: 1rpx; // 0.5 → 1rpx
    line-height: $line-height-tight;
  }

  &__welcome-sub {
    font-size: $font-size-lg; // 14px
    color: $color-ink-soft;
    line-height: $line-height-relaxed;
    white-space: pre-line;
  }

  // ── 按钮组 ──────────────────────────────────────────────
  // top 470 left/right 28 gap 12
  &__actions {
    position: absolute;
    top: 940rpx;
    left: 56rpx;
    right: 56rpx;
    display: flex;
    flex-direction: column;
    gap: 24rpx;
    z-index: $z-base + 2;
  }

  // 高 52 radius radius*1.6 (16*1.6 ≈ 26)
  &__btn {
    // 强制撑满父容器（覆盖 UniApp button 默认 inline-block 行为）
    width: 100%;
    box-sizing: border-box;

    height: 104rpx;
    border-radius: 52rpx; // 26px → 52rpx
    font-size: $font-size-xl; // 16px
    font-weight: $font-weight-semibold;
    font-family: $font-family-body;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 16rpx; // 8px → 16rpx
    padding: 0;
    margin: 0;
    border: none;
    line-height: 1;
    white-space: nowrap;
    overflow: hidden;
    // 按下反馈在 hover-class 里；过渡放这里
    transition: transform 0.12s ease, box-shadow 0.12s ease, opacity 0.12s ease;

    // 彻底去除 UniApp 原生 button 的默认 ::after 伪边框
    &::after {
      display: none;
      border: none;
    }
  }

  // 主按钮：渐变 + 阴影 + inset 高光
  &__btn--primary {
    background: linear-gradient(135deg, $color-primary, $color-primary-ink);
    color: #ffffff;
    box-shadow:
      0 16rpx 40rpx rgba($color-primary, 0.55),
      inset 0 2rpx 0 rgba(255, 255, 255, 0.3);
  }

  &__btn--hover {
    transform: scale(0.98);
    opacity: 0.92;
  }

  // 次按钮：surface 底 + primarySoft 边框 + 轻阴影
  &__btn--outline {
    background: $color-surface;
    color: $color-primary;
    border: 3rpx solid $color-primary-soft; // 1.5px → 3rpx
    box-shadow: 0 8rpx 24rpx rgba($color-primary, 0.15);
  }

  &__btn--outline-hover {
    transform: scale(0.98);
    background: $color-primary-soft;
  }

  &__btn-icon {
    display: block;
    flex-shrink: 0;
  }

  // ── 底部协议 ─────────────────────────────────────────────
  // bottom 40 gap 8 size 11
  &__agreement {
    position: absolute;
    bottom: 80rpx;
    left: 0;
    right: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 16rpx;
    font-size: $font-size-sm; // 11px
    color: $color-ink-muted;
    z-index: $z-base + 2;
  }

  // 14×14 radius 7
  &__checkbox {
    width: 28rpx;
    height: 28rpx;
    border-radius: 14rpx;
    border: 3rpx solid $color-ink-faint; // 1.5px → 3rpx
    background: transparent;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: background 0.15s ease, border-color 0.15s ease;

    &--checked {
      border-color: $color-primary;
      background: $color-primary;
    }
  }

  &__agreement-text {
    font-size: $font-size-sm;
    color: $color-ink-muted;
    line-height: $line-height-normal;
  }

  &__agreement-link {
    color: $color-primary;
  }
}
</style>
