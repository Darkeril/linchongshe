<!-- TODO(design): 待设计师细化 登录面板 -->
<!--
  LoginSheet — 全局登录面板（Phase 6）
  无设计稿 V1：latte 主题，沿用 CommentInputSheet 的遮罩 + 滑入动画。
  挂载位置：App.vue 根级（v-if 控制），不属于任何具体页面。

  交互：
    - 监听 useAuthSheet().open
    - 手机号 / 微信 / 抖音任一登录成功 → store.confirmLoginSuccess() 关闭并回放
    - 点遮罩 / 关闭按钮 → store.cancel() reject(AUTH_CANCELLED)
    - emit 仅作信息透传（App.vue 当前未消费，留给后续埋点）

  gotcha：
    - #4 不嵌套 scroll-view，固定高度内容
    - #5 fixed + 父无 transform：App.vue 根 view 已确认无 transform
    - #11 主按钮用 <button> + hover-class
    - #12 acs- 前缀
    - #14 键盘高度监听条件编译
    - #19 整面板根不加 :active
    - #21 emit 名避 DOM 原生：auth-success / auth-cancel / sheet-close-tap
-->
<template>
  <view
    v-if="open"
    class="acs-login-sheet-mask"
    @tap="onMaskTap"
  >
    <view
      class="acs-login-sheet"
      :style="{ paddingBottom: keyboardPad + 'px' }"
      @tap.stop
    >
      <!-- 顶部：拖拽条 + 关闭按钮 -->
      <view class="acs-login-sheet__header">
        <view class="acs-login-sheet__handle" />
        <view
          class="acs-login-sheet__close"
          @tap="onCloseTap"
        >
          <CloseIcon :size="20" :color="inkColor" />
        </view>
      </view>

      <!-- 标题区 -->
      <view class="acs-login-sheet__title">登录爱宠社</view>
      <view class="acs-login-sheet__subtitle">
        宠友都在这里，等你一起分享
      </view>

      <!-- 手机号表单 -->
      <view class="acs-login-sheet__form">
        <!-- 手机号行 -->
        <view class="acs-login-sheet__field">
          <text class="acs-login-sheet__field-prefix">+86</text>
          <view class="acs-login-sheet__field-divider" />
          <input
            v-model="phone"
            class="acs-login-sheet__field-input"
            type="number"
            maxlength="11"
            placeholder="请输入手机号"
            :placeholder-style="placeholderStyle"
            @input="onPhoneInput"
          />
        </view>

        <!-- 验证码行 -->
        <view class="acs-login-sheet__field">
          <input
            v-model="smsCode"
            class="acs-login-sheet__field-input acs-login-sheet__field-input--code"
            type="number"
            maxlength="6"
            placeholder="6 位验证码"
            :placeholder-style="placeholderStyle"
          />
          <button
            class="acs-login-sheet__sms-btn"
            :disabled="!canRequestSms"
            :class="{ 'acs-login-sheet__sms-btn--disabled': !canRequestSms }"
            @tap="onRequestSms"
          >
            {{ smsButtonText }}
          </button>
        </view>

        <!-- 主按钮 -->
        <button
          class="acs-login-sheet__submit"
          :disabled="!canSubmit || submitting"
          :class="{ 'acs-login-sheet__submit--disabled': !canSubmit || submitting }"
          hover-class="acs-login-sheet__submit--hover"
          :hover-stay-time="120"
          @tap="onSubmit"
        >
          {{ submitting ? '登录中…' : '登录 / 注册' }}
        </button>

        <view class="acs-login-sheet__hint">
          Mock 环境：任意 11 位手机号 + 验证码
          <text class="acs-login-sheet__hint-code">123456</text>
        </view>
      </view>

      <!-- 分隔线 + 第三方登录 -->
      <view class="acs-login-sheet__divider-row">
        <view class="acs-login-sheet__divider-line" />
        <text class="acs-login-sheet__divider-text">其他登录方式</text>
        <view class="acs-login-sheet__divider-line" />
      </view>

      <view class="acs-login-sheet__third-party">
        <view
          class="acs-login-sheet__tp-btn acs-login-sheet__tp-btn--wechat"
          :class="{ 'acs-login-sheet__tp-btn--disabled': submitting }"
          @tap="onWechatLogin"
        >
          <WechatIcon :size="22" color="#FFFFFF" />
        </view>
        <view
          class="acs-login-sheet__tp-btn acs-login-sheet__tp-btn--douyin"
          :class="{ 'acs-login-sheet__tp-btn--disabled': submitting }"
          @tap="onDouyinLogin"
        >
          <DouyinIcon :size="22" color="#FFFFFF" />
        </view>
        <view
          class="acs-login-sheet__tp-btn acs-login-sheet__tp-btn--more"
          @tap="onMoreLogin"
        >
          <text class="acs-login-sheet__tp-more-text">更多</text>
        </view>
      </view>

      <!-- 协议勾选 -->
      <view class="acs-login-sheet__agreement">
        <view
          class="acs-login-sheet__checkbox"
          :class="{ 'acs-login-sheet__checkbox--checked': agreed }"
          @tap="agreed = !agreed"
        >
          <svg
            v-if="agreed"
            width="10"
            height="10"
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
        <text class="acs-login-sheet__agreement-text">
          已阅读并同意《用户协议》和《隐私政策》
        </text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from 'vue';
