# Phase 5b：笔记评论树（2 层拍平 + 底部发评论面板）

- **创建日期**：2026-04-22
- **状态**：✅ 已完成（Reviewer 4.94/5 PASS；用户视觉验收通过；顺手修 TopicTag 遗留 @tap + 孤儿 CSS + 注释笔误）
- **执行方式**：autoresearch-router 严格协议（Boot Block 完整 + worker + reviewer 成对 + 账本）
- **前置依赖**：Phase 5a 完成

---

## 目标

把 `note-detail.vue` 的评论区占位替换为真实的评论系统：
1. 2 层拍平结构（一级评论 + 回复数组，"回复的回复"靠 `@用户名 ...` 文本展现，不再嵌套）
2. 评论列表分页加载
3. 回复默认显示 2 条 + "展开 N 条回复" 折叠
4. 底部半屏弹出发评论面板（Sheet）
5. 评论点赞（乐观更新）
6. Mock 10-20 条一级 + 每条 0-5 条回复

**本 Phase 不做**：
- 登录拦截升级为 Sheet + action replay（Phase 6）
- @提及搜索面板（Phase 7）
- 评论举报 / 删除 / 置顶（后续 Phase）

---

## 评论数据结构（2 层拍平）

```typescript
export interface CommentUser {
  id: string;
  name: string;
  avatarVariant: PetVariant;
}

export interface CommentItem {
  id: string;
  noteId: string;
  author: CommentUser;
  text: string;           // 正文。如果是回复，可能含 "@张三 ..." 文本
  likes: number;
  liked: boolean;
  createdAt: string;      // "2 小时前"
  // 2 层拍平：
  // - 一级评论：replyTo undefined
  // - 二级回复：replyTo 指向被回复的用户（不是评论 id，简化）
  replyTo?: CommentUser;
}

/** 一级评论 + 其回复（回复也是 CommentItem，但 replyTo 有值） */
export interface CommentThread {
  root: CommentItem;
  replies: CommentItem[];       // 同一个 root 下的所有回复（拍平，按时间升序）
}

export interface CommentListParams {
  noteId: string;
  page: number;
  size: number;
}
export interface CommentListResult {
  threads: CommentThread[];
  hasMore: boolean;
  page: number;
  total: number;
}

export interface PostCommentParams {
  noteId: string;
  text: string;
  /** 回复某个 thread root 时填；不填则是发一级评论 */
  rootId?: string;
  /** 回复特定用户（如回复某个 reply 时，自动填被回复人） */
  replyTo?: CommentUser;
}
```

## 设计稿参考

设计稿 `screens-part2.jsx` L266-287 给了评论单条样式（一级评论）。**子回复 / 输入面板设计稿未画**，按 CLAUDE.md 原则 1 分支出 V1：
- 子回复：复用一级样式，左侧缩进 + 头像稍小 + `@用户名` 标粗着色
- 发评论面板：latte 主题 Sheet + 360rpx 高 + 输入框 + "发送"按钮

---

## 产出清单

### 新组件（`src/components/ui/`）

1. **`CommentItem.vue`** — 单条评论
   - Props: `comment: CommentItem`, `variant?: 'root' | 'reply'`（reply 缩进+头像 28 vs root 34）
   - Emits: `@like`, `@reply`
   - 设计稿对齐：
     - Avatar 34 / 28
     - 昵称 12/inkMuted
     - 正文 13/ink/line-height 1.5
     - 底部行：时间 11/inkMuted + "回复" + 右侧 HeartIcon 12 + 数字
   - `@用户名` 文本段特殊标记（primary 色 + semibold）

2. **`CommentThread.vue`** — 一级评论 + 回复折叠
   - Props: `thread: CommentThread`
   - Emits: `@like`, `@reply`
   - 默认显示 root + 前 2 条 replies
   - 如果 replies > 2，显示"展开 N 条回复" 链接；点击展开全部 replies（内部 state）

3. **`CommentList.vue`** — 评论列表
   - Props: `threads, hasMore, loading, total`
   - Emits: `@load-more`, `@like (comment)`, `@reply (comment, thread)`
   - 标题行："共 {{total}} 条评论"
   - 列表循环 `<CommentThread>`
   - 滚到距底 200rpx emit load-more
   - 底部状态：loading / hasMore / 到底了

