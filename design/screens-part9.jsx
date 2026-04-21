// screens-part9.jsx — 宠物档案 & 内容生态：PetProfile / AddPet / Vaccine / Topic / Comments

function PetProfileScreen({ palette, radius }) {
  const p = palette;
  return (
    <div style={{ width: '100%', height: '100%', background: p.bg, position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color="#fff" />
      {/* hero */}
      <div style={{ position: 'absolute', top: 0, left: 0, right: 0, height: 280, background: `linear-gradient(160deg, ${p.primarySoft}, ${p.primary} 120%)`, overflow: 'hidden' }}>
        <div style={{ position: 'absolute', top: 60, right: 20, opacity: 0.2 }}><PawIcon size={80} color="#fff" /></div>
        <div style={{ position: 'absolute', top: 160, left: 30, opacity: 0.15 }}><PawIcon size={46} color="#fff" /></div>
      </div>
      <TopBar palette={{ ...p, ink: '#fff' }} title="" transparent showMenu
        right={<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#fff" strokeWidth="2"><circle cx="12" cy="5" r="1.5" fill="#fff" /><circle cx="12" cy="12" r="1.5" fill="#fff" /><circle cx="12" cy="19" r="1.5" fill="#fff" /></svg>} />

      {/* 汤圆卡 */}
      <div style={{ position: 'absolute', top: 90, left: 14, right: 14, textAlign: 'center', zIndex: 5 }}>
        <div style={{ display: 'inline-block', padding: 4, borderRadius: 50, background: '#fff', boxShadow: `0 8px 24px ${p.ink}20` }}>
          <PetAvatar variant="dog" size={90} bg={p.primarySoft} />
        </div>
        <div style={{ fontSize: 22, fontWeight: 800, color: '#fff', marginTop: 10, fontFamily: TYPE.brand }}>汤圆 🐕</div>
        <div style={{ fontSize: 11, color: 'rgba(255,255,255,0.9)', marginTop: 3 }}>金毛寻回犬 · ♂ · 2 岁 3 月 · 24 kg</div>
        <div style={{ display: 'inline-flex', gap: 5, marginTop: 8, padding: '4px 12px', borderRadius: 14, background: 'rgba(255,255,255,0.25)', backdropFilter: 'blur(10px)' }}>
          <span style={{ fontSize: 10, color: '#fff', fontWeight: 600 }}>🎂 2022/08/15 生日</span>
        </div>
      </div>

      <div style={{ position: 'absolute', top: 304, left: 0, right: 0, bottom: 0, overflow: 'auto', paddingBottom: 20 }}>
        {/* 指标卡 */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 6, padding: '0 14px' }}>
          {[
            { icon: '⚖️', label: '体重', v: '24.2', u: 'kg' },
            { icon: '🍖', label: '今日食量', v: '320', u: 'g' },
            { icon: '🚶', label: '今日步数', v: '8.2k', u: '步' },
            { icon: '🎾', label: '玩耍', v: '45', u: '分' },
          ].map((s, i) => (
            <div key={i} style={{ background: p.surface, borderRadius: 12, padding: '10px 6px', textAlign: 'center' }}>
              <div style={{ fontSize: 16 }}>{s.icon}</div>
              <div style={{ fontSize: 15, fontWeight: 800, color: p.ink, marginTop: 2, fontFamily: TYPE.num }}>{s.v}<span style={{ fontSize: 9, color: p.inkMuted, marginLeft: 1 }}>{s.u}</span></div>
              <div style={{ fontSize: 9, color: p.inkMuted, marginTop: 1 }}>{s.label}</div>
            </div>
          ))}
        </div>

        {/* 健康提醒 */}
        <SectionTitle palette={p} title="💉 健康档案" right={<span>查看 ›</span>} />
        <div style={{ margin: '0 14px', background: p.surface, borderRadius: radius, overflow: 'hidden' }}>
          {[
            { icon: '💉', title: '狂犬疫苗', sub: '有效期至 2024/03/15', soon: false },
            { icon: '🦠', title: '体外驱虫', sub: '距下次 还有 7 天', soon: true },
            { icon: '🏥', title: '体检报告', sub: '2023/09/22 · 一切正常 ✓', soon: false },
            { icon: '💊', title: '肠胃调理', sub: '连续服药 5 / 7 天', soon: false },
          ].map((r, i, arr) => (
            <div key={i} style={{ display: 'flex', alignItems: 'center', gap: 11, padding: '12px 14px', borderBottom: i < arr.length - 1 ? `0.5px solid ${p.divider}` : 'none' }}>
              <div style={{ width: 36, height: 36, borderRadius: 10, background: p.primarySoft, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 16 }}>{r.icon}</div>
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: 12, color: p.ink, fontWeight: 700 }}>{r.title}</div>
                <div style={{ fontSize: 10, color: r.soon ? p.warn : p.inkMuted, marginTop: 2 }}>{r.sub}</div>
              </div>
              {r.soon && <Tag palette={p} tone="warn" size="xs">即将到期</Tag>}
            </div>
          ))}
        </div>

        {/* 成长相册 */}
        <SectionTitle palette={p} title="📸 成长相册" subtitle="共 428 张" right={<span>全部 ›</span>} />
        <div style={{ padding: '0 14px', display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 4 }}>
          {[0, 1, 2, 3].map(i => (
            <div key={i} style={{ aspectRatio: 1, borderRadius: 8, overflow: 'hidden', position: 'relative' }}>
              <PetPlaceholder w={80} h={80} palette={p} seed={i} variant="dog" />
              {i === 3 && <div style={{ position: 'absolute', inset: 0, background: 'rgba(0,0,0,0.4)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#fff', fontSize: 13, fontWeight: 700 }}>+424</div>}
            </div>
          ))}
        </div>

        {/* 喜好 */}
        <SectionTitle palette={p} title="🥰 他的喜好" />
        <div style={{ padding: '0 18px', display: 'flex', flexWrap: 'wrap', gap: 6 }}>
          {['鸭肉肉干', '拆家', '游泳', '晒太阳', '网球', '洗澡（假）', '爸爸', '撒娇'].map((t, i) => (
            <Tag key={i} palette={p} tone={i % 3 === 0 ? 'soft' : i % 3 === 1 ? 'accent' : 'ghost'} size="md">{t}</Tag>
          ))}
        </div>
      </div>
    </div>
  );
}

