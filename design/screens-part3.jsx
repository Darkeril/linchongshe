// screens-part3.jsx — Publish 发布页

function PublishScreen({ palette, radius }) {
  const p = palette;
  const [title, setTitle] = React.useState('汤圆的第 28 次洗澡日记');
  const [content, setContent] = React.useState('');

  return (
    <div style={{ width: '100%', height: '100%', background: p.bg, position: 'relative', overflow: 'hidden', fontFamily: TYPE.body }}>
      <StatusLine color={p.ink} />

      {/* 顶部栏 */}
      <div style={{
        position: 'absolute', top: 38, left: 0, right: 0, height: 50,
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        padding: '0 16px',
      }}>
        <span style={{ fontSize: 15, color: p.inkSoft }}>取消</span>
        <span style={{ fontSize: 16, fontWeight: 700, color: p.ink }}>发布笔记</span>
        <span style={{ fontSize: 13, color: p.primary, fontWeight: 600 }}>存草稿</span>
      </div>

      {/* 内容区 */}
      <div style={{ position: 'absolute', top: 90, left: 0, right: 0, bottom: 170, overflow: 'auto' }}>
        {/* 图片网格 */}
        <div style={{ padding: '0 16px', display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 6 }}>
          {[0, 1, 2, 3].map(i => (
            <div key={i} style={{
              aspectRatio: '1', borderRadius: radius * 0.7, overflow: 'hidden', position: 'relative',
            }}>
              <PetPlaceholder w={110} h={110} palette={p} seed={i + 1} variant={['dog','cat','mochi','puppy'][i]} />
              {i === 0 && (
                <div style={{
                  position: 'absolute', top: 4, left: 4,
                  padding: '2px 6px', borderRadius: 6,
                  background: p.primary, color: '#fff',
                  fontSize: 9, fontWeight: 700,
                }}>封面</div>
              )}
              <div style={{
                position: 'absolute', top: 4, right: 4,
                width: 18, height: 18, borderRadius: 9,
                background: 'rgba(0,0,0,0.5)', color: '#fff',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                fontSize: 12,
              }}>×</div>
            </div>
          ))}
          {/* 添加按钮 */}
          <div style={{
            aspectRatio: '1', borderRadius: radius * 0.7,
            background: p.surface, border: `1.5px dashed ${p.primarySoft}`,
            display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 4,
          }}>
            <CameraIcon size={24} color={p.primary} />
            <span style={{ fontSize: 10, color: p.primary, fontWeight: 600 }}>添加 4/9</span>
          </div>
        </div>

        {/* 标题 */}
        <div style={{ padding: '18px 18px 0' }}>
          <input value={title} onChange={e => setTitle(e.target.value)} placeholder="填个有趣的标题，更容易被看见"
            style={{
              width: '100%', border: 'none', background: 'transparent', outline: 'none',
              fontSize: 18, fontWeight: 700, color: p.ink,
              fontFamily: TYPE.body,
            }} />
        </div>

        {/* 正文 */}
        <div style={{ padding: '10px 18px 0' }}>
          <textarea placeholder="和大家分享今天发生的趣事...&#10;提示：加上话题 #xxx 会被更多人看到哦"
            defaultValue=""
            style={{
              width: '100%', minHeight: 72, border: 'none', background: 'transparent', outline: 'none',
              fontSize: 14, color: p.ink, lineHeight: 1.6, resize: 'none',
              fontFamily: TYPE.body,
            }} />
        </div>

        {/* 话题 */}
        <div style={{ padding: '8px 18px 0', display: 'flex', flexWrap: 'wrap', gap: 6 }}>
          {['#金毛日记', '#洗澡日常'].map((t, i) => (
            <div key={i} style={{
              padding: '4px 10px', borderRadius: 12,
              background: p.primarySoft, color: p.primaryInk,
              fontSize: 11, fontWeight: 600,
            }}>{t} ×</div>
          ))}
          <div style={{
            padding: '4px 10px', borderRadius: 12,
            background: p.surface, color: p.inkSoft,
            fontSize: 11, fontWeight: 500,
            border: `1px dashed ${p.inkFaint}`,
          }}>+ 添加话题</div>
        </div>

        {/* 选项列表 */}
        <div style={{ margin: '18px 14px 0', background: p.surface, borderRadius: radius, overflow: 'hidden' }}>
          {[
            { icon: '📍', label: '所在位置', val: '上海 · 徐汇区' },
            { icon: '🐕', label: '添加我的宠物', val: '汤圆 · 金毛' },
            { icon: '👥', label: '@好友提醒', val: '' },
            { icon: '🛍️', label: '关联商品', val: '' },
          ].map((r, i, arr) => (
            <div key={i} style={{
              display: 'flex', alignItems: 'center', padding: '12px 14px',
              borderBottom: i < arr.length - 1 ? `0.5px solid ${p.divider}` : 'none',
            }}>
              <span style={{ fontSize: 16, marginRight: 10 }}>{r.icon}</span>
              <span style={{ fontSize: 13, color: p.ink, fontWeight: 500 }}>{r.label}</span>
              <span style={{ marginLeft: 'auto', fontSize: 12, color: r.val ? p.primary : p.inkFaint }}>
                {r.val || '未选择'}
              </span>
              <svg width="6" height="10" viewBox="0 0 6 10" fill="none" stroke={p.inkFaint} strokeWidth="1.5" strokeLinecap="round" style={{ marginLeft: 6 }}>
                <path d="M1 1l4 4-4 4" />
              </svg>
            </div>
          ))}
        </div>

        <div style={{ height: 24 }} />
      </div>

      {/* 底部按钮区 */}
      <div style={{
        position: 'absolute', bottom: 0, left: 0, right: 0,
        background: p.surface,
        borderTop: `0.5px solid ${p.divider}`,
        padding: '12px 16px 22px',
      }}>
        <div style={{ display: 'flex', gap: 10, marginBottom: 10 }}>
          {['😊 表情', '# 话题', '@ 好友', '📍 位置'].map((x, i) => (
            <div key={i} style={{
              flex: 1, padding: '8px 0', textAlign: 'center',
              fontSize: 11, color: p.inkSoft,
              background: p.surfaceDim, borderRadius: 10,
            }}>{x}</div>
          ))}
        </div>
        <button style={{
          width: '100%', height: 46, borderRadius: radius * 1.4, border: 'none',
          background: `linear-gradient(135deg, ${p.primary}, ${p.primaryInk})`,
          color: '#fff', fontSize: 15, fontWeight: 700,
          boxShadow: `0 6px 18px ${p.primary}55`,
          fontFamily: TYPE.body,
        }}>
          立即发布 🐾
        </button>
      </div>
    </div>
  );
}

Object.assign(window, { PublishScreen });
