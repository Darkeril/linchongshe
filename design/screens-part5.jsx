// screens-part5.jsx — Messages / Chat / Profile

function MessagesScreen({ palette, radius }) {
  const p = palette;
  const actions = [
    { icon: '❤️', label: '赞和收藏', count: 12, color: '#E8846E' },
    { icon: '👥', label: '新增粉丝', count: 3, color: '#7BA58E' },
    { icon: '💬', label: '评论@', count: 5, color: '#C97B4A' },
    { icon: '🛍️', label: '订单', count: 0, color: '#8F7260' },
  ];

  const chats = [
    { name: '柴柴麻麻', avatar: 'fluff', msg: '亲，这款肉干汤圆爱吃吗？我家宝也挑嘴', time: '刚刚', unread: 2, online: true },
    { name: '爱宠社官方', avatar: 'mochi', msg: '🎉 你的笔记《汤圆洗澡日记》被推荐到首页啦～', time: '10:23', unread: 1, official: true },
    { name: '布偶猫哥哥', avatar: 'cat', msg: '[图片] 今天阳光真好', time: '昨天', unread: 0 },
    { name: '小奶猫领养中心', avatar: 'puppy', msg: '您申请领养的奶牛猫已安排送达，请准时在家等候～', time: '昨天', unread: 0 },
    { name: '宠物医生 · 李医生', avatar: 'dog', msg: '建议继续观察两天，如果仍不进食再来复诊', time: '周三', unread: 0 },
    { name: '同城遛狗群', avatar: 'fluff', msg: '小王：周六上午 10 点徐家汇公园集合～', time: '周二', unread: 5, group: true },
  ];

  return (
    <div style={{ width: '100%', height: '100%', background: p.bg, position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color={p.ink} />

      {/* 顶部 */}
      <div style={{
        position: 'absolute', top: 46, left: 0, right: 0, height: 52,
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        padding: '0 18px', zIndex: 20,
      }}>
        <span style={{ fontSize: 22, fontWeight: 800, color: p.ink, fontFamily: TYPE.brand }}>消息</span>
        <div style={{ display: 'flex', gap: 10 }}>
          <div style={{ width: 34, height: 34, borderRadius: 17, background: p.surface, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <SearchIcon size={16} color={p.ink} />
          </div>
          <div style={{ width: 34, height: 34, borderRadius: 17, background: p.surface, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke={p.ink} strokeWidth="2" strokeLinecap="round">
              <path d="M12 5v14M5 12h14" />
            </svg>
          </div>
        </div>
      </div>

      {/* 可滚动内容 */}
      <div style={{
        position: 'absolute', top: 98, left: 0, right: 0, bottom: 82, overflow: 'auto',
      }}>
        {/* 互动通知行 */}
        <div style={{ padding: '8px 14px 4px', display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 8 }}>
          {actions.map((a, i) => (
            <div key={i} style={{
              background: p.surface, borderRadius: radius,
              padding: '12px 0 10px',
              display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4,
              position: 'relative',
              boxShadow: `0 2px 6px ${p.ink}08`,
            }}>
              <div style={{
                width: 40, height: 40, borderRadius: 20,
                background: a.color + '22',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                fontSize: 20,
              }}>{a.icon}</div>
              <span style={{ fontSize: 10, color: p.inkSoft, fontWeight: 500 }}>{a.label}</span>
              {a.count > 0 && (
                <div style={{
                  position: 'absolute', top: 6, right: 10,
                  minWidth: 16, height: 16, borderRadius: 8, padding: '0 4px',
                  background: p.warn, color: '#fff', fontSize: 9, fontWeight: 700,
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                }}>{a.count}</div>
              )}
            </div>
          ))}
        </div>

        {/* 聊天分组标题 */}
        <div style={{ padding: '14px 18px 8px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <span style={{ fontSize: 13, fontWeight: 700, color: p.inkSoft }}>聊天 <span style={{ color: p.inkMuted, fontWeight: 500 }}>· {chats.length}</span></span>
          <span style={{ fontSize: 11, color: p.inkMuted }}>勿扰模式 🌙</span>
        </div>

        {/* 聊天列表 */}
        <div style={{ background: p.surface, margin: '0 14px', borderRadius: radius }}>
          {chats.map((c, i) => (
            <div key={i} style={{
              display: 'flex', alignItems: 'center', gap: 11,
              padding: '12px 14px',
              borderBottom: i < chats.length - 1 ? `0.5px solid ${p.divider}` : 'none',
            }}>
              <div style={{ position: 'relative' }}>
                <PetAvatar variant={c.avatar} size={46} />
                {c.online && (
                  <div style={{
                    position: 'absolute', bottom: 0, right: 0,
                    width: 11, height: 11, borderRadius: 6,
                    background: '#7BA58E', border: `2px solid ${p.surface}`,
                  }} />
                )}
                {c.official && (
                  <div style={{
                    position: 'absolute', bottom: -2, right: -2,
                    width: 16, height: 16, borderRadius: 8,
                    background: p.primary, color: '#fff',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    fontSize: 9, fontWeight: 700,
                    border: `2px solid ${p.surface}`,
                  }}>V</div>
                )}
              </div>
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 5 }}>
                  <span style={{ fontSize: 13, fontWeight: 700, color: p.ink }}>{c.name}</span>
                  {c.group && <Tag palette={p} tone="ghost" size="xs">群</Tag>}
                  {c.official && <Tag palette={p} tone="soft" size="xs">官方</Tag>}
                </div>
                <div style={{
                  fontSize: 11, color: p.inkMuted, marginTop: 2,
                  overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
                }}>{c.msg}</div>
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: 4 }}>
                <span style={{ fontSize: 10, color: p.inkFaint }}>{c.time}</span>
                {c.unread > 0 && (
                  <div style={{
                    minWidth: 16, height: 16, borderRadius: 8, padding: '0 5px',
                    background: p.warn, color: '#fff',
                    fontSize: 9, fontWeight: 700,
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                  }}>{c.unread}</div>
                )}
              </div>
            </div>
          ))}
        </div>

        <div style={{ height: 30 }} />
      </div>

      <AppTabBar active="msg" palette={p} radius={radius} />
    </div>
  );
}

// 聊天详情页
function ChatScreen({ palette, radius }) {
  const p = palette;
  const messages = [
    { from: 'them', text: '亲，问下你家汤圆多大啦～', time: '10:23', avatar: 'fluff' },
    { from: 'me', text: '两岁多啦，刚过完生日🎂' },
    { from: 'them', text: '这款肉干他爱吃吗？我家柴柴挑嘴，买啥都不吃[哭]', avatar: 'fluff' },
    { from: 'me', kind: 'image', seed: 3, variant: 'dog' },
    { from: 'me', text: '你看～前两天刚吃完一整盒，鸭肉口味最爱' },
    { from: 'them', text: '哇！看起来好好吃的样子！', avatar: 'fluff' },
    { from: 'them', kind: 'product', name: '手工烘焙肉干礼盒', price: 68, seed: 4, avatar: 'fluff' },
    { from: 'them', text: '就是这款对吧？我下单了～', avatar: 'fluff' },
    { from: 'me', text: '对对对！记得用我的邀请码立减 10 元哦🎁' },
  ];

  return (
    <div style={{ width: '100%', height: '100%', background: p.surfaceDim, position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color={p.ink} />

      {/* 顶部 */}
      <div style={{
        position: 'absolute', top: 38, left: 0, right: 0, height: 54,
        background: p.surface,
        display: 'flex', alignItems: 'center', padding: '0 12px',
        zIndex: 30,
        boxShadow: `0 1px 0 ${p.divider}`,
      }}>
        <div style={{ width: 30, display: 'flex', alignItems: 'center' }}>
          <svg width="10" height="18" viewBox="0 0 10 18" fill="none" stroke={p.ink} strokeWidth="2" strokeLinecap="round">
            <path d="M8 2L2 9l6 7" />
          </svg>
        </div>
        <PetAvatar variant="fluff" size={34} />
        <div style={{ marginLeft: 10, flex: 1 }}>
          <div style={{ fontSize: 14, fontWeight: 700, color: p.ink, display: 'flex', alignItems: 'center', gap: 5 }}>
            柴柴麻麻
            <div style={{ width: 6, height: 6, borderRadius: 3, background: '#7BA58E' }} />
          </div>
          <div style={{ fontSize: 9, color: p.inkMuted }}>在线 · 来自上海</div>
        </div>
        <div style={{ display: 'flex', gap: 14 }}>
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke={p.ink} strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
            <path d="M15 10l6-3v10l-6-3M3 6h12v12H3z" />
          </svg>
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke={p.ink} strokeWidth="1.8" strokeLinecap="round">
            <circle cx="12" cy="12" r="1.5" fill={p.ink} />
            <circle cx="5" cy="12" r="1.5" fill={p.ink} />
            <circle cx="19" cy="12" r="1.5" fill={p.ink} />
          </svg>
        </div>
      </div>

      {/* 消息区 */}
      <div style={{
        position: 'absolute', top: 92, left: 0, right: 0, bottom: 66, overflow: 'auto',
        padding: '14px 10px',
      }}>
        <div style={{
          textAlign: 'center', fontSize: 10, color: p.inkMuted, marginBottom: 14,
          background: `${p.inkFaint}30`, display: 'inline-block',
          padding: '3px 10px', borderRadius: 10,
          position: 'relative', left: '50%', transform: 'translateX(-50%)',
        }}>今天 10:23</div>

        {messages.map((m, i) => {
          const isMe = m.from === 'me';
          const avatar = !isMe && m.avatar && (i === 0 || messages[i - 1].from !== m.from) ? (
            <PetAvatar variant={m.avatar} size={32} />
          ) : !isMe ? <div style={{ width: 32 }} /> : null;

          return (
            <div key={i} style={{
              display: 'flex', alignItems: 'flex-end', gap: 6, marginBottom: 8,
              flexDirection: isMe ? 'row-reverse' : 'row',
            }}>
              {avatar}
              {m.kind === 'image' ? (
                <div style={{ borderRadius: 14, overflow: 'hidden', maxWidth: 170 }}>
                  <PetPlaceholder w={170} h={170} palette={p} seed={m.seed} variant={m.variant} />
                </div>
              ) : m.kind === 'product' ? (
                <div style={{
                  background: p.surface, borderRadius: 14,
                  padding: 8, display: 'flex', gap: 8, width: 200,
                  boxShadow: `0 2px 6px ${p.ink}0a`,
                }}>
                  <div style={{ width: 60, height: 60, borderRadius: 8, overflow: 'hidden', flexShrink: 0 }}>
                    <PetPlaceholder w={60} h={60} palette={p} seed={m.seed} variant="puppy" />
                  </div>
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={{ fontSize: 11, color: p.ink, fontWeight: 600, lineHeight: 1.3, overflow: 'hidden', textOverflow: 'ellipsis', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical' }}>
                      {m.name}
                    </div>
                    <div style={{ fontSize: 14, color: p.primary, fontWeight: 800, marginTop: 4, fontFamily: TYPE.num }}>
                      ¥{m.price}
                    </div>
                  </div>
                </div>
              ) : (
                <div style={{
                  maxWidth: 220, padding: '8px 12px',
                  background: isMe ? `linear-gradient(135deg, ${p.primary}, ${p.primaryInk})` : p.surface,
                  color: isMe ? '#fff' : p.ink,
                  borderRadius: isMe ? '18px 18px 4px 18px' : '4px 18px 18px 18px',
                  fontSize: 13, lineHeight: 1.45,
                  boxShadow: isMe ? `0 4px 12px ${p.primary}30` : `0 1px 3px ${p.ink}0a`,
                }}>
                  {m.text}
                </div>
              )}
            </div>
          );
        })}

        {/* 正在输入 */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginTop: 6 }}>
          <PetAvatar variant="fluff" size={26} />
          <div style={{
            padding: '10px 14px', background: p.surface, borderRadius: '4px 16px 16px 16px',
            display: 'flex', gap: 3,
          }}>
            {[0, 1, 2].map(i => (
              <div key={i} style={{ width: 5, height: 5, borderRadius: 3, background: p.inkFaint }} />
            ))}
          </div>
        </div>
      </div>

      {/* 底部输入 */}
      <div style={{
        position: 'absolute', bottom: 0, left: 0, right: 0,
        background: p.surface,
        borderTop: `0.5px solid ${p.divider}`,
        padding: '8px 12px 24px',
        display: 'flex', alignItems: 'center', gap: 8,
      }}>
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke={p.ink} strokeWidth="1.6" strokeLinecap="round">
          <circle cx="12" cy="12" r="9" />
          <path d="M6 12h12M12 6v12" />
        </svg>
        <div style={{
          flex: 1, height: 36, borderRadius: 18,
          background: p.surfaceDim,
          display: 'flex', alignItems: 'center', padding: '0 14px',
          fontSize: 12, color: p.inkMuted,
        }}>
          说点什么吧...
        </div>
        <span style={{ fontSize: 20 }}>😊</span>
        <div style={{
          width: 34, height: 34, borderRadius: 17,
          background: `linear-gradient(135deg, ${p.primary}, ${p.primaryInk})`,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
        }}>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="#fff">
            <path d="M2 21l20-9L2 3v7l15 2-15 2z" />
          </svg>
        </div>
      </div>
    </div>
  );
}

// Profile 个人主页
function ProfileScreen({ palette, radius }) {
  const p = palette;
  const tabs = [
    { id: 'notes', label: '笔记', count: 28, active: true },
    { id: 'collect', label: '收藏', count: 142 },
    { id: 'like', label: '赞过', count: 86 },
  ];

  const notes = [
    { h: 180, title: '汤圆的第 28 次洗澡日记 🛁', likes: 2341, seed: 0, variant: 'dog' },
    { h: 220, title: '金毛毕业啦～第一次拿到证书', likes: 1823, seed: 1, variant: 'puppy' },
    { h: 160, title: '秋日拾光 · 公园打滚', likes: 567, seed: 2, variant: 'dog' },
    { h: 200, title: '新买的蓬蓬窝 · 开箱实测', likes: 892, seed: 3, variant: 'fluff' },
  ];

  return (
    <div style={{ width: '100%', height: '100%', background: p.bg, position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      {/* 顶部渐变背景 */}
      <div style={{
        position: 'absolute', top: 0, left: 0, right: 0, height: 240,
        background: `linear-gradient(180deg, ${p.primarySoft} 0%, ${p.bg} 100%)`,
      }}>
        {/* paw 装饰 */}
        <div style={{ position: 'absolute', top: 70, right: 20, opacity: 0.18 }}>
          <PawIcon size={60} color={p.primaryInk} />
        </div>
        <div style={{ position: 'absolute', top: 150, left: 20, opacity: 0.12 }}>
          <PawIcon size={34} color={p.primaryInk} />
        </div>
      </div>

      <StatusLine color={p.ink} />

      {/* 顶部按钮 */}
      <div style={{
        position: 'absolute', top: 46, left: 0, right: 0,
        display: 'flex', justifyContent: 'space-between', padding: '0 14px', zIndex: 20,
      }}>
        <div style={{ width: 34, height: 34, borderRadius: 17, background: 'rgba(255,255,255,0.7)', backdropFilter: 'blur(8px)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <SearchIcon size={16} color={p.ink} />
        </div>
        <div style={{ display: 'flex', gap: 8 }}>
          <div style={{ width: 34, height: 34, borderRadius: 17, background: 'rgba(255,255,255,0.7)', backdropFilter: 'blur(8px)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <ShareIcon size={15} color={p.ink} />
          </div>
          <div style={{ width: 34, height: 34, borderRadius: 17, background: 'rgba(255,255,255,0.7)', backdropFilter: 'blur(8px)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke={p.ink} strokeWidth="2" strokeLinecap="round">
              <circle cx="12" cy="5" r="1.5" fill={p.ink} /><circle cx="12" cy="12" r="1.5" fill={p.ink} /><circle cx="12" cy="19" r="1.5" fill={p.ink} />
            </svg>
          </div>
        </div>
      </div>

      {/* 可滚动内容 */}
      <div style={{
        position: 'absolute', top: 0, left: 0, right: 0, bottom: 0,
        overflow: 'auto', paddingTop: 92,
      }}>
        {/* 头像 + 昵称区 */}
        <div style={{ padding: '0 18px', display: 'flex', alignItems: 'center', gap: 14 }}>
          <div style={{
            width: 72, height: 72, borderRadius: 36, overflow: 'hidden',
            border: `3px solid ${p.surface}`,
            boxShadow: `0 4px 12px ${p.ink}15`,
          }}>
            <PetAvatar variant="dog" size={66} bg={p.primarySoft} />
          </div>
          <div style={{ flex: 1 }}>
            <div style={{ fontSize: 18, fontWeight: 800, color: p.ink, fontFamily: TYPE.brand }}>汤圆家日记</div>
            <div style={{ fontSize: 11, color: p.inkMuted, marginTop: 2 }}>
              爱宠号: TANGYUAN_2023
            </div>
          </div>
          <button style={{
            padding: '6px 16px', borderRadius: 16,
            background: p.surface, border: `1.5px solid ${p.primary}`,
            color: p.primary, fontSize: 12, fontWeight: 700,
            fontFamily: TYPE.body,
          }}>编辑资料</button>
        </div>

        {/* 宠物卡（我的毛孩子） */}
        <div style={{ padding: '14px 18px 0', display: 'flex', gap: 8 }}>
          {[
            { name: '汤圆', sub: '金毛 · ♂ · 2岁', v: 'dog' },
            { name: '奶冻', sub: '布偶 · ♀ · 8月', v: 'cat' },
          ].map((pet, i) => (
            <div key={i} style={{
              flex: 1, background: p.surface, borderRadius: radius,
              padding: '8px 10px', display: 'flex', alignItems: 'center', gap: 8,
              boxShadow: `0 2px 6px ${p.ink}08`,
            }}>
              <PetAvatar variant={pet.v} size={30} />
              <div style={{ minWidth: 0 }}>
                <div style={{ fontSize: 12, fontWeight: 700, color: p.ink }}>{pet.name}</div>
                <div style={{ fontSize: 9, color: p.inkMuted }}>{pet.sub}</div>
              </div>
            </div>
          ))}
          <div style={{
            width: 42, background: p.surface, borderRadius: radius,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            border: `1.5px dashed ${p.primarySoft}`,
          }}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke={p.primary} strokeWidth="2" strokeLinecap="round">
              <path d="M12 5v14M5 12h14" />
            </svg>
          </div>
        </div>

        {/* 简介 */}
        <div style={{ padding: '12px 18px 0' }}>
          <div style={{ fontSize: 12, color: p.ink, lineHeight: 1.5 }}>
            🐾 金毛汤圆 & 布偶奶冻的日常 | 📍 上海 | 分享养宠小知识
          </div>
          <div style={{ display: 'flex', gap: 6, marginTop: 8, flexWrap: 'wrap' }}>
            <Tag palette={p} tone="soft">🐕 金毛控</Tag>
            <Tag palette={p} tone="accent">📷 爱摄影</Tag>
            <Tag palette={p} tone="ghost">🏠 上海</Tag>
          </div>
        </div>

        {/* 数据行 */}
        <div style={{ padding: '14px 18px 0', display: 'flex', justifyContent: 'space-between' }}>
          {[
            { num: '28', label: '笔记' },
            { num: '1,248', label: '关注' },
            { num: '8,432', label: '粉丝' },
            { num: '12.3w', label: '获赞' },
          ].map((s, i) => (
            <div key={i} style={{ textAlign: 'center' }}>
              <div style={{ fontSize: 17, fontWeight: 800, color: p.ink, fontFamily: TYPE.num }}>{s.num}</div>
              <div style={{ fontSize: 10, color: p.inkMuted, marginTop: 1 }}>{s.label}</div>
            </div>
          ))}
        </div>

        {/* Tabs */}
        <div style={{
          margin: '18px 18px 0', display: 'flex', gap: 18,
          borderBottom: `0.5px solid ${p.divider}`,
        }}>
          {tabs.map(t => (
            <div key={t.id} style={{
              padding: '10px 0',
              borderBottom: t.active ? `2px solid ${p.primary}` : '2px solid transparent',
              marginBottom: -1,
            }}>
              <span style={{
                fontSize: 13, fontWeight: t.active ? 800 : 500,
                color: t.active ? p.ink : p.inkMuted,
              }}>{t.label}</span>
              <span style={{ fontSize: 11, color: p.inkFaint, marginLeft: 4, fontFamily: TYPE.num }}>{t.count}</span>
            </div>
          ))}
        </div>

        {/* 瀑布流 */}
        <div style={{ padding: '12px 8px 100px', columnCount: 2, columnGap: 6 }}>
          {notes.map((n, i) => (
            <div key={i} style={{
              breakInside: 'avoid', marginBottom: 6, borderRadius: 14,
              overflow: 'hidden', background: p.surface,
              boxShadow: `0 2px 6px ${p.ink}08`,
            }}>
              <div style={{ width: '100%', height: n.h - 40, position: 'relative' }}>
                <PetPlaceholder w={160} h={n.h - 40} palette={p} seed={n.seed} variant={n.variant} />
              </div>
              <div style={{ padding: '8px 10px 10px' }}>
                <div style={{ fontSize: 11, color: p.ink, lineHeight: 1.3, fontWeight: 500 }}>
                  {n.title}
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 3, marginTop: 6, color: p.inkMuted, fontSize: 10 }}>
                  <HeartIcon size={11} color={p.inkMuted} /> {n.likes}
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      <AppTabBar active="me" palette={p} radius={radius} />
    </div>
  );
}

Object.assign(window, { MessagesScreen, ChatScreen, ProfileScreen });
