// screens-part2.jsx — Home 首页瀑布流 + Note 笔记详情

// ═══════════════════════════════════════════════════════════
// 3. HOME — 社区动态瀑布流
// ═══════════════════════════════════════════════════════════
const NOTES_DATA = [
  { id: 1, variant: 'dog', h: 180, title: '今天的柴柴超乖的！洗完澡变成一颗毛球', user: '汤圆麻麻', likes: 2341, liked: true, tag: '金毛日记' },
  { id: 2, variant: 'cat', h: 240, title: '橘猫的一百种睡姿，你家的喜欢哪一种？', user: '橘座大人', likes: 892, liked: false, tag: '猫咪治愈' },
  { id: 3, variant: 'mochi', h: 200, title: '自制宠物冰淇淋，天热给毛孩子降降温🍦', user: '小饼干食堂', likes: 5612, liked: true, tag: '爱宠食谱' },
  { id: 4, variant: 'puppy', h: 260, title: '新手养狗三个月，来说说踩过的坑', user: '阿汪爸爸', likes: 1203, liked: false, tag: '新手指南' },
  { id: 5, variant: 'fluff', h: 170, title: '这家宠物店的美容师太会了，我家宝贝帅爆了', user: 'Lulu', likes: 678, liked: false },
  { id: 6, variant: 'cat', h: 220, title: '收到！布偶猫的日常，治愈一整天', user: 'momo', likes: 3421, liked: true, tag: '布偶猫' },
];

