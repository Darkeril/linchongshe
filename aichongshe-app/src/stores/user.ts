/**
 * 用户 store（Pinia）。
 * Phase 0 仅做骨架，登录态/持久化等后续迭代补齐。
 */
import { defineStore } from 'pinia';
import { ref } from 'vue';
import { getProfile } from '@/api/services/user';
import type { UserProfile } from '@/types/user';

export const useUserStore = defineStore('user', () => {
  const profile = ref<UserProfile | null>(null);
  const loading = ref(false);

  async function fetchProfile(): Promise<UserProfile | null> {
    loading.value = true;
    try {
      profile.value = await getProfile();
      return profile.value;
    } catch (err) {
      console.error('[userStore] fetchProfile failed:', err);
      profile.value = null;
      return null;
    } finally {
      loading.value = false;
    }
  }

  function reset(): void {
    profile.value = null;
  }

  return { profile, loading, fetchProfile, reset };
});
