// screens-part7.jsx — 个人中心生态：Settings / EditProfile / Follow / Notify / Address / Coupons

// 通用设置行
function SettingRow({ palette, icon, label, value, badge, danger, noArrow }) {
  const p = palette;
  return (
    <div style={{
      display: 'flex', alignItems: 'center', padding: '13px 14px', gap: 12,
    }}>
      {icon && (
        <div style={{
          width: 28, height: 28, borderRadius: 8,
          background: danger ? p.warn + '22' : p.primarySoft,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          fontSize: 15,
        }}>{icon}</div>
      )}
      <span style={{ flex: 1, fontSize: 13, color: danger ? p.warn : p.ink, fontWeight: 500 }}>{label}</span>
      {badge && <div style={{
        minWidth: 16, height: 16, borderRadius: 8, padding: '0 5px',
        background: p.warn, color: '#fff', fontSize: 9, fontWeight: 700,
        display: 'flex', alignItems: 'center', justifyContent: 'center',
      }}>{badge}</div>}
      {value && <span style={{ fontSize: 12, color: p.inkMuted }}>{value}</span>}
      {!noArrow && <svg width="6" height="10" viewBox="0 0 6 10" fill="none" stroke={p.inkFaint} strokeWidth="1.5" strokeLinecap="round">
        <path d="M1 1l4 4-4 4" />
      </svg>}
    </div>
  );
}