function AddPetScreen({ palette, radius }) {
  const p = palette;
  return (
    <div style={{ width: '100%', height: '100%', background: p.bg, position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color={p.ink} />
      <TopBar palette={p} title="添加宠物" left="close" />

      <div style={{ position: 'absolute', top: 92, left: 0, right: 0, bottom: 86, overflow: 'auto', padding: '10px 14px 20px' }}>
        {/* 头像上传 */}
        <div style={{ background: p.surface, borderRadius: radius, padding: '24px 0', textAlign: 'center', position: 'relative', overflow: 'hidden' }}>
          <div style={{ position: 'absolute', top: 10, right: 10, opacity: 0.12 }}><PawIcon size={50} color={p.primaryInk} /></div>
          <div style={{ display: 'inline-block', position: 'relative' }}>
            <div style={{ width: 88, height: 88, borderRadius: 44, background: p.primarySoft, border: `3px dashed ${p.primary}`, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 34 }}>📷</div>
          </div>
          <div style={{ fontSize: 12, color: p.primary, marginTop: 10, fontWeight: 700 }}>上传毛孩子照片</div>
          <div style={{ fontSize: 10, color: p.inkMuted, marginTop: 3 }}>支持 JPG / PNG · 建议 400×400 以上</div>
        </div>

        {/* 基本信息 */}
        <div style={{ marginTop: 12, fontSize: 13, fontWeight: 700, color: p.ink, padding: '0 4px 8px' }}>基本信息</div>
        <div style={{ background: p.surface, borderRadius: radius, overflow: 'hidden' }}>
          {[
            { label: '名字', ph: '给毛孩子起个名字' },
            { label: '品种', v: '金毛寻回犬', arrow: true },
            { label: '性别', custom: (
              <div style={{ display: 'flex', gap: 6, marginLeft: 'auto' }}>
                {[
                  { id: 'm', label: '♂ 弟弟', active: true },
                  { id: 'f', label: '♀ 妹妹', active: false },
                ].map(g => (
                  <div key={g.id} style={{ padding: '4px 10px', borderRadius: 10, background: g.active ? p.primary : p.surfaceDim, color: g.active ? '#fff' : p.inkSoft, fontSize: 11, fontWeight: 700 }}>{g.label}</div>
                ))}
              </div>
            ) },
            { label: '生日', v: '请选择', ph: true, arrow: true },
            { label: '体重', ph: 'kg', right: 'kg' },
          ].map((f, i, arr) => (
            <div key={i} style={{ display: 'flex', alignItems: 'center', padding: '13px 14px', gap: 10, borderBottom: i < arr.length - 1 ? `0.5px solid ${p.divider}` : 'none' }}>
              <span style={{ width: 60, fontSize: 13, color: p.inkSoft }}>{f.label}</span>
              {f.custom || (
                <>
                  <span style={{ flex: 1, fontSize: 13, color: f.v && !f.ph ? p.ink : p.inkFaint, textAlign: 'right' }}>{f.v || f.ph}{f.right && <span style={{ color: p.inkMuted, marginLeft: 4 }}>{f.right}</span>}</span>
                  {f.arrow && <svg width="6" height="10" viewBox="0 0 6 10" fill="none" stroke={p.inkFaint} strokeWidth="1.5" strokeLinecap="round"><path d="M1 1l4 4-4 4" /></svg>}
                </>
              )}
            </div>
          ))}
        </div>

        {/* 健康档案 */}
        <div style={{ marginTop: 16, fontSize: 13, fontWeight: 700, color: p.ink, padding: '0 4px 8px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <span>健康档案 <span style={{ fontSize: 10, color: p.inkMuted, fontWeight: 500, marginLeft: 4 }}>· 可后续补充</span></span>
        </div>
        <div style={{ background: p.surface, borderRadius: radius, overflow: 'hidden' }}>
          {[
            { l: '是否绝育', v: '未选择' },
            { l: '疫苗接种', v: '添加记录' },
            { l: '过敏源', v: '无 / 添加' },
            { l: '特殊习性', v: '选填' },
          ].map((f, i, arr) => (
            <div key={i} style={{ display: 'flex', alignItems: 'center', padding: '13px 14px', borderBottom: i < arr.length - 1 ? `0.5px solid ${p.divider}` : 'none' }}>
              <span style={{ flex: 1, fontSize: 13, color: p.inkSoft }}>{f.l}</span>
              <span style={{ fontSize: 12, color: p.inkFaint }}>{f.v}</span>
              <svg width="6" height="10" viewBox="0 0 6 10" fill="none" stroke={p.inkFaint} strokeWidth="1.5" strokeLinecap="round" style={{ marginLeft: 6 }}><path d="M1 1l4 4-4 4" /></svg>
            </div>
          ))}
        </div>

        {/* 装饰 */}
        <div style={{ textAlign: 'center', marginTop: 20, fontSize: 11, color: p.inkMuted }}>
          🐾 建立档案，让推荐更懂你的宝贝
        </div>
      </div>

      <div style={{ position: 'absolute', bottom: 0, left: 0, right: 0, padding: '10px 14px 22px', background: p.bg }}>
        <button style={{
          width: '100%', height: 46, borderRadius: 23, border: 'none',
          background: `linear-gradient(135deg, ${p.primary}, ${p.primaryInk})`,
          color: '#fff', fontSize: 14, fontWeight: 700,
          boxShadow: `0 6px 16px ${p.primary}50`, fontFamily: TYPE.body,
        }}>完成添加 🐾</button>
      </div>
    </div>
  );
}

function VaccineScreen({ palette, radius }) {
  const p = palette;
  const records = [
    { date: '2023/08/15', name: '狂犬疫苗（卫佳 5）', hospital: '上海宠颐生徐汇医院', next: '2024/08/15', color: p.primary, done: true },
    { date: '2023/08/15', name: '狗六联疫苗', hospital: '上海宠颐生徐汇医院', next: '2024/08/15', color: p.accent, done: true },
    { date: '2023/07/20', name: '体外驱虫 · 大宠爱', hospital: '自行操作', next: '2023/10/20', color: p.warn, done: true },
    { date: '2023/06/10', name: '体内驱虫 · 海乐妙', hospital: '自行操作', next: '2023/09/10', color: p.warn, done: true },
    { date: '2023/03/01', name: '狂犬首针', hospital: '上海宠颐生徐汇医院', next: '2023/08/15 · 已续针', color: p.primary, done: true },
  ];

  return (
    <div style={{ width: '100%', height: '100%', background: p.bg, position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color={p.ink} />
      <TopBar palette={p} title="汤圆 · 健康档案" right={<span style={{ fontSize: 13, color: p.primary, fontWeight: 700 }}>＋</span>} />

      <div style={{ position: 'absolute', top: 92, left: 0, right: 0, bottom: 0, overflow: 'auto', paddingBottom: 30 }}>
        {/* 到期提醒卡 */}
        <div style={{ margin: '6px 14px 0', padding: 14, borderRadius: radius, background: `linear-gradient(135deg, ${p.warn}, #e89874)`, color: '#fff', position: 'relative', overflow: 'hidden' }}>
          <div style={{ position: 'absolute', top: -10, right: -10, opacity: 0.25 }}>⏰</div>
          <div style={{ fontSize: 11, fontWeight: 600, opacity: 0.95 }}>⏰ 即将到期 · 别忘了</div>
          <div style={{ fontSize: 16, fontWeight: 800, marginTop: 4 }}>体外驱虫 还有 7 天到期</div>
          <div style={{ fontSize: 11, opacity: 0.9, marginTop: 2 }}>上次使用 · 大宠爱 · 建议 10/20 前完成</div>
          <button style={{ marginTop: 10, padding: '5px 12px', borderRadius: 12, background: 'rgba(255,255,255,0.25)', border: '1px solid rgba(255,255,255,0.4)', color: '#fff', fontSize: 11, fontWeight: 700, fontFamily: TYPE.body }}>设置提醒 🔔</button>
        </div>

        {/* 切换宠物 */}
        <div style={{ margin: '14px 14px 0', display: 'flex', gap: 8 }}>
          <div style={{ display: 'inline-flex', gap: 6, background: p.surface, borderRadius: 14, padding: 4 }}>
            {[{ id: 'dog', name: '汤圆', active: true }, { id: 'cat', name: '奶冻' }].map(pet => (
              <div key={pet.id} style={{ padding: '5px 12px', borderRadius: 10, background: pet.active ? p.primary : 'transparent', color: pet.active ? '#fff' : p.inkSoft, fontSize: 11, fontWeight: 700 }}>
                {pet.name}
              </div>
            ))}
          </div>
          <div style={{ marginLeft: 'auto', display: 'flex', gap: 6 }}>
            <Tag palette={p} tone="soft" size="md">💉 疫苗</Tag>
            <Tag palette={p} tone="ghost" size="md">🦠 驱虫</Tag>
            <Tag palette={p} tone="ghost" size="md">💊 用药</Tag>
          </div>
        </div>

        {/* 时间轴记录 */}
        <SectionTitle palette={p} title="📋 历史记录" subtitle={`共 ${records.length} 条`} />
        <div style={{ padding: '0 14px' }}>
          {records.map((r, i) => (
            <div key={i} style={{ display: 'flex', gap: 10, position: 'relative', marginBottom: 4 }}>
              <div style={{ width: 14, flexShrink: 0, position: 'relative' }}>
                <div style={{ width: 10, height: 10, borderRadius: 5, background: r.color, marginTop: 14, boxShadow: `0 0 0 3px ${r.color}22` }} />
                {i < records.length - 1 && <div style={{ position: 'absolute', left: 4, top: 26, bottom: -6, width: 2, background: p.divider }} />}
              </div>
              <div style={{ flex: 1, background: p.surface, borderRadius: radius, padding: 12, marginBottom: 8 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                  <span style={{ fontSize: 10, color: p.inkMuted, fontFamily: TYPE.num }}>{r.date}</span>
                  <div style={{ width: 3, height: 3, borderRadius: 2, background: p.inkFaint }} />
                  <span style={{ fontSize: 10, color: p.inkSoft }}>{r.hospital}</span>
                </div>
                <div style={{ fontSize: 13, fontWeight: 700, color: p.ink, marginTop: 5 }}>{r.name}</div>
                <div style={{ fontSize: 10, color: p.inkMuted, marginTop: 4, display: 'flex', alignItems: 'center', gap: 4 }}>
                  🔜 下次: <span style={{ color: p.ink, fontWeight: 600, fontFamily: TYPE.num }}>{r.next}</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

function TopicScreen({ palette, radius }) {
  const p = palette;
  const notes = [
    { h: 170, title: '汤圆的第 28 次洗澡', user: '汤圆家日记', avatar: 'dog', likes: 2341, seed: 0, variant: 'dog' },
    { h: 220, title: '金毛毕业啦～拿到证书', user: '养宠百科', avatar: 'dog', likes: 1823, seed: 1, variant: 'puppy' },
    { h: 160, title: '秋日拾光 🍂 公园暴走', user: '柴柴麻麻', avatar: 'fluff', likes: 567, seed: 2, variant: 'dog' },
    { h: 200, title: '幼犬换牙期喂啥？', user: '小可爱', avatar: 'cat', likes: 892, seed: 3, variant: 'fluff' },
    { h: 180, title: '自制南瓜泥配方', user: '奶冻厨房', avatar: 'mochi', likes: 410, seed: 4, variant: 'puppy' },
    { h: 210, title: '溜娃必备 · 推荐 5 件', user: '布偶小甜饼', avatar: 'cat', likes: 1105, seed: 5, variant: 'dog' },
  ];

  return (
    <div style={{ width: '100%', height: '100%', background: p.bg, position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color="#fff" />
      {/* hero */}
      <div style={{ position: 'absolute', top: 0, left: 0, right: 0, height: 220, background: `linear-gradient(160deg, ${p.primary}, ${p.primaryInk} 120%)`, overflow: 'hidden' }}>
        <div style={{ position: 'absolute', top: -20, right: -20, opacity: 0.2 }}><PawIcon size={140} color="#fff" /></div>
        <div style={{ position: 'absolute', bottom: 30, left: 30, opacity: 0.14 }}><PawIcon size={60} color="#fff" /></div>
      </div>
      <TopBar palette={{ ...p, ink: '#fff' }} title="" transparent showMenu
        right={<ShareIcon size={18} color="#fff" />} />

      {/* Topic header */}
      <div style={{ position: 'absolute', top: 100, left: 14, right: 14, zIndex: 5 }}>
        <div style={{ fontSize: 10, color: 'rgba(255,255,255,0.9)', fontWeight: 600 }}># 话题 · 中大型犬</div>
        <div style={{ fontSize: 26, fontWeight: 800, color: '#fff', marginTop: 4, fontFamily: TYPE.brand }}>
          #金毛日记 🐕
        </div>
        <div style={{ fontSize: 11, color: 'rgba(255,255,255,0.9)', marginTop: 4 }}>
          全球金毛主子养成记 · 每周五 #金毛日 打卡
        </div>
        <div style={{ display: 'flex', gap: 14, marginTop: 10, fontSize: 10, color: '#fff' }}>
          <span><b style={{ fontFamily: TYPE.num, fontSize: 13 }}>128.4w</b> 浏览</span>
          <span><b style={{ fontFamily: TYPE.num, fontSize: 13 }}>3.2w</b> 笔记</span>
          <span><b style={{ fontFamily: TYPE.num, fontSize: 13 }}>8,240</b> 讨论</span>
        </div>
        <div style={{ display: 'flex', gap: 8, marginTop: 12 }}>
          <button style={{ flex: 1, padding: '8px 0', borderRadius: 18, border: 'none', background: '#fff', color: p.primaryInk, fontSize: 12, fontWeight: 700, fontFamily: TYPE.body }}>+ 参与话题</button>
          <button style={{ padding: '8px 16px', borderRadius: 18, background: 'rgba(255,255,255,0.25)', border: '1px solid rgba(255,255,255,0.4)', color: '#fff', fontSize: 12, fontWeight: 700, backdropFilter: 'blur(10px)', fontFamily: TYPE.body }}>🔔 关注</button>
        </div>
      </div>

      {/* Tabs */}
      <div style={{ position: 'absolute', top: 256, left: 0, right: 0, height: 40, background: p.bg, display: 'flex', padding: '0 18px', gap: 18, borderBottom: `0.5px solid ${p.divider}`, zIndex: 3 }}>
        {[
          { id: 'hot', label: '🔥 热门', active: true },
          { id: 'new', label: '最新' },
          { id: 'join', label: '我参与的' },
        ].map(t => (
          <div key={t.id} style={{ padding: '10px 0', borderBottom: t.active ? `2px solid ${p.primary}` : '2px solid transparent', marginBottom: -0.5 }}>
            <span style={{ fontSize: 13, fontWeight: t.active ? 800 : 500, color: t.active ? p.ink : p.inkMuted }}>{t.label}</span>
          </div>
        ))}
      </div>

      {/* Waterfall */}
      <div style={{ position: 'absolute', top: 298, left: 0, right: 0, bottom: 0, overflow: 'auto', padding: '10px 8px 30px', columnCount: 2, columnGap: 6 }}>
        {notes.map((n, i) => (
          <div key={i} style={{ breakInside: 'avoid', marginBottom: 6, background: p.surface, borderRadius: 14, overflow: 'hidden', boxShadow: `0 2px 6px ${p.ink}08` }}>
            <div style={{ height: n.h - 50 }}><PetPlaceholder w={160} h={n.h - 50} palette={p} seed={n.seed} variant={n.variant} /></div>
            <div style={{ padding: '8px 10px 10px' }}>
              <div style={{ fontSize: 11, color: p.ink, lineHeight: 1.3, fontWeight: 500 }}>{n.title}</div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 5, marginTop: 6 }}>
                <PetAvatar variant={n.avatar} size={16} />
                <span style={{ fontSize: 9, color: p.inkMuted, flex: 1, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{n.user}</span>
                <HeartIcon size={11} color={p.inkMuted} /> <span style={{ fontSize: 9, color: p.inkMuted, fontFamily: TYPE.num }}>{n.likes}</span>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

function CommentsScreen({ palette, radius }) {
  const p = palette;
  const comments = [
    { user: '柴柴麻麻', avatar: 'fluff', time: '2 分钟前', likes: 234, text: '汤圆洗完澡也太可爱了吧！！请问用的什么沐浴露呀，我家柴柴换毛季严重，求推荐😭',
      replies: [{ user: '汤圆家日记', avatar: 'dog', isOP: true, text: '用的 @Bubblebee 宠物蛋白精华，对换毛季真的很友好～', time: '1 分钟前', likes: 48 }] },
    { user: '金毛百科', avatar: 'dog', time: '8 分钟前', likes: 189, verified: true, text: '专业 UP 来了！金毛每周洗 1 次最合适，洗太勤反而破坏毛发油脂保护层 🌟', replies: [] },
    { user: '布偶小甜饼', avatar: 'cat', time: '15 分钟前', likes: 42, text: '救命 汤圆也太乖了！我家布偶洗澡从头吼到尾，快被邻居投诉了[哭]', replies: [] },
    { user: '小泰迪豆豆', avatar: 'puppy', time: '22 分钟前', likes: 18, text: '🐾🐾🐾 云吸一口 ~~~', replies: [] },
    { user: '养宠小能手', avatar: 'fluff', time: '35 分钟前', likes: 76, text: '请问吹水用的什么吹风机？小狗会怕吹风机的声音吗', replies: [] },
    { user: '喵喵喵先生', avatar: 'cat', time: '1 小时前', likes: 12, text: '突然想养狗了...', replies: [] },
  ];

  return (
    <div style={{ width: '100%', height: '100%', background: p.bg, position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color={p.ink} />
      <TopBar palette={p} title="评论 238"
        right={<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke={p.ink} strokeWidth="2" strokeLinecap="round"><circle cx="6" cy="12" r="1.5" fill={p.ink} /><circle cx="12" cy="12" r="1.5" fill={p.ink} /><circle cx="18" cy="12" r="1.5" fill={p.ink} /></svg>} />

      {/* Sort tabs */}
      <div style={{ position: 'absolute', top: 92, left: 0, right: 0, height: 40, background: p.bg, display: 'flex', padding: '0 18px', gap: 16, borderBottom: `0.5px solid ${p.divider}`, zIndex: 5 }}>
        {[{ label: '🔥 热度', active: true }, { label: '⏰ 最新' }, { label: '👑 作者回复' }].map((t, i) => (
          <div key={i} style={{ padding: '10px 0', borderBottom: t.active ? `2px solid ${p.primary}` : '2px solid transparent', marginBottom: -0.5 }}>
            <span style={{ fontSize: 12, fontWeight: t.active ? 800 : 500, color: t.active ? p.ink : p.inkMuted }}>{t.label}</span>
          </div>
        ))}
      </div>

      <div style={{ position: 'absolute', top: 132, left: 0, right: 0, bottom: 68, overflow: 'auto', padding: '4px 0 10px' }}>
        {comments.map((c, i) => (
          <div key={i} style={{ padding: '12px 16px', borderBottom: `0.5px solid ${p.divider}` }}>
            <div style={{ display: 'flex', gap: 10 }}>
              <PetAvatar variant={c.avatar} size={36} />
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                  <span style={{ fontSize: 12, fontWeight: 700, color: p.ink }}>{c.user}</span>
                  {c.verified && <Tag palette={p} tone="primary" size="xs">✓ 博主</Tag>}
                </div>
                <div style={{ fontSize: 13, color: p.ink, marginTop: 4, lineHeight: 1.5 }}>{c.text}</div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginTop: 6 }}>
                  <span style={{ fontSize: 10, color: p.inkMuted }}>{c.time}</span>
                  <span style={{ fontSize: 10, color: p.inkMuted }}>回复</span>
                </div>

                {/* 回复 */}
                {c.replies.length > 0 && (
                  <div style={{ marginTop: 8, background: p.surfaceDim, borderRadius: 10, padding: 10 }}>
                    {c.replies.map((r, j) => (
                      <div key={j} style={{ display: 'flex', gap: 8 }}>
                        <PetAvatar variant={r.avatar} size={24} />
                        <div style={{ flex: 1 }}>
                          <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                            <span style={{ fontSize: 11, fontWeight: 700, color: p.primaryInk }}>{r.user}</span>
                            {r.isOP && <Tag palette={p} tone="soft" size="xs">作者</Tag>}
                          </div>
                          <div style={{ fontSize: 12, color: p.ink, marginTop: 3, lineHeight: 1.5 }}>{r.text}</div>
                          <div style={{ fontSize: 10, color: p.inkMuted, marginTop: 4 }}>{r.time} · <HeartIcon size={9} color={p.inkMuted} style={{ verticalAlign: 'middle' }} /> {r.likes}</div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2 }}>
                <HeartIcon size={16} color={p.inkMuted} />
                <span style={{ fontSize: 10, color: p.inkMuted, fontFamily: TYPE.num }}>{c.likes}</span>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* 输入栏 */}
      <div style={{ position: 'absolute', bottom: 0, left: 0, right: 0, background: p.surface, borderTop: `0.5px solid ${p.divider}`, padding: '10px 14px 22px', display: 'flex', alignItems: 'center', gap: 8 }}>
        <PetAvatar variant="dog" size={28} />
        <div style={{ flex: 1, height: 34, borderRadius: 17, background: p.surfaceDim, padding: '0 14px', display: 'flex', alignItems: 'center', fontSize: 12, color: p.inkMuted }}>
          给 @汤圆家日记 留言...
        </div>
        <span style={{ fontSize: 20 }}>😊</span>
        <span style={{ fontSize: 20 }}>🖼️</span>
      </div>
    </div>
  );
}

Object.assign(window, { PetProfileScreen, AddPetScreen, VaccineScreen, TopicScreen, CommentsScreen });
