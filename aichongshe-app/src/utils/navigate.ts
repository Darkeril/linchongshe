/**
 * 跨端导航统一封装（gotchas #13）
 *
 * 小程序页面栈最多 10 层；`uni.navigateTo` 超限会静默失败。
 * 该函数根据当前栈深度自动降级为 redirectTo，保证强互链页（详情 / 主页 / 话题）
 * 反复跳转不会卡住用户。H5 / App 无栈限制，但走同一封装避免跨端分叉。
 *
 * 使用：
 *   await navigateSafe({ url: '/pages/publish/publish' })
 *   await navigateSafe({ url: '/pages/home/home', reset: true }) // 清栈
 *
 * 禁止：散落 `uni.navigateTo` / `uni.redirectTo` / `uni.reLaunch`。
 */

const STACK_LIMIT = 10;
const SAFE_THRESHOLD = STACK_LIMIT - 1;

export interface NavigateOptions {
  url: string;
  /** 强制 reLaunch（重置栈） */
  reset?: boolean;
  /** navigateTo 事件通道（非 reset 时可选） */
  events?: Record<string, (...args: unknown[]) => void>;
}

export function navigateSafe(options: NavigateOptions): Promise<void> {
  return new Promise((resolve, reject) => {
    if (options.reset) {
      uni.reLaunch({
        url: options.url,
        success: () => resolve(),
        fail: reject,
      });
      return;
    }
    const depth = getCurrentPages().length;
    if (depth >= SAFE_THRESHOLD) {
      uni.redirectTo({
        url: options.url,
        success: () => resolve(),
        fail: reject,
      });
    } else {
      uni.navigateTo({
        url: options.url,
        events: options.events,
        success: () => resolve(),
        fail: reject,
      });
    }
  });
}

/**
 * 切换到 tabBar 页。调用前提：pagePath 必须在 pages.json `tabBar.list` 里。
 * 不在 list 的页面（如 publish）用 `navigateSafe`。
 */
export function switchTab(url: string): Promise<void> {
  return new Promise((resolve, reject) => {
    uni.switchTab({
      url,
      success: () => resolve(),
      fail: reject,
    });
  });
}
