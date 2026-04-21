// screens.jsx — 爱宠社 5 个核心屏幕
// 每个屏幕都接收 palette（3 套色板之一）和 radius（整体圆角倍率）
// Splash / Login / Home / NoteDetail / Publish

// 通用小组件 -------------------------------------------------
function StatusLine({ color = '#000' }) {
  // 精简 iOS 状态条（用在全屏幕里）
  return (
    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '14px 26px 6px', fontFamily: 'inherit', color, fontSize: 15, fontWeight: 600 }}>
      <span style={{ letterSpacing: 0.2 }}>9:41</span>
      <div style={{ width: 120, height: 24 }} />
      <div style={{ display: 'flex', gap: 5, alignItems: 'center' }}>
        <svg width="17" height="11" viewBox="0 0 17 11" fill={color}>
          <rect x="0" y="7" width="2.6" height="4" rx="0.5" /><rect x="4" y="5" width="2.6" height="6" rx="0.5" /><rect x="8" y="2.5" width="2.6" height="8.5" rx="0.5" /><rect x="12" y="0" width="2.6" height="11" rx="0.5" />
        </svg>
        <svg width="15" height="11" viewBox="0 0 15 11" fill={color}>
          <path d="M7.5 3C9.5 3 11.3 3.8 12.6 5.1l1-1A8 8 0 007.5 1.5 8 8 0 001.4 4.1l1 1C3.7 3.8 5.5 3 7.5 3z" /><path d="M7.5 6.2c1.2 0 2.3 0.5 3.1 1.3l1-1a6 6 0 00-8.2 0l1 1c0.8-0.8 1.9-1.3 3.1-1.3z" /><circle cx="7.5" cy="9.2" r="1.3" />
        </svg>
        <svg width="24" height="11" viewBox="0 0 24 11">
          <rect x="0.5" y="0.5" width="21" height="10" rx="2.5" stroke={color} strokeOpacity="0.4" fill="none" />
          <rect x="2" y="2" width="18" height="7" rx="1.5" fill={color} />
          <path d="M22.5 3.5v4c0.6-0.2 1.1-0.9 1.1-2s-0.5-1.8-1.1-2z" fill={color} fillOpacity="0.4" />
        </svg>
      </div>
    </div>
  );
}

// 底部自定义 Tab（复用原项目液态玻璃风格，但配合色板改色）
function AppTabBar({ active = 'home', palette, radius, onTap }) {
  const p = palette;
  const items = [
    { id: 'home', label: '首页', Icon: HomeIcon },
    { id: 'market', label: '市集', Icon: BagIcon },
    { id: 'publish', label: 'publish', Icon: PlusIcon },
    { id: 'msg', label: '消息', Icon: MsgIcon, badge: 3 },
    { id: 'me', label: '我', Icon: MeIcon },
  ];
  return (
    <div style={{
      position: 'absolute', left: 14, right: 14, bottom: 16, height: 60,
      borderRadius: radius * 1.4,
      background: 'rgba(255,255,255,0.72)',
      backdropFilter: 'blur(24px) saturate(180%)',
      WebkitBackdropFilter: 'blur(24px) saturate(180%)',
      boxShadow: `0 8px 28px ${p.primary}22, 0 2px 6px rgba(0,0,0,0.05), inset 0 1px 0 rgba(255,255,255,0.9)`,
      display: 'flex', alignItems: 'center', justifyContent: 'space-around',
      padding: '0 8px', zIndex: 30,
    }}>
      {items.map(it => {
        if (it.id === 'publish') {
          return (
            <div key="pub" onClick={() => onTap && onTap('publish')} style={{
              width: 48, height: 48, marginTop: -18,
              borderRadius: 16,
              background: `linear-gradient(135deg, ${p.primary}, ${p.primaryInk})`,
              boxShadow: `0 10px 22px ${p.primary}55, inset 0 1px 0 rgba(255,255,255,0.4)`,
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              cursor: 'pointer',
            }}>
              <PlusIcon size={24} color="#fff" />
            </div>
          );
        }
        const isActive = active === it.id;
        const color = isActive ? p.primary : p.inkMuted;
        return (
          <div key={it.id} onClick={() => onTap && onTap(it.id)} style={{
            flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2, cursor: 'pointer', position: 'relative',
          }}>
            <div style={{ position: 'relative' }}>
              <it.Icon size={22} color={color} filled={isActive} />
              {it.badge && (
                <div style={{
                  position: 'absolute', top: -4, right: -8,
                  minWidth: 16, height: 16, padding: '0 4px', borderRadius: 8,
                  background: p.warn, color: '#fff', fontSize: 10, fontWeight: 700,
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  boxShadow: '0 2px 4px rgba(0,0,0,0.15)',
                }}>{it.badge}</div>
              )}
            </div>
            <span style={{ fontSize: 10, color, fontWeight: isActive ? 700 : 500 }}>{it.label}</span>
          </div>
        );
      })}
    </div>
  );
}

