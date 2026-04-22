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

// ── 间距（从 design/screens-part*.jsx 提取，按实际频率归纳） ─
// 设计稿不严格走 4px 栅格，小间距 2/3/5/6/10/14 出现频率极高
export const SPACING = {
  xxs: 2,   // tag 小徽章内边距、图标间细缝
  xs: 4,    // 图标组/文字行间 gap 最常用
  sm: 6,    // 中高频，按钮内间距、次级 gap
  md: 8,    // 最常见卡片内 gap
  lg: 10,   // 极高频，标题与副文间距
  xl: 12,   // 卡片内大 padding
  '2xl': 14, // 页面边距（左右 14 最常用）
  '3xl': 16, // 大块 padding
  '4xl': 20, // 区块之间的大留白
  '5xl': 24, // 超大留白、Hero 底部
} as const;
export type SpacingToken = keyof typeof SPACING;

// ── 字号（从 design/screens-part*.jsx 提取） ─────────────
// 设计稿偏小字，9~14px 覆盖绝大多数内容，大字集中在 18/22/26
export const FONT_SIZE = {
  xs: 10,    // 标签、次要元信息
  sm: 11,    // 说明文字
  base: 12,  // 正文基准
  md: 13,    // 列表主文
  lg: 14,    // 强调正文、按钮
  xl: 16,    // 小标题
  '2xl': 18, // 区块标题
  '3xl': 22, // 页面标题
  '4xl': 26, // 品牌大标题、数字
} as const;
export type FontSizeToken = keyof typeof FONT_SIZE;

// ── 字重（从 design/screens-part*.jsx 提取） ─────────────
// 设计稿实际使用：500/600/700/800，最高频是 700
export const FONT_WEIGHT = {
  regular: 400,
  medium: 500,
  semibold: 600,
  bold: 700,
  black: 800, // 品牌字体 Baloo 2 大标题使用
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

// ── 阴影（从 design/screens-part*.jsx 提取的阴影模式归纳） ─
// 设计稿最常用：2px 6/8px、4px 12px、6/8px 16-24px、10px+ 30px
// 保持 latte 暖棕基调（alpha 偏浅，不要黑压压）
export const SHADOW = {
  sm: '0 2px 6px rgba(80, 50, 30, 0.08)',   // 浅卡片、小徽章
  md: '0 4px 12px rgba(80, 50, 30, 0.12)',  // 常规卡片/按钮
  lg: '0 6px 18px rgba(80, 50, 30, 0.15)',  // 浮起卡片、悬浮面板
  xl: '0 10px 30px rgba(80, 50, 30, 0.20)', // 模态、Hero 深阴影
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
