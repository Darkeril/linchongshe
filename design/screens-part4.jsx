// screens-part4.jsx — Market 市集 & ProductDetail 商品详情

function MarketScreen({ palette, radius }) {
  const p = palette;
  const categories = [
    { icon: '🦴', label: '主粮' },
    { icon: '🥫', label: '零食' },
    { icon: '🧴', label: '洗护' },
    { icon: '🎾', label: '玩具' },
    { icon: '🏠', label: '窝垫' },
    { icon: '👕', label: '服饰' },
    { icon: '💊', label: '医疗' },
    { icon: '🎁', label: '全部' },
  ];

  const banners = [
    { title: '秋冬换毛季', sub: '低敏主粮 · 满 299 减 50', bg: `linear-gradient(135deg, ${p.primarySoft}, ${p.surface})`, emoji: '🐕' },
  ];

  const flashSale = [
    { name: '渴望低敏主粮 2kg', price: 198, oldPrice: 248, left: 23, total: 100, variant: 'dog', seed: 1 },
    { name: '可啃咬磨牙棒 x5', price: 39, oldPrice: 59, left: 8, total: 50, variant: 'cat', seed: 2 },
    { name: '温感陶瓷饮水机', price: 159, oldPrice: 219, left: 47, total: 80, variant: 'fluff', seed: 3 },
  ];

  const recommend = [
    { name: '手工烘焙肉干礼盒', sub: '汤圆同款 · 4 口味', price: 68, tag: '新品', seed: 4, variant: 'puppy' },
    { name: '防滑陶瓷双碗', sub: '抗菌食品级', price: 89, tag: '热卖', seed: 5, variant: 'mochi' },
    { name: '蓬松云朵猫窝 M', sub: '可机洗 · 2 色', price: 129, tag: '限量', seed: 6, variant: 'cat' },
    { name: '牵引绳 2.0 加粗', sub: '反光防爆冲', price: 59, tag: '回购王', seed: 7, variant: 'dog' },
  ];

  return (
    <div style={{ width: '100%', height: '100%', background: p.bg, position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color={p.ink} />

      {/* 顶部搜索 + 购物车 */}
      <div style={{
        position: 'absolute', top: 46, left: 0, right: 0, height: 52,
        display: 'flex', alignItems: 'center', gap: 10, padding: '0 14px', zIndex: 20,
      }}>
        <div style={{
          flex: 1, height: 36, borderRadius: 18,
          background: p.surface,
          display: 'flex', alignItems: 'center', padding: '0 12px', gap: 8,
          boxShadow: `0 1px 0 ${p.divider}`,
        }}>
          <SearchIcon size={16} color={p.inkMuted} />
          <span style={{ fontSize: 12, color: p.inkMuted }}>搜主粮 / 零食 / 玩具</span>
        </div>
        <div style={{ position: 'relative', width: 36, height: 36, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <BagIcon size={22} color={p.ink} />
          <div style={{
            position: 'absolute', top: 2, right: 2,
            minWidth: 14, height: 14, borderRadius: 7, padding: '0 4px',
            background: p.warn, color: '#fff',
            fontSize: 9, fontWeight: 700,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>2</div>
        </div>
      </div>

      {/* 可滚动内容 */}
      <div style={{
        position: 'absolute', top: 98, left: 0, right: 0, bottom: 82,
        overflow: 'auto',
      }}>
        {/* Banner */}
        <div style={{ padding: '6px 14px 0' }}>
          <div style={{
            height: 110, borderRadius: radius,
            background: banners[0].bg,
            padding: '16px 18px',
            display: 'flex', alignItems: 'center', justifyContent: 'space-between',
            boxShadow: `0 1px 0 ${p.divider}`,
            position: 'relative', overflow: 'hidden',
          }}>
            <div>
              <div style={{ fontSize: 11, color: p.primaryInk, fontWeight: 700, letterSpacing: 1 }}>PET · AUTUMN</div>
              <div style={{ fontSize: 22, fontWeight: 800, color: p.ink, marginTop: 4, fontFamily: TYPE.brand }}>
                {banners[0].title}
              </div>
              <div style={{ fontSize: 11, color: p.inkSoft, marginTop: 2 }}>{banners[0].sub}</div>
              <div style={{
                marginTop: 8, padding: '5px 12px', display: 'inline-block',
                background: p.primary, color: '#fff', borderRadius: 12,
                fontSize: 11, fontWeight: 700,
              }}>立即逛 →</div>
            </div>
            <div style={{ fontSize: 60, filter: 'drop-shadow(0 6px 10px rgba(0,0,0,0.1))' }}>{banners[0].emoji}</div>
            {/* decoration */}
            <div style={{ position: 'absolute', top: -10, right: -10, opacity: 0.14 }}>
              <PawIcon size={60} color={p.primaryInk} />
            </div>
          </div>
        </div>

        {/* 分类 */}
        <div style={{ padding: '18px 6px 0', display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 4 }}>
          {categories.map((c, i) => (
            <div key={i} style={{
              display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4,
              padding: '8px 0',
            }}>
              <div style={{
                width: 46, height: 46, borderRadius: 14,
                background: p.surface,
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                fontSize: 22,
                boxShadow: `0 2px 6px ${p.ink}0a`,
              }}>{c.icon}</div>
              <span style={{ fontSize: 11, color: p.inkSoft }}>{c.label}</span>
            </div>
          ))}
        </div>

        {/* 限时秒杀 */}
        <SectionTitle palette={p} title="⚡ 限时秒杀" subtitle="距结束 03:24:18"
          right={<span>更多 ›</span>} />
        <div style={{ padding: '0 14px', display: 'flex', gap: 10, overflowX: 'auto' }}>
          {flashSale.map((item, i) => (
            <div key={i} style={{
              flex: '0 0 135px',
              background: p.surface, borderRadius: radius,
              overflow: 'hidden',
              boxShadow: `0 2px 8px ${p.ink}0a`,
            }}>
              <div style={{ width: '100%', height: 100, position: 'relative' }}>
                <PetPlaceholder w={135} h={100} palette={p} seed={item.seed} variant={item.variant} />
                <div style={{
                  position: 'absolute', top: 6, left: 6,
                  padding: '2px 6px', borderRadius: 6,
                  background: p.warn, color: '#fff',
                  fontSize: 9, fontWeight: 700,
                }}>SALE</div>
              </div>
              <div style={{ padding: '8px 10px' }}>
                <div style={{ fontSize: 11, color: p.ink, fontWeight: 600, lineHeight: 1.3,
                  overflow: 'hidden', textOverflow: 'ellipsis', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', height: 28,
                }}>{item.name}</div>
                <div style={{ display: 'flex', alignItems: 'baseline', gap: 4, marginTop: 4 }}>
                  <span style={{ fontSize: 9, color: p.warn, fontWeight: 700 }}>¥</span>
                  <span style={{ fontSize: 16, color: p.warn, fontWeight: 800, fontFamily: TYPE.num }}>{item.price}</span>
                  <span style={{ fontSize: 10, color: p.inkFaint, textDecoration: 'line-through' }}>¥{item.oldPrice}</span>
                </div>
                {/* 进度条 */}
                <div style={{ marginTop: 5, height: 4, borderRadius: 2, background: p.surfaceDim, overflow: 'hidden' }}>
                  <div style={{
                    height: '100%', width: `${(1 - item.left / item.total) * 100}%`,
                    background: `linear-gradient(90deg, ${p.warn}, ${p.primary})`,
                  }} />
                </div>
                <div style={{ fontSize: 9, color: p.inkMuted, marginTop: 3 }}>仅剩 {item.left} 件</div>
              </div>
            </div>
          ))}
        </div>

        {/* 为汤圆精选 */}
        <SectionTitle palette={p} title="🐕 为汤圆精选"
          subtitle="基于你家金毛 2 岁 · 中型犬"
          right={<span>全部 ›</span>} />
        <div style={{ padding: '0 14px', display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
          {recommend.map((item, i) => (
            <div key={i} style={{
              background: p.surface, borderRadius: radius,
              overflow: 'hidden',
              boxShadow: `0 2px 8px ${p.ink}0a`,
            }}>
              <div style={{ width: '100%', height: 130, position: 'relative' }}>
                <PetPlaceholder w={160} h={130} palette={p} seed={item.seed} variant={item.variant} />
                <div style={{
                  position: 'absolute', top: 8, left: 8,
                  padding: '2px 7px', borderRadius: 8,
                  background: 'rgba(255,255,255,0.9)', color: p.primaryInk,
                  fontSize: 9, fontWeight: 700,
                }}>{item.tag}</div>
                <div style={{
                  position: 'absolute', bottom: 6, right: 6,
                  width: 26, height: 26, borderRadius: 13,
                  background: p.primary,
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  boxShadow: `0 3px 8px ${p.primary}66`,
                }}>
                  <BagIcon size={14} color="#fff" />
                </div>
              </div>
              <div style={{ padding: '10px 12px 12px' }}>
                <div style={{ fontSize: 12, color: p.ink, fontWeight: 600, lineHeight: 1.3 }}>
                  {item.name}
                </div>
                <div style={{ fontSize: 10, color: p.inkMuted, marginTop: 2 }}>{item.sub}</div>
                <div style={{ display: 'flex', alignItems: 'baseline', marginTop: 6, gap: 2 }}>
                  <span style={{ fontSize: 10, color: p.primary, fontWeight: 700 }}>¥</span>
                  <span style={{ fontSize: 17, color: p.primary, fontWeight: 800, fontFamily: TYPE.num }}>
                    {item.price}
                  </span>
                </div>
              </div>
            </div>
          ))}
        </div>

        <div style={{ textAlign: 'center', padding: '20px 0 10px', fontSize: 10, color: p.inkFaint }}>
          · 到底啦 ·
        </div>
      </div>

      <AppTabBar active="market" palette={p} radius={radius} />
    </div>
  );
}

// 商品详情页
function ProductDetailScreen({ palette, radius }) {
  const p = palette;
  return (
    <div style={{ width: '100%', height: '100%', background: p.bg, position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      {/* 主图区 */}
      <div style={{ position: 'absolute', top: 0, left: 0, right: 0, height: 380, overflow: 'hidden' }}>
        <PetPlaceholder w={360} h={380} palette={p} seed={4} variant="puppy" />
        {/* paw 装饰 */}
        <div style={{ position: 'absolute', top: '25%', right: '10%', opacity: 0.28 }}>
          <PawIcon size={80} color={p.primaryInk} />
        </div>
        {/* 页码点 */}
        <div style={{
          position: 'absolute', bottom: 12, left: '50%', transform: 'translateX(-50%)',
          display: 'flex', gap: 4,
          padding: '4px 10px', borderRadius: 12,
          background: 'rgba(0,0,0,0.35)',
        }}>
          {[0, 1, 2, 3].map(i => (
            <div key={i} style={{
              width: i === 0 ? 12 : 5, height: 5, borderRadius: 3,
              background: i === 0 ? '#fff' : 'rgba(255,255,255,0.55)',
            }} />
          ))}
        </div>
      </div>

      <StatusLine color="#fff" />
      <TopBar palette={p} title="" transparent
        right={
          <div style={{
            width: 34, height: 34, borderRadius: 17,
            background: 'rgba(255,255,255,0.85)',
            backdropFilter: 'blur(8px)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>
            <ShareIcon size={16} color={p.ink} />
          </div>
        } />

      {/* 内容卡 */}
      <div style={{
        position: 'absolute', top: 348, left: 0, right: 0, bottom: 86,
        background: p.bg, borderRadius: `${radius * 1.4}px ${radius * 1.4}px 0 0`,
        overflow: 'auto',
      }}>
        {/* 价格 & 名称 */}
        <div style={{ padding: '18px 16px 0' }}>
          <div style={{ display: 'flex', alignItems: 'flex-end', gap: 6 }}>
            <span style={{ fontSize: 14, color: p.primary, fontWeight: 700, marginBottom: 4 }}>¥</span>
            <span style={{ fontSize: 32, color: p.primary, fontWeight: 800, fontFamily: TYPE.num, lineHeight: 1 }}>68</span>
            <span style={{ fontSize: 11, color: p.inkFaint, textDecoration: 'line-through', marginBottom: 6 }}>¥88</span>
            <div style={{
              marginLeft: 6, marginBottom: 6,
              padding: '2px 6px', borderRadius: 6,
              background: p.warn, color: '#fff',
              fontSize: 9, fontWeight: 700,
            }}>限时 7.7 折</div>
          </div>
          <div style={{ fontSize: 17, fontWeight: 700, color: p.ink, marginTop: 10, lineHeight: 1.3 }}>
            手工烘焙肉干礼盒 · 汤圆同款 🐕
          </div>
          <div style={{ fontSize: 12, color: p.inkSoft, marginTop: 4 }}>
            4 口味组合 · 鸡胸 / 牛肉 / 鸭肉 / 三文鱼 · 低敏无添加
          </div>
          <div style={{ display: 'flex', gap: 6, marginTop: 10, flexWrap: 'wrap' }}>
            <Tag palette={p} tone="soft">📦 顺丰包邮</Tag>
            <Tag palette={p} tone="ghost">🛡 七天无理由</Tag>
            <Tag palette={p} tone="accent">✓ 正品保障</Tag>
          </div>
        </div>

        {/* 宠物适配 */}
        <div style={{ margin: '16px 14px 0', padding: 14, borderRadius: radius, background: p.surface }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            <PetAvatar variant="dog" size={42} />
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: 12, color: p.ink, fontWeight: 700 }}>
                汤圆适合吗？<span style={{ color: p.accent, marginLeft: 6 }}>✓ 非常适合</span>
              </div>
              <div style={{ fontSize: 10, color: p.inkMuted, marginTop: 2 }}>
                金毛 · 2 岁 · 24kg · 无过敏源
              </div>
            </div>
            <div style={{
              padding: '5px 10px', borderRadius: 12,
              border: `1px solid ${p.primarySoft}`,
              fontSize: 10, color: p.primaryInk, fontWeight: 600,
            }}>切换宠物</div>
          </div>
        </div>

        {/* 规格 */}
        <div style={{ padding: '18px 16px 0' }}>
          <div style={{ fontSize: 12, fontWeight: 700, color: p.ink, marginBottom: 8 }}>选择口味组合</div>
          <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
            {[
              { label: '4 口味混合装', active: true },
              { label: '鸡胸单口味' },
              { label: '牛肉单口味' },
              { label: '三文鱼单口味' },
            ].map((s, i) => (
              <div key={i} style={{
                padding: '6px 12px', borderRadius: 12,
                background: s.active ? p.primarySoft : p.surface,
                color: s.active ? p.primaryInk : p.ink,
                fontSize: 11, fontWeight: 600,
                border: `1.5px solid ${s.active ? p.primary : 'transparent'}`,
              }}>{s.label}</div>
            ))}
          </div>
        </div>

        {/* 买家秀 */}
        <SectionTitle palette={p} title="🐾 主子们都在说" subtitle="238 条真实晒单"
          right={<span>查看全部 ›</span>} />
        <div style={{ padding: '0 14px', display: 'flex', gap: 8, overflowX: 'auto' }}>
          {[1, 2, 3, 4].map(i => (
            <div key={i} style={{
              flex: '0 0 110px', borderRadius: 12, overflow: 'hidden',
              position: 'relative',
            }}>
              <PetPlaceholder w={110} h={110} palette={p} seed={i + 3} variant={['dog','cat','fluff','puppy'][i - 1]} />
              <div style={{
                position: 'absolute', bottom: 0, left: 0, right: 0,
                background: 'linear-gradient(to top, rgba(0,0,0,0.6), transparent)',
                padding: '10px 6px 4px',
                color: '#fff', fontSize: 9, display: 'flex', alignItems: 'center', gap: 3,
              }}>
                <HeartIcon size={10} filled color="#fff" /> {200 + i * 31}
              </div>
            </div>
          ))}
        </div>

        <div style={{ height: 24 }} />
      </div>

      {/* 底部购买 Bar */}
      <div style={{
        position: 'absolute', bottom: 0, left: 0, right: 0, height: 86,
        background: p.surface,
        borderTop: `0.5px solid ${p.divider}`,
        padding: '10px 14px 22px',
        display: 'flex', alignItems: 'center', gap: 10,
      }}>
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 1, padding: '0 8px' }}>
          <ChatIcon size={20} color={p.inkSoft} />
          <span style={{ fontSize: 9, color: p.inkSoft }}>客服</span>
        </div>
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 1, padding: '0 8px', position: 'relative' }}>
          <BagIcon size={20} color={p.inkSoft} />
          <span style={{ fontSize: 9, color: p.inkSoft }}>购物车</span>
          <div style={{
            position: 'absolute', top: -4, right: -2,
            minWidth: 14, height: 14, borderRadius: 7, padding: '0 3px',
            background: p.warn, color: '#fff', fontSize: 9, fontWeight: 700,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>2</div>
        </div>
        <button style={{
          flex: 1, height: 42, borderRadius: 21, border: 'none',
          background: p.primarySoft, color: p.primaryInk,
          fontSize: 13, fontWeight: 700,
          fontFamily: TYPE.body,
        }}>加入购物车</button>
        <button style={{
          flex: 1, height: 42, borderRadius: 21, border: 'none',
          background: `linear-gradient(135deg, ${p.primary}, ${p.primaryInk})`,
          color: '#fff', fontSize: 13, fontWeight: 700,
          boxShadow: `0 4px 12px ${p.primary}50`,
          fontFamily: TYPE.body,
        }}>立即购买</button>
      </div>
    </div>
  );
}

Object.assign(window, { MarketScreen, ProductDetailScreen });
