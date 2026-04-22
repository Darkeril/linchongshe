/**
 * 用户 store（Pinia）。
 * Phase 2 补完：登录态 + 持久化 + 登出清理。
 *
 * 持久化约定（来自 CLAUDE.md 原则 6 + Phase 2 计划）：
 * - accessToken 存 uni.storage，键 `auth_access_token`（见 utils/auth.ts）
 * - UserProfile 缓存一份到 storage，键 `auth_user_profile`
 * - App.vue onLaunch 时调 restoreFromStorage() 恢复，确保刷新 / 冷启动不丢态
 */
import { defineStore } from 'pinia';
import { computed, ref } from 'vue';
import { getProfile } from '@/api/services/user';
import {
  loginByPhone as apiLoginByPhone,
  loginByWechat as apiLoginByWechat,
  logout as apiLogout,
} from '@/api/services/auth';
import {
  clearStoredProfile,
  clearToken,
  getStoredProfile,
  getToken,
  setStoredProfile,
  setToken,
} from '@/utils/auth';
import type { UserProfile } from '@/types/user';
import type { LoginResult, PhoneLoginParams, WechatLoginParams } from '@/types/auth';

export const useUserStore = defineStore('user', () => {
  const profile = ref<UserProfile | null>(null);
  const accessToken = ref<string>('');
  const loading = ref(false);

  const isLogin = computed<boolean>(() => !!accessToken.value);

  /** 将登录返回写入 state + storage */
  function applyLoginResult(result: LoginResult): void {
    accessToken.value = result.accessToken;
    profile.value = result.user;
    setToken(result.accessToken);
    setStoredProfile(result.user);
  }

  /** 微信一键登录 */
  async function loginWithWechat(params: WechatLoginParams): Promise<UserProfile> {
    loading.value = true;
    try {
      const result = await apiLoginByWechat(params);
      applyLoginResult(result);
      return result.user;
    } finally {
      loading.value = false;
    }
  }

  /** 手机号登录 */
  async function loginWithPhone(params: PhoneLoginParams): Promise<UserProfile> {
    loading.value = true;
    try {
      const result = await apiLoginByPhone(params);
      applyLoginResult(result);
      return result.user;
    } finally {
      loading.value = false;
    }
  }

  /** 刷新当前用户资料（已登录时调用） */
  async function fetchProfile(): Promise<UserProfile | null> {
    if (!isLogin.value) return null;
    loading.value = true;
    try {
      const fresh = await getProfile();
      profile.value = fresh;
      setStoredProfile(fresh);
      return fresh;
    } catch (err) {
      console.error('[userStore] fetchProfile failed:', err);
      return profile.value;
    } finally {
      loading.value = false;
    }
  }

  /** 登出：清 state + storage + 通知后端 */
  async function logout(): Promise<void> {
    try {
      await apiLogout();
    } catch (err) {
      // 登出接口失败也要清本地
      console.warn('[userStore] logout api failed, clear local anyway:', err);
    } finally {
      profile.value = null;
      accessToken.value = '';
      clearToken();
      clearStoredProfile();
    }
  }

  /** App 启动时从 storage 恢复登录态 */
  function restoreFromStorage(): void {
    const token = getToken();
    const storedProfile = getStoredProfile();
    if (token) {
      accessToken.value = token;
    }
    if (storedProfile) {
      profile.value = storedProfile;
    }
  }

  /** 清 state（供测试/切换账号用，不触发后端） */
  function reset(): void {
    profile.value = null;
    accessToken.value = '';
    clearToken();
    clearStoredProfile();
  }

  return {
    // state
    profile,
    accessToken,
    loading,
    // getters
    isLogin,
    // actions
    loginWithWechat,
    loginWithPhone,
    fetchProfile,
    logout,
    restoreFromStorage,
    reset,
  };
});