// ═══════════════════════════════════════════════════════════
// 1. SPLASH — 开屏页
// ═══════════════════════════════════════════════════════════
function SplashScreen({ palette, radius }) {
  const p = palette;
  return (
    <div style={{
      width: '100%', height: '100%',
      background: `linear-gradient(180deg, ${p.surface} 0%, ${p.bg} 40%, ${p.primarySoft} 100%)`,
      position: 'relative', overflow: 'hidden',
      fontFamily: TYPE.body,
    }}>
      <StatusLine color={p.ink} />
      {/* 装饰爪印 */}
      <PawIcon size={36} color={p.primarySoft} style={{ position: 'absolute', top: 120, left: 40, transform: 'rotate(-20deg)', opacity: 0.7 }} />
      <PawIcon size={24} color={p.primarySoft} style={{ position: 'absolute', top: 180, right: 60, transform: 'rotate(15deg)', opacity: 0.5 }} />
      <PawIcon size={28} color={p.primary} style={{ position: 'absolute', bottom: 260, left: 70, transform: 'rotate(40deg)', opacity: 0.25 }} />
      <PawIcon size={20} color={p.primary} style={{ position: 'absolute', bottom: 340, right: 80, opacity: 0.2 }} />

      {/* 主 Logo + 欢迎 */}
      <div style={{
        position: 'absolute', top: '32%', left: 0, right: 0,
        display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 20,
      }}>
        {/* Logo 圆形徽标 */}
        <div style={{
          width: 104, height: 104,
          borderRadius: '50%',
          background: `linear-gradient(135deg, ${p.primary}, ${p.primaryInk})`,
          boxShadow: `0 14px 40px ${p.primary}55, inset 0 2px 6px rgba(255,255,255,0.4)`,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
        }}>
          <PawIcon size={50} color="#fff" />
        </div>
        <div style={{ textAlign: 'center' }}>
          <div style={{
            fontFamily: TYPE.brand, fontWeight: 700, fontSize: 34,
            color: p.ink, letterSpacing: 2, marginBottom: 8,
          }}>爱宠社</div>
          <div style={{ fontSize: 14, color: p.inkSoft, letterSpacing: 1 }}>
            和毛孩子一起，分享每一天
          </div>
        </div>
      </div>

      {/* 底部加载条 */}
      <div style={{
        position: 'absolute', bottom: 90, left: '50%', transform: 'translateX(-50%)',
        display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 14,
      }}>
        <div style={{
          width: 44, height: 4, borderRadius: 2, background: p.primarySoft,
          overflow: 'hidden', position: 'relative',
        }}>
          <div style={{
            position: 'absolute', left: 0, top: 0, bottom: 0, width: '60%',
            background: p.primary, borderRadius: 2,
            animation: 'splashLoad 1.8s ease-in-out infinite',
          }} />
        </div>
        <span style={{ fontSize: 11, color: p.inkMuted, letterSpacing: 2 }}>LOADING · v2.0</span>
      </div>

      <style>{`@keyframes splashLoad {
        0% { transform: translateX(-100%); }
        50% { transform: translateX(80%); }
        100% { transform: translateX(180%); }
      }`}</style>
    </div>
  );
}

