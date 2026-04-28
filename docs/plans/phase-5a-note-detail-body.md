# Phase 5a：笔记详情页主体（不含评论 + 登录拦截升级）

- **创建日期**：2026-04-22
- **状态**：✅ 已完成（见提交 3d5833c：笔记详情页主体 + DOM 事件名冲突 bug 修复）
- **执行方式**：autoresearch-router 严格协议（Boot Block 完整 + worker + reviewer 成对 + 账本 + 按 dispatch-template）
- **前置依赖**：Phase 0 ~ 4 完成

---

## 目标

把 `note-detail.vue` 从占位替换为**真实内容**（除评论树和登录拦截升级外全部）：
1. 顶部悬浮导航栏（返回 + 作者条 + 更多）
2. 图文轮播（swiper + 指示器 + 页码）
3. 标题 / 正文 / 话题标签 / 编辑时间
4. 分隔条 + 评论区占位（标题 "共 X 条评论" + 占位 text，评论条目 Phase 5b 实现）
5. 底部固定栏（输入框占位 + 点赞 + 收藏 + 评论入口）

**本 Phase 不做**：
- 评论列表和回复（Phase 5b）
- 登录拦截升级为 Sheet + action replay（独立 Phase 6）

---

## 设计稿规格（来源：screens-part2.jsx 行 159-320）

### 顶部悬浮导航栏（absolute, top 40, height 52）
- 左：返回按钮（36×36 圆，背景 rgba(255,255,255,0.9) + blur 16）
- 中：作者条（椭圆胶囊，毛玻璃，PetAvatar 28 + 名字 13/600 + "+ 关注"按钮 11/600 primary 背景）
- 右：更多操作（36×36 圆 + 三点）
- z-index 10（浮在图文上）

### 图文轮播（顶格，height 360）
- swiper 横滑，PetPlaceholder 占位
- 右下页码 `1 / 4`（rgba 黑 45% 背景，白字，fontFamily num）
- 底部居中指示点组：active 16×5 圆角 / inactive 5×5 圆，白色 / 白 55%

### 标题 + 正文（padding 16 18）
- 标题 19/700 ink, line-height 1.4, margin-bottom 10
- 正文 14/inkSoft, line-height 1.75, margin-bottom 12
- 话题标签组（flex wrap, gap 6）
  - 每个标签：padding 3 9, radius 10, primarySoft 背景, primaryInk 字, 11/500
- 底部信息 11/inkMuted："编辑于 2 小时前 · 来自 上海"

### 分隔条
- height 8, background surfaceDim, margin 4 0

### 评论区占位（padding 14 18 20）
- "共 X 条评论" 14/700 ink, margin-bottom 12
- **评论条目 Phase 5b 实现**，本 Phase 只展示一个 placeholder：`"评论功能 Phase 5b 实现"`
- 根节点加 `<!-- TODO(phase5b): 评论列表 + 回复 + 子楼 -->`

### 底部固定栏（fixed bottom, height 62）
- 背景 rgba(255,255,255,0.95) + blur 20 + 顶部 divider
- 左：输入框占位 flex:1 height 38 radius 19 surfaceDim "说点什么吧..." 12/inkMuted
- 右：3 组小图标 + 数字（每组 width 40, gap 1）
  - HeartIcon 22（active warn 色 filled / inactive inkSoft 色），数字 9/fontFamily num
  - BookmarkIcon 22（active accent 色 filled / inactive inkSoft），数字 9
  - ChatIcon 22（inkSoft），数字 9

---

## 产出清单

### 新组件（`src/components/ui/`）

1. **`NoteDetailHeader.vue`** — 顶部悬浮导航栏
   - Props: `author: { avatarVariant, name, followed }`
   - Emits: `@back`, `@follow`, `@more`
   - 毛玻璃 + 条件编译降级（小程序端 rgba 不透明）

2. **`NoteGallery.vue`** — 图文轮播
   - Props: `images: NoteImage[]`, `currentIndex?: number`
   - Emits: `@change (index)`
   - swiper 横滑 + 右下页码 + 底部指示点
   - 每张图用 PetPlaceholder 占位（seed 不同 + variant 循环）
   - swiper 高度固定 360px（iPhone 13 Pro 视口近似）

