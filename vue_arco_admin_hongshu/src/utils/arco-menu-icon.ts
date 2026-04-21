import * as ArcoIcons from '@arco-design/web-vue/es/icon';

/** 所有 Arco 图标导出名（PascalCase，如 IconHome），已排序 */
export const ARCO_MENU_ICON_NAMES = (Object.keys(ArcoIcons) as string[])
  .filter((k) => k.startsWith('Icon'))
  .sort((a, b) => a.localeCompare(b));

const ARCO_ICON_NAME_SET = new Set(ARCO_MENU_ICON_NAMES);

/** kebab 的图标路径/类名 → IconXxx，如 icon-settings → IconSettings */
export function kebabToIconComponentName(raw: string): string | null {
  const t = raw.trim().toLowerCase();
  if (!t.startsWith('icon-')) return null;
  const body = t.slice(5);
  const parts = body.split('-').filter(Boolean);
  if (!parts.length) return null;
  const pascal = parts.map((p) => p.charAt(0).toUpperCase() + p.slice(1)).join('');
  return `Icon${pascal}`;
}

/**
 * 保存前规范化：只接受 Arco 导出的组件名；支持手输 icon-xxx 形式。
 * 无法识别时原样返回（便于后续 Arco 升级后新图标仍可用）。
 */
export function normalizeMenuIconForSave(raw?: string | null): string {
  if (raw == null || String(raw).trim() === '') return '';
  const s = String(raw).trim();
  if (ARCO_ICON_NAME_SET.has(s)) return s;
  const fromKebab = kebabToIconComponentName(s);
  if (fromKebab && ARCO_ICON_NAME_SET.has(fromKebab)) return fromKebab;
  if (s.startsWith('Icon')) return s;
  return s;
}

export { ArcoIcons };