import CloseIcon from '@/components/ui/icons/CloseIcon.vue';
import WechatIcon from '@/components/ui/icons/WechatIcon.vue';
import DouyinIcon from '@/components/ui/icons/DouyinIcon.vue';
import { useAuthSheet } from '@/stores/authSheet';
import { useUserStore } from '@/stores/user';
import { usePhoneLogin } from '@/composables/usePhoneLogin';
import { storeToRefs } from 'pinia';
import { COLOR } from '@/styles/tokens';

const inkColor = COLOR.ink;
const placeholderStyle = `color: ${COLOR.inkFaint}; font-size: 14px;`;

// gotcha #21：emit 名避 DOM 原生
defineEmits<{
  (e: 'auth-success'): void;
  (e: 'auth-cancel'): void;
  (e: 'sheet-close-tap'): void;
}>();

const authSheet = useAuthSheet();
const userStore = useUserStore();
const { open } = storeToRefs(authSheet);

const agreed = ref<boolean>(false);
const keyboardPad = ref<number>(0);

// 手机号登录逻辑（与 phone-login.vue 同源）
const {
  phone,
  smsCode,
  submitting,
  canRequestSms,
  canSubmit,
  smsButtonText,
  onPhoneInput,
  onRequestSms,
  onSubmit: phoneOnSubmit,
  cleanupCooldown,
} = usePhoneLogin({
  onSuccess: () => {
    void authSheet.confirmLoginSuccess();
  },
});

// 协议校验包装
async function onSubmit(): Promise<void> {
  if (!agreed.value) {
    uni.showToast({ title: '请先同意用户协议和隐私政策', icon: 'none' });
    return;
  }
  await phoneOnSubmit();
}

async function onWechatLogin(): Promise<void> {
  if (!agreed.value) {
    uni.showToast({ title: '请先同意用户协议和隐私政策', icon: 'none' });
    return;
  }
  if (submitting.value) return;
  submitting.value = true;
  try {
    await userStore.loginWithWechat({ code: 'mock_wx_code' });
    uni.showToast({ title: '登录成功', icon: 'success' });
    void authSheet.confirmLoginSuccess();
  } catch (err) {
    const message = err instanceof Error ? err.message : '登录失败';
    uni.showToast({ title: message, icon: 'none' });
  } finally {
    submitting.value = false;
  }
}

function onDouyinLogin(): void {
  // Phase 6 mock：抖音登录路径暂走 mock 微信登录占位（后端真实接入再分流）
  uni.showToast({ title: '抖音登录待接入', icon: 'none' });
}

function onMoreLogin(): void {
  // 跳完整登录方式选择页（先取消当前 Sheet，避免动作回放和导航冲突）
  authSheet.cancel();
  uni.navigateTo({ url: '/pages/login/login' });
}

function onMaskTap(): void {
  authSheet.cancel();
}

function onCloseTap(): void {
  authSheet.cancel();
}

// open 由 false → true 时重置表单（避免上次输入残留）
watch(open, (v) => {
  if (v) {
    phone.value = '';
    smsCode.value = '';
    agreed.value = false;
  }
});

// 键盘高度监听（gotcha #14：必须按上下文用对应注释符）
let kbListenerBound = false;
function onKbChange(res: { height: number }): void {
  keyboardPad.value = res.height ?? 0;
}

onMounted(() => {
  // #ifdef MP-WEIXIN || MP-TOUTIAO || APP-PLUS
  uni.onKeyboardHeightChange(onKbChange);
  kbListenerBound = true;
  // #endif
});

onUnmounted(() => {
  // #ifdef MP-WEIXIN || MP-TOUTIAO || APP-PLUS
  if (kbListenerBound) {
    uni.offKeyboardHeightChange(onKbChange);
  }
  // #endif
  cleanupCooldown();
});
</script>

<style lang="scss" scoped>
@import '@/styles/tokens.scss';

