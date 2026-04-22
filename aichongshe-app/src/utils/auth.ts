/**
 * 登录态本地持久化工具。
 *
 * 跨端使用 uni.getStorageSync / uni.setStorageSync（H5/小程序/App 都兼容）。
 * Pinia store 在 login/logout 时读写这里，刷新后也通过这里恢复。
 *
 * 约定（来自 CLAUDE.md 原则 6 + 原则 7）：
 * - accessToken 通过 Header Authorization: Bearer 下发，不写在 URL query
 * - refreshToken 理论上走 HttpOnly Cookie；小程序环境可能需要 body fallback
 *   Phase 2 mock 阶段还没有真实 refresh 流程，这里只持久化 accessToken 与 profile
 */
import type { UserProfile } from '@/types/user';

const KEY_TOKEN = 'auth_access_token';
const KEY_PROFILE = 'auth_user_profile';

/** 读取 accessToken；无则返回空串 */
export function getToken(): string {
  try {
    return (uni.getStorageSync(KEY_TOKEN) as string) || '';
  } catch {
    return '';
  }
}

/** 写入 accessToken */
export function setToken(token: string): void {
  try {
    uni.setStorageSync(KEY_TOKEN, token);
  } catch (err) {
    console.error('[auth] setToken failed:', err);
  }
}

/** 清除 accessToken */
export function clearToken(): void {
  try {
    uni.removeStorageSync(KEY_TOKEN);
  } catch (err) {
    console.error('[auth] clearToken failed:', err);
  }
}

/** 是否持有有效 token（当前仅判空，真实实现应解析 exp） */
export function hasToken(): boolean {
  return getToken().length > 0;
}

/** 读取持久化的用户资料 */
export function getStoredProfile(): UserProfile | null {
  try {
    const raw = uni.getStorageSync(KEY_PROFILE);
    if (!raw) return null;
    return typeof raw === 'string' ? (JSON.parse(raw) as UserProfile) : (raw as UserProfile);
  } catch {
    return null;
  }
}

/** 持久化用户资料 */
export function setStoredProfile(profile: UserProfile): void {
  try {
    uni.setStorageSync(KEY_PROFILE, JSON.stringify(profile));
  } catch (err) {
    console.error('[auth] setStoredProfile failed:', err);
  }
}

/** 清除用户资料 */
export function clearStoredProfile(): void {
  try {
    uni.removeStorageSync(KEY_PROFILE);
  } catch (err) {
    console.error('[auth] clearStoredProfile failed:', err);
  }
}
