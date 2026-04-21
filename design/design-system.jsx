// design-system.jsx — 爱宠社重新设计的视觉系统
// 3 套色板 × 共享几何 / 字体 / 组件。通过 CSS 变量在 Tweaks 面板里实时切换。

// ─── 3 套色板 ───────────────────────────────────────────────
const PALETTES = {
  cream: {
    id: 'cream',
    name: '奶油治愈',
    caption: '米白 · 蜜桃 · 柔软克制',
    // 底色
    bg: '#FBF6EF',
    surface: '#FFFFFF',
    surfaceDim: '#F5EEE2',
    divider: 'rgba(85, 60, 40, 0.08)',
    // 品牌主色（暖橙/蜜桃）
    primary: '#F29766',       // 蜜桃橙
    primarySoft: '#FFD8BF',
    primaryInk: '#B45A2E',
    // 辅助
    accent: '#6FB89A',        // 抹茶点缀
    warn: '#E8846E',          // 珊瑚
    // 文字
    ink: '#2A2017',
    inkSoft: '#6F5A48',
    inkMuted: '#9E8872',
    inkFaint: '#C9B8A1',
    // 肉垫/萌感装饰
    paw: '#F29766',
  },
  peach: {
    id: 'peach',
    name: '蜜桃活力',
    caption: '粉橙 · 奶油 · 活泼甜感',
    bg: '#FFF4EE',
    surface: '#FFFFFF',
    surfaceDim: '#FFE6D5',
    divider: 'rgba(190, 80, 60, 0.08)',
    primary: '#FF7A59',
    primarySoft: '#FFCCB8',
    primaryInk: '#C44820',
    accent: '#FFB84D',
    warn: '#E8526E',
    ink: '#2E1D14',
    inkSoft: '#7A4E3B',
    inkMuted: '#A88371',
    inkFaint: '#D8BFAD',
    paw: '#FF7A59',
  },
  latte: {
    id: 'latte',
    name: '暖咖温润',
    caption: '焦糖 · 奶茶 · 成熟温润',
    bg: '#F4EDE2',
    surface: '#FFFFFF',
    surfaceDim: '#EADFCB',
    divider: 'rgba(80, 50, 30, 0.1)',
    primary: '#C97B4A',
    primarySoft: '#EFCFB0',
    primaryInk: '#8A4A1F',
    accent: '#7BA58E',
    warn: '#D66A5E',
    ink: '#231710',
    inkSoft: '#63463A',
    inkMuted: '#8F7260',
    inkFaint: '#C4AC95',
    paw: '#C97B4A',
  },
};

// ─── 字体 & 几何 ─────────────────────────────────────────────
const TYPE = {
  // 中文正文（圆润感）：Noto Sans SC
  // 中文强调（可爱一点）：LXGW WenKai / ZCOOL KuaiLe
  // 英文数字：Baloo 2（萌圆体）/ 系统字
  body: '"Noto Sans SC", -apple-system, BlinkMacSystemFont, "PingFang SC", "Helvetica Neue", sans-serif',
  brand: '"Baloo 2", "ZCOOL KuaiLe", "Noto Sans SC", sans-serif',
  num: '"Baloo 2", -apple-system, system-ui, sans-serif',
};

const RADIUS = { sm: 10, md: 16, lg: 22, xl: 28, pill: 999 };

// ─── 通用装饰 SVG ────────────────────────────────────────────
// 小肉垫图标
function PawIcon({ size = 16, color = 'currentColor', style = {} }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill={color} style={style}>
      <ellipse cx="6" cy="9" rx="2.2" ry="3" />
      <ellipse cx="10.5" cy="6" rx="2" ry="2.8" />
      <ellipse cx="15" cy="6" rx="2" ry="2.8" />
      <ellipse cx="19" cy="9" rx="2.2" ry="3" />
      <path d="M12.5 10c-3.2 0-5.8 2.6-5.8 5.4 0 2 1.6 3.6 3.6 3.6 1.1 0 2-0.5 2.2-0.5 0.2 0 1.1 0.5 2.2 0.5 2 0 3.6-1.6 3.6-3.6 0-2.8-2.6-5.4-5.8-5.4z" />
    </svg>
  );
}

// 爱心
function HeartIcon({ size = 20, color = 'currentColor', filled = false, style = {} }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24"
      fill={filled ? color : 'none'} stroke={color} strokeWidth={filled ? 0 : 1.8}
      strokeLinecap="round" strokeLinejoin="round" style={style}>
      <path d="M20.8 6.6a5.5 5.5 0 00-9.3-2 5.5 5.5 0 00-9.3 4.5c0 5.2 9.3 11 9.3 11s9.3-5.8 9.3-11a5.4 5.4 0 000-2.5z" />
    </svg>
  );
}

