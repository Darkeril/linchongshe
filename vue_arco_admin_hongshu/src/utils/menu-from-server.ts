import defaultSettings from '@/config/settings.json';
import cache from '@/plugins/cache';

/**
 * 是否与 Pinia app 初始 state 一致启用「服务端菜单」。
 * 优先读本地 app-settings（与界面开关一致），避免仅改 env 与 store 不一致。
 */
export default function isMenuFromServer(): boolean {
  const saved = cache.local.getJSON('app-settings') as { menuFromServer?: boolean } | null;
  if (typeof saved?.menuFromServer === 'boolean') {
    return saved.menuFromServer;
  }
  if (import.meta.env.VITE_MENU_FROM_SERVER === 'true') {
    return true;
  }
  return (defaultSettings as { menuFromServer?: boolean }).menuFromServer === true;
}