4. **`CommentInputSheet.vue`** — 底部半屏发评论面板
   - Props: `open`, `replyTarget?: CommentUser`（如果回复某人，输入框占位 "回复 @张三："）
   - Emits: `@close`, `@submit (text: string)`
   - Sheet 底部弹出，360rpx 高
   - 内部：
     - 顶部拖拽条
     - 输入框（textarea 支持多行，maxlength 200）
     - 底部栏：字数 / "发送"按钮
   - 点击遮罩关闭
   - **键盘弹起时输入框上推**（`uni.onKeyboardHeightChange` 监听）

### 扩展数据层

5. **`src/types/comment.ts`**（新文件）
   - 上面的 CommentUser / CommentItem / CommentThread / CommentListParams / CommentListResult / PostCommentParams

6. **`src/api/services/comment.ts`**（新文件）
   - `getComments(params: CommentListParams): Promise<CommentListResult>` — GET `/app/web/comment/list?noteId=..&page=..&size=..`
   - `postComment(params: PostCommentParams): Promise<CommentItem>` — POST `/app/web/comment/create` body
   - `likeComment(commentId: string): Promise<{liked, likes}>` — POST `/app/web/comment/{id}/like`
   - `deleteComment(commentId: string): Promise<void>` — DELETE（Phase 5b 不实现 UI，但接口签名预留）

7. **`src/api/mock/comment.mock.ts`**（新文件）
   - 基于 noteId 生成 10-20 条一级 + 每条 0-5 条回复
   - 用户池、评论文本池、时间伪随机
   - 回复中 30% 概率 `@其他用户` 提及
   - likeComment / postComment mock 状态持久化（内存 STORE）
   - postComment mock 返回新 CommentItem（id = `mock-${Date.now()}`）

### 修改

8. **`src/pages/note-detail/note-detail.vue`**
   - 替换评论区占位为 `<CommentList>`
   - 从 service 拉评论数据（onLoad 后调）
   - 点评论输入框 → 打开 CommentInputSheet（一级评论）
   - 点某条评论的"回复" → 打开 CommentInputSheet 并预填 replyTarget
   - 发送成功后：
     - 如果是一级评论：插入 threads 顶部
     - 如果是回复：插入对应 thread 的 replies 末尾

9. **`src/components/ui/NoteDetailActions.vue`**
   - 点评论图标（第三个图标）→ emit 事件 → note-detail 里打开 CommentInputSheet（一级评论模式）
   - 点"说点什么吧..."输入框 → 同上

### 登录拦截（当前最小版）

10. 发评论 / 点赞评论 / 打开输入面板前 → 先过 `useAuthGuard.requireAuth()` 现有 redirect 版本  
    Phase 6 升级为 Sheet 后，这些调用点无需改代码（guard 内部抽象）

---

## 硬性约束

1. **2 层拍平**：没有嵌套递归，UI 层级永远固定 2 层（root + replies）
2. **像素级对齐设计稿** 一级评论样式；子回复按 V1（无设计稿，标注 TODO(design)）
3. **tokens 引用 + rgba 函数**
4. **新 class 加 `acs-` 前缀**（gotcha #12）
5. **emit 名不用 DOM 原生事件**（gotcha #21）：`@comment-like` / `@comment-reply` / `@sheet-close` 等
6. **in-place mutation 乐观更新**（gotcha #18）：点赞评论 / 回复插入 thread.replies
7. **Sheet 遮罩**：覆盖全屏，点击关闭，z-index 在底部 Actions 栏之上
8. **键盘适配**：输入面板监听 `uni.onKeyboardHeightChange`，弹起时输入框向上推
9. **TS 严格零新错**
10. **所有报告按 gotcha 编号引用**

---

## 视觉验证流程（方案 A）