function BookmarkIcon({ size = 20, color = 'currentColor', filled = false, style = {} }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24"
      fill={filled ? color : 'none'} stroke={color} strokeWidth={1.8}
      strokeLinecap="round" strokeLinejoin="round" style={style}>
      <path d="M5 4.5a2 2 0 012-2h10a2 2 0 012 2V22l-7-4-7 4V4.5z" />
    </svg>
  );
}

function ChatIcon({ size = 20, color = 'currentColor', style = {} }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={color}
      strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round" style={style}>
      <path d="M21 11.5c0 4.1-4 7.5-9 7.5a10 10 0 01-4-.8L3 20l1.5-4A7.1 7.1 0 013 11.5C3 7.4 7 4 12 4s9 3.4 9 7.5z" />
    </svg>
  );
}

function ShareIcon({ size = 20, color = 'currentColor', style = {} }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={color}
      strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round" style={style}>
      <circle cx="18" cy="5" r="2.5" />
      <circle cx="6" cy="12" r="2.5" />
      <circle cx="18" cy="19" r="2.5" />
      <path d="M8.2 10.7L15.8 6.4M8.2 13.3L15.8 17.6" />
    </svg>
  );
}

function SearchIcon({ size = 20, color = 'currentColor', style = {} }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={color}
      strokeWidth={2} strokeLinecap="round" strokeLinejoin="round" style={style}>
      <circle cx="11" cy="11" r="7" />
      <path d="M20 20l-3.5-3.5" />
    </svg>
  );
}

function HomeIcon({ size = 22, color = 'currentColor', filled = false, style = {} }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24"
      fill={filled ? color : 'none'} stroke={color} strokeWidth={1.8}
      strokeLinecap="round" strokeLinejoin="round" style={style}>
      <path d="M3 11l9-7.5L21 11v9a1.5 1.5 0 01-1.5 1.5h-4V15h-7v6.5h-4A1.5 1.5 0 013 20v-9z" />
    </svg>
  );
}

function BagIcon({ size = 22, color = 'currentColor', filled = false, style = {} }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24"
      fill={filled ? color : 'none'} stroke={color} strokeWidth={1.8}
      strokeLinecap="round" strokeLinejoin="round" style={style}>
      <path d="M5 8h14l-1 13H6L5 8z" />
      <path d="M9 8V6a3 3 0 016 0v2" />
    </svg>
  );
}

function MsgIcon({ size = 22, color = 'currentColor', filled = false, style = {} }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24"
      fill={filled ? color : 'none'} stroke={color} strokeWidth={1.8}
      strokeLinecap="round" strokeLinejoin="round" style={style}>
      <path d="M21 11.5c0 4.1-4 7.5-9 7.5a10 10 0 01-4-.8L3 20l1.5-4A7.1 7.1 0 013 11.5C3 7.4 7 4 12 4s9 3.4 9 7.5z" />
    </svg>
  );
}

function MeIcon({ size = 22, color = 'currentColor', filled = false, style = {} }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24"
      fill={filled ? color : 'none'} stroke={color} strokeWidth={1.8}
      strokeLinecap="round" strokeLinejoin="round" style={style}>
      <circle cx="12" cy="8" r="4" />
      <path d="M4 21c0-4.4 3.6-8 8-8s8 3.6 8 8" />
    </svg>
  );
}

function PlusIcon({ size = 26, color = '#fff', style = {} }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={color}
      strokeWidth={2.6} strokeLinecap="round" style={style}>
      <path d="M12 5v14M5 12h14" />
    </svg>
  );
}

function BellIcon({ size = 20, color = 'currentColor', style = {} }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={color}
      strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round" style={style}>
      <path d="M18 16H6l1.5-2v-4a4.5 4.5 0 019 0v4L18 16z" />
      <path d="M10.5 20a1.5 1.5 0 003 0" />
    </svg>
  );
}

function CameraIcon({ size = 22, color = 'currentColor', style = {} }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={color}
      strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round" style={style}>
      <path d="M3 8.5h3l2-3h8l2 3h3v11H3v-11z" />
      <circle cx="12" cy="13.5" r="3.5" />
    </svg>
  );
}

