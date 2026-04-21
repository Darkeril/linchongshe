// screens-part12.jsx — PetDate / Adopt / Lost / Album / VIP / About

function PetDateScreen({ palette, radius }) {
  const p = palette;
  return (
    <div style={{ width: '100%', height: '100%', background: p.bg, position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color={p.ink} />
      <TopBar palette={p} title="遛娃 · 宠友 🐾" right={<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke={p.ink} strokeWidth="2" strokeLinecap="round"><path d="M3 6h18M3 12h18M3 18h18" /></svg>} />

      {/* 条件 Chip */}
      <div style={{ position: 'absolute', top: 92, left: 0, right: 0, padding: '8px 14px', display: 'flex', gap: 6, overflowX: 'auto', borderBottom: `0.5px solid ${p.divider}`, zIndex: 3 }}>
        <Tag palette={p} tone="primary" size="md">📍 1km 内</Tag>
        <Tag palette={p} tone="ghost" size="md">🐕 中大型犬</Tag>
        <Tag palette={p} tone="ghost" size="md">♂ 弟弟</Tag>
        <Tag palette={p} tone="ghost" size="md">已绝育</Tag>
        <Tag palette={p} tone="ghost" size="md">🎾 喜欢玩球</Tag>
      </div>

      {/* 主卡片 · Tinder 风 */}
      <div style={{ position: 'absolute', top: 142, left: 0, right: 0, bottom: 130, display: 'flex', alignItems: 'center', justifyContent: 'center', perspective: 800 }}>
        {/* 后卡 */}
        <div style={{ position: 'absolute', width: 280, height: 410, borderRadius: 20, background: p.surface, transform: 'translateY(12px) scale(0.94)', boxShadow: `0 2px 10px ${p.ink}10`, opacity: 0.6 }} />
        <div style={{ position: 'absolute', width: 290, height: 420, borderRadius: 20, background: p.surface, transform: 'translateY(6px) scale(0.97)', boxShadow: `0 4px 14px ${p.ink}15` }} />

        {/* 前卡 */}
        <div style={{ position: 'relative', width: 300, height: 430, borderRadius: 20, overflow: 'hidden', boxShadow: `0 10px 30px ${p.ink}25`, transform: 'rotate(-2deg)' }}>
          <PetPlaceholder w={300} h={430} palette={p} seed={2} variant="dog" />
          <div style={{ position: 'absolute', inset: 0, background: 'linear-gradient(180deg, transparent 50%, rgba(0,0,0,0.85) 100%)' }} />

          {/* 距离牌 */}
          <div style={{ position: 'absolute', top: 14, left: 14, padding: '4px 10px', borderRadius: 14, background: 'rgba(0,0,0,0.45)', backdropFilter: 'blur(10px)', color: '#fff', fontSize: 10, fontWeight: 600 }}>📍 320m</div>
          <div style={{ position: 'absolute', top: 14, right: 14, padding: '4px 10px', borderRadius: 14, background: p.primary, color: '#fff', fontSize: 10, fontWeight: 700 }}>✨ 新成员</div>

          {/* 照片点 */}
          <div style={{ position: 'absolute', top: 8, left: 14, right: 14, display: 'flex', gap: 4 }}>
            {[0, 1, 2, 3].map(i => (
              <div key={i} style={{ flex: 1, height: 3, borderRadius: 2, background: i === 0 ? '#fff' : 'rgba(255,255,255,0.4)' }} />
            ))}
          </div>

          {/* 信息 */}
          <div style={{ position: 'absolute', bottom: 18, left: 18, right: 18, color: '#fff' }}>
            <div style={{ display: 'flex', alignItems: 'baseline', gap: 6 }}>
              <span style={{ fontSize: 26, fontWeight: 800, fontFamily: TYPE.brand }}>球球</span>
              <span style={{ fontSize: 14, opacity: 0.9 }}>· 1 岁 2 月</span>
              <span style={{ fontSize: 13, marginLeft: 'auto' }}>♂</span>
            </div>
            <div style={{ fontSize: 11, opacity: 0.9, marginTop: 3 }}>博美 · 4.5kg · 已绝育 · 苗苗齐全</div>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 4, marginTop: 8 }}>
              {['🎾 玩球', '🚶 遛弯', '🥩 零食诱惑', '🐕 狗友多'].map((t, i) => (
                <div key={i} style={{ padding: '3px 8px', borderRadius: 10, background: 'rgba(255,255,255,0.22)', fontSize: 9, fontWeight: 600, backdropFilter: 'blur(6px)' }}>{t}</div>
              ))}
            </div>
            <div style={{ fontSize: 11, marginTop: 8, lineHeight: 1.5, opacity: 0.92 }}>
              "爱社交的小毛球 🌟 最近常去徐汇公园，找个狗友一起玩耍～"
            </div>
          </div>
        </div>
      </div>

      {/* 操作按钮 */}
      <div style={{ position: 'absolute', bottom: 40, left: 0, right: 0, display: 'flex', justifyContent: 'center', gap: 18 }}>
        <div style={{ width: 44, height: 44, borderRadius: 22, background: p.surface, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 20, boxShadow: `0 4px 12px ${p.ink}15` }}>↺</div>
        <div style={{ width: 56, height: 56, borderRadius: 28, background: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 26, color: p.warn, boxShadow: `0 6px 18px ${p.warn}30`, border: `2px solid ${p.warn}30` }}>✕</div>
        <div style={{ width: 60, height: 60, borderRadius: 30, background: `linear-gradient(135deg, ${p.primary}, ${p.accent})`, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 28, color: '#fff', boxShadow: `0 8px 20px ${p.primary}50` }}>🐾</div>
        <div style={{ width: 56, height: 56, borderRadius: 28, background: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 24, boxShadow: `0 6px 18px ${p.ink}15`, border: `2px solid ${p.accent}30` }}>💬</div>
        <div style={{ width: 44, height: 44, borderRadius: 22, background: p.surface, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 20, color: p.accent, boxShadow: `0 4px 12px ${p.ink}15` }}>⭐</div>
      </div>
    </div>
  );
}

