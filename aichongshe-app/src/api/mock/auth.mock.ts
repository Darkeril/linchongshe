/**
 * 鉴权相关 mock 数据。
 * 必须满足 src/types/auth.ts 的类型契约。
 *
 * 规则（来自 Phase 2 计划 + CLAUDE.md 原则 5）：
 * - 微信登录：无条件成功，返回固定 UserProfile + access_token
 * - 手机号登录：任意 11 位 + 验证码 "123456" → 成功；否则抛错
 *   （Phase 6 修：原 mock "1234" 与前端 6 位 SMS_REGEX 校验冲突，统一为 6 位）
 * - 请求验证码：任意 11 位手机号 → 成功
 */
import type {
  LoginResult,
  PhoneLoginParams,
  SmsCodeRequestParams,
  SmsCodeRequestResult,
  WechatLoginParams,
} from '@/types/auth';
import type { UserProfile } from '@/types/user';
import { mockUserProfile, delay } from '@/api/mock/user.mock';

/** 假 accessToken（JWT 风格字符串，但不是真 JWT） */
function fakeToken(seed: string): string {
  const now = Date.now();
  return `mock.${seed}.${now.toString(36)}`;
}

/** 随机 traceId */
function fakeTraceId(): string {
  return `trace_${Math.random().toString(36).slice(2, 10)}`;
}

/** 微信登录 mock */
export function mockLoginByWechat(params: WechatLoginParams): Promise<LoginResult> {
  const user: UserProfile = { ...mockUserProfile };
  const result: LoginResult = {
    accessToken: fakeToken(`wx_${params.code || 'auto'}`),
    refreshToken: fakeToken('refresh'),
    expiresIn: 7200,
    user,
  };
  return delay(result, 400);
}

/** 手机号登录 mock */
export function mockLoginByPhone(params: PhoneLoginParams): Promise<LoginResult> {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      if (!/^\d{11}$/.test(params.phone)) {
        reject(new Error('手机号格式不正确'));
        return;
      }
      if (params.smsCode !== '123456') {
        reject(new Error('验证码错误（mock 专用码为 123456）'));
        return;
      }
      const user: UserProfile = {
        ...mockUserProfile,
        nickname: `宠友_${params.phone.slice(-4)}`,
      };
      resolve({
        accessToken: fakeToken(`phone_${params.phone}`),
        refreshToken: fakeToken('refresh'),
        expiresIn: 7200,
        user,
      });
    }, 500);
  });
}

/** 请求短信验证码 mock */
export function mockRequestSmsCode(
  params: SmsCodeRequestParams,
): Promise<SmsCodeRequestResult> {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      if (!/^\d{11}$/.test(params.phone)) {
        reject(new Error('手机号格式不正确'));
        return;
      }
      resolve({
        traceId: fakeTraceId(),
        cooldownSeconds: 60,
      });
    }, 300);
  });
}

/** 登出 mock：无副作用，直接 resolve */
export function mockLogout(): Promise<void> {
  return delay(undefined as unknown as void, 100);
}
