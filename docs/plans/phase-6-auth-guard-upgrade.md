# Phase 6：登录拦截升级（Sheet + 动作回放）

- **创建日期**：2026-04-23
- **状态**：✅ 已完成（Reviewer 4.7/5 PASS；用户视觉验收发现 3 个遗留 bug 全修：phone-login V1 布局错乱 + 按钮文字未居中（gotcha #1 残余）+ mock 验证码 1234↔6 位前端校验冲突）
- **执行方式**：autoresearch-router 严格协议（Boot Block + worker + reviewer 成对 + 账本）
- **前置依赖**：Phase 0-5b 完成
- **对应 CLAUDE.md 原则**：原则 6（登录态拦截全局约定）的完整实现

---

## 目标

把 `useAuthGuard.requireAuth` 从「未登录 redirect 到 Login 页」升级为「未登录弹底部 Sheet → 登录成功后**自动执行原动作**（action replay）」。

参考小红书：点心形 → 弹半屏登录面板 → 登录成功 → **自动完成那次点赞**（用户不用再点一次）。

---

## 现状盘点

### 当前 `useAuthGuard` 行为（`src/composables/useAuthGuard.ts`）
```ts
// 最小版：未登录直接 reLaunch 到 login
async function requireAuth(action: () => void): Promise<void> {
  if (userStore.isLogin) {
    action()
  } else {
    uni.reLaunch({ url: '/pages/login/login' })
  }
}
```

### 现有写操作拦截点（要升级的调用点）
| 文件 | 函数 | 受保护动作 |
|------|------|----------|
| `pages/home/home.vue` | `onNoteLike` | 笔记点赞 |
| `pages/note-detail/note-detail.vue` | `onLike` | 详情点赞 |
| `pages/note-detail/note-detail.vue` | `onSave` | 收藏 |
| `pages/note-detail/note-detail.vue` | `onFollow` | 关注作者 |
| `pages/note-detail/note-detail.vue` | `onListLike` | 评论点赞 |
| `pages/note-detail/note-detail.vue` | `onListReply` | 回复评论 |
| `pages/note-detail/note-detail.vue` | `onInputTap` | 发评论 |

⚠️ 注意：`home.vue` 的 `onNoteLike` **当前未走 useAuthGuard**（Phase 4 时机太早，guard 还没建立）。本 Phase 一并补上。

---

## 升级后的接口

```ts
const { requireAuth } = useAuthGuard()

// 用法 1：已登录直接执行；未登录弹 Sheet → 登录成功后回放
await requireAuth(async () => {
  await likeNote(noteId)
})

// 用法 2：用户取消登录 → reject（调用方可选择 try/catch 静默处理）
try {
  await requireAuth(async () => { ... })
} catch (e) {
  if (e.code === 'AUTH_CANCELLED') return
  throw e
}
```

`requireAuth` Promise 语义：
- **已登录**：直接执行 action，resolve（action 的返回值）
- **未登录 + 用户登录成功**：执行 action（动作回放），resolve
- **未登录 + 用户取消（关闭 Sheet）**：reject 一个 `{ code: 'AUTH_CANCELLED' }`

---

## 产出清单

### 新组件

1. **`src/components/ui/LoginSheet.vue`** —— 全局登录面板（底部半屏）
   - 触发方式：监听 `useAuthSheet` store 的 `open` state
   - 内容：
     - 顶部拖拽条 + 关闭按钮
     - 大标题"登录爱宠社"
     - 副标题"宠友都在这里，等你一起分享"
     - **手机号登录区**：
       - 国家区号 +86 + 11 位手机号输入
       - 6 位验证码 + 60s 倒计时按钮
       - "登录 / 注册"主按钮（latte 渐变）
     - **第三方登录区**（小图标横排）：
       - 微信（绿色 #07c160）
       - 抖音（黑色，第三方品牌色保留）
       - "更多登录方式"链接 → 跳 `/pages/login/login`（多入口选择页）
     - 底部协议勾选 + 用户协议 / 隐私政策链接
   - 交互：
     - 点遮罩 / 关闭按钮 / 系统返回手势 → 触发 `cancel`（rejection）
     - 任一登录成功 → store 更新登录态 → 执行 `pendingAction`（动作回放）→ 关闭 Sheet
   - **无设计稿**，按 latte 主题 V1 + `TODO(design)` 注释

