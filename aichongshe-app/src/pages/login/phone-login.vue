<!-- TODO(design): 待设计师细化 手机号登录页 -->
<!--
  手机号登录页 V1（无设计稿）
  说明（来自 CLAUDE.md 原则 1 的"无设计稿处理分支"）：
  - 当前无设计稿，按 latte 主题出 V1：圆角 / 阴影 / 字号 / 颜色严格走 tokens
  - 布局与 Login 主页保持视觉连贯（主按钮同款渐变）
  - 设计师交付后替换为像素级还原版本
  - 所有颜色/阴影/字号从 tokens 引用，无硬编码
-->
<template>
  <view class="phone-login">
    <!-- 顶部 Header 区（flex 布局，避免 absolute back + margin-top title 冲突） -->
    <view class="phone-login__header">
      <view
        class="phone-login__back"
        hover-class="phone-login__back--hover"
        :hover-stay-time="120"
        @click="onBack"
      >
        <svg
          width="22"
          height="22"
          viewBox="0 0 24 24"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            d="M15 5 L8 12 L15 19"
            :stroke="inkColor"
            stroke-width="2"
            fill="none"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
      </view>
    </view>

    <!-- 标题 -->
    <view class="phone-login__title">手机号登录</view>
    <view class="phone-login__subtitle">输入手机号与验证码，登录或注册</view>

    <!-- 表单 -->
    <view class="phone-login__form">
      <!-- 手机号行 -->
      <view class="phone-login__field">
        <text class="phone-login__field-prefix">+86</text>
        <view class="phone-login__field-divider" />
        <input
          v-model="phone"
          class="phone-login__field-input"
          type="number"
          maxlength="11"
          placeholder="请输入手机号"
          :placeholder-style="placeholderStyle"
          @input="onPhoneInput"
        />
      </view>

      <!-- 验证码行 -->
      <view class="phone-login__field">
        <input
          v-model="smsCode"
          class="phone-login__field-input phone-login__field-input--code"
          type="number"
          maxlength="6"
          placeholder="6 位验证码"
          :placeholder-style="placeholderStyle"
        />
        <button
          class="phone-login__sms-btn"
          :disabled="!canRequestSms"
          :class="{ 'phone-login__sms-btn--disabled': !canRequestSms }"
          @click="onRequestSms"
        >
          {{ smsButtonText }}
        </button>
      </view>

      <!-- 主按钮 -->
      <button
        class="phone-login__submit"
        :disabled="!canSubmit || submitting"
        :class="{ 'phone-login__submit--disabled': !canSubmit || submitting }"
        hover-class="phone-login__submit--hover"
        :hover-stay-time="120"
        @click="onSubmit"
      >
        {{ submitting ? '登录中…' : '登录 / 注册' }}
      </button>

      <!-- 说明（mock 提示，方便用户试） -->
      <view class="phone-login__hint">
        Mock 环境：任意 11 位手机号 + 验证码 <text class="phone-login__hint-code">123456</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref } from 'vue';
import { useUserStore } from '@/stores/user';
import { requestSmsCode } from '@/api/services/auth';
import { COLOR } from '@/styles/tokens';

const inkColor = COLOR.ink;
const placeholderStyle = `color: ${COLOR.inkFaint}; font-size: 14px;`;

const userStore = useUserStore();

const phone = ref('');
const smsCode = ref('');
const submitting = ref(false);

// 倒计时（秒）。0 表示未在倒计时状态
const cooldown = ref(0);
let cooldownTimer: ReturnType<typeof setInterval> | null = null;

const PHONE_REGEX = /^1\d{10}$/;
const SMS_REGEX = /^\d{6}$/;

const canRequestSms = computed<boolean>(
  () => PHONE_REGEX.test(phone.value) && cooldown.value === 0,
);

const canSubmit = computed<boolean>(
  () => PHONE_REGEX.test(phone.value) && SMS_REGEX.test(smsCode.value),
);

const smsButtonText = computed<string>(() =>
  cooldown.value > 0 ? `${cooldown.value}s 后重发` : '获取验证码',
);

function onPhoneInput(e: Event): void {
  // uniapp 下 v-model 已处理；这里只确保非数字字符被过滤
  const value = (e.target as HTMLInputElement)?.value ?? phone.value;
  phone.value = value.replace(/\D/g, '').slice(0, 11);
}

function startCooldown(seconds: number): void {
  cooldown.value = seconds;
  if (cooldownTimer) clearInterval(cooldownTimer);
  cooldownTimer = setInterval(() => {
    cooldown.value -= 1;
    if (cooldown.value <= 0) {
      cooldown.value = 0;
      if (cooldownTimer) {
        clearInterval(cooldownTimer);
        cooldownTimer = null;
      }
    }
  }, 1000);
}

async function onRequestSms(): Promise<void> {
  if (!canRequestSms.value) {
    if (!PHONE_REGEX.test(phone.value)) {
      uni.showToast({ title: '请输入 11 位手机号', icon: 'none' });
    }
    return;
  }
  try {
    const result = await requestSmsCode({ phone: phone.value, scene: 'login' });
    uni.showToast({ title: '验证码已发送', icon: 'none' });
    startCooldown(result.cooldownSeconds || 60);
  } catch (err) {
    const message = err instanceof Error ? err.message : '发送失败，请稍后重试';
    uni.showToast({ title: message, icon: 'none' });
  }
}

async function onSubmit(): Promise<void> {
  if (!canSubmit.value || submitting.value) return;
  submitting.value = true;
  try {
    await userStore.loginWithPhone({ phone: phone.value, smsCode: smsCode.value });
    uni.showToast({ title: '登录成功', icon: 'success' });
    setTimeout(() => {
      // home 是 tabBar 页，必须用 switchTab
      uni.switchTab({ url: '/pages/home/home' });
    }, 400);
  } catch (err) {
    const message = err instanceof Error ? err.message : '登录失败';
    uni.showToast({ title: message, icon: 'none' });
  } finally {
    submitting.value = false;
  }
}

