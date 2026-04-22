/**
 * 登录拦截中间件（Phase 2 最小版）。
 *
 * 对标 CLAUDE.md 原则 6 的完整版：
 *   - 未登录时弹底部半屏 Sheet（不离开当前页）
 *   - 缓存原动作（action replay），登录成功后自动执行
 *
 * 当前 Phase 2 只做最简实现：未登录直接 reLaunch 到 /pages/login/login。
 * Sheet + action replay 会在 Phase 3 首页实现点赞 / 关注 / 收藏时补齐。
 *
 * 使用示例：
 *   const { requireAuth } = useAuthGuard();
 *   requireAuth(() => doLike(noteId));
 */
import { useUserStore } from '@/stores/user';

export interface RequireAuthOptions {
  /** 拦截触发时的可选提示（预留给 Phase 3 Sheet 用，当前版本未使用） */
  reason?: string;
  /** 未登录时跳转目标；默认 /pages/login/login */
  redirect?: string;
}

export function useAuthGuard() {
  const userStore = useUserStore();

  /**
   * 包装一个需要登录的动作。
   * - 已登录 → 立即执行 action
   * - 未登录 → 跳转 Login 页（Phase 3 会升级为 Sheet + replay）
   */
  function requireAuth(action: () => void, options: RequireAuthOptions = {}): void {
    if (userStore.isLogin) {
      action();
      return;
    }
    // TODO(Phase 3): 替换为底部 Sheet + 动作回放；Phase 2 先做 redirect
    const target = options.redirect ?? '/pages/login/login';
    uni.reLaunch({ url: target });
  }

  return { requireAuth };
}