```bash
cd /Users/fanziqi/linchongshe/linchong_src/aichongshe-app
npm run dev:h5 > /tmp/phase5b-dev.log 2>&1 &
sleep 8
tail -15 /tmp/phase5b-dev.log
```
用户截图验证：
1. 从 home 点卡片进详情，滚到下方评论区 → 显示 10-20 条评论
2. 每条一级评论下显示 root + 前 2 条 replies
3. 有 >2 条 replies 的评论显示"展开 N 条回复" → 点击全部展开
4. 点任意评论"回复" → 底部弹出 Sheet，占位"回复 @张三：..."
5. 点底部"说点什么吧" → Sheet 以一级评论模式弹出
6. 输入发送 → mock 成功，评论插入列表顶部（一级）或 thread.replies 末尾（回复）
7. 点评论右下心形 → 立即变红 + 数字 +1
8. Sheet 点击遮罩关闭
9. H5 可选测键盘弹起输入框上推

---

## 验收 Harness

### Functional
- 从 home 点卡进详情，评论区真实渲染（非占位）
- 展开/折叠回复交互正确
- 发一级评论成功，列表顶部新增
- 回复某人成功，对应 thread.replies 末尾新增
- 评论点赞乐观更新正确
- Sheet 打开/关闭正常，遮罩点击关闭

### Quality
- 所有颜色 tokens 引用
- emit 名避开 DOM 原生事件（gotcha #21）
- in-place mutation（gotcha #18）
- 触发 #4 / #12 / #18 / #21 按编号引用
- TS 零新错
- `build:mp-weixin` exit=0

### Delivery
- 截图对比设计稿
- Reviewer 独审 PASS
- 用户视觉验收 PASS
- commit + push

**指标优先级**：`interaction_correctness > pixel_match > token_alignment > code_cleanliness`

---

## 风险与预案

| 风险 | 预案 |
|------|------|
| 回复嵌套超 2 层（设计稿没明确） | 硬性拍平：任何回复 replyTo 指的都是被回复用户，不携带嵌套父 thread |
| Sheet 在小程序端动画卡顿 | transform: translateY + 300ms ease 足够；复杂动画 Phase X 优化 |
| 键盘适配跨端差异 | H5 用 window.innerHeight 监听 / 小程序 onKeyboardHeightChange / App uni-app 一致 —— 条件编译差异化处理 |
| 评论太长 overflow 换行 | 确保 word-break: break-word，不出现横向溢出 |
| 发评论后滚动位置跳动 | 新评论插入顶部时用 `scroll-into-view` 滚到新评论 |

---

## 结项交付物

- 4 新组件 + 1 新类型文件 + 1 新 service + 1 新 mock + 2 处改造
- 用户视觉验收
- reviewer PASS
- commit + push

---

## 后续衔接

Phase 5b 完成 → **Phase 6**：登录拦截全面升级
- useAuthGuard 改成底部 Sheet（手机号/微信/抖音 快速登录）+ 动作回放
- 升级所有写操作拦截点：点赞 / 关注 / 收藏 / 评论 / 发布 / 下单

---

## 实验账本

| round | persona | 假设 | 改动 | 验收 | status | decision | notes |
|-------|---------|------|------|------|--------|---------|-------|
| 1 | frontend-developer (worker) | 4 组件 + 3 数据层 + note-detail 接入；emit 全用 `comment-*` / `thread-*` / `sheet-*` / `list-*` 前缀避 DOM 原生 | 8 文件产出；STORE Map 持久；CSS keyframes Sheet 动画；键盘高度条件编译 | worker 自报：gotcha #4/#5/#11/#12/#14/#18/#19/#21 全处理；2 层拍平；3 处 useAuthGuard.requireAuth；TS 2 预存在 0 新错；mp-weixin exit=0；Phase 5a 0 回归 | success 自报 | 待 2a reviewer + 2b 用户视觉 | dev @ 5173 |
| 2a | reality-checker (reviewer) | 代码审（重点 emit 黑名单 / 2 层拍平 / scroll 不嵌套 / in-place mutation） | 12 项报告 | pending | running | pending | 运行中 |
| 2a' | 主 agent | 顺手修 reviewer flag 的 3 个非阻塞项 | TopicTag `@tap` → `@topic-tap`（Phase 5a 遗留）；note-detail 孤儿 CSS 清理；CommentList 注释笔误修正 | 静态检查 | success | keep | 避免 gotcha #21 再踩 |
| 2b | 用户（视觉验收） | 7 动作验收 | "很不错" | 口头 PASS | success | **keep** | Phase 5b 正式收尾 |
