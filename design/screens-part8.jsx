// screens-part8.jsx — 交易闭环：Cart / Checkout / Orders / OrderDetail

function CartScreen({ palette, radius }) {
  const p = palette;
  const items = [
    { name: '手工烘焙肉干礼盒', spec: '4 口味混合装 · 200g', price: 68, qty: 2, seed: 4, variant: 'puppy', checked: true, shop: '爱宠手作坊' },
    { name: '蓬松云朵猫窝 M', spec: '奶油色 · 可机洗', price: 129, qty: 1, seed: 5, variant: 'mochi', checked: true, shop: '爱宠手作坊' },
    { name: '防滑陶瓷双碗', spec: '食品级 · 抗菌款', price: 89, qty: 1, seed: 6, variant: 'cat', checked: false, shop: '毛孩之家' },
    { name: '渴望低敏主粮 2kg', spec: '狗粮 · 六种鱼配方', price: 198, qty: 1, seed: 1, variant: 'dog', checked: true, shop: '毛孩之家' },
  ];

  const QtyControl = ({ qty }) => (
    <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
      <div style={{ width: 22, height: 22, borderRadius: 6, border: `1px solid ${p.divider}`, display: 'flex', alignItems: 'center', justifyContent: 'center', color: p.inkSoft, fontSize: 14, lineHeight: 1 }}>−</div>
      <div style={{ minWidth: 22, textAlign: 'center', fontSize: 12, color: p.ink, fontFamily: TYPE.num, fontWeight: 600 }}>{qty}</div>
      <div style={{ width: 22, height: 22, borderRadius: 6, background: p.primary, color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 14, lineHeight: 1 }}>+</div>
    </div>
  );

  const Checkbox = ({ checked }) => (
    <div style={{
      width: 18, height: 18, borderRadius: 9,
      background: checked ? p.primary : 'transparent',
      border: `1.5px solid ${checked ? p.primary : p.inkFaint}`,
      display: 'flex', alignItems: 'center', justifyContent: 'center',
    }}>
      {checked && <svg width="10" height="10" viewBox="0 0 10 10" fill="none" stroke="#fff" strokeWidth="2" strokeLinecap="round"><path d="M2 5l2 2 4-4" /></svg>}
    </div>
  );

  // group by shop
  const shops = {};
  items.forEach(it => { if (!shops[it.shop]) shops[it.shop] = []; shops[it.shop].push(it); });
  const total = items.filter(i => i.checked).reduce((s, i) => s + i.price * i.qty, 0);
  const count = items.filter(i => i.checked).reduce((s, i) => s + i.qty, 0);

  return (
    <div style={{ width: '100%', height: '100%', background: p.bg, position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color={p.ink} />
      <TopBar palette={p} title="购物车" subtitle="共 4 件商品"
        right={<span style={{ fontSize: 12, color: p.inkSoft }}>管理</span>} />

      <div style={{ position: 'absolute', top: 96, left: 0, right: 0, bottom: 86, overflow: 'auto', padding: '4px 14px 10px' }}>
        {Object.entries(shops).map(([shop, its]) => (
          <div key={shop} style={{ marginBottom: 10, background: p.surface, borderRadius: radius, overflow: 'hidden' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '12px 14px', borderBottom: `0.5px solid ${p.divider}` }}>
              <Checkbox checked />
              <span style={{ fontSize: 13, fontWeight: 700, color: p.ink }}>🏪 {shop}</span>
              <span style={{ fontSize: 10, color: p.primary, marginLeft: 6 }}>领券 ›</span>
              <Tag palette={p} tone="soft" size="xs" style={{ marginLeft: 'auto' }}>满 199 减 30</Tag>
            </div>
            {its.map((it, i) => (
              <div key={i} style={{ display: 'flex', gap: 10, padding: '12px 14px', alignItems: 'center' }}>
                <Checkbox checked={it.checked} />
                <div style={{ width: 70, height: 70, borderRadius: 10, overflow: 'hidden', flexShrink: 0 }}>
                  <PetPlaceholder w={70} h={70} palette={p} seed={it.seed} variant={it.variant} />
                </div>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ fontSize: 12, color: p.ink, fontWeight: 600, lineHeight: 1.3, overflow: 'hidden', textOverflow: 'ellipsis', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical' }}>{it.name}</div>
                  <div style={{ display: 'inline-flex', marginTop: 4, padding: '2px 6px', borderRadius: 6, background: p.surfaceDim, fontSize: 10, color: p.inkMuted }}>
                    {it.spec} ›
                  </div>
                  <div style={{ display: 'flex', alignItems: 'flex-end', justifyContent: 'space-between', marginTop: 6 }}>
                    <div style={{ display: 'flex', alignItems: 'baseline', gap: 2 }}>
                      <span style={{ fontSize: 10, color: p.primary, fontWeight: 700 }}>¥</span>
                      <span style={{ fontSize: 16, color: p.primary, fontWeight: 800, fontFamily: TYPE.num }}>{it.price}</span>
                    </div>
                    <QtyControl qty={it.qty} />
                  </div>
                </div>
              </div>
            ))}
          </div>
        ))}

        {/* 猜你喜欢 */}
        <div style={{ padding: '6px 0', fontSize: 12, color: p.inkMuted, textAlign: 'center' }}>———— 猜你还喜欢 ————</div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8 }}>
          {[{ s: 2, v: 'fluff', n: '温感陶瓷饮水机', p: 159 }, { s: 7, v: 'dog', n: '牵引绳 2.0 加粗款', p: 59 }].map((r, i) => (
            <div key={i} style={{ background: p.surface, borderRadius: radius, overflow: 'hidden' }}>
              <div style={{ height: 110 }}><PetPlaceholder w={160} h={110} palette={p} seed={r.s} variant={r.v} /></div>
              <div style={{ padding: '8px 10px' }}>
                <div style={{ fontSize: 11, color: p.ink, fontWeight: 600 }}>{r.n}</div>
                <div style={{ fontSize: 14, color: p.primary, fontWeight: 800, marginTop: 4, fontFamily: TYPE.num }}>¥{r.p}</div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* 结算条 */}
      <div style={{ position: 'absolute', bottom: 0, left: 0, right: 0, height: 86, background: p.surface, borderTop: `0.5px solid ${p.divider}`, padding: '12px 14px 22px', display: 'flex', alignItems: 'center', gap: 10 }}>
        <Checkbox checked />
        <span style={{ fontSize: 12, color: p.ink }}>全选</span>
        <div style={{ flex: 1, marginLeft: 8 }}>
          <div style={{ fontSize: 11, color: p.inkSoft }}>合计 <span style={{ fontSize: 18, color: p.warn, fontWeight: 800, fontFamily: TYPE.num }}>¥{total}</span></div>
          <div style={{ fontSize: 10, color: p.inkMuted }}>已省 ¥48 · 含运费</div>
        </div>
        <button style={{
          padding: '10px 22px', borderRadius: 20, border: 'none',
          background: `linear-gradient(135deg, ${p.primary}, ${p.primaryInk})`,
          color: '#fff', fontSize: 13, fontWeight: 700,
          boxShadow: `0 4px 12px ${p.primary}50`,
          fontFamily: TYPE.body,
        }}>去结算 ({count})</button>
      </div>
    </div>
  );
}

function CheckoutScreen({ palette, radius }) {
  const p = palette;
  return (
    <div style={{ width: '100%', height: '100%', background: p.bg, position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color={p.ink} />
      <TopBar palette={p} title="确认订单" />

      <div style={{ position: 'absolute', top: 92, left: 0, right: 0, bottom: 86, overflow: 'auto', padding: '8px 14px 20px' }}>
        {/* 地址 */}
        <div style={{ background: p.surface, borderRadius: radius, padding: 14, position: 'relative', overflow: 'hidden' }}>
          <div style={{ display: 'flex', gap: 10 }}>
            <div style={{
              width: 32, height: 32, borderRadius: 8, background: p.primarySoft,
              display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 16, flexShrink: 0,
            }}>📍</div>
            <div style={{ flex: 1 }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <span style={{ fontSize: 14, fontWeight: 700, color: p.ink }}>汤圆妈</span>
                <span style={{ fontSize: 12, color: p.inkMuted, fontFamily: TYPE.num }}>138 **** 2341</span>
                <Tag palette={p} tone="soft" size="xs">默认</Tag>
              </div>
              <div style={{ fontSize: 12, color: p.inkSoft, marginTop: 5, lineHeight: 1.5 }}>
                上海市 徐汇区 漕溪北路 333 号 8 栋 502 室
              </div>
            </div>
            <svg width="6" height="10" viewBox="0 0 6 10" fill="none" stroke={p.inkFaint} strokeWidth="1.5" strokeLinecap="round" style={{ alignSelf: 'center' }}><path d="M1 1l4 4-4 4" /></svg>
          </div>
          {/* 波浪装饰条 */}
          <svg viewBox="0 0 300 6" preserveAspectRatio="none" style={{ position: 'absolute', bottom: 0, left: 0, right: 0, width: '100%', height: 6 }}>
            <path d="M0 3 Q 15 0, 30 3 T 60 3 T 90 3 T 120 3 T 150 3 T 180 3 T 210 3 T 240 3 T 270 3 T 300 3" stroke={p.primary} strokeWidth="1.5" fill="none" />
          </svg>
        </div>

        {/* 商品列表 */}
        <div style={{ background: p.surface, borderRadius: radius, marginTop: 10, overflow: 'hidden' }}>
          <div style={{ padding: '12px 14px', borderBottom: `0.5px solid ${p.divider}`, fontSize: 12, fontWeight: 700, color: p.ink }}>
            🏪 爱宠手作坊 <span style={{ color: p.inkMuted, fontWeight: 500, marginLeft: 4 }}>· 2 件</span>
          </div>
          {[
            { name: '手工烘焙肉干礼盒', spec: '4 口味混合', price: 68, qty: 2, seed: 4, variant: 'puppy' },
            { name: '蓬松云朵猫窝 M', spec: '奶油色', price: 129, qty: 1, seed: 5, variant: 'mochi' },
          ].map((it, i) => (
            <div key={i} style={{ display: 'flex', gap: 10, padding: '12px 14px', borderBottom: `0.5px solid ${p.divider}` }}>
              <div style={{ width: 60, height: 60, borderRadius: 8, overflow: 'hidden', flexShrink: 0 }}>
                <PetPlaceholder w={60} h={60} palette={p} seed={it.seed} variant={it.variant} />
              </div>
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontSize: 12, color: p.ink, fontWeight: 600, lineHeight: 1.3 }}>{it.name}</div>
                <div style={{ fontSize: 10, color: p.inkMuted, marginTop: 3 }}>{it.spec}</div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', marginTop: 5 }}>
                  <span style={{ fontSize: 14, color: p.ink, fontWeight: 800, fontFamily: TYPE.num }}>¥{it.price}</span>
                  <span style={{ fontSize: 11, color: p.inkMuted, fontFamily: TYPE.num }}>× {it.qty}</span>
                </div>
              </div>
            </div>
          ))}
          {/* 配送 */}
          <div style={{ padding: '12px 14px', display: 'flex', alignItems: 'center' }}>
            <span style={{ fontSize: 12, color: p.inkSoft }}>配送方式</span>
            <span style={{ marginLeft: 'auto', fontSize: 12, color: p.ink }}>顺丰速运 · 免邮</span>
          </div>
        </div>

        {/* 优惠 */}
        <div style={{ background: p.surface, borderRadius: radius, marginTop: 10, overflow: 'hidden' }}>
          {[
            { l: '优惠券', v: '立减 ¥30', p: true },
            { l: '爱宠豆', v: '抵扣 ¥5.8', p: true },
            { l: '订单备注', v: '留言给商家' },
          ].map((r, i, arr) => (
            <div key={i} style={{ display: 'flex', alignItems: 'center', padding: '12px 14px', borderBottom: i < arr.length - 1 ? `0.5px solid ${p.divider}` : 'none' }}>
              <span style={{ fontSize: 12, color: p.inkSoft }}>{r.l}</span>
              <span style={{ marginLeft: 'auto', fontSize: 12, color: r.p ? p.primary : p.inkMuted, fontWeight: r.p ? 700 : 400 }}>{r.v}</span>
              <svg width="6" height="10" viewBox="0 0 6 10" fill="none" stroke={p.inkFaint} strokeWidth="1.5" strokeLinecap="round" style={{ marginLeft: 6 }}><path d="M1 1l4 4-4 4" /></svg>
            </div>
          ))}
        </div>

        {/* 金额明细 */}
        <div style={{ background: p.surface, borderRadius: radius, marginTop: 10, padding: '12px 14px' }}>
          {[
            ['商品金额', '¥265'],
            ['运费', '¥0'],
            ['优惠券', '-¥30'],
            ['爱宠豆抵扣', '-¥5.8'],
          ].map(([l, v], i) => (
            <div key={i} style={{ display: 'flex', justifyContent: 'space-between', padding: '4px 0', fontSize: 12 }}>
              <span style={{ color: p.inkSoft }}>{l}</span>
              <span style={{ color: v.startsWith('-') ? p.warn : p.ink, fontFamily: TYPE.num }}>{v}</span>
            </div>
          ))}
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', marginTop: 8, paddingTop: 8, borderTop: `0.5px dashed ${p.divider}` }}>
            <span style={{ fontSize: 12, color: p.ink, fontWeight: 700 }}>应付</span>
            <span><span style={{ fontSize: 12, color: p.warn, fontWeight: 700 }}>¥</span><span style={{ fontSize: 22, color: p.warn, fontWeight: 800, fontFamily: TYPE.num }}>229.2</span></span>
          </div>
        </div>
      </div>

      {/* 提交 */}
      <div style={{ position: 'absolute', bottom: 0, left: 0, right: 0, height: 86, background: p.surface, borderTop: `0.5px solid ${p.divider}`, padding: '12px 14px 22px', display: 'flex', alignItems: 'center', gap: 10 }}>
        <div style={{ flex: 1 }}>
          <div style={{ fontSize: 10, color: p.inkMuted }}>应付</div>
          <div><span style={{ fontSize: 13, color: p.warn, fontWeight: 700 }}>¥</span><span style={{ fontSize: 22, color: p.warn, fontWeight: 800, fontFamily: TYPE.num }}>229.2</span></div>
        </div>
        <button style={{
          padding: '12px 26px', borderRadius: 22, border: 'none',
          background: `linear-gradient(135deg, ${p.primary}, ${p.primaryInk})`,
          color: '#fff', fontSize: 14, fontWeight: 700,
          boxShadow: `0 4px 12px ${p.primary}50`, fontFamily: TYPE.body,
        }}>提交订单 🐾</button>
      </div>
    </div>
  );
}

function OrdersScreen({ palette, radius }) {
  const p = palette;
  const [tab, setTab] = React.useState('all');
  const orders = [
    { status: '待付款', statusColor: 'warn', shop: '爱宠手作坊', items: [{ seed: 4, variant: 'puppy', name: '手工烘焙肉干礼盒', spec: '4 口味', price: 68, qty: 2 }], total: 136, count: 2, action: '去付款', countdown: '剩 14:32' },
    { status: '待发货', statusColor: 'primary', shop: '毛孩之家', items: [{ seed: 1, variant: 'dog', name: '渴望低敏主粮 2kg', spec: '六种鱼配方', price: 198, qty: 1 }], total: 198, count: 1, action: '提醒发货' },
    { status: '运输中', statusColor: 'accent', shop: '爱宠手作坊', items: [
      { seed: 5, variant: 'mochi', name: '蓬松云朵猫窝 M', spec: '奶油色', price: 129, qty: 1 },
      { seed: 6, variant: 'cat', name: '防滑陶瓷双碗', spec: '食品级', price: 89, qty: 1 },
    ], total: 218, count: 2, action: '查看物流', ship: '顺丰 · 已到徐汇区' },
    { status: '已完成', statusColor: 'muted', shop: '毛孩之家', items: [{ seed: 2, variant: 'fluff', name: '温感陶瓷饮水机', spec: '暖白色', price: 159, qty: 1 }], total: 159, count: 1, action: '评价有礼 +10 🐾' },
  ];

  return (
    <div style={{ width: '100%', height: '100%', background: p.bg, position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color={p.ink} />
      <TopBar palette={p} title="我的订单" right={<SearchIcon size={18} color={p.ink} />} />

      <div style={{ position: 'absolute', top: 92, left: 0, right: 0, height: 44, background: p.bg, display: 'flex', padding: '0 12px', justifyContent: 'space-around', zIndex: 10 }}>
        {[
          { id: 'all', label: '全部' },
          { id: 'pay', label: '待付款', dot: true },
          { id: 'ship', label: '待发货' },
          { id: 'recv', label: '运输中' },
          { id: 'done', label: '已完成' },
        ].map(t => (
          <div key={t.id} onClick={() => setTab(t.id)} style={{
            padding: '12px 4px', position: 'relative',
            borderBottom: tab === t.id ? `2px solid ${p.primary}` : '2px solid transparent',
          }}>
            <span style={{ fontSize: 12, fontWeight: tab === t.id ? 700 : 500, color: tab === t.id ? p.ink : p.inkMuted }}>{t.label}</span>
            {t.dot && <div style={{ position: 'absolute', top: 10, right: -4, width: 5, height: 5, borderRadius: 3, background: p.warn }} />}
          </div>
        ))}
      </div>

      <div style={{ position: 'absolute', top: 136, left: 0, right: 0, bottom: 0, overflow: 'auto', padding: '8px 14px 30px' }}>
        {orders.map((o, i) => {
          const color = { warn: p.warn, primary: p.primary, accent: p.accent, muted: p.inkMuted }[o.statusColor];
          return (
            <div key={i} style={{ background: p.surface, borderRadius: radius, marginBottom: 10, overflow: 'hidden' }}>
              <div style={{ display: 'flex', alignItems: 'center', padding: '12px 14px', borderBottom: `0.5px solid ${p.divider}` }}>
                <span style={{ fontSize: 12, fontWeight: 700, color: p.ink }}>🏪 {o.shop}</span>
                <span style={{ marginLeft: 'auto', fontSize: 12, color, fontWeight: 700 }}>{o.status}{o.countdown && <span style={{ fontSize: 10, color: p.warn, marginLeft: 4, fontFamily: TYPE.num }}>{o.countdown}</span>}</span>
              </div>
              {o.items.map((it, j) => (
                <div key={j} style={{ display: 'flex', gap: 10, padding: '12px 14px' }}>
                  <div style={{ width: 62, height: 62, borderRadius: 8, overflow: 'hidden', flexShrink: 0 }}>
                    <PetPlaceholder w={62} h={62} palette={p} seed={it.seed} variant={it.variant} />
                  </div>
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={{ fontSize: 12, color: p.ink, fontWeight: 600, lineHeight: 1.3, overflow: 'hidden', textOverflow: 'ellipsis', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical' }}>{it.name}</div>
                    <div style={{ fontSize: 10, color: p.inkMuted, marginTop: 3 }}>{it.spec}</div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', marginTop: 4 }}>
                      <span style={{ fontSize: 13, color: p.ink, fontWeight: 700, fontFamily: TYPE.num }}>¥{it.price}</span>
                      <span style={{ fontSize: 10, color: p.inkMuted, fontFamily: TYPE.num }}>× {it.qty}</span>
                    </div>
                  </div>
                </div>
              ))}
              {o.ship && <div style={{ padding: '0 14px 10px', fontSize: 10, color: p.accent }}>🚚 {o.ship}</div>}
              <div style={{ padding: '10px 14px', borderTop: `0.5px solid ${p.divider}`, display: 'flex', alignItems: 'center', gap: 8 }}>
                <div style={{ flex: 1, fontSize: 11, color: p.inkSoft }}>共 {o.count} 件 · 合计 <span style={{ color: p.ink, fontWeight: 700, fontFamily: TYPE.num }}>¥{o.total}</span></div>
                {o.statusColor !== 'muted' && <button style={{ padding: '5px 12px', borderRadius: 14, background: 'transparent', border: `1px solid ${p.divider}`, color: p.inkSoft, fontSize: 11, fontFamily: TYPE.body }}>取消订单</button>}
                <button style={{
                  padding: '5px 14px', borderRadius: 14, border: `1.5px solid ${p.primary}`,
                  background: o.statusColor === 'warn' ? p.primary : 'transparent',
                  color: o.statusColor === 'warn' ? '#fff' : p.primary,
                  fontSize: 11, fontWeight: 700, fontFamily: TYPE.body,
                }}>{o.action}</button>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

function OrderDetailScreen({ palette, radius }) {
  const p = palette;
  const steps = [
    { label: '已下单', time: '11/18 14:22', done: true },
    { label: '已付款', time: '11/18 14:23', done: true },
    { label: '商家已发货', time: '11/19 09:15', done: true, active: true },
    { label: '运输中', time: '预计 11/21 送达', done: false },
    { label: '已签收', time: '', done: false },
  ];

  return (
    <div style={{ width: '100%', height: '100%', background: p.bg, position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color="#fff" />
      {/* 顶部彩色 hero */}
      <div style={{
        position: 'absolute', top: 0, left: 0, right: 0, height: 230,
        background: `linear-gradient(160deg, ${p.primary}, ${p.primaryInk})`,
        overflow: 'hidden',
      }}>
        <div style={{ position: 'absolute', top: 60, right: -20, opacity: 0.2 }}><PawIcon size={120} color="#fff" /></div>
        <div style={{ position: 'absolute', top: 140, left: 30, opacity: 0.15 }}><PawIcon size={50} color="#fff" /></div>
      </div>
      <TopBar palette={{ ...p, ink: '#fff' }} title="订单详情" transparent
        right={<span style={{ fontSize: 12, color: '#fff' }}>客服</span>} />

      {/* 状态卡 */}
      <div style={{
        position: 'absolute', top: 96, left: 14, right: 14,
        background: 'rgba(255,255,255,0.15)',
        backdropFilter: 'blur(14px)', WebkitBackdropFilter: 'blur(14px)',
        borderRadius: radius, padding: 14,
        border: '1px solid rgba(255,255,255,0.25)',
        zIndex: 5,
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <div style={{ fontSize: 22 }}>🚚</div>
          <div>
            <div style={{ fontSize: 15, fontWeight: 800, color: '#fff' }}>商家已发货</div>
            <div style={{ fontSize: 10, color: 'rgba(255,255,255,0.85)', marginTop: 2 }}>顺丰速运 · SF1234567890 · 预计 11/21 送达</div>
          </div>
        </div>
      </div>

      <div style={{ position: 'absolute', top: 210, left: 0, right: 0, bottom: 0, overflow: 'auto', paddingBottom: 20 }}>
        {/* 物流时间轴 */}
        <div style={{ background: p.surface, margin: '0 14px', borderRadius: radius, padding: '14px 16px' }}>
          <div style={{ fontSize: 13, fontWeight: 700, color: p.ink, marginBottom: 12 }}>🐾 物流进度</div>
          {steps.map((s, i) => (
            <div key={i} style={{ display: 'flex', gap: 10, position: 'relative' }}>
              <div style={{ width: 18, flexShrink: 0, position: 'relative' }}>
                <div style={{
                  width: 12, height: 12, borderRadius: 6, marginTop: 2,
                  background: s.done ? (s.active ? p.primary : p.primarySoft) : p.surfaceDim,
                  border: s.active ? `3px solid ${p.primary}44` : 'none',
                  boxShadow: s.active ? `0 0 0 4px ${p.primary}22` : 'none',
                }} />
                {i < steps.length - 1 && <div style={{ position: 'absolute', left: 5, top: 16, bottom: -14, width: 2, background: s.done ? p.primarySoft : p.surfaceDim }} />}
              </div>
              <div style={{ flex: 1, paddingBottom: 14 }}>
                <div style={{ fontSize: 12, color: s.done ? p.ink : p.inkFaint, fontWeight: s.active ? 700 : 500 }}>{s.label}</div>
                {s.time && <div style={{ fontSize: 10, color: p.inkMuted, marginTop: 2 }}>{s.time}</div>}
              </div>
            </div>
          ))}
        </div>

        {/* 地址 */}
        <div style={{ background: p.surface, margin: '10px 14px 0', borderRadius: radius, padding: 14, display: 'flex', gap: 10 }}>
          <div style={{ fontSize: 18 }}>📍</div>
          <div style={{ flex: 1 }}>
            <div style={{ fontSize: 13, fontWeight: 700, color: p.ink }}>汤圆妈 · 138 **** 2341</div>
            <div style={{ fontSize: 11, color: p.inkSoft, marginTop: 3, lineHeight: 1.5 }}>上海市 徐汇区 漕溪北路 333 号 8 栋 502 室</div>
          </div>
        </div>

        {/* 商品 */}
        <div style={{ background: p.surface, margin: '10px 14px 0', borderRadius: radius, overflow: 'hidden' }}>
          <div style={{ padding: '12px 14px', fontSize: 12, fontWeight: 700, color: p.ink, borderBottom: `0.5px solid ${p.divider}` }}>🏪 爱宠手作坊</div>
          {[
            { seed: 4, variant: 'puppy', name: '手工烘焙肉干礼盒', spec: '4 口味混合装', price: 68, qty: 2 },
            { seed: 5, variant: 'mochi', name: '蓬松云朵猫窝 M', spec: '奶油色', price: 129, qty: 1 },
          ].map((it, i, arr) => (
            <div key={i} style={{ display: 'flex', gap: 10, padding: '12px 14px', borderBottom: i < arr.length - 1 ? `0.5px solid ${p.divider}` : 'none' }}>
              <div style={{ width: 58, height: 58, borderRadius: 8, overflow: 'hidden', flexShrink: 0 }}>
                <PetPlaceholder w={58} h={58} palette={p} seed={it.seed} variant={it.variant} />
              </div>
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: 12, color: p.ink, fontWeight: 600 }}>{it.name}</div>
                <div style={{ fontSize: 10, color: p.inkMuted, marginTop: 3 }}>{it.spec}</div>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 5, fontSize: 12 }}>
                  <span style={{ color: p.ink, fontWeight: 700, fontFamily: TYPE.num }}>¥{it.price}</span>
                  <span style={{ color: p.inkMuted, fontFamily: TYPE.num }}>× {it.qty}</span>
                </div>
              </div>
            </div>
          ))}
        </div>

        {/* 金额明细 */}
        <div style={{ background: p.surface, margin: '10px 14px 0', borderRadius: radius, padding: 14 }}>
          {[['商品金额', '¥265'], ['运费', '¥0'], ['优惠', '-¥30'], ['爱宠豆', '-¥5.8'], ['实付', '¥229.2', true]].map(([l, v, big], i) => (
            <div key={i} style={{ display: 'flex', justifyContent: 'space-between', padding: '4px 0', fontSize: big ? 13 : 12 }}>
              <span style={{ color: big ? p.ink : p.inkSoft, fontWeight: big ? 700 : 400 }}>{l}</span>
              <span style={{ color: big ? p.warn : (v.startsWith('-') ? p.warn : p.ink), fontFamily: TYPE.num, fontWeight: big ? 800 : 400, fontSize: big ? 15 : 12 }}>{v}</span>
            </div>
          ))}
        </div>

        {/* 订单信息 */}
        <div style={{ background: p.surface, margin: '10px 14px 20px', borderRadius: radius, padding: 14 }}>
          {[['订单号', 'AC2023111814220981'], ['下单时间', '2023-11-18 14:22'], ['支付方式', '微信支付']].map(([l, v], i) => (
            <div key={i} style={{ display: 'flex', justifyContent: 'space-between', padding: '4px 0', fontSize: 11 }}>
              <span style={{ color: p.inkMuted }}>{l}</span>
              <span style={{ color: p.inkSoft, fontFamily: l === '订单号' ? TYPE.num : TYPE.body }}>{v} {l === '订单号' && <span style={{ color: p.primary, marginLeft: 4 }}>复制</span>}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

Object.assign(window, { CartScreen, CheckoutScreen, OrdersScreen, OrderDetailScreen });
