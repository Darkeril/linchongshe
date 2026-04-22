/**
 * 爱宠社 Design Tokens (TypeScript)
 *
 * 来源：design/design-system.jsx 的 PALETTES.latte / TYPE / RADIUS
 * 警告：本文件与 tokens.scss 必须同源同值，改动请双向同步。
 *
 * 使用建议：
 * - SCSS 样式中请用 `src/styles/tokens.scss` 变量
 * - TS / 脚本 / 运行时计算（如 canvas、nvue style 对象）引用本文件
 */

// ── 颜色：latte 暖咖温润 ───────────────────────────────────
export const COLOR = {
  // 底色
  bg: '#F4EDE2',
  surface: '#FFFFFF',
  surfaceDim: '#EADFCB',
  divider: 'rgba(80, 50, 30, 0.1)',

  // 品牌主色（焦糖）
  primary: '#C97B4A',
  primarySoft: '#EFCFB0',
  primaryInk: '#8A4A1F',

  // 辅助
  accent: '#7BA58E',
  warn: '#D66A5E',

  // 文字（深浅梯度）
  ink: '#231710',
  inkSoft: '#63463A',
  inkMuted: '#8F7260',
  inkFaint: '#C4AC95',

  // 装饰
  paw: '#C97B4A',
} as const;
export type ColorToken = keyof typeof COLOR;

// ── 圆角 ─────────────────────────────────────────────────
export const RADIUS = {
  sm: 10,
  md: 16,
  lg: 22,
  xl: 28,
  pill: 999,
} as const;
export type RadiusToken = keyof typeof RADIUS;

// ── 间距 ─────────────────────────────────────────────────
export const SPACING = {
  xxs: 2,
  xs: 4,
  sm: 8,
  md: 12,
  lg: 16,
  xl: 20,
  '2xl': 24,
  '3xl': 32,
  '4xl': 40,
  '5xl': 48,
} as const;
export type SpacingToken = keyof typeof SPACING;

// ── 字号 ─────────────────────────────────────────────────
export const FONT_SIZE = {
  xs: 10,
  sm: 12,
  base: 14,
  md: 16,
  lg: 18,
  xl: 20,
  '2xl': 24,
  '3xl': 28,
  '4xl': 32,
} as const;
export type FontSizeToken = keyof typeof FONT_SIZE;

// ── 字重 ─────────────────────────────────────────────────
export const FONT_WEIGHT = {
  regular: 400,
  medium: 500,
  semibold: 600,
  bold: 700,
} as const;
export type FontWeightToken = keyof typeof FONT_WEIGHT;

// ── 行高 ─────────────────────────────────────────────────
export const LINE_HEIGHT = {
  tight: 1.2,
  normal: 1.4,
  relaxed: 1.6,
} as const;
export type LineHeightToken = keyof typeof LINE_HEIGHT;

// ── 字体族 ───────────────────────────────────────────────
export const FONT_FAMILY = {
  body: '"Noto Sans SC", -apple-system, BlinkMacSystemFont, "PingFang SC", "Helvetica Neue", sans-serif',
  brand: '"Baloo 2", "ZCOOL KuaiLe", "Noto Sans SC", sans-serif',
  num: '"Baloo 2", -apple-system, system-ui, sans-serif',
} as const;
export type FontFamilyToken = keyof typeof FONT_FAMILY;

// ── 阴影 ─────────────────────────────────────────────────
export const SHADOW = {
  sm: '0 1px 2px rgba(80, 50, 30, 0.06)',
  md: '0 4px 12px rgba(80, 50, 30, 0.08)',
  lg: '0 8px 24px rgba(80, 50, 30, 0.12)',
  xl: '0 16px 40px rgba(80, 50, 30, 0.16)',
} as const;
export type ShadowToken = keyof typeof SHADOW;

// ── Z-Index ──────────────────────────────────────────────
export const Z_INDEX = {
  base: 1,
  dropdown: 100,
  sticky: 200,
  overlay: 500,
  modal: 900,
  toast: 1000,
} as const;
export type ZIndexToken = keyof typeof Z_INDEX;

// ── 一体化汇总 ───────────────────────────────────────────
export const DESIGN_TOKENS = {
  color: COLOR,
  radius: RADIUS,
  spacing: SPACING,
  fontSize: FONT_SIZE,
  fontWeight: FONT_WEIGHT,
  lineHeight: LINE_HEIGHT,
  fontFamily: FONT_FAMILY,
  shadow: SHADOW,
  zIndex: Z_INDEX,
} as const;

export type DesignTokens = typeof DESIGN_TOKENS;
