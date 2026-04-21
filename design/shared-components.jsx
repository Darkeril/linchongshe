// shared-components.jsx — 跨页面复用：TopBar、通用小组件
// （StatusLine 和 AppTabBar 已在 screens-part1.jsx 里定义）

// 通用顶部栏
function TopBar({ palette, title, left = 'back', right, onBack, bg, onRight, subtitle, transparent }) {
  const p = palette;
  return (
    <div style={{
      position: 'absolute', top: 38, left: 0, right: 0, height: 50,
      display: 'flex', alignItems: 'center', justifyContent: 'space-between',
      padding: '0 14px', zIndex: 30,
      background: transparent ? 'transparent' : (bg || 'transparent'),
    }}>
      <div style={{ width: 40, height: 36, display: 'flex', alignItems: 'center', justifyContent: 'flex-start', cursor: 'pointer' }}
        onClick={onBack}>
        {left === 'back' && (
          <div style={{
            width: 34, height: 34, borderRadius: 17,
            background: transparent ? 'rgba(255,255,255,0.85)' : 'transparent',
            backdropFilter: transparent ? 'blur(8px)' : 'none',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>
            <svg width="10" height="18" viewBox="0 0 10 18" fill="none" stroke={p.ink} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M8 2L2 9l6 7" />
            </svg>
          </div>
        )}
        {left === 'close' && (
          <svg width="18" height="18" viewBox="0 0 18 18" fill="none" stroke={p.ink} strokeWidth="2" strokeLinecap="round">
            <path d="M3 3l12 12M15 3L3 15" />
          </svg>
        )}
        {left === 'none' && null}
      </div>
      <div style={{ textAlign: 'center', flex: 1 }}>
        <div style={{ fontSize: 16, fontWeight: 700, color: p.ink, fontFamily: TYPE.body }}>{title}</div>
        {subtitle && <div style={{ fontSize: 10, color: p.inkMuted, marginTop: 1 }}>{subtitle}</div>}
      </div>
      <div style={{ width: 40, display: 'flex', justifyContent: 'flex-end', cursor: 'pointer' }} onClick={onRight}>
        {right}
      </div>
    </div>
  );
}

// 通用小圆标签（tag）
function Tag({ palette, children, tone = 'soft', size = 'sm' }) {
  const p = palette;
  const tones = {
    soft: { bg: p.primarySoft, fg: p.primaryInk },
    ghost: { bg: p.surfaceDim, fg: p.inkSoft },
    accent: { bg: p.accent + '33', fg: p.accent },
    warn: { bg: p.warn + '22', fg: p.warn },
    primary: { bg: p.primary, fg: '#fff' },
  };
  const t = tones[tone] || tones.soft;
  const sizes = {
    xs: { p: '2px 7px', f: 9, r: 8 },
    sm: { p: '3px 9px', f: 10, r: 10 },
    md: { p: '5px 11px', f: 11, r: 12 },
  };
  const sz = sizes[size] || sizes.sm;
  return (
    <span style={{
      display: 'inline-flex', alignItems: 'center', gap: 3,
      padding: sz.p, borderRadius: sz.r,
      background: t.bg, color: t.fg,
      fontSize: sz.f, fontWeight: 600,
      fontFamily: TYPE.body, whiteSpace: 'nowrap',
    }}>
      {children}
    </span>
  );
}

// 通用段标题
function SectionTitle({ palette, title, right, subtitle, style = {} }) {
  const p = palette;
  return (
    <div style={{
      display: 'flex', alignItems: 'flex-end', justifyContent: 'space-between',
      padding: '0 18px', margin: '18px 0 12px', ...style,
    }}>
      <div>
        <div style={{ fontSize: 17, fontWeight: 800, color: p.ink, fontFamily: TYPE.brand, letterSpacing: -0.2 }}>
          {title}
        </div>
        {subtitle && <div style={{ fontSize: 11, color: p.inkMuted, marginTop: 2 }}>{subtitle}</div>}
      </div>
      {right && <div style={{ fontSize: 12, color: p.inkSoft }}>{right}</div>}
    </div>
  );
}

Object.assign(window, { TopBar, Tag, SectionTitle });