3. **`TopicTag.vue`** — 话题标签（小号）
   - Props: `text`, `compact?: boolean` 
   - Emits: `@tap` 
   - 注意：与现有 `TopicPill.vue`（首页话题胶囊）**是不同组件** —— 话题标签更小（padding 3 9 / radius 10 / 11/500）

4. **`NoteDetailActions.vue`** — 底部固定栏
   - Props: `likes, liked, saves, saved, comments`
   - Emits: `@like`, `@save`, `@comment`, `@input-tap`
   - 包含输入框占位 + 3 个图标按钮

5. **图标组件**（`src/components/ui/icons/`）
   - `BookmarkIcon.vue`（书签图标，filled / outline）
   - `ChatIcon.vue`（对话框图标）
   - `MoreDotsIcon.vue`（三点，复用小菜单）
   - `ArrowLeftIcon.vue`（返回箭头）

### 扩展数据层

6. **`src/types/note.ts`** 扩展
```typescript
export interface NoteImage {
  url: string;           // Phase 5a 用 seed 字符串（PetPlaceholder 用）
  variant: PetVariant;   // 决定占位图风格
  seed: string | number;
}
export interface NoteAuthor {
  id: string;
  name: string;
  avatarVariant: PetVariant;
  followed: boolean;
}
export interface NoteDetail extends NoteListItem {
  images: NoteImage[];   // 1-9 张
  content: string;       // 正文
  topics: string[];      // 话题标签（不带 # 前缀，组件里加）
  author: NoteAuthor;
  saves: number;
  saved: boolean;
  comments: number;      // 评论数
  editedAt: string;      // "2 小时前"（Phase X 改用时间戳 + 格式化）
  fromCity: string;      // "上海"
}
```

7. **`src/api/services/note.ts`** 扩展
   - `getNoteDetail(id: string): Promise<NoteDetail>` — mock 从 note.mock.ts 里根据 id 找到 NoteListItem + 生成详情字段
   - `toggleSave(noteId: string): Promise<{ saved: boolean; saves: number }>`
   - `toggleFollow(authorId: string): Promise<{ followed: boolean }>`

8. **`src/api/mock/note.mock.ts`** 扩展
   - `buildNoteDetail(id: string): NoteDetail | null` — 根据 id 找到 note，生成 3-5 张 images（PetPlaceholder seed 基于 id + 0..N）、随机 content 正文、2-3 个话题、作者信息、saves/comments 伪随机
   - mock store 存 saved / followed 状态，切换持久化

### 新页面（改造现有占位）

9. **`src/pages/note-detail/note-detail.vue`** — 真实内容
   - 布局：flex column（顶部悬浮 header + 内容 scroll-view + 底部 actions）
   - onLoad 读 query.id，调 getNoteDetail
   - 组合组件：NoteDetailHeader + NoteGallery + 标题正文 + TopicTag 列表 + 评论区占位 + NoteDetailActions
   - 点赞 / 收藏走 in-place mutation（参考 Phase 4 home.vue 的经验）
   - 关注按钮走 toggleFollow
   - 评论输入框点击：调用 `useAuthGuard.requireAuth(() => uni.showToast('Phase 5b 实现'))` — Phase 5b 改成打开评论面板

---

## 硬性约束

1. **像素级还原** 对照 screens-part2.jsx 行 159-320
2. **所有颜色 / 字号 / 阴影 tokens 引用**；rgba 走 SCSS `rgba()` 函数
3. **新组件 class 加 `acs-` 前缀**（gotcha #12）
4. **swiper 高度显式**（gotcha #4，#16）
5. **毛玻璃条件编译**（gotcha #14 格式）
6. **返回按钮**：`uni.navigateBack`，栈深度=1 时兜底 `uni.reLaunch` 到 home（gotcha #13）
7. **点赞 / 收藏乐观更新用 in-place mutation**（gotcha #18）
8. **整页容器**：不加 `:active` 防止冒泡（gotcha #19）
9. **所有按钮用 `@tap`**（gotcha #11）
10. **TypeScript 严格，零新错**
11. **报告必须按条目编号引用 gotchas**

