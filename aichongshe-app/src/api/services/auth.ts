/**
 * 鉴权 API service。
 * 同一签名下根据 isMock() 切换真/假数据源。
 *
 * 接口路径与方法严格按 CLAUDE.md 原则 7：
 * - 写操作用 POST（登录、登出、请求验证码都是写）
 * - Token 走 Header（getAuthHeader）
 * - 不允许 GET 承载写语义 / token 走 URL query
 *
 * 真实后端接入时（VITE_USE_MOCK != true）才会真正发请求；
 * mock 模式下只走 src/api/mock/auth.mock.ts。
 */
import { isMock, request } from '@/api/request';
import {
  mockLoginByPhone,
  mockLoginByWechat,
  mockLogout,
  mockRequestSmsCode,
} from '@/api/mock/auth.mock';
import { getToken } from '@/utils/auth';
import type {
  LoginResult,
  PhoneLoginParams,
  SmsCodeRequestParams,
  SmsCodeRequestResult,
  WechatLoginParams,
} from '@/types/auth';

/**
 * 生成携带 accessToken 的 Authorization Header。
 * 即使当前走 mock，真实 service 也按规范留出写法。
 */
function getAuthHeader(): Record<string, string> {
  const token = getToken();
  return token ? { Authorization: `Bearer ${token}` } : {};
}

/**
 * 微信一键登录（真实签名预留）。
 * @param params.code wx.login 拿到的 code
 */
export function loginByWechat(params: WechatLoginParams): Promise<LoginResult> {
  if (isMock()) {
    return mockLoginByWechat(params);
  }
  return request<LoginResult>({
    url: '/app/auth/login/wechat',
    method: 'POST',
    data: { code: params.code },
  });
}

/**
 * 手机号+验证码登录。
 */
export function loginByPhone(params: PhoneLoginParams): Promise<LoginResult> {
  if (isMock()) {
    return mockLoginByPhone(params);
  }
  return request<LoginResult>({
    url: '/app/auth/login/phone',
    method: 'POST',
    data: { phone: params.phone, smsCode: params.smsCode },
  });
}

/**
 * 请求短信验证码。
 */
export function requestSmsCode(
  params: SmsCodeRequestParams,
): Promise<SmsCodeRequestResult> {
  if (isMock()) {
    return mockRequestSmsCode(params);
  }
  return request<SmsCodeRequestResult>({
    url: '/app/auth/sms/request',
    method: 'POST',
    data: { phone: params.phone, scene: params.scene ?? 'login' },
  });
}

/**
 * 退出登录。
 * 真实后端应吊销 refreshToken（走 Cookie）；前端只需清本地 token。
 */
export function logout(): Promise<void> {
  if (isMock()) {
    return mockLogout();
  }
  return request<void>({
    url: '/app/auth/logout',
    method: 'POST',
    header: getAuthHeader(),
  });
}
