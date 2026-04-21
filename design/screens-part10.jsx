// screens-part10.jsx — Onboarding / Explore / Reels / Nearby / Live / SignIn

function OnboardingScreen({ palette, radius }) {
  const p = palette;
  const [step, setStep] = React.useState(0);
  const slides = [
    {
      emoji: '🐾', title: '记录毛孩子的\n每一个瞬间',
      desc: '建立专属档案，生长曲线、疫苗、喜好\n一站式托管',
      accent: p.primary, bg: p.primarySoft,
    },
    {
      emoji: '📸', title: '和 280 万铲屎官\n一起分享日常',
      desc: '瀑布流笔记、话题挑战、视频打卡\n找到你的「云养宠」同伴',
      accent: p.accent, bg: '#f5e4d8',
    },
    {
      emoji: '🛒', title: '靠谱好物 · 比价一步到位',
      desc: '主粮、零食、玩具、医护用品\n社区真实口碑推荐',
      accent: p.warn, bg: '#fce4d4',
    },
    {
      emoji: '🏥', title: '专家医生 7×24 在线',
      desc: '轻症咨询、疫苗提醒、附近医院\n让养宠不再手足无措',
      accent: '#8fbf9f', bg: '#e0efd8',
    },
  ];
  const cur = slides[step];

  return (
    <div style={{ width: '100%', height: '100%', background: p.bg, position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color={p.ink} />
      {/* 跳过 */}
      <div style={{ position: 'absolute', top: 58, right: 22, zIndex: 10 }}>
        <span style={{ fontSize: 12, color: p.inkMuted, fontWeight: 500 }}>跳过 ›</span>
      </div>

      {/* 主视觉 */}
      <div style={{ position: 'absolute', top: 96, left: 0, right: 0, height: 320, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <div style={{
          width: 240, height: 240, borderRadius: 120,
          background: `radial-gradient(circle at 30% 30%, ${cur.bg}, ${cur.accent}22)`,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          fontSize: 110, position: 'relative',
        }}>
          {cur.emoji}
          <div style={{ position: 'absolute', top: 10, right: 30, opacity: 0.3 }}><PawIcon size={24} color={cur.accent} /></div>
          <div style={{ position: 'absolute', bottom: 20, left: 20, opacity: 0.25 }}><PawIcon size={18} color={cur.accent} /></div>
          <div style={{ position: 'absolute', top: 40, left: -8, opacity: 0.2 }}><PawIcon size={14} color={cur.accent} /></div>
        </div>
      </div>

      {/* 文案 */}
      <div style={{ position: 'absolute', top: 440, left: 32, right: 32, textAlign: 'center' }}>
        <div style={{ fontSize: 26, fontWeight: 800, color: p.ink, lineHeight: 1.3, fontFamily: TYPE.brand, whiteSpace: 'pre-line' }}>
          {cur.title}
        </div>
        <div style={{ fontSize: 13, color: p.inkSoft, marginTop: 14, lineHeight: 1.65, whiteSpace: 'pre-line' }}>
          {cur.desc}
        </div>
      </div>

      {/* 指示点 */}
      <div style={{ position: 'absolute', bottom: 130, left: 0, right: 0, display: 'flex', justifyContent: 'center', gap: 6 }}>
        {slides.map((_, i) => (
          <div key={i} style={{
            width: i === step ? 22 : 6, height: 6, borderRadius: 3,
            background: i === step ? p.primary : p.divider, transition: 'all .3s',
          }} />
        ))}
      </div>

      {/* CTA */}
      <div style={{ position: 'absolute', bottom: 54, left: 32, right: 32 }}>
        <button
          onClick={() => setStep((step + 1) % slides.length)}
          style={{
            width: '100%', height: 48, borderRadius: 24, border: 'none',
            background: `linear-gradient(135deg, ${p.primary}, ${p.primaryInk})`,
            color: '#fff', fontSize: 14, fontWeight: 700,
            boxShadow: `0 8px 20px ${p.primary}50`, fontFamily: TYPE.body,
          }}>
          {step < slides.length - 1 ? '下一步 →' : '开始我的云养宠之旅 🐾'}
        </button>
      </div>
    </div>
  );
}

function ExploreScreen({ palette, radius }) {
  const p = palette;
  const categories = [
    { icon: '🐕', label: '狗狗', color: '#d9956a', count: '82w 笔记' },
    { icon: '🐈', label: '猫咪', color: '#c7825f', count: '114w 笔记' },
    { icon: '🐰', label: '异宠', color: '#b89d7a', count: '12w 笔记' },
    { icon: '🐠', label: '水族', color: '#8aa5b5', count: '8w 笔记' },
  ];
  const breeds = [
    { seed: 0, variant: 'dog', name: '金毛', note: '3.2w 笔记' },
    { seed: 1, variant: 'cat', name: '布偶', note: '5.8w 笔记' },
    { seed: 2, variant: 'puppy', name: '柴犬', note: '2.1w 笔记' },
    { seed: 3, variant: 'fluff', name: '英短', note: '4.5w 笔记' },
    { seed: 4, variant: 'mochi', name: '泰迪', note: '2.8w 笔记' },
    { seed: 5, variant: 'dog', name: '边牧', note: '1.6w 笔记' },
  ];
  const topics = [
    { tag: '#换毛季自救', count: '28.4w', hot: true },
    { tag: '#猫咪便便分析', count: '12.1w' },
    { tag: '#金毛毕业典礼', count: '9.8w', hot: true },
    { tag: '#异宠大合集', count: '4.2w' },
    { tag: '#新手养猫', count: '18.5w' },
    { tag: '#自制狗粮', count: '6.7w' },
  ];

  return (
    <div style={{ width: '100%', height: '100%', background: p.bg, position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color={p.ink} />
      <TopBar palette={p} title="发现 · Explore" left={null} right={
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke={p.ink} strokeWidth="2" strokeLinecap="round"><circle cx="11" cy="11" r="7" /><path d="M21 21l-4.35-4.35" /></svg>
      } />

      <div style={{ position: 'absolute', top: 92, left: 0, right: 0, bottom: 0, overflow: 'auto', paddingBottom: 20 }}>
        {/* 分类卡 */}
        <div style={{ padding: '8px 14px 0', display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: 8 }}>
          {categories.map((c, i) => (
            <div key={i} style={{
              padding: 14, borderRadius: radius,
              background: `linear-gradient(135deg, ${c.color}, ${c.color}cc)`,
              color: '#fff', position: 'relative', overflow: 'hidden',
            }}>
              <div style={{ position: 'absolute', top: -8, right: -8, opacity: 0.3, fontSize: 50 }}>{c.icon}</div>
              <div style={{ fontSize: 24 }}>{c.icon}</div>
              <div style={{ fontSize: 15, fontWeight: 800, marginTop: 6, fontFamily: TYPE.brand }}>{c.label}</div>
              <div style={{ fontSize: 10, opacity: 0.9, marginTop: 2 }}>{c.count}</div>
            </div>
          ))}
        </div>

        {/* 品种云 */}
        <SectionTitle palette={p} title="🐕 按品种看" right={<span>全部 ›</span>} />
        <div style={{ paddingLeft: 14, display: 'flex', gap: 10, overflowX: 'auto' }}>
          {breeds.map((b, i) => (
            <div key={i} style={{ flexShrink: 0, width: 82, textAlign: 'center' }}>
              <div style={{ width: 78, height: 78, borderRadius: 14, overflow: 'hidden' }}>
                <PetPlaceholder w={78} h={78} palette={p} seed={b.seed} variant={b.variant} />
              </div>
              <div style={{ fontSize: 12, fontWeight: 700, color: p.ink, marginTop: 6 }}>{b.name}</div>
              <div style={{ fontSize: 9, color: p.inkMuted, marginTop: 1 }}>{b.note}</div>
            </div>
          ))}
        </div>

        {/* 热门话题 */}
        <SectionTitle palette={p} title="🔥 热门话题" subtitle="今日最热" />
        <div style={{ margin: '0 14px', background: p.surface, borderRadius: radius, overflow: 'hidden' }}>
          {topics.map((t, i) => (
            <div key={i} style={{ display: 'flex', alignItems: 'center', gap: 10, padding: '12px 14px', borderBottom: i < topics.length - 1 ? `0.5px solid ${p.divider}` : 'none' }}>
              <div style={{ width: 22, fontSize: 14, fontWeight: 800, color: i < 3 ? p.primary : p.inkMuted, fontFamily: TYPE.num }}>{i + 1}</div>
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: 13, color: p.ink, fontWeight: 600 }}>{t.tag}</div>
                <div style={{ fontSize: 10, color: p.inkMuted, marginTop: 2 }}>{t.count} 浏览</div>
              </div>
              {t.hot && <Tag palette={p} tone="warn" size="xs">🔥 HOT</Tag>}
            </div>
          ))}
        </div>

        {/* 专栏 */}
        <SectionTitle palette={p} title="📖 精选专栏" />
        <div style={{ paddingLeft: 14, display: 'flex', gap: 10, overflowX: 'auto', paddingBottom: 2 }}>
          {[
            { title: '新手养猫 101', sub: '12 节课 · 8.2w 人学过', color: p.primary },
            { title: '狗狗社交课', sub: '6 节课 · 3.4w 人学过', color: p.accent },
            { title: '急救手册', sub: '20 节课 · 5.6w 人学过', color: p.warn },
          ].map((c, i) => (
            <div key={i} style={{ flexShrink: 0, width: 180, height: 100, borderRadius: radius, padding: 14, background: `linear-gradient(135deg, ${c.color}, ${c.color}aa)`, color: '#fff', position: 'relative', overflow: 'hidden' }}>
              <div style={{ position: 'absolute', top: -10, right: -10, opacity: 0.2 }}><PawIcon size={50} color="#fff" /></div>
              <div style={{ fontSize: 14, fontWeight: 800, fontFamily: TYPE.brand }}>{c.title}</div>
              <div style={{ fontSize: 10, opacity: 0.9, marginTop: 4 }}>{c.sub}</div>
              <div style={{ position: 'absolute', bottom: 12, left: 14, fontSize: 10, padding: '3px 8px', borderRadius: 8, background: 'rgba(255,255,255,0.25)', backdropFilter: 'blur(8px)' }}>免费学 →</div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

function ReelsScreen({ palette, radius }) {
  const p = palette;
  return (
    <div style={{ width: '100%', height: '100%', background: '#000', position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color="#fff" />
      {/* 视频占位 */}
      <div style={{ position: 'absolute', inset: 0 }}>
        <PetPlaceholder w={340} h={720} palette={p} seed={7} variant="dog" />
        <div style={{ position: 'absolute', inset: 0, background: 'linear-gradient(180deg, rgba(0,0,0,0.35) 0%, transparent 25%, transparent 55%, rgba(0,0,0,0.7) 100%)' }} />
      </div>

      {/* 播放图标 */}
      <div style={{ position: 'absolute', top: '42%', left: '50%', transform: 'translate(-50%,-50%)', width: 60, height: 60, borderRadius: 30, background: 'rgba(0,0,0,0.4)', display: 'flex', alignItems: 'center', justifyContent: 'center', backdropFilter: 'blur(10px)', opacity: 0.6 }}>
        <div style={{ width: 0, height: 0, borderTop: '10px solid transparent', borderBottom: '10px solid transparent', borderLeft: '16px solid #fff', marginLeft: 4 }} />
      </div>

      {/* Top bar */}
      <div style={{ position: 'absolute', top: 56, left: 0, right: 0, height: 44, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 24, zIndex: 5 }}>
        <span style={{ fontSize: 13, color: 'rgba(255,255,255,0.6)', fontWeight: 500 }}>关注</span>
        <span style={{ fontSize: 14, color: '#fff', fontWeight: 800, position: 'relative' }}>
          推荐
          <div style={{ position: 'absolute', bottom: -6, left: '50%', transform: 'translateX(-50%)', width: 16, height: 3, borderRadius: 2, background: '#fff' }} />
        </span>
        <span style={{ fontSize: 13, color: 'rgba(255,255,255,0.6)', fontWeight: 500 }}>附近</span>
        <div style={{ position: 'absolute', right: 18 }}>
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#fff" strokeWidth="2" strokeLinecap="round"><circle cx="11" cy="11" r="7" /><path d="M21 21l-4.35-4.35" /></svg>
        </div>
      </div>

      {/* 右侧操作栏 */}
      <div style={{ position: 'absolute', right: 12, bottom: 140, display: 'flex', flexDirection: 'column', gap: 22, alignItems: 'center', zIndex: 5 }}>
        <div style={{ position: 'relative' }}>
          <PetAvatar variant="dog" size={48} />
          <div style={{ position: 'absolute', bottom: -6, left: '50%', transform: 'translateX(-50%)', width: 20, height: 20, borderRadius: 10, background: p.primary, color: '#fff', fontSize: 12, fontWeight: 800, display: 'flex', alignItems: 'center', justifyContent: 'center', border: '2px solid #fff' }}>+</div>
        </div>
        {[
          { icon: <HeartIcon size={32} color="#fff" />, num: '12.8w', active: true },
          { icon: <svg width="30" height="30" viewBox="0 0 24 24" fill="none" stroke="#fff" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" /></svg>, num: '3,240' },
          { icon: <ShareIcon size={28} color="#fff" />, num: '1.2w' },
          { icon: <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#fff" strokeWidth="2" strokeLinecap="round"><path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z" /></svg>, num: '收藏' },
        ].map((x, i) => (
          <div key={i} style={{ textAlign: 'center' }}>
            {x.icon}
            <div style={{ fontSize: 10, color: '#fff', marginTop: 3, fontFamily: TYPE.num }}>{x.num}</div>
          </div>
        ))}
        {/* 音乐唱片 */}
        <div style={{ width: 36, height: 36, borderRadius: 18, background: `conic-gradient(from 0deg, ${p.primary}, #000 70%)`, border: '2px solid rgba(255,255,255,0.3)', animation: 'spin 4s linear infinite' }} />
      </div>

      {/* 底部信息 */}
      <div style={{ position: 'absolute', left: 14, right: 70, bottom: 110, color: '#fff', zIndex: 5 }}>
        <div style={{ fontSize: 14, fontWeight: 700 }}>@汤圆家日记</div>
        <div style={{ fontSize: 12, marginTop: 6, lineHeight: 1.5, textShadow: '0 2px 4px rgba(0,0,0,0.3)' }}>
          今天汤圆第一次学会坐好求饭，奖励三根鸭肉 🥰 #金毛日记 #狗狗训练
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 4, marginTop: 8, fontSize: 10, color: 'rgba(255,255,255,0.85)' }}>
          <svg width="11" height="11" viewBox="0 0 24 24" fill="#fff"><path d="M9 18V5l12-2v13" /><circle cx="6" cy="18" r="3" /><circle cx="18" cy="16" r="3" /></svg>
          <div style={{ display: 'flex', overflow: 'hidden', maxWidth: 160 }}>
            <span style={{ display: 'inline-block', animation: 'marquee 12s linear infinite', whiteSpace: 'nowrap' }}>♪ 原声 · 小狗圆舞曲 @阿汤 </span>
          </div>
        </div>
      </div>

      {/* Bottom tab (overlay) */}
      <div style={{ position: 'absolute', bottom: 0, left: 0, right: 0, height: 76, background: 'linear-gradient(0deg, rgba(0,0,0,0.6), transparent)', display: 'flex', alignItems: 'center', justifyContent: 'space-around', paddingBottom: 22 }}>
        {['首页', '发现', '+', '消息', '我'].map((t, i) => (
          <div key={i} style={{ fontSize: 11, color: i === 1 ? '#fff' : 'rgba(255,255,255,0.55)', fontWeight: i === 1 ? 700 : 500 }}>
            {t === '+' ? <div style={{ width: 40, height: 28, borderRadius: 8, background: `linear-gradient(135deg, ${p.primary}, ${p.accent})`, color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 20, fontWeight: 300 }}>+</div> : t}
          </div>
        ))}
      </div>

      <style>{`
        @keyframes spin { to { transform: rotate(360deg); } }
        @keyframes marquee { 0% { transform: translateX(0); } 100% { transform: translateX(-100%); } }
      `}</style>
    </div>
  );
}

function NearbyScreen({ palette, radius }) {
  const p = palette;
  const pins = [
    { x: 38, y: 28, type: 'pet', icon: '🐕', label: '汤圆', dist: '120m' },
    { x: 65, y: 42, type: 'hospital', icon: '🏥', label: '宠颐生', dist: '680m' },
    { x: 22, y: 55, type: 'pet', icon: '🐈', label: '奶冻', dist: '340m' },
    { x: 72, y: 68, type: 'shop', icon: '🛍️', label: '爪爪商店', dist: '1.2km' },
    { x: 50, y: 70, type: 'user', icon: '👤', you: true },
    { x: 15, y: 35, type: 'pet', icon: '🐕', label: '球球', dist: '520m' },
  ];

  return (
    <div style={{ width: '100%', height: '100%', background: p.bg, position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color={p.ink} />
      <TopBar palette={p} title="附近 · Nearby" right={<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke={p.ink} strokeWidth="2" strokeLinecap="round"><circle cx="12" cy="12" r="3" /><path d="M12 1v6M12 17v6M4.22 4.22l4.24 4.24M15.54 15.54l4.24 4.24M1 12h6M17 12h6M4.22 19.78l4.24-4.24M15.54 8.46l4.24-4.24" /></svg>} />

      {/* 地图 */}
      <div style={{ position: 'absolute', top: 92, left: 0, right: 0, height: 380, background: '#e8e3d9', overflow: 'hidden' }}>
        {/* 假路网 */}
        <svg width="340" height="380" style={{ position: 'absolute', inset: 0 }}>
          <defs>
            <pattern id="grid" width="40" height="40" patternUnits="userSpaceOnUse">
              <path d="M 40 0 L 0 0 0 40" fill="none" stroke="#d6cfc0" strokeWidth="0.5" />
            </pattern>
          </defs>
          <rect width="100%" height="100%" fill="url(#grid)" />
          <path d="M 0 120 Q 150 100 340 180" stroke="#fff" strokeWidth="10" fill="none" />
          <path d="M 0 120 Q 150 100 340 180" stroke="#e8dac8" strokeWidth="2" fill="none" strokeDasharray="4 4" />
          <path d="M 80 0 Q 100 150 140 380" stroke="#fff" strokeWidth="8" fill="none" />
          <path d="M 220 0 L 260 380" stroke="#fff" strokeWidth="6" fill="none" />
          <rect x="20" y="200" width="80" height="50" rx="6" fill="#d8e4d0" opacity="0.6" />
          <rect x="210" y="240" width="60" height="60" rx="6" fill="#d8e4d0" opacity="0.6" />
          <text x="52" y="230" fontSize="9" fill={p.inkMuted} textAnchor="middle">徐汇公园</text>
        </svg>

        {/* Pins */}
        {pins.map((pin, i) => (
          <div key={i} style={{ position: 'absolute', left: `${pin.x}%`, top: `${pin.y}%`, transform: 'translate(-50%, -100%)' }}>
            {pin.you ? (
              <div style={{ position: 'relative' }}>
                <div style={{ position: 'absolute', left: '50%', top: '50%', transform: 'translate(-50%,-50%)', width: 80, height: 80, borderRadius: 40, background: `${p.primary}25`, animation: 'pulse 2s infinite' }} />
                <div style={{ width: 16, height: 16, borderRadius: 8, background: p.primary, border: '3px solid #fff', boxShadow: `0 2px 6px rgba(0,0,0,0.3)` }} />
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                <div style={{
                  padding: '4px 8px 4px 4px', borderRadius: 18, background: '#fff', display: 'flex', alignItems: 'center', gap: 5,
                  boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
                }}>
                  <div style={{ width: 22, height: 22, borderRadius: 11, background: pin.type === 'hospital' ? '#8fbf9f' : pin.type === 'shop' ? p.accent : p.primary, color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 11 }}>{pin.icon}</div>
                  <div>
                    <div style={{ fontSize: 10, fontWeight: 700, color: p.ink, lineHeight: 1 }}>{pin.label}</div>
                    <div style={{ fontSize: 8, color: p.inkMuted, marginTop: 1 }}>{pin.dist}</div>
                  </div>
                </div>
                <div style={{ width: 0, height: 0, borderTop: `5px solid #fff`, borderLeft: '4px solid transparent', borderRight: '4px solid transparent' }} />
              </div>
            )}
          </div>
        ))}

        {/* Map ctrl */}
        <div style={{ position: 'absolute', bottom: 12, right: 12, display: 'flex', flexDirection: 'column', gap: 6 }}>
          {['📍', '+', '−'].map((x, i) => (
            <div key={i} style={{ width: 34, height: 34, borderRadius: 10, background: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 13, fontWeight: 600, color: p.ink, boxShadow: '0 2px 6px rgba(0,0,0,0.12)' }}>{x}</div>
          ))}
        </div>
      </div>

      {/* 过滤 */}
      <div style={{ position: 'absolute', top: 100, left: 14, display: 'flex', gap: 6 }}>
        <Tag palette={p} tone="primary" size="md">🐾 宠物</Tag>
        <Tag palette={p} tone="ghost" size="md">🏥 医院</Tag>
        <Tag palette={p} tone="ghost" size="md">🛍️ 商店</Tag>
        <Tag palette={p} tone="ghost" size="md">✂️ 美容</Tag>
      </div>

      {/* 列表 */}
      <div style={{ position: 'absolute', top: 472, left: 0, right: 0, bottom: 0, background: p.bg, borderRadius: '22px 22px 0 0', padding: '14px 0', overflow: 'auto' }}>
        <div style={{ padding: '0 18px 10px', display: 'flex', alignItems: 'center', gap: 6 }}>
          <div style={{ width: 34, height: 3, borderRadius: 2, background: p.divider, margin: '-8px auto 10px' }} />
        </div>
        <div style={{ padding: '0 16px', fontSize: 13, fontWeight: 700, color: p.ink, marginBottom: 6 }}>附近 6 个「毛孩子」 🐕</div>
        {[
          { variant: 'dog', name: '球球 · 博美', dist: '120m', mood: '出门遛弯中' },
          { variant: 'cat', name: '煎蛋 · 橘猫', dist: '320m', mood: '窗台晒太阳' },
          { variant: 'fluff', name: '胖虎 · 柯基', dist: '580m', mood: '求玩耍' },
        ].map((n, i) => (
          <div key={i} style={{ display: 'flex', alignItems: 'center', gap: 10, padding: '10px 16px' }}>
            <PetAvatar variant={n.variant} size={44} />
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: 13, fontWeight: 700, color: p.ink }}>{n.name}</div>
              <div style={{ fontSize: 10, color: p.inkMuted, marginTop: 2 }}>📍 {n.dist} · {n.mood}</div>
            </div>
            <button style={{ padding: '5px 12px', borderRadius: 14, background: p.primarySoft, border: 'none', color: p.primaryInk, fontSize: 11, fontWeight: 700, fontFamily: TYPE.body }}>打招呼 👋</button>
          </div>
        ))}
      </div>

      <style>{`@keyframes pulse { 0%, 100% { transform: translate(-50%,-50%) scale(1); opacity: 0.5; } 50% { transform: translate(-50%,-50%) scale(1.5); opacity: 0; } }`}</style>
    </div>
  );
}

function LiveScreen({ palette, radius }) {
  const p = palette;
  return (
    <div style={{ width: '100%', height: '100%', background: '#1a0f08', position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color="#fff" />
      {/* 背景 */}
      <div style={{ position: 'absolute', inset: 0 }}>
        <PetPlaceholder w={340} h={720} palette={p} seed={3} variant="dog" />
        <div style={{ position: 'absolute', inset: 0, background: 'linear-gradient(180deg, rgba(0,0,0,0.5) 0%, transparent 20%, transparent 60%, rgba(0,0,0,0.75) 100%)' }} />
      </div>

      {/* Top 主播信息 */}
      <div style={{ position: 'absolute', top: 56, left: 12, right: 12, display: 'flex', alignItems: 'center', gap: 8, zIndex: 5 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8, background: 'rgba(0,0,0,0.5)', backdropFilter: 'blur(10px)', padding: '5px 12px 5px 5px', borderRadius: 22 }}>
          <PetAvatar variant="dog" size={32} />
          <div style={{ color: '#fff' }}>
            <div style={{ fontSize: 12, fontWeight: 700 }}>汤圆主播间 🔴</div>
            <div style={{ fontSize: 9, opacity: 0.8 }}>4.2w 在看 · 128 点赞</div>
          </div>
        </div>
        <button style={{ padding: '6px 14px', borderRadius: 18, border: 'none', background: p.primary, color: '#fff', fontSize: 11, fontWeight: 700, fontFamily: TYPE.body }}>+ 关注</button>
        <div style={{ marginLeft: 'auto', width: 28, height: 28, borderRadius: 14, background: 'rgba(0,0,0,0.5)', color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 14 }}>×</div>
      </div>

      {/* 观众头像条 */}
      <div style={{ position: 'absolute', top: 102, left: 12, display: 'flex', alignItems: 'center', gap: -6, zIndex: 5 }}>
        {['cat', 'dog', 'fluff', 'puppy'].map((v, i) => (
          <div key={i} style={{ marginLeft: i > 0 ? -8 : 0, border: '2px solid rgba(0,0,0,0.4)', borderRadius: 20 }}>
            <PetAvatar variant={v} size={22} />
          </div>
        ))}
        <div style={{ marginLeft: 4, padding: '3px 8px', borderRadius: 10, background: 'rgba(0,0,0,0.5)', color: '#fff', fontSize: 9, fontWeight: 600 }}>+41,238 观众</div>
      </div>

      {/* 礼物飘屏 */}
      <div style={{ position: 'absolute', top: 170, right: 12, display: 'flex', flexDirection: 'column', gap: 6, zIndex: 5 }}>
        {[
          { user: '柴柴麻麻', gift: '🎀 蝴蝶结', color: p.accent },
          { user: '小泰迪', gift: '🦴 大骨头', color: p.warn },
        ].map((g, i) => (
          <div key={i} style={{ padding: '5px 10px 5px 5px', borderRadius: 16, background: `linear-gradient(90deg, ${g.color}ee, ${g.color}99)`, display: 'flex', alignItems: 'center', gap: 6, color: '#fff', boxShadow: '0 4px 8px rgba(0,0,0,0.2)' }}>
            <PetAvatar variant="dog" size={20} />
            <div style={{ fontSize: 10 }}>
              <div style={{ fontWeight: 700 }}>{g.user}</div>
              <div style={{ opacity: 0.9, fontSize: 9 }}>送出 {g.gift}</div>
            </div>
          </div>
        ))}
      </div>

      {/* 商品卡 */}
      <div style={{ position: 'absolute', bottom: 170, left: 10, width: 200, background: 'rgba(255,255,255,0.95)', borderRadius: 12, padding: 8, display: 'flex', gap: 8, backdropFilter: 'blur(10px)', boxShadow: '0 6px 16px rgba(0,0,0,0.3)' }}>
        <div style={{ width: 48, height: 48, borderRadius: 8, overflow: 'hidden' }}>
          <PetPlaceholder w={48} h={48} palette={p} seed={5} variant="mochi" />
        </div>
        <div style={{ flex: 1 }}>
          <div style={{ fontSize: 9, color: p.warn, fontWeight: 700 }}>🔥 直播专享</div>
          <div style={{ fontSize: 11, color: p.ink, fontWeight: 700, lineHeight: 1.3, marginTop: 2 }}>蓬松云朵猫窝</div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 4, marginTop: 2 }}>
            <span style={{ fontSize: 13, color: p.warn, fontWeight: 800, fontFamily: TYPE.num }}>¥129</span>
            <span style={{ fontSize: 8, color: p.inkFaint, textDecoration: 'line-through' }}>¥199</span>
          </div>
        </div>
        <button style={{ alignSelf: 'center', padding: '5px 10px', borderRadius: 12, border: 'none', background: p.warn, color: '#fff', fontSize: 10, fontWeight: 700, fontFamily: TYPE.body }}>去抢</button>
      </div>

      {/* 聊天弹幕 */}
      <div style={{ position: 'absolute', bottom: 76, left: 10, right: 100, display: 'flex', flexDirection: 'column', gap: 4, zIndex: 5 }}>
        {[
          { user: '系统', msg: '欢迎来到汤圆直播间，文明用语～', sys: true },
          { user: '布偶小甜饼', msg: '汤圆太帅了 😍😍😍' },
          { user: '金毛百科', msg: '问一下沐浴露链接' },
          { user: '铲屎官小王', msg: '来啦来啦～' },
        ].map((m, i) => (
          <div key={i} style={{ alignSelf: 'flex-start', maxWidth: '85%', padding: '5px 10px', borderRadius: 14, background: m.sys ? `${p.warn}cc` : 'rgba(0,0,0,0.35)', color: '#fff', fontSize: 11, backdropFilter: 'blur(8px)' }}>
            {!m.sys && <span style={{ color: p.accent, fontWeight: 700 }}>{m.user}: </span>}
            {m.msg}
          </div>
        ))}
      </div>

      {/* 底部操作 */}
      <div style={{ position: 'absolute', bottom: 22, left: 10, right: 10, display: 'flex', alignItems: 'center', gap: 8, zIndex: 5 }}>
        <div style={{ flex: 1, height: 36, borderRadius: 18, background: 'rgba(255,255,255,0.15)', backdropFilter: 'blur(10px)', padding: '0 14px', display: 'flex', alignItems: 'center', color: 'rgba(255,255,255,0.7)', fontSize: 11, border: '1px solid rgba(255,255,255,0.2)' }}>
          聊点什么吧...
        </div>
        {[
          <svg key="gift" width="22" height="22" viewBox="0 0 24 24" fill="#fff"><path d="M20 7h-2.2A3 3 0 0 0 13 3.1l-1 1-1-1A3 3 0 0 0 6.2 7H4a1 1 0 0 0-1 1v3a1 1 0 0 0 1 1h1v8a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2v-8h1a1 1 0 0 0 1-1V8a1 1 0 0 0-1-1z" /></svg>,
          <svg key="heart" width="22" height="22" viewBox="0 0 24 24" fill="#ff4e6a"><path d="M12 21s-7-4.5-9-9C1.5 7.5 4 4 8 4c1.7 0 3.3.8 4 2 .7-1.2 2.3-2 4-2 4 0 6.5 3.5 5 8-2 4.5-9 9-9 9z" /></svg>,
          <svg key="share" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#fff" strokeWidth="2" strokeLinecap="round"><circle cx="18" cy="5" r="3" /><circle cx="6" cy="12" r="3" /><circle cx="18" cy="19" r="3" /><path d="M8.59 13.51l6.83 3.98M15.41 6.51l-6.82 3.98" /></svg>,
        ].map((ic, i) => (
          <div key={i} style={{ width: 36, height: 36, borderRadius: 18, background: 'rgba(255,255,255,0.15)', backdropFilter: 'blur(10px)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>{ic}</div>
        ))}
      </div>
    </div>
  );
}

function SignInScreen({ palette, radius }) {
  const p = palette;
  const days = [
    { d: 1, done: true, reward: '5 🐾' },
    { d: 2, done: true, reward: '5 🐾' },
    { d: 3, done: true, reward: '10 🐾' },
    { d: 4, done: true, reward: '10 🐾' },
    { d: 5, done: false, today: true, reward: '15 🐾' },
    { d: 6, done: false, reward: '20 🐾' },
    { d: 7, done: false, reward: '🎁 惊喜礼包' },
  ];

  return (
    <div style={{ width: '100%', height: '100%', background: p.bg, position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color="#fff" />
      {/* Hero */}
      <div style={{ position: 'absolute', top: 0, left: 0, right: 0, height: 260, background: `linear-gradient(160deg, ${p.primary}, ${p.primaryInk} 100%)`, overflow: 'hidden' }}>
        <div style={{ position: 'absolute', top: 20, right: -10, opacity: 0.25 }}><PawIcon size={90} color="#fff" /></div>
        <div style={{ position: 'absolute', bottom: 30, left: 20, opacity: 0.18 }}><PawIcon size={50} color="#fff" /></div>
      </div>
      <TopBar palette={{ ...p, ink: '#fff' }} title="" transparent right={<span style={{ fontSize: 12, color: '#fff' }}>规则 ›</span>} />

      {/* 标题 */}
      <div style={{ position: 'absolute', top: 100, left: 0, right: 0, textAlign: 'center', color: '#fff', zIndex: 2 }}>
        <div style={{ fontSize: 22, fontWeight: 800, fontFamily: TYPE.brand }}>每日签到</div>
        <div style={{ fontSize: 12, opacity: 0.9, marginTop: 4 }}>已连续签到 <b style={{ fontFamily: TYPE.num }}>4</b> 天 · 明天领 15 🐾</div>
        <div style={{ marginTop: 14, display: 'inline-flex', alignItems: 'center', gap: 14, padding: '10px 20px', borderRadius: 16, background: 'rgba(255,255,255,0.2)', backdropFilter: 'blur(10px)' }}>
          <div>
            <div style={{ fontSize: 9, opacity: 0.85 }}>我的爪币</div>
            <div style={{ fontSize: 22, fontWeight: 800, fontFamily: TYPE.num }}>1,280 🐾</div>
          </div>
          <div style={{ width: 1, height: 28, background: 'rgba(255,255,255,0.3)' }} />
          <div>
            <div style={{ fontSize: 9, opacity: 0.85 }}>可兑换</div>
            <div style={{ fontSize: 12, fontWeight: 700 }}>9 个奖品</div>
          </div>
        </div>
      </div>

      {/* 签到卡 */}
      <div style={{ position: 'absolute', top: 240, left: 14, right: 14, background: p.surface, borderRadius: radius, padding: 14, zIndex: 3, boxShadow: `0 8px 20px ${p.ink}10` }}>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: 6 }}>
          {days.map(d => (
            <div key={d.d} style={{ textAlign: 'center' }}>
              <div style={{
                width: 34, height: 34, borderRadius: 10, margin: '0 auto',
                background: d.done ? p.primary : d.today ? '#fff' : p.surfaceDim,
                border: d.today ? `2px dashed ${p.primary}` : 'none',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                color: d.done ? '#fff' : d.today ? p.primary : p.inkMuted,
                fontSize: 14, fontWeight: 800, fontFamily: TYPE.num,
              }}>{d.done ? '✓' : d.d}</div>
              <div style={{ fontSize: 8, color: d.today ? p.primary : p.inkMuted, marginTop: 3, fontWeight: d.today ? 700 : 400 }}>
                {d.d === 7 ? '第7天' : `第${d.d}天`}
              </div>
              <div style={{ fontSize: 8, color: p.inkFaint, marginTop: 1 }}>{d.reward}</div>
            </div>
          ))}
        </div>
        <button style={{
          width: '100%', height: 42, borderRadius: 21, border: 'none', marginTop: 14,
          background: `linear-gradient(135deg, ${p.warn}, ${p.accent})`,
          color: '#fff', fontSize: 13, fontWeight: 800,
          boxShadow: `0 4px 12px ${p.warn}50`, fontFamily: TYPE.body,
        }}>🎉 立即签到 · 领 15 爪币</button>
      </div>

      {/* 任务中心 */}
      <div style={{ position: 'absolute', top: 448, left: 0, right: 0, bottom: 0, overflow: 'auto' }}>
        <SectionTitle palette={p} title="🎯 每日任务" subtitle="完成赚爪币" />
        <div style={{ margin: '0 14px', background: p.surface, borderRadius: radius, overflow: 'hidden' }}>
          {[
            { icon: '📸', t: '发布一条笔记', r: '+30 🐾', p: 1, total: 1, done: true },
            { icon: '❤️', t: '给 3 条笔记点赞', r: '+10 🐾', p: 2, total: 3 },
            { icon: '💬', t: '评论 5 次', r: '+15 🐾', p: 1, total: 5 },
            { icon: '🛒', t: '浏览市集 1 分钟', r: '+5 🐾', p: 0, total: 1 },
          ].map((t, i, arr) => (
            <div key={i} style={{ padding: '12px 14px', display: 'flex', alignItems: 'center', gap: 10, borderBottom: i < arr.length - 1 ? `0.5px solid ${p.divider}` : 'none' }}>
              <div style={{ width: 34, height: 34, borderRadius: 10, background: p.primarySoft, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 16 }}>{t.icon}</div>
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: 12, color: p.ink, fontWeight: 600 }}>{t.t}</div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginTop: 4 }}>
                  <div style={{ flex: 1, height: 4, borderRadius: 2, background: p.surfaceDim, overflow: 'hidden' }}>
                    <div style={{ width: `${(t.p / t.total) * 100}%`, height: '100%', background: p.primary }} />
                  </div>
                  <span style={{ fontSize: 9, color: p.inkMuted, fontFamily: TYPE.num }}>{t.p}/{t.total}</span>
                </div>
              </div>
              <div style={{ textAlign: 'right' }}>
                <div style={{ fontSize: 11, color: p.warn, fontWeight: 700, fontFamily: TYPE.num }}>{t.r}</div>
                <div style={{ marginTop: 3, padding: '3px 8px', borderRadius: 10, background: t.done ? p.surfaceDim : p.primary, color: t.done ? p.inkMuted : '#fff', fontSize: 9, fontWeight: 700 }}>
                  {t.done ? '已领' : '去完成'}
                </div>
              </div>
            </div>
          ))}
        </div>
        <div style={{ height: 20 }} />
      </div>
    </div>
  );
}

Object.assign(window, { OnboardingScreen, ExploreScreen, ReelsScreen, NearbyScreen, LiveScreen, SignInScreen });