2. **`src/components/ui/icons/CloseIcon.vue`** —— X 关闭图标（如果已有可复用）
3. **（可选）`src/components/ui/icons/WechatIcon.vue` / `DouyinIcon.vue`** —— 第三方登录品牌图标

### 状态管理

4. **`src/stores/authSheet.ts`** —— LoginSheet 的全局 store
   ```ts
   interface AuthSheetState {
     open: boolean
     pendingAction: (() => any | Promise<any>) | null
     pendingResolver: { resolve: (v: any) => void; reject: (e: any) => void } | null
   }
   ```
   - `requestAuth(action, resolver)`：打开 Sheet，存 pendingAction + resolver
   - `confirmLoginSuccess()`：登录成功 → 执行 pendingAction → resolve → 关闭
   - `cancel()`：用户关闭 → reject({ code: 'AUTH_CANCELLED' }) → 清理 state

### Composable 升级

5. **`src/composables/useAuthGuard.ts`** —— 重写
   ```ts
   async function requireAuth<T>(action: () => T | Promise<T>): Promise<T> {
     const userStore = useUserStore()
     if (userStore.isLogin) return action()
     return new Promise<T>((resolve, reject) => {
       useAuthSheet().requestAuth(action, { resolve, reject })
     })
   }
   ```

### 修改

6. **`src/App.vue`** —— 全局挂载 `<LoginSheet />`
   - 放在根 view 的最后（z-index 最高）
   - 不影响其他页面布局

7. **拦截点全面接入**（7 处）
   - 把 7 个写操作函数全部改成：
     ```ts
     try {
       await requireAuth(async () => { 真实动作 })
     } catch (e) {
       if ((e as { code?: string })?.code !== 'AUTH_CANCELLED') {
         uni.showToast({ title: '操作失败', icon: 'none' })
       }
     }
     ```
   - 已登录场景行为不变；未登录场景从「跳页」变「弹面板 + 回放」

8. **`src/pages/login/login.vue`** —— 登录方式选择页保留
   - LoginSheet 里的"更多登录方式"链接到这里
   - 这个页面继续作为完整登录页（首次进 App 必登场景）

### 抽出公共逻辑（DRY）

9. **`src/composables/usePhoneLogin.ts`**（新）—— 手机号登录逻辑
   - 把 `phone-login.vue` 里的 phone / smsCode / cooldown / canSubmit / onRequestSms / onSubmit 抽出来
   - 让 `phone-login.vue` 和 `LoginSheet.vue` 复用，不复制代码
   - Phase 6 不强制改 phone-login.vue（如果时间紧），但 LoginSheet 必须用这个 composable

---

## 硬性约束

1. **emit 名严格避 DOM 原生**（gotcha #21）：LoginSheet 用 `@auth-success` / `@auth-cancel` / `@sheet-close-tap` 等
2. **in-place mutation**（gotcha #18）：登录态由 user store 管，update 走 store actions
3. **scroll-view 不嵌套**（gotcha #4）：Sheet 内手机号区是固定结构，不需要 scroll
4. **fixed + transform**（gotcha #5）：Sheet `position: fixed`，App.vue 根无 transform
5. **`acs-` 前缀**（gotcha #12）
6. **整页容器无 :active**（gotcha #19）
7. **键盘适配**（参考 CommentInputSheet 经验）：手机号/验证码输入时 Sheet 上推
8. **TS 严格 + 泛型**：`requireAuth<T>` 返回 `Promise<T>`
9. **Token 流程不变**：复用现有 `loginWithWechat / loginWithPhone` actions
10. **报告必须按 gotcha 编号引用**

---

## 视觉验证流程（方案 A）

启动 dev server 后通知用户：

1. **清除 storage**（DevTools → Application → Local Storage → Clear）模拟未登录
2. 进 home 页（不会被强制跳 login，因为 home 是浏览态开放，gotcha 原则 6.1）
3. 找一张笔记**点心形** → 应**弹底部 LoginSheet**（不是跳页）
4. 关闭 Sheet（点遮罩） → 心形保持原状（动作没执行）
5. 再点心形 → 又弹 Sheet
6. 这次输 11 位手机号 + 验证码 1234 → 点登录
7. 登录成功 → Sheet 关闭 → **心形自动变红 + 数字 +1**（动作回放）
8. 进笔记详情，测点赞 / 收藏 / 关注 / 发评论同样的流程

---

## 验收 Harness

### Functional
- 7 个写操作拦截点未登录都会触发 LoginSheet
- 登录成功后动作能正确回放
- 用户取消登录不报错（toast 不弹）
- 已登录场景行为完全不变
- LoginSheet 内手机号 / 微信登录都能 mock 成功