// gotcha #5：本组件 fixed，要求父根（App.vue）无 transform
.acs-login-sheet-mask {
  position: fixed;
  inset: 0;
  left: 0; right: 0; top: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 950; // 比 CommentInputSheet (900) 略高，确保覆盖
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.acs-login-sheet {
  width: 100%;
  background: $color-surface;
  border-top-left-radius: $radius-xl;
  border-top-right-radius: $radius-xl;
  box-shadow: 0 -10rpx 40rpx rgba(80, 50, 30, 0.18);
  padding: 16rpx 48rpx 40rpx;
  box-sizing: border-box;
  animation: acs-login-sheet-slide-up 280ms ease-out;
}

@keyframes acs-login-sheet-slide-up {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}

// ── 头部：拖拽条 + 关闭按钮 ─────────────────────────────
.acs-login-sheet__header {
  position: relative;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.acs-login-sheet__handle {
  width: 72rpx;
  height: 6rpx;
  border-radius: 3rpx;
  background: $color-ink-faint;
}

.acs-login-sheet__close {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 56rpx;
  height: 56rpx;
  border-radius: 28rpx;
  background: $color-surface-dim;
  display: flex;
  align-items: center;
  justify-content: center;
  &:active { opacity: 0.7; }
}

// ── 标题 ────────────────────────────────────────────────
.acs-login-sheet__title {
  margin-top: 16rpx;
  font-size: $font-size-3xl; // 22
  font-weight: $font-weight-bold;
  color: $color-ink;
  line-height: $line-height-tight;
  font-family: $font-family-body;
}

.acs-login-sheet__subtitle {
  margin-top: 12rpx;
  font-size: $font-size-lg; // 14
  color: $color-ink-soft;
  line-height: $line-height-relaxed;
}

// ── 表单 ────────────────────────────────────────────────
.acs-login-sheet__form {
  margin-top: 36rpx;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.acs-login-sheet__field {
  display: flex;
  align-items: center;
  height: 96rpx;
  padding: 0 28rpx;
  background: $color-surface;
  border-radius: 48rpx;
  border: 3rpx solid $color-primary-soft;
  box-shadow: 0 6rpx 20rpx rgba($color-primary, 0.08);
  gap: 18rpx;
}

.acs-login-sheet__field-prefix {
  font-size: $font-size-xl;
  font-weight: $font-weight-semibold;
  color: $color-ink;
  font-family: $font-family-num;
}

.acs-login-sheet__field-divider {
  width: 2rpx;
  height: 36rpx;
  background: $color-ink-faint;
}

.acs-login-sheet__field-input {
  flex: 1;
  height: 100%;
  font-size: $font-size-xl;
  color: $color-ink;
  background: transparent;
  border: none;
  padding: 0;

  &--code {
    letter-spacing: 4rpx;
  }
}

.acs-login-sheet__sms-btn {
  height: 64rpx;
  padding: 0 22rpx;
  border-radius: 32rpx;
  background: $color-primary-soft;
  color: $color-primary-ink;
  font-size: $font-size-md;
  font-weight: $font-weight-semibold;
  border: none;
  line-height: 1;
  flex-shrink: 0;

  &::after { border: none; }

  &--disabled {
    background: $color-surface-dim;
    color: $color-ink-muted;
  }
}

// ── 主按钮（与 Login 主按钮同款渐变） ───────────────────
.acs-login-sheet__submit {
  width: 100%;
  box-sizing: border-box;
  margin-top: 12rpx;
  height: 96rpx;
  border-radius: 48rpx;
  border: none;
  padding: 0;
  background: linear-gradient(135deg, $color-primary, $color-primary-ink);
  color: #ffffff;
  font-size: $font-size-xl;
  font-weight: $font-weight-semibold;
  font-family: $font-family-body;
  box-shadow:
    0 14rpx 36rpx rgba($color-primary, 0.5),
    inset 0 2rpx 0 rgba(255, 255, 255, 0.3);
  transition: transform 0.12s ease, opacity 0.12s ease;
  line-height: 1;
  white-space: nowrap;

  &::after { display: none; border: none; }

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

.acs-login-sheet__hint {
  margin-top: 4rpx;
  font-size: $font-size-sm;
  color: $color-ink-muted;
  text-align: center;
  line-height: $line-height-normal;
}

.acs-login-sheet__hint-code {
  color: $color-primary;
  font-weight: $font-weight-bold;
  font-family: $font-family-num;
}

// ── 分隔线 ───────────────────────────────────────────────
.acs-login-sheet__divider-row {
  margin-top: 36rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.acs-login-sheet__divider-line {
  flex: 1;
  height: 2rpx;
  background: $color-divider;
}

.acs-login-sheet__divider-text {
  font-size: $font-size-sm;
  color: $color-ink-muted;
}

// ── 第三方登录区 ────────────────────────────────────────
.acs-login-sheet__third-party {
  margin-top: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 36rpx;
}

.acs-login-sheet__tp-btn {
  width: 88rpx;
  height: 88rpx;
  border-radius: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  &:active { opacity: 0.85; }

  &--wechat {
    background: #07c160;
    box-shadow: 0 8rpx 20rpx rgba(7, 193, 96, 0.32);
  }
  &--douyin {
    background: #000000;
    box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.28);
  }
  &--more {
    background: $color-surface-dim;
  }
  &--disabled { opacity: 0.6; }
}

.acs-login-sheet__tp-more-text {
  font-size: $font-size-md;
  color: $color-ink-soft;
  font-weight: $font-weight-medium;
}

// ── 协议 ────────────────────────────────────────────────
.acs-login-sheet__agreement {
  margin-top: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14rpx;
}

.acs-login-sheet__checkbox {
  width: 28rpx;
  height: 28rpx;
  border-radius: 14rpx;
  border: 3rpx solid $color-ink-faint;
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

.acs-login-sheet__agreement-text {
  font-size: $font-size-sm;
  color: $color-ink-muted;
  line-height: $line-height-normal;
}
</style>