// 萌宠 emoji 风格占位头像 / 图片（纯 SVG，用于不依赖外部资源）
function PetAvatar({ variant = 'dog', size = 40, bg }) {
  const palettes = {
    dog: { bg: bg || '#FFD8BF', face: '#F4C9A4', ear: '#C97B4A', nose: '#3a2a20' },
    cat: { bg: bg || '#FFE6D5', face: '#FBE6CE', ear: '#E8846E', nose: '#3a2a20' },
    fluff: { bg: bg || '#EFCFB0', face: '#F4DDC2', ear: '#A67050', nose: '#3a2a20' },
    puppy: { bg: bg || '#FFCCB8', face: '#FBDAC4', ear: '#6FB89A', nose: '#3a2a20' },
    mochi: { bg: bg || '#F5EEE2', face: '#FFFFFF', ear: '#C4AC95', nose: '#3a2a20' },
  };
  const c = palettes[variant] || palettes.dog;
  return (
    <svg width={size} height={size} viewBox="0 0 60 60" style={{ display: 'block', borderRadius: '50%' }}>
      <rect width="60" height="60" fill={c.bg} rx="30" />
      {/* ears */}
      <ellipse cx="17" cy="22" rx="7" ry="10" fill={c.ear} transform="rotate(-18 17 22)" />
      <ellipse cx="43" cy="22" rx="7" ry="10" fill={c.ear} transform="rotate(18 43 22)" />
      {/* face */}
      <ellipse cx="30" cy="34" rx="16" ry="15" fill={c.face} />
      {/* eyes */}
      <ellipse cx="24" cy="32" rx="1.8" ry="2.3" fill={c.nose} />
      <ellipse cx="36" cy="32" rx="1.8" ry="2.3" fill={c.nose} />
      {/* eye shine */}
      <circle cx="24.5" cy="31.2" r="0.6" fill="#fff" />
      <circle cx="36.5" cy="31.2" r="0.6" fill="#fff" />
      {/* nose */}
      <ellipse cx="30" cy="38" rx="1.6" ry="1.2" fill={c.nose} />
      {/* mouth */}
      <path d="M28 40 Q30 42 32 40" stroke={c.nose} strokeWidth="1" fill="none" strokeLinecap="round" />
      {/* blush */}
      <circle cx="21" cy="38" r="2.2" fill="#FFB4A0" opacity="0.5" />
      <circle cx="39" cy="38" r="2.2" fill="#FFB4A0" opacity="0.5" />
    </svg>
  );
}

// 占位图（瀑布流图片） — 柔和渐变 + 隐约萌宠轮廓
function PetPlaceholder({ w = 150, h = 200, palette, seed = 0, variant = 'dog' }) {
  const p = palette || PALETTES.cream;
  const bgs = [
    [p.primarySoft, p.surfaceDim],
    [p.surfaceDim, p.primarySoft],
    [p.primarySoft, '#FFFFFF'],
    ['#F5E1D3', p.primarySoft],
    [p.surfaceDim, '#FFF4EE'],
  ];
  const [c1, c2] = bgs[seed % bgs.length];
  const gid = `pg${seed}-${variant}`;
  return (
    <svg width={w} height={h} viewBox={`0 0 ${w} ${h}`} style={{ display: 'block' }} preserveAspectRatio="xMidYMid slice">
      <defs>
        <linearGradient id={gid} x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stopColor={c1} />
          <stop offset="100%" stopColor={c2} />
        </linearGradient>
      </defs>
      <rect width={w} height={h} fill={`url(#${gid})`} />
      {/* 隐约宠物轮廓 */}
      <g opacity="0.55" transform={`translate(${w / 2 - 40}, ${h / 2 - 30}) scale(1.4)`}>
        <PetAvatarShape variant={variant} />
      </g>
      {/* 小爪印 */}
      <g fill={p.primary} opacity="0.35">
        <circle cx={w * 0.15} cy={h * 0.85} r="2" />
        <circle cx={w * 0.18} cy={h * 0.82} r="1.5" />
        <circle cx={w * 0.2} cy={h * 0.86} r="1.5" />
      </g>
    </svg>
  );
}

function PetAvatarShape({ variant = 'dog' }) {
  // 简化版形状，给 placeholder 用
  return (
    <g>
      <ellipse cx="30" cy="34" rx="22" ry="20" fill="rgba(255,255,255,0.55)" />
      <ellipse cx="14" cy="20" rx="6" ry="9" fill="rgba(255,255,255,0.5)" transform="rotate(-18 14 20)" />
      <ellipse cx="46" cy="20" rx="6" ry="9" fill="rgba(255,255,255,0.5)" transform="rotate(18 46 20)" />
    </g>
  );
}

Object.assign(window, {
  PALETTES, TYPE, RADIUS,
  PawIcon, HeartIcon, BookmarkIcon, ChatIcon, ShareIcon, SearchIcon,
  HomeIcon, BagIcon, MsgIcon, MeIcon, PlusIcon, BellIcon, CameraIcon,
  PetAvatar, PetPlaceholder,
});