### Quality
- 所有 emit 名避 DOM 原生
- gotchas #4 / #5 / #12 / #18 / #19 / #21 处理
- TS 严格零新错
- `build:mp-weixin` exit=0
- DRY：phone-login 和 LoginSheet 共享 `usePhoneLogin` composable

### Delivery
- 截图对比（虽然无设计稿，但要 latte 主题 V1）
- Reviewer 独审 PASS
- 用户视觉验收
- commit + push

**指标优先级**：`interaction_correctness > token_alignment > pixel_match > code_cleanliness`（无设计稿，pixel_match 降权）

---

## 风险与预案

| 风险 | 预案 |
|------|------|
| pendingAction 在 Sheet 取消后内存泄漏 | store cancel 时显式置 null |
| 登录成功 → 回放 action 又抛错 | resolver.reject(e) 正常路径返回 |
| LoginSheet 全局挂载在 App.vue 影响其他页面性能 | v-if（关闭时不渲染）+ store open state |
| 用户连续点 2 次心形 → 出现 2 个 Sheet | store 加 guard：open=true 时 requestAuth 替换 pendingAction，不重复弹 |
| 微信 / 抖音第三方登录 mock 不真实 | 直接 mock 返回成功 token + 假 UserProfile，Phase X 真接入时改 service |
| 键盘弹起遮挡输入 | 同 Phase 5b CommentInputSheet 经验，监听 keyboardHeightChange |

---

## 结项交付物

- 1 新组件 LoginSheet + 0-3 新图标
- 1 新 store authSheet
- 1 新 composable usePhoneLogin（DRY）
- useAuthGuard 重写
- App.vue 全局挂载
- 7 个拦截点接入
- Reviewer PASS + 用户视觉验收
- commit + push

---

## 后续衔接

Phase 6 完成 → **Phase 7+**：剩余功能（按用户动线推进）
- Phase 7：发布笔记流程（多图上传 + 文本编辑 + 话题选择）
- Phase 8：搜索（搜索结果页 + 历史搜索）
- Phase 9：消息中心（系统通知 + 私信列表）
- ...

---

## 实验账本

| round | persona | 假设 | 改动 | 验收 | status | decision | notes |
|-------|---------|------|------|------|--------|---------|-------|
| 1 | frontend-developer (worker) | LoginSheet + authSheet store + useAuthGuard 重写 Promise+泛型 + usePhoneLogin DRY + 7 拦截点接入 | 7 新增 + 3 改造 = 10 文件；LoginSheet 挂 MainLayout + note-detail 两处（弃 App.vue 因为它无 UI）；pendingAction 三出口清理；乐观更新进 action 闭包 | worker 自报：gotchas #4/#5/#11/#12/#14/#18/#19/#21 全处理；TS 2 预存 0 新错；mp-weixin exit=0；Phase 1-5 0 回归 | success 自报 | 待 2a reviewer + 2b 用户视觉 | dev @ 5174 |
| 2a | reality-checker (reviewer) | 代码审：泛型 / 内存清理 / emit 黑名单 / 乐观更新位置 / 错误码静默 | 13 项报告 | pending | running | pending | 运行中 |
| 2b | 用户（视觉验收 Round 1） | 6 动作 + 顺路测 phone-login | 用户发现 3 个遗留 bug：1) phone-login V1 布局错乱（back 按钮 absolute + title margin-top 重叠）；2) phone-login 两个按钮文字未居中（gotcha #1 残余 UniApp button 默认 left-aligned）；3) mock 验证码 "1234" 与前端 6 位 SMS_REGEX 冲突 | 用户反馈 | success | 需要修 | bug 1/2 是 Phase 2 V1 一直存在的，今天才走链路；bug 3 是 mock 设定问题 |
| 3a | 主 agent | 修 3 个 bug | 1) phone-login header 改 flex 布局 + back 入文档流；2) sms-btn / submit 加 `display:flex; align-items:center; justify-content:center; ::after display:none`；3) auth.mock.ts + phone-login.vue + LoginSheet.vue 三处验证码统一改 "123456" | 热重载 | success | 用户 Round 2 验收 | 一次改完 3 个 |
| 2b' | 用户（视觉验收 Round 2） | 复验 3 bug 修复 | "可以了" | 口头通过 | success | **keep** | Phase 6 正式收尾 |
