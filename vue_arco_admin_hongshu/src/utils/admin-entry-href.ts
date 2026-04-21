/**
 * 切换「静态 / 服务端菜单」后整页跳转用：避免 reload 仍停留在旧路径（如 /dashboard/workplace）
 * 与后端 getRouters 常见「工作台」路径 /dashboard/dashboard 对齐。
 */
export default function getAdminReloadHref(menuFromServer: boolean): string {
  const base = (import.meta.env.BASE_URL || '/arco-admin/').replace(/\/?$/, '');
  const path = menuFromServer ? '/dashboard/dashboard' : '/dashboard/workplace';
  return `${window.location.origin}${base}${path}`;
}
