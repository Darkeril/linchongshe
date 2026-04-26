/**
 * 登录拦截中间件（Phase 6 升级）。
 *
 * 对标 CLAUDE.md 原则 6：未登录时弹底部 Sheet（不离开当前页）→
 * 登录成功后自动执行原动作（action replay）。
 *
 * Promise 语义：
 *   - 已登录                       → 直接 await action()，resolve(result)
 *   - 未登录 + 用户登录成功         → 执行 action（动作回放），resolve(result)
 *   - 未登录 + 用户取消（关闭 Sheet）→ reject({ code: 'AUTH_CANCELLED' })
 *   - 未登录 + 被新动作替换         → reject({ code: 'AUTH_REPLACED' })
 *   - 登录成功后回放抛错           → reject({ code: 'ACTION_FAILED', message })
 *
 * 使用示例：
 *   const { requireAuth } = useAuthGuard();
 *   try {
 *     await requireAuth(async () => { await likeNote(noteId); });
 *   } catch (err) {
 *     const code = (err as { code?: string })?.code;
 *     if (code === 'AUTH_CANCELLED' || code === 'AUTH_REPLACED') return; // 静默
 *     uni.showToast({ title: '操作失败', icon: 'none' });
 *   }
 */
import { useUserStore } from '@/stores/user';
import { useAuthSheet } from '@/stores/authSheet';

export function useAuthGuard() {
  /**
   * 包装一个需要登录的动作。
   * 已登录立即执行；未登录弹 Sheet 等登录成功后回放。
   */
  async function requireAuth<T>(action: () => T | Promise<T>): Promise<T> {
    const userStore = useUserStore();
    if (userStore.isLogin) {
      return await action();
    }
    return new Promise<T>((resolve, reject) => {
      useAuthSheet().requestAuth<T>(action, { resolve, reject });
    });
  }

  return { requireAuth };
}