function HomeScreen({ palette, radius }) {
  const p = palette;
  const [tab, setTab] = React.useState('discover');
  const [liked, setLiked] = React.useState({ 1: true, 3: true, 6: true });

  const tabs = [
    { id: 'follow', label: '关注' },
    { id: 'discover', label: '发现' },
    { id: 'nearby', label: '附近' },
  ];

  const col1 = NOTES_DATA.filter((_, i) => i % 2 === 0);
  const col2 = NOTES_DATA.filter((_, i) => i % 2 === 1);

  const renderCard = (n) => {
    const isLiked = liked[n.id] ?? n.liked;
    return (
      <div key={n.id} style={{
        background: p.surface, borderRadius: radius,
        overflow: 'hidden', marginBottom: 10,
        boxShadow: '0 2px 8px rgba(0,0,0,0.04)',
      }}>
        <div style={{ position: 'relative' }}>
          <PetPlaceholder w={180} h={n.h} palette={p} seed={n.id} variant={n.variant} />
          {n.tag && (
            <div style={{
              position: 'absolute', top: 8, left: 8,
              background: 'rgba(255,255,255,0.85)',
              backdropFilter: 'blur(8px)',
              padding: '3px 8px', borderRadius: 8,
              fontSize: 10, fontWeight: 600, color: p.primaryInk,
            }}>#{n.tag}</div>
          )}
        </div>
        <div style={{ padding: '8px 10px 10px' }}>
          <div style={{ fontSize: 12, lineHeight: 1.4, color: p.ink, fontWeight: 500, marginBottom: 8,
            display: '-webkit-box', WebkitBoxOrient: 'vertical', WebkitLineClamp: 2, overflow: 'hidden' }}>
            {n.title}
          </div>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 5, minWidth: 0 }}>
              <PetAvatar variant={n.variant} size={18} />
              <span style={{ fontSize: 10, color: p.inkMuted, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{n.user}</span>
            </div>
            <div onClick={() => setLiked(l => ({ ...l, [n.id]: !(l[n.id] ?? n.liked) }))}
              style={{ display: 'flex', alignItems: 'center', gap: 3, cursor: 'pointer' }}>
              <HeartIcon size={12} color={isLiked ? p.warn : p.inkMuted} filled={isLiked} />
              <span style={{ fontSize: 10, color: isLiked ? p.warn : p.inkMuted, fontWeight: 500, fontFamily: TYPE.num }}>
                {n.likes > 999 ? (n.likes / 1000).toFixed(1) + 'k' : n.likes}
              </span>
            </div>
          </div>
        </div>
      </div>
    );
  };

  return (
    <div style={{ width: '100%', height: '100%', background: p.bg, position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color={p.ink} />

      {/* 顶部 Tab + 搜索 */}
      <div style={{
        padding: '6px 16px 10px',
        display: 'flex', alignItems: 'center', gap: 12,
      }}>
        <div style={{ display: 'flex', gap: 18, flex: 1 }}>
          {tabs.map(t => (
            <div key={t.id} onClick={() => setTab(t.id)} style={{
              position: 'relative', padding: '4px 2px', cursor: 'pointer',
              fontSize: tab === t.id ? 18 : 15,
              fontWeight: tab === t.id ? 700 : 500,
              color: tab === t.id ? p.ink : p.inkMuted,
              transition: 'all .2s',
            }}>
              {t.label}
              {tab === t.id && (
                <div style={{ position: 'absolute', bottom: -2, left: '50%', transform: 'translateX(-50%)', width: 16, height: 3, borderRadius: 2, background: p.primary }} />
              )}
            </div>
          ))}
        </div>
        <div style={{
          width: 36, height: 36, borderRadius: 18,
          background: p.surface,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          boxShadow: '0 2px 6px rgba(0,0,0,0.04)',
        }}>
          <SearchIcon size={18} color={p.ink} />
        </div>
      </div>

      {/* 话题胶囊 */}
      <div style={{
        display: 'flex', gap: 8, padding: '0 16px 10px',
        overflowX: 'auto', scrollbarWidth: 'none',
      }}>
        {['🔥 热榜', '🐕 狗狗', '🐈 猫咪', '🥘 爱宠食谱', '🏥 宠物医院', '✂️ 美容'].map((c, i) => (
          <div key={i} style={{
            padding: '6px 12px', borderRadius: 14,
            background: i === 0 ? p.primary : p.surface,
            color: i === 0 ? '#fff' : p.inkSoft,
            fontSize: 12, fontWeight: 500, whiteSpace: 'nowrap',
            boxShadow: i === 0 ? `0 4px 10px ${p.primary}44` : '0 1px 3px rgba(0,0,0,0.03)',
          }}>{c}</div>
        ))}
      </div>

      {/* 瀑布流 */}
      <div style={{
        position: 'absolute', top: 128, left: 0, right: 0, bottom: 88,
        overflow: 'auto', padding: '0 10px',
      }}>
        <div style={{ display: 'flex', gap: 8 }}>
          <div style={{ flex: 1 }}>{col1.map(renderCard)}</div>
          <div style={{ flex: 1 }}>{col2.map(renderCard)}</div>
        </div>
        <div style={{ textAlign: 'center', padding: '12px 0 20px', color: p.inkFaint, fontSize: 10 }}>
          — 到底了，歇会儿吧 🐾 —
        </div>
      </div>

      <AppTabBar active="home" palette={p} radius={radius} />
    </div>
  );
}

// ═══════════════════════════════════════════════════════════
// 4. NOTE DETAIL — 笔记详情（图文 + 评论）
// ═══════════════════════════════════════════════════════════
const COMMENTS = [
  { id: 1, variant: 'cat', user: '奶油小糕', text: '太萌了！！我家的也是这样，每次洗完澡都变成一团毛球 😂', likes: 42, time: '2小时前' },
  { id: 2, variant: 'puppy', user: 'Tommy粑粑', text: '求推荐洗护产品～我家柴总是觉得毛扎扎的', likes: 18, time: '1小时前' },
  { id: 3, variant: 'mochi', user: '毛绒绒收藏家', text: '这个表情绝了！保存了保存了', likes: 67, time: '30分钟前' },
];

function NoteDetailScreen({ palette, radius }) {
  const p = palette;
  const [liked, setLiked] = React.useState(true);
  const [saved, setSaved] = React.useState(false);

  return (
    <div style={{ width: '100%', height: '100%', background: p.bg, position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color={p.ink} />

      {/* 顶部导航 */}
      <div style={{
        position: 'absolute', top: 40, left: 0, right: 0, height: 52,
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        padding: '0 14px', zIndex: 10,
      }}>
        <div style={{
          width: 36, height: 36, borderRadius: 18,
          background: 'rgba(255,255,255,0.9)', backdropFilter: 'blur(16px)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          boxShadow: '0 2px 8px rgba(0,0,0,0.06)',
        }}>
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke={p.ink} strokeWidth="2" strokeLinecap="round">
            <path d="M10 3L5 8l5 5" />
          </svg>
        </div>
        <div style={{
          display: 'flex', alignItems: 'center', gap: 8,
          background: 'rgba(255,255,255,0.9)', backdropFilter: 'blur(16px)',
          padding: '4px 12px 4px 4px', borderRadius: 20,
          boxShadow: '0 2px 8px rgba(0,0,0,0.06)',
        }}>
          <PetAvatar variant="dog" size={28} />
          <span style={{ fontSize: 13, fontWeight: 600, color: p.ink }}>汤圆麻麻</span>
          <button style={{
            background: p.primary, color: '#fff', border: 'none',
            padding: '4px 10px', borderRadius: 12, fontSize: 11, fontWeight: 600,
          }}>+ 关注</button>
        </div>
        <div style={{
          width: 36, height: 36, borderRadius: 18,
          background: 'rgba(255,255,255,0.9)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
        }}>
          <svg width="18" height="4" viewBox="0 0 18 4" fill={p.ink}>
            <circle cx="2" cy="2" r="1.6" /><circle cx="9" cy="2" r="1.6" /><circle cx="16" cy="2" r="1.6" />
          </svg>
        </div>
      </div>

      {/* 滚动内容 */}
      <div style={{
        position: 'absolute', top: 38, left: 0, right: 0, bottom: 64,
        overflow: 'auto',
      }}>
        {/* 大图 */}
        <div style={{ width: '100%', height: 360, position: 'relative' }}>
          <PetPlaceholder w={390} h={360} palette={p} seed={1} variant="dog" />
          {/* 图片页码 */}
          <div style={{
            position: 'absolute', bottom: 12, right: 12,
            padding: '3px 10px', borderRadius: 10,
            background: 'rgba(0,0,0,0.45)', color: '#fff', fontSize: 11, fontWeight: 600,
            fontFamily: TYPE.num,
          }}>1 / 4</div>
          {/* 指示点 */}
          <div style={{
            position: 'absolute', bottom: 12, left: '50%', transform: 'translateX(-50%)',
            display: 'flex', gap: 4,
          }}>
            {[0, 1, 2, 3].map(i => (
              <div key={i} style={{
                width: i === 0 ? 16 : 5, height: 5, borderRadius: 3,
                background: i === 0 ? '#fff' : 'rgba(255,255,255,0.55)',
              }} />
            ))}
          </div>
        </div>

        {/* 标题 + 正文 */}
        <div style={{ padding: '16px 18px' }}>
          <div style={{ fontSize: 19, fontWeight: 700, color: p.ink, lineHeight: 1.4, marginBottom: 10 }}>
            今天的柴柴超乖的！洗完澡变成一颗毛球 🐕
          </div>
          <div style={{ fontSize: 14, color: p.inkSoft, lineHeight: 1.75, marginBottom: 12 }}>
            记录一下汤圆每周的洗澡日常～这次用了新的沐浴露，洗完之后毛特别柔软好摸。吹干之后整个变成了一个大毛球，傻乎乎地坐在那里，忍不住多拍了几张。
            <br /><br />
            有推荐的狗狗沐浴露吗？最好是香味淡一点的，汤圆对味道比较敏感。
          </div>
          {/* 话题 */}
          <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap', marginBottom: 14 }}>
            {['#金毛日记', '#洗澡日常', '#毛绒绒'].map((t, i) => (
              <div key={i} style={{
                padding: '3px 9px', borderRadius: 10,
                background: p.primarySoft, color: p.primaryInk,
                fontSize: 11, fontWeight: 500,
              }}>{t}</div>
            ))}
          </div>
          <div style={{ fontSize: 11, color: p.inkMuted }}>
            编辑于 2 小时前 · 来自 上海
          </div>
        </div>

        {/* 分隔 */}
        <div style={{ height: 8, background: p.surfaceDim, margin: '4px 0' }} />

        {/* 评论区 */}
        <div style={{ padding: '14px 18px 20px' }}>
          <div style={{ fontSize: 14, fontWeight: 700, color: p.ink, marginBottom: 12 }}>
            共 128 条评论
          </div>
          {COMMENTS.map(c => (
            <div key={c.id} style={{ display: 'flex', gap: 10, marginBottom: 16 }}>
              <PetAvatar variant={c.variant} size={34} />
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontSize: 12, color: p.inkMuted, marginBottom: 3 }}>{c.user}</div>
                <div style={{ fontSize: 13, color: p.ink, lineHeight: 1.5, marginBottom: 6 }}>{c.text}</div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 14, fontSize: 11, color: p.inkMuted }}>
                  <span>{c.time}</span>
                  <span>回复</span>
                  <div style={{ marginLeft: 'auto', display: 'flex', alignItems: 'center', gap: 3 }}>
                    <HeartIcon size={12} color={p.inkMuted} />
                    <span style={{ fontFamily: TYPE.num }}>{c.likes}</span>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* 底部操作栏 */}
      <div style={{
        position: 'absolute', bottom: 0, left: 0, right: 0, height: 62,
        background: 'rgba(255,255,255,0.95)', backdropFilter: 'blur(20px)',
        borderTop: `0.5px solid ${p.divider}`,
        display: 'flex', alignItems: 'center', padding: '0 12px', gap: 10,
      }}>
        <div style={{
          flex: 1, height: 38, borderRadius: 19,
          background: p.surfaceDim,
          display: 'flex', alignItems: 'center', padding: '0 14px',
          fontSize: 12, color: p.inkMuted,
        }}>
          说点什么吧...
        </div>
        <div onClick={() => setLiked(l => !l)} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 1, width: 40, cursor: 'pointer' }}>
          <HeartIcon size={22} color={liked ? p.warn : p.inkSoft} filled={liked} />
          <span style={{ fontSize: 9, color: liked ? p.warn : p.inkMuted, fontWeight: 600, fontFamily: TYPE.num }}>2342</span>
        </div>
        <div onClick={() => setSaved(s => !s)} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 1, width: 40, cursor: 'pointer' }}>
          <BookmarkIcon size={22} color={saved ? p.accent : p.inkSoft} filled={saved} />
          <span style={{ fontSize: 9, color: saved ? p.accent : p.inkMuted, fontWeight: 600, fontFamily: TYPE.num }}>{saved ? 457 : 456}</span>
        </div>
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 1, width: 40 }}>
          <ChatIcon size={22} color={p.inkSoft} />
          <span style={{ fontSize: 9, color: p.inkMuted, fontWeight: 600, fontFamily: TYPE.num }}>128</span>
        </div>
      </div>
    </div>
  );
}

Object.assign(window, { HomeScreen, NoteDetailScreen });