// ═══════════════════════════════════════════════════════════
// 2. LOGIN — 登录方式选择
// ═══════════════════════════════════════════════════════════
function LoginScreen({ palette, radius }) {
  const p = palette;
  const [checked, setChecked] = React.useState(false);
  return (
    <div style={{
      width: '100%', height: '100%',
      background: p.bg,
      position: 'relative', overflow: 'hidden',
      fontFamily: TYPE.body,
    }}>
      <StatusLine color={p.ink} />
      {/* 顶部柔和背景块 */}
      <div style={{
        position: 'absolute', top: -60, left: -40, right: -40, height: 340,
        background: `radial-gradient(ellipse at 50% 60%, ${p.primarySoft} 0%, ${p.bg} 70%)`,
      }} />
      {/* 浮动宠物头像群 */}
      <div style={{ position: 'absolute', top: 70, left: 0, right: 0, height: 280, pointerEvents: 'none' }}>
        <div style={{ position: 'absolute', top: 30, left: 40, transform: 'rotate(-10deg)' }}>
          <PetAvatar variant="dog" size={56} />
        </div>
        <div style={{ position: 'absolute', top: 10, right: 50, transform: 'rotate(12deg)' }}>
          <PetAvatar variant="cat" size={64} />
        </div>
        <div style={{ position: 'absolute', top: 140, left: 20, transform: 'rotate(-5deg)' }}>
          <PetAvatar variant="mochi" size={44} />
        </div>
        <div style={{ position: 'absolute', top: 150, right: 30, transform: 'rotate(8deg)' }}>
          <PetAvatar variant="puppy" size={48} />
        </div>
        <PawIcon size={22} color={p.primary} style={{ position: 'absolute', top: 110, left: '40%', opacity: 0.35, transform: 'rotate(-20deg)' }} />
      </div>

      {/* Logo + 品牌 */}
      <div style={{
        position: 'absolute', top: 210, left: 0, right: 0,
        display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 12, zIndex: 2,
      }}>
        <div style={{
          width: 72, height: 72, borderRadius: '50%',
          background: `linear-gradient(135deg, ${p.primary}, ${p.primaryInk})`,
          boxShadow: `0 10px 28px ${p.primary}55`,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
        }}>
          <PawIcon size={34} color="#fff" />
        </div>
        <div style={{ fontFamily: TYPE.brand, fontWeight: 700, fontSize: 28, color: p.ink, letterSpacing: 1.5 }}>爱宠社</div>
      </div>

      {/* 文案 */}
      <div style={{
        position: 'absolute', top: 360, left: 32, right: 32, textAlign: 'center',
      }}>
        <div style={{ fontSize: 22, fontWeight: 700, color: p.ink, marginBottom: 8, letterSpacing: 0.5 }}>
          欢迎回来 🐾
        </div>
        <div style={{ fontSize: 14, color: p.inkSoft, lineHeight: 1.6 }}>
          宠友都在这里，等你一起分享
          <br />萌瞬间、闲置好物和日常治愈
        </div>
      </div>

      {/* 按钮组 */}
      <div style={{
        position: 'absolute', top: 470, left: 28, right: 28,
        display: 'flex', flexDirection: 'column', gap: 12,
      }}>
        <button style={{
          height: 52, borderRadius: radius * 1.6, border: 'none',
          background: `linear-gradient(135deg, ${p.primary}, ${p.primaryInk})`,
          color: '#fff', fontSize: 16, fontWeight: 600,
          boxShadow: `0 8px 20px ${p.primary}55, inset 0 1px 0 rgba(255,255,255,0.3)`,
          display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8,
          fontFamily: TYPE.body,
        }}>
          <svg width="18" height="18" viewBox="0 0 24 24" fill="#fff"><path d="M8.7 13.8c-0.4 0-0.8-0.3-0.8-0.8s0.3-0.8 0.8-0.8 0.8 0.3 0.8 0.8-0.3 0.8-0.8 0.8zm7.2 0c-0.4 0-0.8-0.3-0.8-0.8s0.3-0.8 0.8-0.8c0.4 0 0.8 0.3 0.8 0.8s-0.3 0.8-0.8 0.8zm-3.6-8.8c-5.2 0-9.4 3.5-9.4 7.9 0 2.5 1.4 4.7 3.5 6.1-0.2 0.6-0.6 2-0.6 2.3 0 0.2 0.1 0.3 0.3 0.3 0.1 0 2.3-1.5 2.7-1.8 1.1 0.3 2.2 0.5 3.5 0.5 5.2 0 9.4-3.5 9.4-7.9S17.5 5 12.3 5z"/></svg>
          微信一键登录
        </button>
        <button style={{
          height: 52, borderRadius: radius * 1.6,
          background: p.surface, color: p.primary,
          border: `1.5px solid ${p.primarySoft}`,
          fontSize: 16, fontWeight: 600,
          boxShadow: `0 4px 12px ${p.primary}15`,
          fontFamily: TYPE.body,
        }}>
          手机号登录 / 注册
        </button>
      </div>

      {/* 底部协议 */}
      <div style={{
        position: 'absolute', bottom: 40, left: 0, right: 0,
        display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8,
        fontSize: 11, color: p.inkMuted,
      }}>
        <div onClick={() => setChecked(c => !c)} style={{
          width: 14, height: 14, borderRadius: 7, cursor: 'pointer',
          border: `1.5px solid ${checked ? p.primary : p.inkFaint}`,
          background: checked ? p.primary : 'transparent',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
        }}>
          {checked && <svg width="8" height="8" viewBox="0 0 10 10"><path d="M1 5l3 3 5-6" stroke="#fff" strokeWidth="1.8" fill="none" strokeLinecap="round" /></svg>}
        </div>
        <span>已阅读并同意
          <span style={{ color: p.primary }}> 用户协议 </span>和
          <span style={{ color: p.primary }}> 隐私政策</span>
        </span>
      </div>
    </div>
  );
}

Object.assign(window, { SplashScreen, LoginScreen, AppTabBar, StatusLine });
