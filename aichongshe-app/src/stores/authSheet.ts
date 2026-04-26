/**
 * LoginSheet 全局 store（Phase 6）。
 *
 * 用途：useAuthGuard.requireAuth 在未登录时把原动作 + Promise resolver 暂存于此，
 *      LoginSheet 组件根据 open 状态打开 / 关闭，登录成功后驱动动作回放。
 *
 * 三态约定（Promise 语义）：
 *   - confirmLoginSuccess() → 执行 pendingAction，resolve(action 的返回值)
 *   - cancel()              → reject({ code: 'AUTH_CANCELLED' })
 *   - 连续触发              → 旧 resolver reject({ code: 'AUTH_REPLACED' })
 *
 * 内存安全：cancel / confirm / replace 三处都显式置空 pendingAction + resolver，
 *           避免闭包中持有的对象引用泄漏。
 */
import { defineStore } from 'pinia';
import { ref } from 'vue';

export interface AuthSheetError {
  code: 'AUTH_CANCELLED' | 'AUTH_REPLACED' | 'ACTION_FAILED';
  message?: string;
}

interface AuthSheetResolver<T = unknown> {
  resolve: (v: T) => void;
  reject: (e: AuthSheetError) => void;
}

export const useAuthSheet = defineStore('authSheet', () => {
  const open = ref<boolean>(false);
  // 用 unknown 弱类型保存（运行时是泛型 T 的具体类型）
  const pendingAction = ref<(() => unknown | Promise<unknown>) | null>(null);
  const pendingResolver = ref<AuthSheetResolver | null>(null);

  /** 进入待登录状态：缓存原动作 + Promise resolver，打开 Sheet */
  function requestAuth<T>(
    action: () => T | Promise<T>,
    resolver: AuthSheetResolver<T>,
  ): void {
    // 防御：连续触发 → 旧 resolver 静默 reject，新 action 替换
    if (pendingResolver.value) {
      pendingResolver.value.reject({ code: 'AUTH_REPLACED' });
    }
    pendingAction.value = action as () => unknown;
    pendingResolver.value = resolver as AuthSheetResolver;
    open.value = true;
  }

  /** 登录成功：关闭 Sheet → 执行 pendingAction → resolve / reject 调用方 */
  async function confirmLoginSuccess(): Promise<void> {
    const action = pendingAction.value;
    const resolver = pendingResolver.value;
    open.value = false;
    pendingAction.value = null;
    pendingResolver.value = null;
    if (action && resolver) {
      try {
        const result = await action();
        resolver.resolve(result);
      } catch (err) {
        resolver.reject({
          code: 'ACTION_FAILED',
          message: err instanceof Error ? err.message : 'unknown',
        });
      }
    }
  }

  /** 用户取消：清状态 + reject(AUTH_CANCELLED) */
  function cancel(): void {
    const resolver = pendingResolver.value;
    open.value = false;
    pendingAction.value = null;
    pendingResolver.value = null;
    if (resolver) {
      resolver.reject({ code: 'AUTH_CANCELLED' });
    }
  }

  return { open, requestAuth, confirmLoginSuccess, cancel };
});