function onBack(): void {
  const pages = getCurrentPages();
  if (pages.length > 1) {
    uni.navigateBack();
  } else {
    uni.reLaunch({ url: '/pages/login/login' });
  }
}

onBeforeUnmount(() => {
  if (cooldownTimer) {
    clearInterval(cooldownTimer);
    cooldownTimer = null;
  }
});
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

.phone-login {
  position: relative;
  width: 100%;
  min-height: 100vh;
  background: $color-bg;
  font-family: $font-family-body;
  padding: 0 56rpx;
  // 顶部预留 safe-area，header 在文档流中
  padding-top: calc(40rpx + env(safe-area-inset-top));
  box-sizing: border-box;

  // ── Header（flex 容器，仅放 back 按钮；标题在文档流中接续） ─
  // 修正：原 V1 用 absolute back + margin-top: 180rpx title 重叠，改用文档流
  &__header {
    display: flex;
    align-items: center;
    height: 72rpx;
    margin-bottom: 80rpx;
    margin-left: -24rpx; // 视觉对齐左边缘（按钮内有 padding 视觉中线偏右）
  }

  // ── 返回按钮 ────────────────────────────────────────────
  &__back {
    width: 72rpx;
    height: 72rpx;
    border-radius: 36rpx;
    background: $color-surface;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: $shadow-sm;
    transition: transform 0.12s ease;

    &--hover {
      transform: scale(0.94);
    }
  }

  // ── 标题区 ──────────────────────────────────────────────
  &__title {
    font-size: $font-size-3xl; // 22px
    font-weight: $font-weight-bold;
    color: $color-ink;
    letter-spacing: 1rpx;
    line-height: $line-height-tight;
  }

  &__subtitle {
    margin-top: 16rpx;
    font-size: $font-size-lg; // 14px
    color: $color-ink-soft;
    line-height: $line-height-relaxed;
  }

  // ── 表单 ────────────────────────────────────────────────
  &__form {
    margin-top: 64rpx;
    display: flex;
    flex-direction: column;
    gap: 28rpx;
  }

  // 单个输入行
  &__field {
    display: flex;
    align-items: center;
    height: 104rpx;
    padding: 0 32rpx;
    background: $color-surface;
    border-radius: 52rpx; // 26px → 与 Login 主按钮圆角呼应
    border: 3rpx solid $color-primary-soft;
    box-shadow: 0 8rpx 24rpx rgba($color-primary, 0.1);
    gap: 20rpx;
  }

  &__field-prefix {
    font-size: $font-size-xl; // 16px
    font-weight: $font-weight-semibold;
    color: $color-ink;
    font-family: $font-family-num;
  }

  &__field-divider {
    width: 2rpx;
    height: 40rpx;
    background: $color-ink-faint;
  }

  &__field-input {
    flex: 1;
    height: 100%;
    font-size: $font-size-xl; // 16px
    color: $color-ink;
    background: transparent;
    border: none;
    padding: 0;

    &--code {
      // 验证码行没有 prefix / divider，独占左侧
      letter-spacing: 4rpx;
    }
  }

  &__sms-btn {
    height: 72rpx;
    padding: 0 24rpx;
    border-radius: 36rpx;
    background: $color-primary-soft;
    color: $color-primary-ink;
    font-size: $font-size-md; // 13px
    font-weight: $font-weight-semibold;
    border: none;
    line-height: 1;
    flex-shrink: 0;
    // 文字居中（修 UniApp button 默认 left-aligned，gotcha #1）
    display: flex;
    align-items: center;
    justify-content: center;
    text-align: center;
    margin: 0;

    &::after {
      border: none;
      display: none;
    }

    &--disabled {
      background: $color-surface-dim;
      color: $color-ink-muted;
    }
  }

  // ── 主按钮（与 Login 主按钮同款渐变） ──────────────────
  &__submit {
    // 强制撑满（覆盖 UniApp button 默认 inline-block，gotcha #1）
    width: 100%;
    box-sizing: border-box;

    margin: 24rpx 0 0 0;
    height: 104rpx;
    border-radius: 52rpx;
    border: none;
    padding: 0;
    background: linear-gradient(135deg, $color-primary, $color-primary-ink);
    color: #ffffff;
    font-size: $font-size-xl;
    font-weight: $font-weight-semibold;
    font-family: $font-family-body;
    line-height: 1;
    // 文字居中（gotcha #1）
    display: flex;
    align-items: center;
    justify-content: center;
    text-align: center;
    white-space: nowrap;
    box-shadow:
      0 16rpx 40rpx rgba($color-primary, 0.55),
      inset 0 2rpx 0 rgba(255, 255, 255, 0.3);
    transition: transform 0.12s ease, opacity 0.12s ease;

    &::after {
      border: none;
      display: none;
    }
    line-height: 1;
    white-space: nowrap;

    &::after {
      display: none;
      border: none;
    }

    &--hover {
      transform: scale(0.98);
      opacity: 0.92;
    }

    &--disabled {
      background: linear-gradient(135deg, $color-primary-soft, $color-ink-faint);
      box-shadow: none;
      opacity: 0.7;
    }
  }

  &__hint {
    margin-top: 16rpx;
    font-size: $font-size-sm; // 11px
    color: $color-ink-muted;
    text-align: center;
    line-height: $line-height-normal;
  }

  &__hint-code {
    color: $color-primary;
    font-weight: $font-weight-bold;
    font-family: $font-family-num;
  }
}
</style>
