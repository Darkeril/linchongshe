// screens-part6.jsx — Search 搜索发现 & Empty 空状态集锦

function SearchScreen({ palette, radius }) {
  const p = palette;
  const hotItems = [
    { rank: 1, text: '秋冬换毛怎么办', heat: '328w', hot: true },
    { rank: 2, text: '金毛幼犬喂食指南', heat: '215w', hot: true },
    { rank: 3, text: '布偶猫拉稀', heat: '98w' },
    { rank: 4, text: '宠物友好咖啡馆 上海', heat: '76w' },
    { rank: 5, text: '猫咪指甲修剪', heat: '54w', hot: true },
    { rank: 6, text: '狗狗驱虫周期', heat: '48w' },
  ];

  const history = ['金毛掉毛', '自制肉干', '上海宠物医院', '洗澡频率'];

  const discover = [
    { label: '🐕 养犬', color: '#C97B4A' },
    { label: '🐈 养猫', color: '#E8846E' },
    { label: '🐹 小宠', color: '#7BA58E' },
    { label: '🦜 飞鸟', color: '#8F7260' },
    { label: '🐠 水族', color: '#6F8EB8' },
    { label: '🍖 食谱', color: '#D6A75A' },
  ];

  return (
    <div style={{ width: '100%', height: '100%', background: p.bg, position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color={p.ink} />

      {/* 搜索框 */}
      <div style={{
        position: 'absolute', top: 42, left: 0, right: 0, height: 54,
        display: 'flex', alignItems: 'center', gap: 10, padding: '0 14px', zIndex: 20,
      }}>
        <div style={{ width: 24, display: 'flex', alignItems: 'center' }}>
          <svg width="10" height="18" viewBox="0 0 10 18" fill="none" stroke={p.ink} strokeWidth="2" strokeLinecap="round">
            <path d="M8 2L2 9l6 7" />
          </svg>
        </div>
        <div style={{
          flex: 1, height: 38, borderRadius: 19,
          background: p.surface,
          display: 'flex', alignItems: 'center', padding: '0 14px', gap: 8,
          boxShadow: `0 1px 0 ${p.divider}`,
        }}>
          <SearchIcon size={16} color={p.primary} />
          <input autoFocus placeholder="金毛换毛严重怎么办"
            style={{ flex: 1, border: 'none', outline: 'none', background: 'transparent', fontSize: 13, color: p.ink, fontFamily: TYPE.body }}
            defaultValue="" />
          <span style={{ fontSize: 14 }}>🐾</span>
        </div>
        <span style={{ fontSize: 13, color: p.primary, fontWeight: 600 }}>搜索</span>
      </div>

      {/* 滚动区 */}
      <div style={{
        position: 'absolute', top: 96, left: 0, right: 0, bottom: 0, overflow: 'auto', paddingBottom: 30,
      }}>
        {/* 最近搜索 */}
        <div style={{ padding: '4px 18px 0', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <span style={{ fontSize: 13, fontWeight: 700, color: p.ink }}>最近搜索</span>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke={p.inkMuted} strokeWidth="2" strokeLinecap="round">
            <path d="M3 6h18M8 6V4a2 2 0 012-2h4a2 2 0 012 2v2M5 6l1 14h12l1-14" />
          </svg>
        </div>
        <div style={{ padding: '8px 14px 0', display: 'flex', gap: 6, flexWrap: 'wrap' }}>
          {history.map((h, i) => (
            <div key={i} style={{
              padding: '5px 12px', borderRadius: 14,
              background: p.surface, fontSize: 11, color: p.inkSoft,
            }}>{h}</div>
          ))}
        </div>

        {/* 热搜榜 */}
        <SectionTitle palette={p} title="🔥 爱宠热榜" subtitle="每日 9:00 更新"
          right={<span>更多 ›</span>} />
        <div style={{ margin: '0 14px', background: p.surface, borderRadius: radius, overflow: 'hidden' }}>
          {hotItems.map((h, i) => (
            <div key={i} style={{
              display: 'flex', alignItems: 'center', gap: 12,
              padding: '11px 14px',
              borderBottom: i < hotItems.length - 1 ? `0.5px solid ${p.divider}` : 'none',
            }}>
              <div style={{
                width: 22, textAlign: 'center',
                fontSize: 14, fontWeight: 800, fontFamily: TYPE.num,
                color: h.rank <= 3 ? p.warn : p.inkFaint,
              }}>{h.rank}</div>
              <span style={{ flex: 1, fontSize: 13, color: p.ink, fontWeight: h.rank <= 3 ? 700 : 500 }}>{h.text}</span>
              {h.hot && <Tag palette={p} tone="warn" size="xs">HOT</Tag>}
              <span style={{ fontSize: 10, color: p.inkMuted, fontFamily: TYPE.num }}>{h.heat}</span>
            </div>
          ))}
        </div>

        {/* 发现分类 */}
        <SectionTitle palette={p} title="🌈 逛逛分类" />
        <div style={{ padding: '0 14px', display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 8 }}>
          {discover.map((d, i) => (
            <div key={i} style={{
              height: 76, borderRadius: radius,
              background: `linear-gradient(135deg, ${d.color}22, ${d.color}0a)`,
              border: `1px solid ${d.color}22`,
              padding: 12,
              display: 'flex', flexDirection: 'column', justifyContent: 'space-between',
            }}>
              <span style={{ fontSize: 22 }}>{d.label.split(' ')[0]}</span>
              <span style={{ fontSize: 12, color: p.ink, fontWeight: 700 }}>{d.label.split(' ')[1]}</span>
            </div>
          ))}
        </div>

        {/* 猜你想搜 */}
        <SectionTitle palette={p} title="💡 你可能想了解" />
        <div style={{ padding: '0 18px', display: 'flex', gap: 6, flexWrap: 'wrap' }}>
          {['幼犬补钙', '猫砂推荐', '宠物保险', '绝育后护理', '狗狗拆家', '猫抓板'].map((g, i) => (
            <div key={i} style={{
              padding: '6px 12px', borderRadius: 14,
              background: p.primarySoft, color: p.primaryInk,
              fontSize: 11, fontWeight: 600,
            }}>{g}</div>
          ))}
        </div>
      </div>
    </div>
  );
}

// 空状态集锦（一屏展示 3 个常见空态）
function EmptyStatesScreen({ palette, radius }) {
  const p = palette;

  const EmptyBlock = ({ emoji, title, sub, cta }) => (
    <div style={{
      background: p.surface, borderRadius: radius,
      padding: '20px 16px', textAlign: 'center',
      boxShadow: `0 2px 8px ${p.ink}08`,
      position: 'relative', overflow: 'hidden',
    }}>
      <div style={{ position: 'absolute', top: 8, right: 10, opacity: 0.1 }}>
        <PawIcon size={34} color={p.primaryInk} />
      </div>
      <div style={{
        width: 72, height: 72, borderRadius: 36, margin: '0 auto',
        background: p.primarySoft,
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        fontSize: 34,
      }}>{emoji}</div>
      <div style={{ fontSize: 13, fontWeight: 700, color: p.ink, marginTop: 10 }}>{title}</div>
      <div style={{ fontSize: 11, color: p.inkMuted, marginTop: 3, lineHeight: 1.5 }}>{sub}</div>
      {cta && (
        <button style={{
          marginTop: 12, padding: '6px 18px', borderRadius: 14, border: 'none',
          background: `linear-gradient(135deg, ${p.primary}, ${p.primaryInk})`,
          color: '#fff', fontSize: 11, fontWeight: 700,
          boxShadow: `0 3px 8px ${p.primary}40`,
          fontFamily: TYPE.body,
        }}>{cta}</button>
      )}
    </div>
  );

  return (
    <div style={{ width: '100%', height: '100%', background: p.bg, position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color={p.ink} />
      <TopBar palette={p} title="空状态 · 暖心兜底" />

      <div style={{
        position: 'absolute', top: 92, left: 0, right: 0, bottom: 0, overflow: 'auto',
        padding: '8px 14px 30px', display: 'flex', flexDirection: 'column', gap: 10,
      }}>
        <EmptyBlock
          emoji="📭"
          title="还没有任何消息哦"
          sub="去认识一些养宠同好吧～&#10;留下一句话，可能就有新朋友"
          cta="逛逛广场"
        />
        <EmptyBlock
          emoji="🌧"
          title="网络偷偷跑开啦"
          sub="检查一下 WiFi 或者切换到移动网络试试"
          cta="重新加载"
        />
        <EmptyBlock
          emoji="🍪"
          title="收藏夹空空的"
          sub="看到喜欢的笔记 / 商品，&#10;点一下小书签就能保存"
          cta="去首页逛逛"
        />
        <EmptyBlock
          emoji="🐾"
          title="还没有添加宠物"
          sub="添加你的毛孩子，获取更精准的推荐"
          cta="＋ 添加宠物"
        />
      </div>
    </div>
  );
}

Object.assign(window, { SearchScreen, EmptyStatesScreen });