function AdoptScreen({ palette, radius }) {
  const p = palette;
  const pets = [
    { name: '豆豆', info: '橘猫 · 1 岁 · ♂', loc: '上海 · 浦东', tag: '疫苗齐全', urgent: false, seed: 0, variant: 'cat' },
    { name: '团子', info: '田园犬 · 3 月 · ♀', loc: '北京 · 朝阳', tag: '急需救助', urgent: true, seed: 1, variant: 'puppy' },
    { name: '奶糖', info: '比熊 · 2 岁 · ♀', loc: '杭州 · 西湖', tag: '已绝育', urgent: false, seed: 2, variant: 'fluff' },
    { name: '小黑', info: '中华田园 · 4 月 · ♂', loc: '成都 · 武侯', tag: '救助站', urgent: false, seed: 3, variant: 'dog' },
  ];
  return (
    <div style={{ width: '100%', height: '100%', background: p.bg, position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color="#fff" />
      {/* Hero */}
      <div style={{ position: 'absolute', top: 0, left: 0, right: 0, height: 180, background: `linear-gradient(160deg, #b89a73, #8e7454)`, overflow: 'hidden' }}>
        <div style={{ position: 'absolute', bottom: -10, right: -10, opacity: 0.15 }}><PawIcon size={100} color="#fff" /></div>
        <div style={{ position: 'absolute', top: 20, left: 40, opacity: 0.12 }}><PawIcon size={40} color="#fff" /></div>
      </div>
      <TopBar palette={{ ...p, ink: '#fff' }} title="" transparent right={<span style={{ fontSize: 12, color: '#fff' }}>我要送养 ›</span>} />

      <div style={{ position: 'absolute', top: 68, left: 14, right: 14, color: '#fff', zIndex: 2 }}>
        <div style={{ fontSize: 10, opacity: 0.9 }}>🤝 公益板块</div>
        <div style={{ fontSize: 22, fontWeight: 800, fontFamily: TYPE.brand, marginTop: 4 }}>领养代替购买 🏡</div>
        <div style={{ fontSize: 11, opacity: 0.9, marginTop: 4 }}>已有 8,420 只毛孩子找到了家 · 请谨慎决定</div>
      </div>

      <div style={{ position: 'absolute', top: 180, left: 0, right: 0, bottom: 0, overflow: 'auto', paddingBottom: 20 }}>
        {/* 数据带 */}
        <div style={{ margin: '-24px 14px 0', background: p.surface, borderRadius: radius, padding: 14, display: 'flex', justifyContent: 'space-around', boxShadow: `0 6px 16px ${p.ink}10`, zIndex: 3, position: 'relative' }}>
          {[
            { v: '1,248', l: '待领养' },
            { v: '8,420', l: '已成功' },
            { v: '86', l: '合作机构' },
          ].map((s, i) => (
            <div key={i} style={{ textAlign: 'center' }}>
              <div style={{ fontSize: 18, fontWeight: 800, color: p.primaryInk, fontFamily: TYPE.num }}>{s.v}</div>
              <div style={{ fontSize: 10, color: p.inkMuted, marginTop: 2 }}>{s.l}</div>
            </div>
          ))}
        </div>

        {/* Filter */}
        <div style={{ padding: '16px 14px 8px', display: 'flex', gap: 6, overflowX: 'auto' }}>
          <Tag palette={p} tone="primary" size="md">全部</Tag>
          <Tag palette={p} tone="ghost" size="md">🐕 狗狗</Tag>
          <Tag palette={p} tone="ghost" size="md">🐈 猫咪</Tag>
          <Tag palette={p} tone="ghost" size="md">🔥 紧急</Tag>
          <Tag palette={p} tone="ghost" size="md">📍 附近</Tag>
        </div>

        {/* 待领养列表 · 双列 */}
        <div style={{ padding: '0 14px', display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
          {pets.map((pet, i) => (
            <div key={i} style={{ background: p.surface, borderRadius: radius, overflow: 'hidden', position: 'relative' }}>
              <div style={{ height: 140, position: 'relative' }}>
                <PetPlaceholder w={160} h={140} palette={p} seed={pet.seed} variant={pet.variant} />
                {pet.urgent && <div style={{ position: 'absolute', top: 6, left: 6, padding: '2px 7px', borderRadius: 8, background: '#e07a5a', color: '#fff', fontSize: 9, fontWeight: 700 }}>🆘 急需</div>}
                <div style={{ position: 'absolute', bottom: 6, right: 6, padding: '2px 7px', borderRadius: 8, background: 'rgba(0,0,0,0.5)', backdropFilter: 'blur(6px)', color: '#fff', fontSize: 9 }}>{pet.loc}</div>
              </div>
              <div style={{ padding: '10px 10px 12px' }}>
                <div style={{ display: 'flex', alignItems: 'baseline', gap: 4 }}>
                  <span style={{ fontSize: 14, fontWeight: 800, color: p.ink, fontFamily: TYPE.brand }}>{pet.name}</span>
                  <span style={{ fontSize: 10, color: p.inkMuted }}>· {pet.info}</span>
                </div>
                <div style={{ marginTop: 6, display: 'inline-block', padding: '2px 7px', borderRadius: 7, background: p.primarySoft, fontSize: 9, color: p.primaryInk, fontWeight: 600 }}>✓ {pet.tag}</div>
                <button style={{ marginTop: 8, width: '100%', padding: '6px 0', borderRadius: 10, border: 'none', background: `linear-gradient(135deg, ${p.primary}, ${p.primaryInk})`, color: '#fff', fontSize: 11, fontWeight: 700, fontFamily: TYPE.body }}>
                  🏡 我想领养
                </button>
              </div>
            </div>
          ))}
        </div>

        {/* 领养须知 */}
        <div style={{ margin: '14px 14px 0', padding: 12, background: `${p.warn}15`, borderRadius: radius, border: `1px dashed ${p.warn}50` }}>
          <div style={{ fontSize: 11, fontWeight: 700, color: p.warn, marginBottom: 4 }}>⚠️ 领养须知</div>
          <div style={{ fontSize: 10, color: p.inkSoft, lineHeight: 1.6 }}>
            · 需填写《领养申请》并通过审核<br />
            · 承诺绝不遗弃、定期接种疫苗<br />
            · 部分机构会上门回访 3 个月
          </div>
        </div>
      </div>
    </div>
  );
}

function LostScreen({ palette, radius }) {
  const p = palette;
  const cases = [
    { status: 'lost', name: '胖虎', info: '柯基 · ♂ · 7岁', loc: '徐汇田林 · 昨日 18:30', feature: '短腿长身，白色胸口，颈圈粉色', reward: '¥2,000', seed: 0, variant: 'dog' },
    { status: 'found', name: '一只橘猫', loc: '浦东金桥 · 2 小时前', feature: '疑似走失，脖子戴蓝色项圈', seed: 1, variant: 'cat' },
    { status: 'lost', name: '二狗', info: '田园犬 · ♂ · 3岁', loc: '静安寺 · 3 天前', feature: '右耳缺口，见人就跑', reward: '¥500', seed: 2, variant: 'dog' },
  ];

  return (
    <div style={{ width: '100%', height: '100%', background: p.bg, position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color="#fff" />
      <div style={{ position: 'absolute', top: 0, left: 0, right: 0, height: 160, background: `linear-gradient(160deg, #c06a55, #9f4e3a)`, overflow: 'hidden' }}>
        <div style={{ position: 'absolute', top: 30, right: -10, opacity: 0.2 }}><PawIcon size={80} color="#fff" /></div>
      </div>
      <TopBar palette={{ ...p, ink: '#fff' }} title="" transparent right={<span style={{ fontSize: 12, color: '#fff', fontWeight: 600 }}>📢 我要发布</span>} />

      <div style={{ position: 'absolute', top: 74, left: 14, right: 14, color: '#fff', zIndex: 2 }}>
        <div style={{ fontSize: 10, opacity: 0.9 }}>🆘 走失播报</div>
        <div style={{ fontSize: 22, fontWeight: 800, fontFamily: TYPE.brand, marginTop: 4 }}>让每一只毛孩子都能回家</div>
        <div style={{ fontSize: 11, opacity: 0.9, marginTop: 4 }}>附近 3km · 1,248 位好心人已帮忙转发</div>
      </div>

      <div style={{ position: 'absolute', top: 176, left: 0, right: 0, bottom: 0, overflow: 'auto' }}>
        {/* 快捷入口 */}
        <div style={{ margin: '-16px 14px 10px', padding: 12, background: p.surface, borderRadius: radius, display: 'flex', justifyContent: 'space-around', boxShadow: `0 6px 16px ${p.ink}12`, position: 'relative' }}>
          {[
            { icon: '📍', l: '我的走失', sub: '2 条进行中' },
            { icon: '👀', l: '我看见', sub: '快速标记' },
            { icon: '📞', l: '救助热线', sub: '24h 在线' },
          ].map((q, i) => (
            <div key={i} style={{ textAlign: 'center' }}>
              <div style={{ width: 40, height: 40, borderRadius: 12, background: p.primarySoft, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 18, margin: '0 auto' }}>{q.icon}</div>
              <div style={{ fontSize: 11, color: p.ink, fontWeight: 600, marginTop: 6 }}>{q.l}</div>
              <div style={{ fontSize: 9, color: p.inkMuted, marginTop: 1 }}>{q.sub}</div>
            </div>
          ))}
        </div>

        {/* Filter */}
        <div style={{ padding: '0 14px', display: 'flex', gap: 6 }}>
          <Tag palette={p} tone="primary" size="md">全部播报</Tag>
          <Tag palette={p} tone="warn" size="md">🔴 走失</Tag>
          <Tag palette={p} tone="soft" size="md">🟢 捡到</Tag>
        </div>

        {/* 列表 */}
        <div style={{ padding: '10px 14px 20px' }}>
          {cases.map((c, i) => (
            <div key={i} style={{ marginBottom: 10, background: p.surface, borderRadius: radius, overflow: 'hidden', borderLeft: `4px solid ${c.status === 'lost' ? '#e07a5a' : '#7fa780'}` }}>
              <div style={{ padding: 12, display: 'flex', gap: 10 }}>
                <div style={{ width: 70, height: 70, borderRadius: 10, overflow: 'hidden', flexShrink: 0 }}>
                  <PetPlaceholder w={70} h={70} palette={p} seed={c.seed} variant={c.variant} />
                </div>
                <div style={{ flex: 1 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                    <div style={{ padding: '2px 8px', borderRadius: 8, background: c.status === 'lost' ? '#e07a5a' : '#7fa780', color: '#fff', fontSize: 10, fontWeight: 700 }}>
                      {c.status === 'lost' ? '🔴 走失' : '🟢 捡到'}
                    </div>
                    <span style={{ fontSize: 13, fontWeight: 800, color: p.ink }}>{c.name}</span>
                    {c.info && <span style={{ fontSize: 10, color: p.inkMuted }}>{c.info}</span>}
                  </div>
                  <div style={{ fontSize: 10, color: p.inkMuted, marginTop: 5 }}>📍 {c.loc}</div>
                  <div style={{ fontSize: 11, color: p.inkSoft, marginTop: 4, lineHeight: 1.4 }}>{c.feature}</div>
                  {c.reward && (
                    <div style={{ marginTop: 6, display: 'inline-block', padding: '2px 8px', borderRadius: 8, background: `linear-gradient(90deg, ${p.warn}, ${p.accent})`, color: '#fff', fontSize: 10, fontWeight: 700 }}>
                      💰 重谢 {c.reward}
                    </div>
                  )}
                </div>
              </div>
              <div style={{ display: 'flex', borderTop: `0.5px solid ${p.divider}` }}>
                {['📞 联系', '🔗 转发', '👀 我看见过'].map((a, j) => (
                  <div key={j} style={{ flex: 1, padding: '10px 0', textAlign: 'center', fontSize: 11, color: j === 2 ? p.primary : p.inkSoft, fontWeight: j === 2 ? 700 : 500, borderLeft: j > 0 ? `0.5px solid ${p.divider}` : 'none' }}>{a}</div>
                ))}
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

function AlbumScreen({ palette, radius }) {
  const p = palette;
  const months = [
    { m: '2023 · 10 月', count: 68, items: [{ seed: 0, v: 'dog' }, { seed: 1, v: 'dog' }, { seed: 2, v: 'dog' }, { seed: 3, v: 'dog' }] },
    { m: '2023 · 09 月', count: 42, items: [{ seed: 4, v: 'dog' }, { seed: 5, v: 'dog' }, { seed: 6, v: 'dog' }] },
    { m: '2023 · 08 月 🎂 汤圆生日', count: 124, highlight: true, items: [{ seed: 7, v: 'puppy' }, { seed: 8, v: 'puppy' }, { seed: 9, v: 'puppy' }, { seed: 10, v: 'puppy' }] },
  ];

  return (
    <div style={{ width: '100%', height: '100%', background: p.bg, position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color={p.ink} />
      <TopBar palette={p} title="汤圆的相册" right={<span style={{ fontSize: 13, color: p.primary, fontWeight: 700 }}>＋上传</span>} />

      {/* 切换视图 */}
      <div style={{ position: 'absolute', top: 92, left: 0, right: 0, padding: '8px 14px', display: 'flex', alignItems: 'center', gap: 10, zIndex: 3 }}>
        <div style={{ display: 'inline-flex', gap: 4, background: p.surface, borderRadius: 12, padding: 3 }}>
          {[{ l: '📅 时间', active: true }, { l: '🗂️ 分类' }, { l: '✨ 智能' }].map((v, i) => (
            <div key={i} style={{ padding: '4px 10px', borderRadius: 9, background: v.active ? p.primary : 'transparent', color: v.active ? '#fff' : p.inkSoft, fontSize: 11, fontWeight: 700 }}>{v.l}</div>
          ))}
        </div>
        <div style={{ marginLeft: 'auto', fontSize: 11, color: p.inkMuted }}>428 张 · 12 个视频</div>
      </div>

      <div style={{ position: 'absolute', top: 138, left: 0, right: 0, bottom: 0, overflow: 'auto', paddingBottom: 20 }}>
        {/* 精选封面 */}
        <div style={{ margin: '0 14px', borderRadius: radius, overflow: 'hidden', position: 'relative', height: 160 }}>
          <PetPlaceholder w={310} h={160} palette={p} seed={11} variant="dog" />
          <div style={{ position: 'absolute', inset: 0, background: 'linear-gradient(0deg, rgba(0,0,0,0.7), transparent 60%)' }} />
          <div style={{ position: 'absolute', bottom: 10, left: 12, right: 12, color: '#fff' }}>
            <div style={{ fontSize: 10, opacity: 0.85 }}>✨ 系统为你生成</div>
            <div style={{ fontSize: 16, fontWeight: 800, fontFamily: TYPE.brand }}>汤圆成长故事 · 2 周年</div>
            <div style={{ fontSize: 10, opacity: 0.9, marginTop: 2 }}>从 3 个月到现在 · 共 368 个瞬间</div>
          </div>
          <div style={{ position: 'absolute', top: 10, right: 10, padding: '3px 8px', borderRadius: 10, background: 'rgba(0,0,0,0.4)', backdropFilter: 'blur(8px)', color: '#fff', fontSize: 10, fontWeight: 600 }}>▶ 观看</div>
        </div>

        {/* 月度分组 */}
        {months.map((g, gi) => (
          <div key={gi} style={{ marginTop: 18 }}>
            <div style={{ padding: '0 14px 8px', display: 'flex', alignItems: 'center', gap: 6 }}>
              <span style={{ fontSize: 13, fontWeight: 700, color: p.ink, fontFamily: TYPE.num }}>{g.m}</span>
              <span style={{ fontSize: 10, color: p.inkMuted }}>· {g.count} 张</span>
              {g.highlight && <Tag palette={p} tone="accent" size="xs">🎉 特别</Tag>}
              <div style={{ marginLeft: 'auto', fontSize: 11, color: p.primary, fontWeight: 600 }}>全部 ›</div>
            </div>
            <div style={{ padding: '0 14px', display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 3 }}>
              {g.items.map((it, i) => (
                <div key={i} style={{ aspectRatio: 1, borderRadius: 6, overflow: 'hidden', position: 'relative' }}>
                  <PetPlaceholder w={72} h={72} palette={p} seed={it.seed} variant={it.v} />
                  {i === 0 && gi === 0 && <div style={{ position: 'absolute', bottom: 3, right: 3, padding: '1px 4px', borderRadius: 4, background: 'rgba(0,0,0,0.55)', color: '#fff', fontSize: 8 }}>▶ 0:12</div>}
                  {i === 3 && <div style={{ position: 'absolute', inset: 0, background: 'rgba(0,0,0,0.4)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#fff', fontSize: 12, fontWeight: 700 }}>+{g.count - 4}</div>}
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

function VIPScreen({ palette, radius }) {
  const p = palette;
  return (
    <div style={{ width: '100%', height: '100%', background: '#1a120a', position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color="#fff" />
      {/* Gold 背景 */}
      <div style={{ position: 'absolute', top: 0, left: 0, right: 0, height: 320, background: 'linear-gradient(160deg, #3d2a18, #1a120a)', overflow: 'hidden' }}>
        <div style={{ position: 'absolute', top: 40, right: -20, opacity: 0.15, color: '#d4a86a' }}><PawIcon size={120} color="#d4a86a" /></div>
        <div style={{ position: 'absolute', bottom: 20, left: -10, opacity: 0.12, color: '#d4a86a' }}><PawIcon size={60} color="#d4a86a" /></div>
      </div>
      <TopBar palette={{ ...p, ink: '#fff' }} title="" transparent right={<span style={{ fontSize: 12, color: '#d4a86a' }}>会员规则</span>} />

      {/* 会员卡 */}
      <div style={{ position: 'absolute', top: 82, left: 18, right: 18, height: 170, borderRadius: 18, background: 'linear-gradient(135deg, #d4a86a, #8a6230)', padding: 20, boxShadow: '0 12px 30px rgba(212, 168, 106, 0.25)', overflow: 'hidden' }}>
        <div style={{ position: 'absolute', top: -20, right: -20, opacity: 0.3 }}><PawIcon size={140} color="#fff" /></div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          <div style={{ width: 48, height: 48, borderRadius: 24, background: 'rgba(255,255,255,0.25)', border: '2px solid rgba(255,255,255,0.5)', padding: 2 }}>
            <PetAvatar variant="dog" size={42} />
          </div>
          <div style={{ color: '#fff' }}>
            <div style={{ fontSize: 13, fontWeight: 700 }}>铲屎官小张</div>
            <div style={{ display: 'inline-flex', alignItems: 'center', gap: 3, marginTop: 3, padding: '2px 8px', borderRadius: 8, background: 'rgba(0,0,0,0.3)', fontSize: 10, fontWeight: 700 }}>
              👑 VIP 2 · 爪爪会员
            </div>
          </div>
        </div>
        <div style={{ marginTop: 14, color: '#fff' }}>
          <div style={{ fontSize: 10, opacity: 0.85 }}>距下一等级</div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginTop: 4 }}>
            <div style={{ flex: 1, height: 5, borderRadius: 3, background: 'rgba(0,0,0,0.3)' }}>
              <div style={{ width: '62%', height: '100%', borderRadius: 3, background: '#fff' }} />
            </div>
            <span style={{ fontSize: 10, fontWeight: 700, fontFamily: TYPE.num }}>620 / 1,000</span>
          </div>
          <div style={{ fontSize: 9, opacity: 0.85, marginTop: 4 }}>有效期至 2024/10/22 · 会员号 CP88230142</div>
        </div>
      </div>

      <div style={{ position: 'absolute', top: 268, left: 0, right: 0, bottom: 0, background: p.bg, borderRadius: '22px 22px 0 0', overflow: 'auto', paddingTop: 14, paddingBottom: 20 }}>
        {/* 权益卡 */}
        <div style={{ padding: '0 14px', fontSize: 13, fontWeight: 700, color: p.ink, marginBottom: 10 }}>👑 尊享权益</div>
        <div style={{ padding: '0 14px', display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 8 }}>
          {[
            { icon: '🚫', l: '去广告', sub: '净享体验' },
            { icon: '🎁', l: '会员礼包', sub: '月月送' },
            { icon: '💎', l: '专属客服', sub: '优先响应' },
            { icon: '🏥', l: '医院折扣', sub: '8.8 折起' },
            { icon: '🎬', l: '视频无水印', sub: '高清下载' },
            { icon: '📦', l: '包邮次数', sub: '月 4 次' },
            { icon: '🎂', l: '生日礼物', sub: '毛孩子生日' },
            { icon: '✨', l: '更多', sub: '24 项权益' },
          ].map((v, i) => (
            <div key={i} style={{ padding: '10px 4px', background: p.surface, borderRadius: 12, textAlign: 'center' }}>
              <div style={{ fontSize: 20 }}>{v.icon}</div>
              <div style={{ fontSize: 10, color: p.ink, fontWeight: 700, marginTop: 3 }}>{v.l}</div>
              <div style={{ fontSize: 8, color: p.inkMuted, marginTop: 1 }}>{v.sub}</div>
            </div>
          ))}
        </div>

        {/* 开通套餐 */}
        <div style={{ padding: '16px 14px 8px', fontSize: 13, fontWeight: 700, color: p.ink }}>💰 续费套餐</div>
        <div style={{ padding: '0 14px', display: 'flex', gap: 8 }}>
          {[
            { period: '连续包月', price: '12', save: '省 3', unit: '/月' },
            { period: '年卡', price: '128', save: '仅 10.6/月', unit: '/年', best: true },
            { period: '季卡', price: '38', save: '省 4', unit: '/季' },
          ].map((pkg, i) => (
            <div key={i} style={{ flex: 1, padding: 12, borderRadius: 14, background: pkg.best ? `linear-gradient(135deg, #d4a86a, #8a6230)` : p.surface, border: pkg.best ? 'none' : `1.5px solid ${p.divider}`, color: pkg.best ? '#fff' : p.ink, textAlign: 'center', position: 'relative' }}>
              {pkg.best && <div style={{ position: 'absolute', top: -8, left: '50%', transform: 'translateX(-50%)', padding: '2px 8px', borderRadius: 8, background: '#e07a5a', color: '#fff', fontSize: 9, fontWeight: 700 }}>🔥 推荐</div>}
              <div style={{ fontSize: 11, fontWeight: 600, opacity: pkg.best ? 0.95 : 1 }}>{pkg.period}</div>
              <div style={{ marginTop: 4, fontSize: 22, fontWeight: 800, fontFamily: TYPE.num }}>
                ¥{pkg.price}<span style={{ fontSize: 11, fontWeight: 500, opacity: 0.7 }}>{pkg.unit}</span>
              </div>
              <div style={{ fontSize: 9, marginTop: 3, opacity: pkg.best ? 0.9 : 0.7 }}>{pkg.save}</div>
            </div>
          ))}
        </div>

        <div style={{ padding: '16px 14px 0' }}>
          <button style={{
            width: '100%', height: 46, borderRadius: 23, border: 'none',
            background: `linear-gradient(135deg, #d4a86a, #8a6230)`, color: '#fff',
            fontSize: 14, fontWeight: 800, fontFamily: TYPE.body,
            boxShadow: '0 8px 20px rgba(138, 98, 48, 0.4)',
          }}>👑 立即续费 · ¥128/年</button>
          <div style={{ textAlign: 'center', fontSize: 9, color: p.inkMuted, marginTop: 8 }}>
            开通即同意《爪爪会员协议》· 自动续费可随时取消
          </div>
        </div>
      </div>
    </div>
  );
}

function AboutScreen({ palette, radius }) {
  const p = palette;
  return (
    <div style={{ width: '100%', height: '100%', background: p.bg, position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color={p.ink} />
      <TopBar palette={p} title="关于爱宠社" />

      <div style={{ position: 'absolute', top: 92, left: 0, right: 0, bottom: 0, overflow: 'auto', paddingBottom: 30 }}>
        {/* Brand */}
        <div style={{ textAlign: 'center', padding: '30px 20px 20px' }}>
          <div style={{ display: 'inline-block', padding: 18, borderRadius: 28, background: `linear-gradient(135deg, ${p.primary}, ${p.primaryInk})`, boxShadow: `0 10px 24px ${p.primary}40` }}>
            <PawIcon size={48} color="#fff" />
          </div>
          <div style={{ fontSize: 24, fontWeight: 800, color: p.ink, marginTop: 14, fontFamily: TYPE.brand, letterSpacing: 1 }}>爱宠社 🐾</div>
          <div style={{ fontSize: 11, color: p.inkMuted, marginTop: 4 }}>一起把日子过成毛茸茸的样子</div>
          <div style={{ marginTop: 10, display: 'inline-block', padding: '3px 10px', borderRadius: 10, background: p.surface, fontSize: 10, color: p.inkMuted, fontFamily: TYPE.num }}>v 4.2.8 (build 2810) · 已是最新版</div>
        </div>

        {/* 快捷 */}
        <div style={{ margin: '8px 14px 0', background: p.surface, borderRadius: radius, overflow: 'hidden' }}>
          {[
            { icon: '📜', l: '用户协议' },
            { icon: '🔒', l: '隐私政策' },
            { icon: '👥', l: '儿童隐私保护声明' },
            { icon: '🎯', l: '应用权限说明' },
            { icon: '📄', l: '第三方信息共享清单' },
          ].map((r, i, arr) => (
            <div key={i} style={{ display: 'flex', alignItems: 'center', gap: 10, padding: '13px 14px', borderBottom: i < arr.length - 1 ? `0.5px solid ${p.divider}` : 'none' }}>
              <div style={{ width: 28, height: 28, borderRadius: 8, background: p.primarySoft, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 13 }}>{r.icon}</div>
              <span style={{ flex: 1, fontSize: 13, color: p.ink }}>{r.l}</span>
              <svg width="6" height="10" viewBox="0 0 6 10" fill="none" stroke={p.inkFaint} strokeWidth="1.5" strokeLinecap="round"><path d="M1 1l4 4-4 4" /></svg>
            </div>
          ))}
        </div>

        {/* 联系我们 */}
        <div style={{ margin: '14px 14px 0', fontSize: 12, fontWeight: 700, color: p.ink, marginBottom: 8 }}>💬 联系我们</div>
        <div style={{ margin: '0 14px', background: p.surface, borderRadius: radius, overflow: 'hidden' }}>
          {[
            { icon: '📧', l: '商务合作', v: 'biz@aichongshe.com' },
            { icon: '🐾', l: '客服邮箱', v: 'hi@aichongshe.com' },
            { icon: '📮', l: '公司地址', v: '上海市徐汇区漕溪北路 398 号' },
            { icon: '☎️', l: '客服热线', v: '400-889-1234 · 9-21 点' },
          ].map((r, i, arr) => (
            <div key={i} style={{ display: 'flex', alignItems: 'flex-start', gap: 10, padding: '12px 14px', borderBottom: i < arr.length - 1 ? `0.5px solid ${p.divider}` : 'none' }}>
              <div style={{ width: 28, height: 28, borderRadius: 8, background: p.primarySoft, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 13, flexShrink: 0 }}>{r.icon}</div>
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: 12, color: p.inkSoft }}>{r.l}</div>
                <div style={{ fontSize: 11, color: p.ink, marginTop: 2, fontFamily: /^\d/.test(r.v) || /@/.test(r.v) ? TYPE.num : TYPE.body }}>{r.v}</div>
              </div>
            </div>
          ))}
        </div>

        {/* 社交 */}
        <div style={{ margin: '16px 14px 0', display: 'flex', gap: 8, justifyContent: 'center' }}>
          {[
            { icon: '🎵', name: '抖音', c: '#fe2c55' },
            { icon: '📕', name: '小红书', c: '#ff2442' },
            { icon: '🐦', name: '微博', c: '#e6162d' },
            { icon: '💬', name: '微信公众号', c: '#07c160' },
            { icon: 'B', name: 'B 站', c: '#00a1d6' },
          ].map((s, i) => (
            <div key={i} style={{ width: 46, height: 46, borderRadius: 14, background: p.surface, display: 'flex', alignItems: 'center', justifyContent: 'center', flexDirection: 'column', gap: 2 }}>
              <div style={{ fontSize: 14, color: s.c, fontWeight: 800 }}>{s.icon}</div>
            </div>
          ))}
        </div>

        {/* 鸣谢 */}
        <div style={{ textAlign: 'center', padding: '24px 20px 10px', color: p.inkMuted }}>
          <div style={{ fontSize: 11, color: p.inkSoft }}>感谢 280 万铲屎官的陪伴 🐾</div>
          <div style={{ fontSize: 10, marginTop: 6 }}>Made with 💛 by 爱宠社团队 · Shanghai</div>
          <div style={{ fontSize: 9, marginTop: 10, color: p.inkFaint }}>© 2021-2023 上海爱宠网络科技有限公司<br />沪 ICP 备 21008230 号-2 | 沪公网安备 31010402009876</div>
        </div>
      </div>
    </div>
  );
}

// ============ DrawerScreen 侧边抽屉 ============
function DrawerScreen({ palette, radius }) {
  const p = palette;
  const rr = radius ?? 16;
  const drawerW = 260;

  // 菜单组配置
  const groups = [
    {
      title: '我的账户',
      items: [
        { icon: '💰', label: '我的钱包', sub: '¥ 328.50', accent: p.primary },
        { icon: '🎟', label: '优惠券', sub: '5 张可用', accent: '#e07a5a' },
        { icon: '📦', label: '我的订单', sub: '2 个待收货', accent: '#6fa36f' },
        { icon: '⭐', label: '我的收藏', sub: '128 篇', accent: '#d9956a' },
      ],
    },
    {
      title: '激励与成长',
      items: [
        { icon: '📅', label: '每日签到', sub: '已连签 7 天 · 可领 10 喵币', accent: '#e9a94a', badge: '领取' },
        { icon: '👑', label: 'PetClub 会员', sub: '再 3 天到期', accent: '#c97b4a' },
        { icon: '🎁', label: '邀请好友', sub: '各得 15 元无门槛券', accent: '#b07a9c' },
        { icon: '🏆', label: '我的成就', sub: '已解锁 24 / 60', accent: '#7fa4c4' },
      ],
    },
    {
      title: '工具与帮助',
      items: [
        { icon: '🔖', label: '浏览历史' },
        { icon: '✏️', label: '草稿箱', sub: '3 篇未发布' },
        { icon: '🎧', label: '在线客服' },
        { icon: '⚙️', label: '设置' },
        { icon: 'ℹ️', label: '关于爱宠社' },
      ],
    },
  ];

  return (
    <div style={{ width: '100%', height: '100%', background: p.bg, position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color="#fff" />

      {/* 背后的个人主页（模糊示意）*/}
      <div style={{ position: 'absolute', inset: 0, background: `linear-gradient(180deg, ${p.primarySoft} 0%, ${p.bg} 40%)`, filter: 'blur(2px)' }}>
        {/* 背后的个人主页头图 */}
        <div style={{ position: 'absolute', top: 0, left: 0, right: 0, height: 200, background: `linear-gradient(135deg, ${p.primary}, ${p.accent})` }} />
        <div style={{ position: 'absolute', top: 160, left: 0, right: 0, height: 80, background: p.surface, borderRadius: '22px 22px 0 0' }} />
        {/* 假装的头像 */}
        <div style={{ position: 'absolute', top: 130, left: 20, width: 72, height: 72, borderRadius: 40, background: p.surface, padding: 3 }}>
          <div style={{ width: '100%', height: '100%', borderRadius: 40, background: `linear-gradient(135deg, ${p.accent}, ${p.primary})` }} />
        </div>
      </div>

      {/* 遮罩 */}
      <div style={{ position: 'absolute', inset: 0, background: 'rgba(20, 12, 6, 0.45)', backdropFilter: 'blur(2px)' }} />

      {/* Drawer */}
      <div style={{
        position: 'absolute', top: 0, left: 0, bottom: 0, width: drawerW,
        background: p.bg,
        boxShadow: '8px 0 40px rgba(0,0,0,0.2)',
        display: 'flex', flexDirection: 'column',
        overflow: 'hidden',
      }}>
        {/* 顶部用户区（带弧形背景）*/}
        <div style={{ position: 'relative', paddingTop: 46, paddingBottom: 18, background: `linear-gradient(165deg, ${p.primary} 0%, ${p.accent} 100%)`, overflow: 'hidden' }}>
          {/* 装饰爪印 */}
          <div style={{ position: 'absolute', top: 14, right: -10, fontSize: 80, opacity: 0.1 }}>🐾</div>
          <div style={{ position: 'absolute', bottom: -20, right: 30, fontSize: 50, opacity: 0.12, transform: 'rotate(25deg)' }}>🐾</div>

          {/* 用户卡片 */}
          <div style={{ padding: '16px 18px 0', display: 'flex', alignItems: 'center', gap: 12 }}>
            <div style={{ width: 54, height: 54, borderRadius: 28, background: '#fff', padding: 2, flexShrink: 0 }}>
              <div style={{ width: '100%', height: '100%', borderRadius: 26, background: `linear-gradient(135deg, #ffd9b8, #c97b4a)`, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 26 }}>🐱</div>
            </div>
            <div style={{ flex: 1, color: '#fff' }}>
              <div style={{ fontSize: 17, fontWeight: 800, fontFamily: TYPE.brand, display: 'flex', alignItems: 'center', gap: 6 }}>
                喵酱的铲屎官
                <span style={{ fontSize: 9, padding: '1px 5px', borderRadius: 6, background: 'rgba(255,255,255,0.25)', fontWeight: 700 }}>Lv.12</span>
              </div>
              <div style={{ fontSize: 11, opacity: 0.9, marginTop: 3 }}>ID: petlover_0304 · 已加入 687 天</div>
            </div>
          </div>

          {/* 数据栏 */}
          <div style={{ margin: '14px 14px 0', padding: '10px 4px', borderRadius: 14, background: 'rgba(255,255,255,0.18)', backdropFilter: 'blur(10px)', display: 'flex', justifyContent: 'space-around', color: '#fff' }}>
            {[['128', '关注'], ['2.3k', '粉丝'], ['86', '笔记'], ['1.2w', '获赞']].map(([n, l]) => (
              <div key={l} style={{ textAlign: 'center' }}>
                <div style={{ fontSize: 15, fontWeight: 800, fontFamily: TYPE.num, lineHeight: 1 }}>{n}</div>
                <div style={{ fontSize: 9, opacity: 0.85, marginTop: 4 }}>{l}</div>
              </div>
            ))}
          </div>
        </div>

        {/* 宠物快切 */}
        <div style={{ padding: '14px 18px 8px' }}>
          <div style={{ fontSize: 10, color: p.inkMuted, fontWeight: 700, letterSpacing: 0.5, marginBottom: 8 }}>我的毛孩子</div>
          <div style={{ display: 'flex', gap: 10, overflowX: 'auto' }}>
            {[
              { name: '喵酱', emoji: '🐱', active: true },
              { name: '球球', emoji: '🐕', active: false },
              { name: '闪闪', emoji: '🐠', active: false },
            ].map((pet, i) => (
              <div key={i} style={{ flexShrink: 0, width: 50, textAlign: 'center' }}>
                <div style={{
                  width: 44, height: 44, borderRadius: 24, margin: '0 auto',
                  background: pet.active ? `linear-gradient(135deg, ${p.primary}, ${p.accent})` : p.surfaceDim,
                  padding: pet.active ? 2 : 0,
                  border: pet.active ? 'none' : `1px dashed ${p.divider}`,
                }}>
                  <div style={{ width: '100%', height: '100%', borderRadius: 22, background: p.surface, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 22 }}>{pet.emoji}</div>
                </div>
                <div style={{ fontSize: 10, color: pet.active ? p.ink : p.inkMuted, fontWeight: pet.active ? 700 : 500, marginTop: 4 }}>{pet.name}</div>
              </div>
            ))}
            <div style={{ flexShrink: 0, width: 50, textAlign: 'center' }}>
              <div style={{ width: 44, height: 44, borderRadius: 24, margin: '0 auto', background: p.surfaceDim, border: `1px dashed ${p.inkFaint}`, display: 'flex', alignItems: 'center', justifyContent: 'center', color: p.inkMuted, fontSize: 18 }}>+</div>
              <div style={{ fontSize: 10, color: p.inkMuted, marginTop: 4 }}>添加</div>
            </div>
          </div>
        </div>

        {/* 菜单组 */}
        <div style={{ flex: 1, overflowY: 'auto', padding: '0 12px 12px' }}>
          {groups.map((g, gi) => (
            <div key={gi} style={{ marginTop: 12 }}>
              <div style={{ fontSize: 10, color: p.inkMuted, fontWeight: 700, letterSpacing: 0.5, padding: '0 6px 6px' }}>{g.title}</div>
              <div style={{ background: p.surface, borderRadius: rr, overflow: 'hidden' }}>
                {g.items.map((it, i) => (
                  <div key={i} style={{
                    display: 'flex', alignItems: 'center', gap: 12,
                    padding: '11px 12px',
                    borderBottom: i === g.items.length - 1 ? 'none' : `0.5px solid ${p.divider}`,
                    cursor: 'pointer',
                  }}>
                    <div style={{
                      width: 30, height: 30, borderRadius: 10,
                      background: it.accent ? it.accent + '22' : p.surfaceDim,
                      display: 'flex', alignItems: 'center', justifyContent: 'center',
                      fontSize: 15, flexShrink: 0,
                    }}>{it.icon}</div>
                    <div style={{ flex: 1, minWidth: 0 }}>
                      <div style={{ fontSize: 13, color: p.ink, fontWeight: 600 }}>{it.label}</div>
                      {it.sub && <div style={{ fontSize: 10, color: p.inkMuted, marginTop: 1 }}>{it.sub}</div>}
                    </div>
                    {it.badge ? (
                      <div style={{ fontSize: 10, color: '#fff', fontWeight: 700, padding: '3px 8px', borderRadius: 10, background: it.accent || p.primary }}>{it.badge}</div>
                    ) : (
                      <svg width="6" height="10" viewBox="0 0 6 10" fill="none" stroke={p.inkFaint} strokeWidth="1.5" strokeLinecap="round"><path d="M1 1l4 4-4 4" /></svg>
                    )}
                  </div>
                ))}
              </div>
            </div>
          ))}

          {/* 夜间模式切换 */}
          <div style={{ marginTop: 12, background: p.surface, borderRadius: rr, padding: '11px 12px', display: 'flex', alignItems: 'center', gap: 12 }}>
            <div style={{ width: 30, height: 30, borderRadius: 10, background: '#2a2017', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 14 }}>🌙</div>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: 13, color: p.ink, fontWeight: 600 }}>夜间模式</div>
              <div style={{ fontSize: 10, color: p.inkMuted, marginTop: 1 }}>跟随系统</div>
            </div>
            {/* 开关 */}
            <div style={{ width: 34, height: 20, borderRadius: 10, background: p.primary, position: 'relative' }}>
              <div style={{ position: 'absolute', top: 2, right: 2, width: 16, height: 16, borderRadius: 8, background: '#fff', boxShadow: '0 1px 2px rgba(0,0,0,0.2)' }} />
            </div>
          </div>
        </div>

        {/* 底部退出 */}
        <div style={{ padding: '10px 18px 20px', borderTop: `0.5px solid ${p.divider}`, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div style={{ fontSize: 11, color: p.inkMuted }}>版本 v5.2.1</div>
          <div style={{ fontSize: 11, color: p.inkSoft, fontWeight: 600, display: 'flex', alignItems: 'center', gap: 4, cursor: 'pointer' }}>
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4M16 17l5-5-5-5M21 12H9" /></svg>
            退出登录
          </div>
        </div>
      </div>
    </div>
  );
}

Object.assign(window, { PetDateScreen, AdoptScreen, LostScreen, AlbumScreen, VIPScreen, AboutScreen, DrawerScreen });