function SettingsScreen({ palette, radius }) {
  const p = palette;
  const [darkMode, setDarkMode] = React.useState(false);

  const Group = ({ children, title }) => {
    const items = React.Children.toArray(children);
    return (
      <>
        {title && <div style={{ padding: '14px 18px 6px', fontSize: 11, color: p.inkMuted, fontWeight: 600 }}>{title}</div>}
        <div style={{ margin: '0 14px', background: p.surface, borderRadius: radius, overflow: 'hidden' }}>
          {items.map((c, i) => (
            <React.Fragment key={i}>
              {c}
              {i < items.length - 1 && <div style={{ height: 0.5, background: p.divider, marginLeft: 54 }} />}
            </React.Fragment>
          ))}
        </div>
      </>
    );
  };

  return (
    <div style={{ width: '100%', height: '100%', background: p.bg, position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color={p.ink} />
      <TopBar palette={p} title="设置" />

      <div style={{ position: 'absolute', top: 92, left: 0, right: 0, bottom: 0, overflow: 'auto', paddingBottom: 30 }}>
        {/* 账号卡 */}
        <div style={{
          margin: '8px 14px 0', padding: '14px', borderRadius: radius,
          background: `linear-gradient(135deg, ${p.primarySoft} 0%, ${p.surface} 100%)`,
          display: 'flex', alignItems: 'center', gap: 12,
          position: 'relative', overflow: 'hidden',
        }}>
          <div style={{ position: 'absolute', top: -10, right: -10, opacity: 0.15 }}>
            <PawIcon size={56} color={p.primaryInk} />
          </div>
          <PetAvatar variant="dog" size={48} />
          <div style={{ flex: 1 }}>
            <div style={{ fontSize: 14, fontWeight: 800, color: p.ink }}>汤圆家日记</div>
            <div style={{ fontSize: 10, color: p.inkMuted, marginTop: 2 }}>爱宠号: TANGYUAN_2023</div>
          </div>
          <div style={{
            padding: '4px 10px', borderRadius: 10,
            background: p.primary, color: '#fff',
            fontSize: 10, fontWeight: 700,
          }}>⭐ VIP1</div>
        </div>

        <Group title="账号">
          <SettingRow palette={p} icon="👤" label="个人资料" value="已完善" />
          <SettingRow palette={p} icon="🔒" label="账号与安全" />
          <SettingRow palette={p} icon="🐾" label="我的宠物" value="2 只" />
          <SettingRow palette={p} icon="📍" label="收货地址" value="3 个地址" />
        </Group>

        <Group title="通用">
          <SettingRow palette={p} icon="🔔" label="消息通知" value="已开启" />
          <SettingRow palette={p} icon="🌓" label="深色模式" value={
            <div onClick={() => setDarkMode(!darkMode)} style={{
              width: 34, height: 20, borderRadius: 10,
              background: darkMode ? p.primary : p.inkFaint, position: 'relative',
              transition: 'all .2s',
            }}>
              <div style={{
                position: 'absolute', top: 2, left: darkMode ? 16 : 2,
                width: 16, height: 16, borderRadius: 8, background: '#fff',
                transition: 'all .2s',
                boxShadow: '0 1px 3px rgba(0,0,0,0.2)',
              }} />
            </div>
          } noArrow />
          <SettingRow palette={p} icon="🌐" label="语言" value="简体中文" />
          <SettingRow palette={p} icon="🗄" label="清理缓存" value="328 MB" />
        </Group>

        <Group title="隐私与安全">
          <SettingRow palette={p} icon="🛡" label="隐私设置" />
          <SettingRow palette={p} icon="🚫" label="黑名单" value="2 人" />
          <SettingRow palette={p} icon="📋" label="第三方信息共享" />
        </Group>

        <Group title="关于">
          <SettingRow palette={p} icon="💬" label="帮助与反馈" badge={1} />
          <SettingRow palette={p} icon="⭐" label="给爱宠社评分" />
          <SettingRow palette={p} icon="ℹ" label="关于" value="v3.2.1" />
        </Group>

        <div style={{ margin: '20px 14px 0', background: p.surface, borderRadius: radius, overflow: 'hidden' }}>
          <SettingRow palette={p} label="退出登录" danger noArrow />
        </div>

        <div style={{ textAlign: 'center', padding: '20px 0', fontSize: 10, color: p.inkFaint }}>
          爱宠社 · 让每只毛孩子都被温柔以待 🐾
        </div>
      </div>
    </div>
  );
}

function EditProfileScreen({ palette, radius }) {
  const p = palette;
  return (
    <div style={{ width: '100%', height: '100%', background: p.bg, position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color={p.ink} />
      <TopBar palette={p} title="编辑资料"
        right={<span style={{ fontSize: 13, color: p.primary, fontWeight: 700 }}>保存</span>} />

      <div style={{ position: 'absolute', top: 92, left: 0, right: 0, bottom: 0, overflow: 'auto', paddingBottom: 30 }}>
        {/* 头像 */}
        <div style={{ padding: '20px 0', textAlign: 'center', background: p.surface, marginBottom: 10 }}>
          <div style={{ display: 'inline-block', position: 'relative' }}>
            <div style={{
              width: 84, height: 84, borderRadius: 42, overflow: 'hidden',
              border: `3px solid ${p.surface}`,
              boxShadow: `0 6px 18px ${p.ink}15`,
            }}>
              <PetAvatar variant="dog" size={78} bg={p.primarySoft} />
            </div>
            <div style={{
              position: 'absolute', bottom: 0, right: 0,
              width: 28, height: 28, borderRadius: 14,
              background: p.primary, color: '#fff',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              border: `2px solid ${p.surface}`,
            }}>
              <CameraIcon size={14} color="#fff" />
            </div>
          </div>
          <div style={{ fontSize: 11, color: p.primary, marginTop: 8, fontWeight: 600 }}>更换头像</div>
        </div>

        {/* 资料项 */}
        <div style={{ background: p.surface, margin: '0 14px', borderRadius: radius, overflow: 'hidden' }}>
          {[
            { label: '昵称', value: '汤圆家日记', hint: '2 / 20' },
            { label: '爱宠号', value: 'TANGYUAN_2023', lock: true },
            { label: '性别', value: '女 ♀' },
            { label: '生日', value: '2000-03-14' },
            { label: '地区', value: '上海 · 徐汇区' },
          ].map((r, i, arr) => (
            <div key={i} style={{
              padding: '14px', display: 'flex', alignItems: 'center', gap: 12,
              borderBottom: i < arr.length - 1 ? `0.5px solid ${p.divider}` : 'none',
            }}>
              <span style={{ width: 60, fontSize: 13, color: p.inkSoft }}>{r.label}</span>
              <span style={{ flex: 1, fontSize: 13, color: r.lock ? p.inkFaint : p.ink, fontWeight: r.lock ? 400 : 500 }}>{r.value}</span>
              {r.hint && <span style={{ fontSize: 10, color: p.inkFaint }}>{r.hint}</span>}
              {r.lock && <span style={{ fontSize: 10, color: p.inkFaint }}>仅可修改一次</span>}
              {!r.lock && <svg width="6" height="10" viewBox="0 0 6 10" fill="none" stroke={p.inkFaint} strokeWidth="1.5" strokeLinecap="round"><path d="M1 1l4 4-4 4" /></svg>}
            </div>
          ))}
        </div>

        {/* 简介 */}
        <div style={{ background: p.surface, margin: '10px 14px 0', borderRadius: radius, padding: 14 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
            <span style={{ fontSize: 13, color: p.inkSoft }}>个人简介</span>
            <span style={{ fontSize: 10, color: p.inkFaint }}>38 / 100</span>
          </div>
          <div style={{ fontSize: 13, color: p.ink, lineHeight: 1.5 }}>
            🐾 金毛汤圆 & 布偶奶冻的日常 | 📍 上海 | 分享养宠小知识
          </div>
        </div>

        {/* 标签 */}
        <div style={{ background: p.surface, margin: '10px 14px 0', borderRadius: radius, padding: 14 }}>
          <div style={{ fontSize: 13, color: p.inkSoft, marginBottom: 10 }}>个人标签</div>
          <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
            <Tag palette={p} tone="soft">🐕 金毛控 ×</Tag>
            <Tag palette={p} tone="soft">📷 爱摄影 ×</Tag>
            <Tag palette={p} tone="soft">🏠 上海 ×</Tag>
            <Tag palette={p} tone="ghost">＋ 添加</Tag>
          </div>
        </div>

        {/* 主页背景 */}
        <div style={{ background: p.surface, margin: '10px 14px 0', borderRadius: radius, padding: 14 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
            <span style={{ fontSize: 13, color: p.inkSoft }}>主页背景</span>
            <span style={{ fontSize: 11, color: p.primary }}>更换 ›</span>
          </div>
          <div style={{
            height: 70, borderRadius: 10, overflow: 'hidden',
            background: `linear-gradient(135deg, ${p.primarySoft}, ${p.surfaceDim})`,
            position: 'relative',
          }}>
            <div style={{ position: 'absolute', top: 14, left: 14, opacity: 0.3 }}>
              <PawIcon size={30} color={p.primaryInk} />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

// 关注/粉丝列表
function FollowListScreen({ palette, radius }) {
  const p = palette;
  const [tab, setTab] = React.useState('fans');
  const users = [
    { name: '柴柴麻麻', avatar: 'fluff', sub: '上海 · 柴犬 · 3只', mutual: true, followed: true },
    { name: '奶油猫铲屎官', avatar: 'cat', sub: '📍 杭州 · 英短 2只', mutual: false, followed: false },
    { name: '金毛百科全书', avatar: 'dog', sub: '专业级养狗博主 · 28万粉', verified: true, followed: true },
    { name: '布偶小甜饼', avatar: 'mochi', sub: '粉丝 5.8w · 布偶 × 蓝猫 🎀', followed: false },
    { name: '小泰迪豆豆', avatar: 'puppy', sub: '新朋友 · 刚关注了你', followed: false, isNew: true },
    { name: '养宠大作战', avatar: 'fluff', sub: '每日养宠知识更新', followed: true },
  ];

  return (
    <div style={{ width: '100%', height: '100%', background: p.bg, position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color={p.ink} />
      <TopBar palette={p} title="汤圆家日记" subtitle="@TANGYUAN_2023" />

      {/* Tabs */}
      <div style={{
        position: 'absolute', top: 92, left: 0, right: 0, height: 44,
        background: p.bg,
        display: 'flex', padding: '0 18px', gap: 20,
        borderBottom: `0.5px solid ${p.divider}`, zIndex: 10,
      }}>
        {[
          { id: 'fans', label: '粉丝', num: '8,432' },
          { id: 'follow', label: '关注', num: '1,248' },
        ].map(t => (
          <div key={t.id} onClick={() => setTab(t.id)} style={{
            padding: '10px 0',
            borderBottom: tab === t.id ? `2px solid ${p.primary}` : '2px solid transparent',
            marginBottom: -0.5,
          }}>
            <span style={{ fontSize: 14, fontWeight: tab === t.id ? 800 : 500, color: tab === t.id ? p.ink : p.inkMuted }}>{t.label}</span>
            <span style={{ fontSize: 11, color: p.inkFaint, marginLeft: 4, fontFamily: TYPE.num }}>{t.num}</span>
          </div>
        ))}
      </div>

      {/* Search */}
      <div style={{ position: 'absolute', top: 142, left: 14, right: 14, zIndex: 5 }}>
        <div style={{
          height: 34, borderRadius: 17, background: p.surface,
          display: 'flex', alignItems: 'center', padding: '0 12px', gap: 6,
        }}>
          <SearchIcon size={14} color={p.inkMuted} />
          <span style={{ fontSize: 11, color: p.inkMuted }}>搜索{tab === 'fans' ? '粉丝' : '关注'}</span>
        </div>
      </div>

      <div style={{ position: 'absolute', top: 188, left: 0, right: 0, bottom: 0, overflow: 'auto', paddingBottom: 30 }}>
        {users.map((u, i) => (
          <div key={i} style={{
            display: 'flex', alignItems: 'center', gap: 12, padding: '10px 18px',
            background: u.isNew ? p.primarySoft + '40' : 'transparent',
          }}>
            <PetAvatar variant={u.avatar} size={42} />
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 5 }}>
                <span style={{ fontSize: 13, fontWeight: 700, color: p.ink }}>{u.name}</span>
                {u.verified && <Tag palette={p} tone="primary" size="xs">✓ 认证</Tag>}
                {u.mutual && <Tag palette={p} tone="ghost" size="xs">互关</Tag>}
                {u.isNew && <Tag palette={p} tone="warn" size="xs">新</Tag>}
              </div>
              <div style={{ fontSize: 10, color: p.inkMuted, marginTop: 2, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                {u.sub}
              </div>
            </div>
            <button style={{
              padding: '5px 14px', borderRadius: 14, border: 'none',
              background: u.followed ? p.surface : `linear-gradient(135deg, ${p.primary}, ${p.primaryInk})`,
              color: u.followed ? p.inkSoft : '#fff',
              fontSize: 11, fontWeight: 700,
              boxShadow: u.followed ? `0 0 0 1px ${p.divider} inset` : `0 3px 8px ${p.primary}40`,
              fontFamily: TYPE.body,
            }}>{u.followed ? '已关注' : u.mutual ? '回关' : '+ 关注'}</button>
          </div>
        ))}
      </div>
    </div>
  );
}

// 通知详情（赞和收藏列表）
function NotificationsScreen({ palette, radius }) {
  const p = palette;
  const items = [
    { user: '柴柴麻麻', avatar: 'fluff', action: '赞了你的笔记', target: '《汤圆的第 28 次洗澡日记》', time: '2 分钟前', thumb: 0, icon: '❤️' },
    { user: '布偶小甜饼', avatar: 'cat', action: '收藏了你的笔记', target: '《金毛毕业啦》', time: '5 分钟前', thumb: 1, icon: '⭐' },
    { user: '小泰迪豆豆', avatar: 'puppy', action: '赞了你的评论', target: '「肉干真的好好吃～」', time: '20 分钟前', thumb: null, icon: '❤️' },
    { user: '养宠小能手', avatar: 'dog', action: '收藏了你的笔记', target: '《新买的蓬蓬窝开箱》', time: '1 小时前', thumb: 3, icon: '⭐' },
    { user: '金毛百科', avatar: 'dog', action: '和 8 人 赞了你的笔记', target: '《秋日拾光·公园打滚》', time: '昨天 · 32 位', thumb: 2, icon: '❤️', multi: true },
    { user: '喵喵喵先生', avatar: 'cat', action: '赞了你的笔记', target: '《给汤圆剃毛了》', time: '昨天', thumb: 0, icon: '❤️' },
  ];

  return (
    <div style={{ width: '100%', height: '100%', background: p.bg, position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color={p.ink} />
      <TopBar palette={p} title="赞和收藏" subtitle="共 12 条新互动" />

      <div style={{ position: 'absolute', top: 96, left: 0, right: 0, bottom: 0, overflow: 'auto', paddingBottom: 30 }}>
        {/* 今天 */}
        <div style={{ padding: '10px 18px 6px', fontSize: 11, color: p.inkMuted, fontWeight: 600 }}>今天</div>
        <div style={{ background: p.surface, margin: '0 14px', borderRadius: radius, overflow: 'hidden' }}>
          {items.slice(0, 4).map((it, i, arr) => (
            <div key={i} style={{
              display: 'flex', alignItems: 'center', gap: 12, padding: '12px 14px',
              borderBottom: i < arr.length - 1 ? `0.5px solid ${p.divider}` : 'none',
            }}>
              <div style={{ position: 'relative' }}>
                <PetAvatar variant={it.avatar} size={40} />
                <div style={{
                  position: 'absolute', bottom: -2, right: -2,
                  width: 18, height: 18, borderRadius: 9,
                  background: it.icon === '❤️' ? p.warn : p.primary,
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  fontSize: 9,
                  border: `2px solid ${p.surface}`,
                }}>{it.icon}</div>
              </div>
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontSize: 12, color: p.ink, lineHeight: 1.4 }}>
                  <span style={{ fontWeight: 700 }}>{it.user}</span>
                  <span style={{ color: p.inkSoft }}> {it.action}</span>
                </div>
                <div style={{ fontSize: 11, color: p.inkMuted, marginTop: 2, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                  {it.target}
                </div>
                <div style={{ fontSize: 10, color: p.inkFaint, marginTop: 3 }}>{it.time}</div>
              </div>
              {it.thumb !== null ? (
                <div style={{ width: 44, height: 44, borderRadius: 8, overflow: 'hidden', flexShrink: 0 }}>
                  <PetPlaceholder w={44} h={44} palette={p} seed={it.thumb} variant={['dog', 'puppy', 'cat', 'fluff'][it.thumb]} />
                </div>
              ) : (
                <div style={{ width: 44, height: 44, borderRadius: 8, background: p.surfaceDim, flexShrink: 0, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 16 }}>💬</div>
              )}
            </div>
          ))}
        </div>

        <div style={{ padding: '16px 18px 6px', fontSize: 11, color: p.inkMuted, fontWeight: 600 }}>昨天</div>
        <div style={{ background: p.surface, margin: '0 14px', borderRadius: radius, overflow: 'hidden' }}>
          {items.slice(4).map((it, i, arr) => (
            <div key={i} style={{
              display: 'flex', alignItems: 'center', gap: 12, padding: '12px 14px',
              borderBottom: i < arr.length - 1 ? `0.5px solid ${p.divider}` : 'none',
            }}>
              <div style={{ position: 'relative' }}>
                {it.multi ? (
                  <div style={{ position: 'relative', width: 40, height: 40 }}>
                    <div style={{ position: 'absolute', top: 0, left: 0 }}>
                      <PetAvatar variant={it.avatar} size={30} />
                    </div>
                    <div style={{ position: 'absolute', bottom: 0, right: 0, border: `2px solid ${p.surface}`, borderRadius: '50%' }}>
                      <PetAvatar variant="cat" size={22} />
                    </div>
                  </div>
                ) : (
                  <PetAvatar variant={it.avatar} size={40} />
                )}
                <div style={{
                  position: 'absolute', bottom: -2, right: -2,
                  width: 18, height: 18, borderRadius: 9,
                  background: it.icon === '❤️' ? p.warn : p.primary,
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  fontSize: 9, border: `2px solid ${p.surface}`,
                }}>{it.icon}</div>
              </div>
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontSize: 12, color: p.ink, lineHeight: 1.4 }}>
                  <span style={{ fontWeight: 700 }}>{it.user}</span>
                  <span style={{ color: p.inkSoft }}> {it.action}</span>
                </div>
                <div style={{ fontSize: 11, color: p.inkMuted, marginTop: 2, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                  {it.target}
                </div>
                <div style={{ fontSize: 10, color: p.inkFaint, marginTop: 3 }}>{it.time}</div>
              </div>
              {it.thumb !== null && (
                <div style={{ width: 44, height: 44, borderRadius: 8, overflow: 'hidden', flexShrink: 0 }}>
                  <PetPlaceholder w={44} h={44} palette={p} seed={it.thumb} variant={['dog', 'puppy', 'cat', 'fluff'][it.thumb]} />
                </div>
              )}
            </div>
          ))}
        </div>

        <div style={{ textAlign: 'center', padding: '20px 0', fontSize: 10, color: p.inkFaint }}>
          · 没有更多了 ·
        </div>
      </div>
    </div>
  );
}

function AddressBookScreen({ palette, radius }) {
  const p = palette;
  const addrs = [
    { name: '汤圆妈', phone: '138 **** 2341', addr: '上海市 徐汇区 漕溪北路 333 号 8 栋 502 室', tag: '家', default: true },
    { name: '汤圆妈', phone: '138 **** 2341', addr: '上海市 浦东新区 张江高科 博云路 88 号 A 座 16F', tag: '公司' },
    { name: '汤圆爸', phone: '139 **** 7788', addr: '杭州市 西湖区 文三路 259 号 华星时代广场 2 号楼 9F', tag: '父母家' },
  ];

  return (
    <div style={{ width: '100%', height: '100%', background: p.bg, position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color={p.ink} />
      <TopBar palette={p} title="收货地址" />

      <div style={{ position: 'absolute', top: 92, left: 0, right: 0, bottom: 80, overflow: 'auto', padding: '8px 14px' }}>
        {addrs.map((a, i) => (
          <div key={i} style={{
            background: p.surface, borderRadius: radius,
            padding: 14, marginBottom: 10,
            position: 'relative', overflow: 'hidden',
          }}>
            {a.default && (
              <div style={{
                position: 'absolute', top: 0, right: 0,
                background: `linear-gradient(135deg, ${p.primary}, ${p.primaryInk})`,
                color: '#fff', fontSize: 9, fontWeight: 700,
                padding: '3px 10px', borderRadius: '0 0 0 10px',
              }}>默认</div>
            )}
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
              <span style={{ fontSize: 14, fontWeight: 700, color: p.ink }}>{a.name}</span>
              <span style={{ fontSize: 12, color: p.inkMuted, fontFamily: TYPE.num }}>{a.phone}</span>
              <Tag palette={p} tone="soft" size="xs">{a.tag}</Tag>
            </div>
            <div style={{ fontSize: 12, color: p.inkSoft, lineHeight: 1.5 }}>{a.addr}</div>
            <div style={{
              display: 'flex', alignItems: 'center', gap: 14, marginTop: 10,
              paddingTop: 10, borderTop: `0.5px dashed ${p.divider}`,
            }}>
              <label style={{ display: 'flex', alignItems: 'center', gap: 4, fontSize: 11, color: p.inkSoft }}>
                <div style={{
                  width: 14, height: 14, borderRadius: 7,
                  border: `1.5px solid ${a.default ? p.primary : p.inkFaint}`,
                  background: a.default ? p.primary : 'transparent',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                }}>{a.default && <svg width="8" height="8" viewBox="0 0 8 8" fill="none" stroke="#fff" strokeWidth="1.8" strokeLinecap="round"><path d="M1.5 4l2 2 3-4" /></svg>}</div>
                设为默认
              </label>
              <div style={{ marginLeft: 'auto', display: 'flex', gap: 14 }}>
                <span style={{ fontSize: 11, color: p.inkSoft }}>✏ 编辑</span>
                <span style={{ fontSize: 11, color: p.warn }}>🗑 删除</span>
              </div>
            </div>
          </div>
        ))}
      </div>

      <div style={{
        position: 'absolute', bottom: 0, left: 0, right: 0,
        padding: '10px 14px 22px', background: p.bg,
      }}>
        <button style={{
          width: '100%', height: 46, borderRadius: 23, border: 'none',
          background: `linear-gradient(135deg, ${p.primary}, ${p.primaryInk})`,
          color: '#fff', fontSize: 14, fontWeight: 700,
          boxShadow: `0 6px 16px ${p.primary}50`,
          fontFamily: TYPE.body,
        }}>＋ 新增收货地址</button>
      </div>
    </div>
  );
}

function CouponsScreen({ palette, radius }) {
  const p = palette;
  const [tab, setTab] = React.useState('unused');
  const coupons = [
    { amt: '30', min: 199, title: '全场通用券', desc: '除主粮外全场可用', expire: '11/30 23:59', hot: true },
    { amt: '50', min: 299, title: '主粮专享', desc: '限渴望/爱肯拿/巅峰品牌', expire: '12/10', hot: true },
    { amt: '15', min: 89, title: '新人礼包', desc: '首次下单专用', expire: '12/31' },
    { amt: '20', min: 129, title: '宠物玩具券', desc: '玩具/磨牙类目', expire: '11/25 · 即将过期', urgent: true },
    { amt: '8.5 折', min: 0, title: '会员专属', desc: 'VIP 专享 8.5 折', expire: '长期有效', pct: true },
  ];

  return (
    <div style={{ width: '100%', height: '100%', background: p.bg, position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color={p.ink} />
      <TopBar palette={p} title="我的券包"
        right={<span style={{ fontSize: 11, color: p.inkSoft }}>使用说明</span>} />

      {/* Tabs */}
      <div style={{
        position: 'absolute', top: 92, left: 0, right: 0, height: 40,
        display: 'flex', padding: '0 14px', gap: 6,
      }}>
        {[
          { id: 'unused', label: '未使用 5' },
          { id: 'used', label: '已使用' },
          { id: 'expired', label: '已过期' },
        ].map(t => (
          <div key={t.id} onClick={() => setTab(t.id)} style={{
            flex: 1, textAlign: 'center', padding: '8px 0',
            borderRadius: 14,
            background: tab === t.id ? p.surface : 'transparent',
            fontSize: 12, fontWeight: tab === t.id ? 700 : 500,
            color: tab === t.id ? p.ink : p.inkMuted,
            boxShadow: tab === t.id ? `0 1px 3px ${p.ink}0a` : 'none',
          }}>{t.label}</div>
        ))}
      </div>

      {/* Scroll */}
      <div style={{ position: 'absolute', top: 140, left: 0, right: 0, bottom: 0, overflow: 'auto', padding: '8px 14px 30px' }}>
        {coupons.map((c, i) => (
          <div key={i} style={{
            marginBottom: 10, position: 'relative',
            borderRadius: radius, overflow: 'hidden',
            background: p.surface,
            boxShadow: `0 2px 8px ${p.ink}0a`,
            display: 'flex',
          }}>
            {/* 左侧金额 */}
            <div style={{
              width: 120, padding: '14px 10px',
              background: c.urgent ? `linear-gradient(135deg, ${p.warn}, #e89874)`
                : `linear-gradient(135deg, ${p.primary}, ${p.primaryInk})`,
              color: '#fff',
              display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center',
              position: 'relative',
            }}>
              <div style={{ display: 'flex', alignItems: 'baseline' }}>
                {!c.pct && <span style={{ fontSize: 13, fontWeight: 700, marginRight: 2 }}>¥</span>}
                <span style={{ fontSize: c.pct ? 22 : 32, fontWeight: 800, fontFamily: TYPE.num, lineHeight: 1 }}>{c.amt}</span>
              </div>
              <div style={{ fontSize: 10, marginTop: 4, opacity: 0.9 }}>
                {c.min > 0 ? `满 ${c.min} 可用` : '无门槛'}
              </div>
              {/* 虚线分割 */}
              <div style={{
                position: 'absolute', right: -6, top: '50%', transform: 'translateY(-50%)',
                width: 12, height: 12, borderRadius: 6, background: p.surface,
              }} />
            </div>

            {/* 右侧信息 */}
            <div style={{ flex: 1, padding: '14px 14px 14px 18px', display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 5 }}>
                <span style={{ fontSize: 14, fontWeight: 700, color: p.ink }}>{c.title}</span>
                {c.hot && <Tag palette={p} tone="warn" size="xs">热门</Tag>}
              </div>
              <div style={{ fontSize: 11, color: p.inkMuted, marginTop: 3 }}>{c.desc}</div>
              <div style={{ fontSize: 10, color: c.urgent ? p.warn : p.inkFaint, marginTop: 6, fontWeight: c.urgent ? 700 : 400 }}>
                ⏰ {c.expire}
              </div>
            </div>

            {/* 去使用按钮 */}
            <div style={{ display: 'flex', alignItems: 'center', paddingRight: 12 }}>
              <button style={{
                padding: '6px 12px', borderRadius: 14, border: `1.5px solid ${p.primary}`,
                background: 'transparent', color: p.primary,
                fontSize: 11, fontWeight: 700,
                fontFamily: TYPE.body,
              }}>去使用</button>
            </div>
          </div>
        ))}

        {/* 领券中心入口 */}
        <div style={{
          background: `linear-gradient(135deg, ${p.primarySoft}, ${p.surfaceDim})`,
          borderRadius: radius, padding: 14, marginTop: 6,
          display: 'flex', alignItems: 'center', gap: 10,
        }}>
          <div style={{ fontSize: 28 }}>🎁</div>
          <div style={{ flex: 1 }}>
            <div style={{ fontSize: 13, fontWeight: 700, color: p.ink }}>领券中心</div>
            <div style={{ fontSize: 10, color: p.inkSoft, marginTop: 2 }}>上百张优惠券 · 每日 9:00 更新</div>
          </div>
          <span style={{ fontSize: 11, color: p.primaryInk, fontWeight: 700 }}>前往 ›</span>
        </div>
      </div>
    </div>
  );
}

Object.assign(window, { SettingsScreen, EditProfileScreen, FollowListScreen, NotificationsScreen, AddressBookScreen, CouponsScreen });
