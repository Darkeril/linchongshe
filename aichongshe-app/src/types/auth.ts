/**
 * 登录/鉴权相关类型。
 * 字段按「真实后端接口结构」定义（API 盘点阶段若有出入再调整），
 * mock 数据必须遵循同一份类型契约。
 *
 * Token 机制（来自 CLAUDE.md 原则 6）：
 * - accessToken → HTTP Header: Authorization: Bearer <token>（短期）
 * - refreshToken → HttpOnly Cookie（长期，小程序端特殊处理见原则 6）
 *   小程序端尚不确认 HttpOnly 能否生效，此处类型先把 refreshToken 留作可选字符串
 *   （便于小程序 fallback 走 body 返回）。真实接入时由 service 层决定读取途径。
 */
import type { UserProfile } from './user';

/** 登录方式枚举 */
export type LoginProvider = 'wechat' | 'phone';

/** 微信登录入参 */
export interface WechatLoginParams {
  /** wx.login 获取的 code */
  code: string;
}

/** 手机号+验证码登录入参 */
export interface PhoneLoginParams {
  /** 11 位手机号（不含区号） */
  phone: string;
  /** 6 位短信验证码 */
  smsCode: string;
}

/** 请求短信验证码入参 */
export interface SmsCodeRequestParams {
  /** 11 位手机号 */
  phone: string;
  /** 场景：登录 / 注册 / 找回 / 绑定；暂全部走 login */
  scene?: 'login' | 'register' | 'bind' | 'reset';
}

/** 请求短信验证码返回 */
export interface SmsCodeRequestResult {
  /** 请求追踪 ID（排障用，mock 也返回假 traceId） */
  traceId: string;
  /** 下次可重发冷却时间（秒），默认 60 */
  cooldownSeconds: number;
}

/** 登录返回 */
export interface LoginResult {
  accessToken: string;
  /** 小程序端可能由 body 返回（Cookie 不生效时的 fallback） */
  refreshToken?: string;
  /** accessToken 有效期（秒） */
  expiresIn: number;
  user: UserProfile;
}
