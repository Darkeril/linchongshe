// screens-part11.jsx — Favorites / History / Shipping / Wallet / Support

function FavoritesScreen({ palette, radius }) {
  const p = palette;
  const folders = [
    { name: '全部收藏', count: 234, cover: 0, variant: 'dog', pinned: true },
    { name: '狗粮收藏夹', count: 42, cover: 1, variant: 'puppy' },
    { name: '猫咪穿搭', count: 68, cover: 2, variant: 'cat' },
    { name: '美容护理', count: 28, cover: 3, variant: 'fluff' },
    { name: '训练技巧', count: 15, cover: 4, variant: 'mochi' },
    { name: '急救常识', count: 12, cover: 5, variant: 'dog', private: true },
  ];

  return (
    <div style={{ width: '100%', height: '100%', background: p.bg, position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color={p.ink} />
      <TopBar palette={p} title="我的收藏" showMenu right={<span style={{ fontSize: 13, color: p.primary, fontWeight: 700 }}>＋新建</span>} />

      {/* Tab */}
      <div style={{ position: 'absolute', top: 92, left: 0, right: 0, height: 42, background: p.bg, display: 'flex', padding: '0 18px', gap: 18, borderBottom: `0.5px solid ${p.divider}`, zIndex: 5 }}>
        {[
          { l: '收藏夹', active: true, c: 234 },
          { l: '笔记' },
          { l: '商品', c: 56 },
          { l: '视频', c: 18 },
        ].map((t, i) => (
          <div key={i} style={{ padding: '12px 0', borderBottom: t.active ? `2px solid ${p.primary}` : '2px solid transparent', marginBottom: -0.5, display: 'flex', alignItems: 'center', gap: 4 }}>
            <span style={{ fontSize: 13, fontWeight: t.active ? 800 : 500, color: t.active ? p.ink : p.inkMuted }}>{t.l}</span>
            {t.c && <span style={{ fontSize: 9, color: p.inkMuted, fontFamily: TYPE.num }}>{t.c}</span>}
          </div>
        ))}
      </div>

      <div style={{ position: 'absolute', top: 134, left: 0, right: 0, bottom: 0, overflow: 'auto', padding: '14px 14px 20px' }}>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
          {folders.map((f, i) => (
            <div key={i} style={{ background: p.surface, borderRadius: radius, overflow: 'hidden', position: 'relative' }}>
              <div style={{ height: 100, position: 'relative' }}>
                <PetPlaceholder w={160} h={100} palette={p} seed={f.cover} variant={f.variant} />
                {f.pinned && <div style={{ position: 'absolute', top: 6, left: 6, padding: '2px 7px', borderRadius: 8, background: 'rgba(0,0,0,0.4)', backdropFilter: 'blur(8px)', color: '#fff', fontSize: 9, fontWeight: 600 }}>📌 置顶</div>}
                {f.private && <div style={{ position: 'absolute', top: 6, right: 6, padding: '2px 7px', borderRadius: 8, background: 'rgba(0,0,0,0.4)', backdropFilter: 'blur(8px)', color: '#fff', fontSize: 9, fontWeight: 600 }}>🔒 私密</div>}
              </div>
              <div style={{ padding: 10 }}>
                <div style={{ fontSize: 12, fontWeight: 700, color: p.ink }}>{f.name}</div>
                <div style={{ fontSize: 10, color: p.inkMuted, marginTop: 3, fontFamily: TYPE.num }}>{f.count} 条收藏</div>
              </div>
            </div>
          ))}

          {/* 新建卡片 */}
          <div style={{ height: 164, borderRadius: radius, border: `1.5px dashed ${p.primary}`, background: `${p.primary}08`, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', color: p.primary }}>
            <div style={{ fontSize: 30, fontWeight: 300 }}>＋</div>
            <div style={{ fontSize: 12, fontWeight: 700, marginTop: 4 }}>新建收藏夹</div>
            <div style={{ fontSize: 9, color: p.inkMuted, marginTop: 3 }}>分类整理更方便</div>
          </div>
        </div>
      </div>
    </div>
  );
}

function HistoryScreen({ palette, radius }) {
  const p = palette;
  const groups = [
    {
      title: '今天',
      items: [
        { type: 'note', title: '汤圆的第 28 次洗澡', author: '汤圆家日记', time: '14:32', seed: 0, variant: 'dog' },
        { type: 'product', title: '金毛宝宝狗粮 1.5kg 幼犬装', price: 168, time: '13:18', seed: 1, variant: 'puppy' },
        { type: 'note', title: '金毛毕业啦～训练证书', author: '养宠百科', time: '11:05', seed: 2, variant: 'dog' },
      ],
    },
    {
      title: '昨天',
      items: [
        { type: 'note', title: '秋日拾光 🍂 公园暴走', author: '柴柴麻麻', time: '20:48', seed: 3, variant: 'dog' },
        { type: 'shop', title: '爪爪宠物生活馆', sub: '综合评分 4.8 · 订单 124', time: '19:22', seed: 4, variant: 'mochi' },
        { type: 'product', title: '猫咪磨爪球玩具', price: 39, time: '15:10', seed: 5, variant: 'cat' },
      ],
    },
    {
      title: '10 月 18 日',
      items: [
        { type: 'note', title: '自制南瓜泥配方', author: '奶冻厨房', time: '16:40', seed: 6, variant: 'puppy' },
      ],
    },
  ];

  return (
    <div style={{ width: '100%', height: '100%', background: p.bg, position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color={p.ink} />
      <TopBar palette={p} title="浏览历史" showMenu right={<span style={{ fontSize: 12, color: p.inkMuted }}>🗑️ 清空</span>} />

      {/* Filter */}
      <div style={{ position: 'absolute', top: 92, left: 0, right: 0, padding: '8px 14px', display: 'flex', gap: 6, overflowX: 'auto', borderBottom: `0.5px solid ${p.divider}`, zIndex: 3 }}>
        <Tag palette={p} tone="primary" size="md">全部 · 128</Tag>
        <Tag palette={p} tone="ghost" size="md">📝 笔记</Tag>
        <Tag palette={p} tone="ghost" size="md">🛒 商品</Tag>
        <Tag palette={p} tone="ghost" size="md">🏪 店铺</Tag>
        <Tag palette={p} tone="ghost" size="md">🎥 视频</Tag>
      </div>

      <div style={{ position: 'absolute', top: 140, left: 0, right: 0, bottom: 0, overflow: 'auto', paddingBottom: 20 }}>
        {groups.map((g, gi) => (
          <div key={gi}>
            <div style={{ padding: '12px 18px 4px', display: 'flex', alignItems: 'center', gap: 6 }}>
              <div style={{ width: 3, height: 12, borderRadius: 2, background: p.primary }} />
              <span style={{ fontSize: 12, fontWeight: 700, color: p.ink }}>{g.title}</span>
              <span style={{ fontSize: 10, color: p.inkMuted, fontFamily: TYPE.num }}>· {g.items.length} 项</span>
            </div>
            <div style={{ margin: '0 14px', background: p.surface, borderRadius: radius, overflow: 'hidden' }}>
              {g.items.map((it, i, arr) => (
                <div key={i} style={{ display: 'flex', gap: 10, padding: 10, borderBottom: i < arr.length - 1 ? `0.5px solid ${p.divider}` : 'none' }}>
                  <div style={{ width: 60, height: 60, borderRadius: 8, overflow: 'hidden', flexShrink: 0, position: 'relative' }}>
                    <PetPlaceholder w={60} h={60} palette={p} seed={it.seed} variant={it.variant} />
                    <div style={{ position: 'absolute', bottom: 3, left: 3, padding: '1px 5px', borderRadius: 5, background: 'rgba(0,0,0,0.55)', color: '#fff', fontSize: 8 }}>
                      {it.type === 'note' && '📝'}{it.type === 'product' && '🛒'}{it.type === 'shop' && '🏪'}
                    </div>
                  </div>
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={{ fontSize: 12, color: p.ink, fontWeight: 600, lineHeight: 1.4, display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>{it.title}</div>
                    <div style={{ fontSize: 10, color: p.inkMuted, marginTop: 4 }}>
                      {it.author && `@${it.author} · `}
                      {it.sub}
                      {it.price && <span style={{ color: p.warn, fontWeight: 700, fontFamily: TYPE.num }}>¥{it.price}</span>}
                    </div>
                    <div style={{ fontSize: 9, color: p.inkFaint, marginTop: 4 }}>浏览于 {it.time}</div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

function ShippingScreen({ palette, radius }) {
  const p = palette;
  const steps = [
    { t: '已签收', time: '10/22 14:38', note: '快递已由您本人签收，感谢使用 🐾', done: true, icon: '📦', hi: true },
    { t: '派送中', time: '10/22 09:12', note: '快递员 陈师傅 (138****2345) 正在为您派送', done: true, icon: '🛵' },
    { t: '到达站点', time: '10/22 06:20', note: '【上海徐汇·田林菜场营业点】已到达', done: true, icon: '🏢' },
    { t: '运输中', time: '10/21 22:08', note: '【上海转运中心】已发往【上海徐汇】', done: true, icon: '🚚' },
    { t: '运输中', time: '10/21 18:44', note: '【苏州转运中心】已发往【上海转运中心】', done: true, icon: '🚚' },
    { t: '已揽收', time: '10/21 15:20', note: '【南京仓库】快递员已取件', done: true, icon: '📥' },
    { t: '已发货', time: '10/21 12:30', note: '商家【爪爪宠物生活馆】已发货 SF1234567890', done: true, icon: '🎁' },
  ];

  return (
    <div style={{ width: '100%', height: '100%', background: p.bg, position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color={p.ink} />
      <TopBar palette={p} title="物流详情" right={<span style={{ fontSize: 12, color: p.primary, fontWeight: 600 }}>联系商家</span>} />

      <div style={{ position: 'absolute', top: 92, left: 0, right: 0, bottom: 0, overflow: 'auto', paddingBottom: 20 }}>
        {/* 状态卡 */}
        <div style={{ margin: '8px 14px 0', borderRadius: radius, background: `linear-gradient(135deg, ${p.primary}, ${p.primaryInk})`, padding: '16px 16px 14px', color: '#fff', position: 'relative', overflow: 'hidden' }}>
          <div style={{ position: 'absolute', top: -10, right: -10, opacity: 0.2, fontSize: 80 }}>📦</div>
          <div style={{ display: 'inline-flex', alignItems: 'center', gap: 5, padding: '3px 8px', borderRadius: 8, background: 'rgba(255,255,255,0.25)', fontSize: 10, fontWeight: 700 }}>✓ 已签收</div>
          <div style={{ fontSize: 16, fontWeight: 800, marginTop: 8 }}>爪爪宠物生活馆 共 3 件</div>
          <div style={{ fontSize: 11, opacity: 0.9, marginTop: 4 }}>顺丰速运 · SF1234567890 <span style={{ marginLeft: 6, padding: '1px 6px', background: 'rgba(255,255,255,0.2)', borderRadius: 5 }}>复制</span></div>
          <div style={{ display: 'flex', gap: 8, marginTop: 12 }}>
            {[0, 1, 2].map(i => (
              <div key={i} style={{ width: 40, height: 40, borderRadius: 8, overflow: 'hidden', border: '2px solid rgba(255,255,255,0.4)' }}>
                <PetPlaceholder w={40} h={40} palette={p} seed={i + 4} variant="mochi" />
              </div>
            ))}
            <div style={{ marginLeft: 'auto', alignSelf: 'flex-end', fontSize: 11, opacity: 0.9 }}>查看包裹 ›</div>
          </div>
        </div>

        {/* 物流地图缩略 */}
        <div style={{ margin: '12px 14px 0', padding: 12, background: p.surface, borderRadius: radius, display: 'flex', alignItems: 'center', gap: 12 }}>
          <div style={{ flex: 1, position: 'relative', height: 40 }}>
            <svg width="100%" height="40" viewBox="0 0 260 40">
              <path d="M 15 30 Q 80 5 130 20 T 245 15" stroke={p.primary} strokeWidth="2" fill="none" strokeDasharray="3 3" />
              <circle cx="15" cy="30" r="6" fill={p.inkMuted} />
              <circle cx="130" cy="20" r="5" fill={p.primary} />
              <circle cx="245" cy="15" r="6" fill={p.warn} stroke="#fff" strokeWidth="2" />
            </svg>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 9, color: p.inkMuted, marginTop: 2 }}>
              <span>🏪 南京</span><span>🚚 苏州</span><span>🎉 您的家</span>
            </div>
          </div>
        </div>

        {/* 时间轴 */}
        <div style={{ margin: '14px 14px 0', background: p.surface, borderRadius: radius, padding: '14px 14px 4px' }}>
          <div style={{ fontSize: 12, fontWeight: 700, color: p.ink, marginBottom: 10 }}>📋 物流轨迹</div>
          {steps.map((s, i) => (
            <div key={i} style={{ display: 'flex', gap: 10, position: 'relative', paddingBottom: i < steps.length - 1 ? 14 : 0 }}>
              <div style={{ width: 28, flexShrink: 0, position: 'relative' }}>
                <div style={{
                  width: 28, height: 28, borderRadius: 14,
                  background: s.hi ? p.primary : s.done ? p.primarySoft : p.surfaceDim,
                  color: s.hi ? '#fff' : p.primary, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 13,
                  boxShadow: s.hi ? `0 0 0 4px ${p.primary}20` : 'none',
                }}>{s.icon}</div>
                {i < steps.length - 1 && <div style={{ position: 'absolute', left: 13, top: 30, bottom: -8, width: 2, background: p.divider }} />}
              </div>
              <div style={{ flex: 1 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                  <span style={{ fontSize: 12, fontWeight: 700, color: s.hi ? p.primaryInk : p.ink }}>{s.t}</span>
                  <span style={{ fontSize: 9, color: p.inkMuted, fontFamily: TYPE.num }}>{s.time}</span>
                </div>
                <div style={{ fontSize: 11, color: p.inkSoft, marginTop: 3, lineHeight: 1.5 }}>{s.note}</div>
              </div>
            </div>
          ))}
        </div>

        {/* 地址卡 */}
        <div style={{ margin: '14px 14px 0', padding: 12, background: p.surface, borderRadius: radius, display: 'flex', gap: 10 }}>
          <div style={{ width: 28, height: 28, borderRadius: 8, background: `${p.warn}22`, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>📍</div>
          <div style={{ flex: 1 }}>
            <div style={{ fontSize: 12, fontWeight: 700, color: p.ink }}>张小铲 · 138****1234</div>
            <div style={{ fontSize: 11, color: p.inkSoft, marginTop: 3 }}>上海市徐汇区田林街道 田林路 100 弄 5 号 202 室</div>
          </div>
        </div>
      </div>
    </div>
  );
}

function WalletScreen({ palette, radius }) {
  const p = palette;
  return (
    <div style={{ width: '100%', height: '100%', background: p.bg, position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color="#fff" />
      <div style={{ position: 'absolute', top: 0, left: 0, right: 0, height: 260, background: `linear-gradient(160deg, ${p.primaryInk}, ${p.primary})`, overflow: 'hidden' }}>
        <div style={{ position: 'absolute', top: 20, right: -20, opacity: 0.15 }}><PawIcon size={120} color="#fff" /></div>
      </div>
      <TopBar palette={{ ...p, ink: '#fff' }} title="我的钱包" transparent showMenu right={<span style={{ fontSize: 12, color: '#fff' }}>明细 ›</span>} />

      <div style={{ position: 'absolute', top: 100, left: 14, right: 14, zIndex: 2 }}>
        <div style={{ fontSize: 12, color: 'rgba(255,255,255,0.85)' }}>钱包余额 (CNY)</div>
        <div style={{ fontSize: 36, fontWeight: 800, color: '#fff', marginTop: 4, fontFamily: TYPE.num, letterSpacing: -1 }}>
          ¥ 1,284<span style={{ fontSize: 20, opacity: 0.7 }}>.60</span>
        </div>
        <div style={{ display: 'flex', gap: 8, marginTop: 16 }}>
          <button style={{ flex: 1, padding: '10px 0', borderRadius: 18, border: 'none', background: '#fff', color: p.primaryInk, fontSize: 13, fontWeight: 700, fontFamily: TYPE.body }}>↓ 充值</button>
          <button style={{ flex: 1, padding: '10px 0', borderRadius: 18, background: 'rgba(255,255,255,0.2)', border: '1px solid rgba(255,255,255,0.4)', color: '#fff', fontSize: 13, fontWeight: 700, backdropFilter: 'blur(10px)', fontFamily: TYPE.body }}>↑ 提现</button>
        </div>
      </div>

      <div style={{ position: 'absolute', top: 248, left: 0, right: 0, bottom: 0, overflow: 'auto', paddingBottom: 20 }}>
        {/* 资产格 */}
        <div style={{ margin: '0 14px', display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 8 }}>
          {[
            { icon: '🐾', v: '1,280', label: '爪币', sub: '可兑礼品', color: p.primary },
            { icon: '🎟️', v: '6', label: '优惠券', sub: '3 张将过期', color: p.accent },
            { icon: '💳', v: '88', label: '会员积分', sub: 'VIP 2', color: p.warn },
          ].map((a, i) => (
            <div key={i} style={{ padding: 12, background: p.surface, borderRadius: radius, textAlign: 'center' }}>
              <div style={{ fontSize: 20 }}>{a.icon}</div>
              <div style={{ fontSize: 16, fontWeight: 800, color: a.color, marginTop: 4, fontFamily: TYPE.num }}>{a.v}</div>
              <div style={{ fontSize: 10, color: p.ink, fontWeight: 600 }}>{a.label}</div>
              <div style={{ fontSize: 8, color: p.inkMuted, marginTop: 2 }}>{a.sub}</div>
            </div>
          ))}
        </div>

        {/* 快捷功能 */}
        <SectionTitle palette={p} title="⚡ 快捷功能" />
        <div style={{ margin: '0 14px', padding: '14px 10px', background: p.surface, borderRadius: radius, display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 12 }}>
          {[
            { icon: '📋', l: '账单' },
            { icon: '🏦', l: '银行卡' },
            { icon: '🎁', l: '红包' },
            { icon: '🛡️', l: '安全中心' },
            { icon: '🔄', l: '退款' },
            { icon: '💰', l: '理财' },
            { icon: '📄', l: '发票' },
            { icon: '❓', l: '帮助' },
          ].map((q, i) => (
            <div key={i} style={{ textAlign: 'center' }}>
              <div style={{ width: 38, height: 38, borderRadius: 10, background: p.primarySoft, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 18, margin: '0 auto' }}>{q.icon}</div>
              <div style={{ fontSize: 10, color: p.ink, marginTop: 5, fontWeight: 500 }}>{q.l}</div>
            </div>
          ))}
        </div>

        {/* 账单明细 */}
        <SectionTitle palette={p} title="📒 近期交易" right={<span>全部 ›</span>} />
        <div style={{ margin: '0 14px', background: p.surface, borderRadius: radius, overflow: 'hidden' }}>
          {[
            { icon: '🛒', t: '爪爪宠物生活馆', d: '消费 · 10/22 14:32', v: '-286.00', neg: true },
            { icon: '💰', t: '充值', d: '支付宝 · 10/21 09:15', v: '+500.00' },
            { icon: '🎁', t: '签到奖励', d: '连续签到7天 · 10/20', v: '+5.00' },
            { icon: '↩️', t: '退款到账', d: '订单 #5823 · 10/19', v: '+39.00' },
            { icon: '🐾', t: '爪币兑换', d: '100 爪币 抵 10 元 · 10/18', v: '-10.00', neg: true },
          ].map((r, i, arr) => (
            <div key={i} style={{ display: 'flex', alignItems: 'center', gap: 10, padding: '12px 14px', borderBottom: i < arr.length - 1 ? `0.5px solid ${p.divider}` : 'none' }}>
              <div style={{ width: 34, height: 34, borderRadius: 10, background: p.primarySoft, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 14 }}>{r.icon}</div>
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: 12, color: p.ink, fontWeight: 600 }}>{r.t}</div>
                <div style={{ fontSize: 9, color: p.inkMuted, marginTop: 2 }}>{r.d}</div>
              </div>
              <div style={{ fontSize: 14, fontWeight: 700, color: r.neg ? p.ink : '#7fa780', fontFamily: TYPE.num }}>{r.v}</div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

function SupportScreen({ palette, radius }) {
  const p = palette;
  return (
    <div style={{ width: '100%', height: '100%', background: p.bg, position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color={p.ink} />
      <TopBar palette={p} title="在线客服 · 小爪" showMenu right={<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke={p.ink} strokeWidth="2" strokeLinecap="round"><circle cx="12" cy="12" r="1.5" fill={p.ink} /><circle cx="19" cy="12" r="1.5" fill={p.ink} /><circle cx="5" cy="12" r="1.5" fill={p.ink} /></svg>} />

      {/* AI 客服条 */}
      <div style={{ position: 'absolute', top: 92, left: 0, right: 0, padding: '10px 14px', background: `linear-gradient(90deg, ${p.primarySoft}, ${p.surface})`, display: 'flex', alignItems: 'center', gap: 10, zIndex: 3, borderBottom: `0.5px solid ${p.divider}` }}>
        <div style={{ width: 32, height: 32, borderRadius: 16, background: `linear-gradient(135deg, ${p.primary}, ${p.accent})`, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 16, position: 'relative' }}>
          🤖
          <div style={{ position: 'absolute', bottom: -1, right: -1, width: 10, height: 10, borderRadius: 5, background: '#7fa780', border: '2px solid #fff' }} />
        </div>
        <div style={{ flex: 1 }}>
          <div style={{ fontSize: 12, fontWeight: 700, color: p.ink }}>小爪 · AI 助手</div>
          <div style={{ fontSize: 10, color: '#7fa780', marginTop: 1 }}>🟢 在线 · 平均响应 3 秒 · 转人工 ›</div>
        </div>
      </div>

      {/* Chat */}
      <div style={{ position: 'absolute', top: 145, left: 0, right: 0, bottom: 140, overflow: 'auto', padding: '12px 14px' }}>
        <div style={{ textAlign: 'center', fontSize: 9, color: p.inkMuted, margin: '4px 0 10px' }}>—— 今天 14:28 ——</div>

        {/* AI 开场 */}
        <div style={{ display: 'flex', gap: 8, marginBottom: 12 }}>
          <div style={{ width: 28, height: 28, borderRadius: 14, background: `linear-gradient(135deg, ${p.primary}, ${p.accent})`, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 14, flexShrink: 0 }}>🤖</div>
          <div style={{ maxWidth: '78%' }}>
            <div style={{ background: p.surface, padding: '10px 12px', borderRadius: '12px 12px 12px 4px', fontSize: 12, color: p.ink, lineHeight: 1.55 }}>
              你好呀～我是小爪 🐾<br />
              请问遇到什么问题啦？我可以帮你：
            </div>
            {/* 快捷问题 */}
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6, marginTop: 6 }}>
              {['📦 订单问题', '💰 退款/发票', '🛡️ 账号安全', '🐾 宠物咨询', '🎁 优惠券', '其他问题'].map((q, i) => (
                <div key={i} style={{ padding: '6px 10px', borderRadius: 14, background: '#fff', border: `1px solid ${p.divider}`, fontSize: 11, color: p.primaryInk, fontWeight: 600 }}>{q}</div>
              ))}
            </div>
          </div>
        </div>

        {/* 用户 */}
        <div style={{ display: 'flex', gap: 8, marginBottom: 12, flexDirection: 'row-reverse' }}>
          <PetAvatar variant="dog" size={28} />
          <div style={{ maxWidth: '78%', background: p.primary, padding: '10px 12px', borderRadius: '12px 12px 4px 12px', fontSize: 12, color: '#fff' }}>
            订单 #58230 的快递到哪里了呀？
          </div>
        </div>

        {/* AI 答 · 带卡片 */}
        <div style={{ display: 'flex', gap: 8, marginBottom: 12 }}>
          <div style={{ width: 28, height: 28, borderRadius: 14, background: `linear-gradient(135deg, ${p.primary}, ${p.accent})`, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 14, flexShrink: 0 }}>🤖</div>
          <div style={{ maxWidth: '78%' }}>
            <div style={{ background: p.surface, padding: '10px 12px', borderRadius: '12px 12px 12px 4px', fontSize: 12, color: p.ink, lineHeight: 1.55 }}>
              已经查到啦～您的订单在派送中，预计今天下午 4 点前送达 📦
            </div>
            {/* 订单卡 */}
            <div style={{ marginTop: 6, background: p.surface, borderRadius: 12, padding: 10, display: 'flex', gap: 8 }}>
              <div style={{ width: 46, height: 46, borderRadius: 8, overflow: 'hidden' }}>
                <PetPlaceholder w={46} h={46} palette={p} seed={1} variant="mochi" />
              </div>
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: 11, fontWeight: 700, color: p.ink }}>订单 #58230</div>
                <div style={{ fontSize: 10, color: p.inkMuted, marginTop: 2 }}>派送中 · 顺丰速运</div>
                <div style={{ fontSize: 10, color: p.primary, marginTop: 3, fontWeight: 600 }}>查看物流 →</div>
              </div>
            </div>
            <div style={{ marginTop: 4, fontSize: 9, color: p.inkMuted, paddingLeft: 4 }}>✓ 小爪 · 14:30</div>
          </div>
        </div>

        {/* 用户追问 */}
        <div style={{ display: 'flex', gap: 8, marginBottom: 12, flexDirection: 'row-reverse' }}>
          <PetAvatar variant="dog" size={28} />
          <div style={{ maxWidth: '78%', background: p.primary, padding: '10px 12px', borderRadius: '12px 12px 4px 12px', fontSize: 12, color: '#fff' }}>
            我要改地址，现在还来得及吗
          </div>
        </div>

        {/* 正在输入 */}
        <div style={{ display: 'flex', gap: 8 }}>
          <div style={{ width: 28, height: 28, borderRadius: 14, background: `linear-gradient(135deg, ${p.primary}, ${p.accent})`, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 14, flexShrink: 0 }}>🤖</div>
          <div style={{ background: p.surface, padding: '12px', borderRadius: '12px 12px 12px 4px', display: 'flex', gap: 3 }}>
            {[0, 1, 2].map(i => (
              <div key={i} style={{ width: 6, height: 6, borderRadius: 3, background: p.inkMuted, animation: `typing 1.4s ${i * 0.15}s infinite` }} />
            ))}
          </div>
        </div>
      </div>

      {/* 满意度 & 输入 */}
      <div style={{ position: 'absolute', bottom: 70, left: 0, right: 0, padding: '8px 14px', background: `${p.warn}12`, fontSize: 10, color: p.inkSoft, display: 'flex', alignItems: 'center', gap: 8 }}>
        <span>💡 本次服务满意吗？</span>
        <div style={{ display: 'flex', gap: 5 }}>
          <Tag palette={p} tone="soft" size="xs">😍 满意</Tag>
          <Tag palette={p} tone="ghost" size="xs">😐 一般</Tag>
          <Tag palette={p} tone="ghost" size="xs">😞 不满意</Tag>
        </div>
      </div>

      <div style={{ position: 'absolute', bottom: 0, left: 0, right: 0, padding: '10px 10px 22px', background: p.surface, borderTop: `0.5px solid ${p.divider}`, display: 'flex', alignItems: 'center', gap: 8 }}>
        <span style={{ fontSize: 20, color: p.inkMuted }}>📎</span>
        <div style={{ flex: 1, height: 34, borderRadius: 17, background: p.surfaceDim, padding: '0 14px', display: 'flex', alignItems: 'center', fontSize: 12, color: p.inkMuted }}>输入问题...</div>
        <div style={{ width: 34, height: 34, borderRadius: 17, background: p.primary, color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="#fff"><path d="M22 2L11 13M22 2l-7 20-4-9-9-4z" stroke="#fff" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round" /></svg>
        </div>
      </div>

      <style>{`@keyframes typing { 0%, 60%, 100% { transform: translateY(0); opacity: 0.4; } 30% { transform: translateY(-4px); opacity: 1; } }`}</style>
    </div>
  );
}

Object.assign(window, { FavoritesScreen, HistoryScreen, ShippingScreen, WalletScreen, SupportScreen });
