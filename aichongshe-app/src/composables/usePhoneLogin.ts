/**
 * 手机号登录公共逻辑（Phase 6）。
 *
 * 复用方：
 *   - LoginSheet（全局登录面板，本 Phase 必接）
 *   - phone-login.vue（独立登录页，可后续接入，本 Phase 不强制）
 *
 * 状态：
 *   - phone / smsCode / cooldown / submitting
 * 计算：
 *   - canRequestSms / canSubmit / smsButtonText
 * 方法：
 *   - onPhoneInput / onRequestSms / onSubmit / cleanupCooldown
 *
 * onSuccess 回调：登录成功后调用（LoginSheet 用它驱动 store.confirmLoginSuccess；
 *                独立登录页用它跳 home）。
 */
import { computed, ref } from 'vue';
import { useUserStore } from '@/stores/user';
import { requestSmsCode } from '@/api/services/auth';

const PHONE_REGEX = /^1\d{10}$/;
const SMS_REGEX = /^\d{6}$/;

export interface UsePhoneLoginOptions {
  /** 登录成功后的回调（注意：内部已 showToast('登录成功')） */
  onSuccess?: () => void;
}

export function usePhoneLogin(options: UsePhoneLoginOptions = {}) {
  const userStore = useUserStore();

  const phone = ref<string>('');
  const smsCode = ref<string>('');
  const submitting = ref<boolean>(false);
  const cooldown = ref<number>(0);

  let cooldownTimer: ReturnType<typeof setInterval> | null = null;

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
      options.onSuccess?.();
    } catch (err) {
      const message = err instanceof Error ? err.message : '登录失败';
      uni.showToast({ title: message, icon: 'none' });
    } finally {
      submitting.value = false;
    }
  }

  function cleanupCooldown(): void {
    if (cooldownTimer) {
      clearInterval(cooldownTimer);
      cooldownTimer = null;
    }
  }

  return {
    // state
    phone,
    smsCode,
    submitting,
    cooldown,
    // computed
    canRequestSms,
    canSubmit,
    smsButtonText,
    // methods
    onPhoneInput,
    onRequestSms,
    onSubmit,
    cleanupCooldown,
  };
}