---

## 视觉验证流程（方案 A）

Worker 完成后启动 dev:h5 保持运行；主线程通知用户截图验收：

1. 从 home 任意 feed 点一张卡 → 跳详情页，测图文轮播（滑动 + 页码变 + 指示点动）
2. 页面正文和话题标签像素级对齐设计稿
3. 点底部"说点什么吧" → 目前 toast 提示"Phase 5b 实现"
4. 点心形 → 立即变红 + 数字 +1
5. 点收藏 → 变绿（accent 色）+ 数字 +1
6. 点"+ 关注" → 切换为"已关注"（或类似）
7. 点返回 → 回到 home（tab 保留原 active）

---

## 验收 Harness

### Functional
- 从 home 点卡片能正确携带 id 跳详情
- 详情页 5 部分都显示（header / gallery / body / 评论占位 / actions）
- 图文轮播可滑动，页码和指示器同步
- 点赞 / 收藏 / 关注交互正确（乐观更新）
- 返回按钮回到 home 保持 tab 状态

### Quality
- 所有 tokens 引用
- gotchas 遵守度 100%
- TS 严格零新错
- `build:mp-weixin` exit=0
- 与 home.vue 共享 `NoteListItem` / `PetPlaceholder` 不冗余

### Delivery
- 截图对比设计稿
- Reviewer 独审 PASS（代码层）
- 用户视觉验收 PASS
- git commit + push

**指标优先级**：`pixel_match > interaction_correctness > token_alignment > code_cleanliness`

---

## 风险与预案

| 风险 | 预案 |
|------|------|
| swiper 轮播在小程序 / App 端 indicator 定制不够灵活 | 用自绘的 indicator view（绝对定位），不用 swiper 自带的 indicator-dots |
| PetPlaceholder 大图（390×360）性能 | 已是 SVG 渲染，图集 ≤ 5 张时无瓶颈；如卡再加懒加载 |
| 毛玻璃在小程序端失效 | 条件编译降级为 rgba 0.95 不透明 |
| 关注按钮 + 点赞 + 收藏 的 mock store 和 home.vue 的点赞状态不同步 | 共享同一 mock store 的 `liked` 字段；进入详情页同步最新状态；返回 home 时 home 的 list 指向同一对象，自动更新 |
| Phase 5b 评论入口空置 | 底部评论输入框点击 toast 提示；标题"共 X 条评论"X 从 detail.comments 读；评论条目区写 placeholder |

---

## 结项交付物

- 4 新组件 + 4 新图标
- types/note.ts 扩展
- services/note.ts + mock/note.mock.ts 扩展
- note-detail.vue 重写
- Reviewer PASS
- 用户视觉验收
- commit + push

---

## 后续衔接

Phase 5a 完成 → **Phase 5b**：评论树（一级评论 + 子楼回复 + 发评论面板）
Phase 5b 完成 → **Phase 6**：登录拦截升级（Sheet + action replay + 升级所有写操作拦截）

---

## 实验账本

| round | persona | 假设 | 改动 | 验收 | status | decision | notes |
|-------|---------|------|------|------|--------|---------|-------|
| 1 | frontend-developer (worker) | 4 组件 + 4 图标 + 数据层扩展 + note-detail.vue 重写 | 12 产出（4 组件 + 4 图标 + 3 扩展 + 1 重写）；DETAIL_CACHE/AUTHOR_CACHE 机制持久化状态 | worker 自报：gotchas #4/#5/#11/#12/#13/#14/#15/#16/#18/#19 全处理；in-place mutation；TS 2 预存在 0 新错；mp-weixin build exit=0；home/Splash/Login/tabs 0 回归 | success 自报 | 待 2a reviewer + 2b 用户视觉 | dev @ 5174；派发模板角色澄清生效，一次性完成无角色混乱 |
| 2a | reality-checker (reviewer) | 独立代码审查（数值对齐 / gotchas grep / 实跑 tsc + build） | 10 项报告 | pending | running | pending | 运行中 |
| 2b | 用户（视觉验收） | 10 个验证点 + 返回栈兜底 | 截图 | pending | pending | pending | 等用户 |
