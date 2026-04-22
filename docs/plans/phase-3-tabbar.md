# Phase 3：全局底部 TabBar 架构 + 5 个 Tab 页壳

- **创建日期**：2026-04-22
- **状态**：✅ 已完成（Reviewer 4.43/5 PASS；视觉修复 TabBar 发布按钮 margin-top 2x bug；用户交互修正后首页 Tab 支持左右滑 + 顶部栏固定 + tab 独立滚动；沉淀 2 条新 gotcha #16/#17）
- **执行方式**：autoresearch-router 严格协议（Boot Block 完整 + worker + reviewer 成对 + 账本 + 派发模板走 `docs/plans/dispatch-template.md`）
- **前置依赖**：Phase 0 / 0.5 / 1 / 2 / 2.5 全部完成

---

## 目标

搭建**全局底部 TabBar 架构**，让 5 个主 Tab 页共用同一个 TabBar 实例，保证像素级一致。

**核心原则**（来自用户强调）：
- **TabBar 是全局的**，不允许每个页面自己写
- 一次写好，所有页面用同一套
- 视觉、交互、active 状态高亮、badge、切换动画全部一致

**本 Phase 不做**：瀑布流、话题胶囊、首页具体内容（留给 Phase 4）。先把架构跑通。

---

## 设计稿规格（来源：screens-part1.jsx 行 30-89 AppTabBar + screens-part2.jsx 行 145）

### TabBar 容器
- `position: fixed`（绝对底部悬浮）
- `left: 14 right: 14 bottom: 16`（脱离屏幕边缘 14/14/16）
- `height: 60`
- `borderRadius: radius × 1.4`（≈ 22px）
- `background: rgba(255,255,255,0.72)` + `backdrop-filter: blur(24px) saturate(180%)`（毛玻璃）
- `box-shadow: 0 8px 28px primary@22, 0 2px 6px rgba(0,0,0,0.05), inset 0 1px 0 rgba(255,255,255,0.9)`
- `z-index: 30`

### 5 个 Tab

| ID | Label | 图标 | 特殊处理 |
|----|-------|------|---------|
| home | 首页 | HomeIcon | 默认 active |
| market | 市集 | BagIcon | — |
| publish | publish | PlusIcon | **悬浮按钮**：48×48, `marginTop: -18`（向上突出 18px），radius 16, 135° 渐变 primary→primaryInk, 阴影 |
| msg | 消息 | MsgIcon | 可带 badge（数字 / 小红点） |
| me | 我 | MeIcon | — |

### Tab 交互
- Active：图标 + label 颜色 `primary`，图标 `filled=true`，label weight 700
- Inactive：颜色 `inkMuted`，label weight 500
- 点击切换：`uni.switchTab` 或 `uni.reLaunch`（方案见下面"架构决策"）
- 发布按钮点击：navigate 到发布页（不是 tab 切换，是 push 新页面）
- Badge：`minWidth 16 height 16 padding 0 4px`，背景 `warn` 色，位置 `top -4 right -8`

---

## 架构决策（解决"全局一致性"问题）

### 方案对比

| 方案 | 机制 | 一致性 | 难点 |
|------|------|-------|------|
| **A. UniApp 原生 `pages.json` tabBar** | 配置式 | ✅ 系统级 | ❌ 无法实现毛玻璃 + 悬浮按钮设计稿 |
| **B. 原生 tabBar `custom: true` + 自定义组件** | 每 tab 页顶层渲染一个 `<AppTabBar>` | ✅ 小程序官方推荐 | ⚠️ H5 端要特殊处理 |
| **C. 关掉原生 tabBar + 每 tab 页引用 `<AppTabBar>`** | 靠 `uni.switchTab` / `uni.reLaunch` 跳转 | ✅ 简单直接 | ⚠️ pages.json 仍要声明 tabBar 页列表 |
| **D. MainLayout 包裹 + slot + AppTabBar** | 每 tab 页的 root 用 `<MainLayout>` 包 | ✅✅ 最强一致 | 要写布局组件 |

### 选中：**D 方案（MainLayout + AppTabBar）**

理由：
1. 设计稿 TabBar 是悬浮毛玻璃 + 悬浮发布按钮 → 原生 tabBar 做不到（排除 A）
2. 小程序 `custom-tab-bar` 机制在 uni-app 下还需要 pages.json 配 `"custom": true`，跨端行为不一致（B 扣分）
3. 让每个 tab 页自己引用 `<AppTabBar>` 组件，看起来简单但**容易不一致**（C 扣分 —— 这就是用户担心的场景）
4. **D 方案强制每个 tab 页用 `<MainLayout>` 包裹**，TabBar 是 MainLayout 的一部分，tab 页本身**根本接触不到 TabBar**，一致性从架构层保证

### 架构细节

```
MainLayout.vue
├── <view class="main-layout__content">
│   └── <slot />  ← tab 页内容
├── <AppTabBar :active="active" @tap="onTabTap" />
```

- `MainLayout` 接受 `active` prop（`home` / `market` / `msg` / `me`），传给 `AppTabBar`
- 每个 tab 页（home.vue / market.vue / message.vue / mine.vue）的模板根节点：
  ```vue
  <MainLayout active="home">
    <!-- 页面实际内容 -->
  </MainLayout>
  ```
- TabBar 切换逻辑封装在 `AppTabBar` 内部，用 `uni.switchTab` 跳转（前提：pages.json 声明 tabBar 页列表）
- 发布按钮点击：**不切换 tab**，而是 `uni.navigateTo` 到发布页（发布页独立路由，不在 tabBar 里）

### pages.json 配置策略

```json
"tabBar": {
  "custom": true,    // 彻底禁用系统渲染，仅保留 switchTab 路由能力
  "list": [
    { "pagePath": "pages/home/home", "text": "首页" },
    { "pagePath": "pages/market/market", "text": "市集" },
    { "pagePath": "pages/message/message", "text": "消息" },
    { "pagePath": "pages/mine/mine", "text": "我" }
  ]
}
```

**注意**：
- 发布页 `pages/publish/publish` **不在 tabBar list**（悬浮按钮是 navigate 不是 switchTab）
- 4 个 tab 页必须在 list，否则 `uni.switchTab` 不工作
- `custom: true` 在 H5 可能仍渲染原生 tabBar，worker 需验证（触发 gotchas #12 uview-plus 全局污染可能相关）—— **worker 必须在报告里说明各端渲染情况**

---

## 产出清单

### 新组件
- `src/components/ui/AppTabBar.vue` — 全局底部 TabBar（悬浮毛玻璃 + 5 tab + 悬浮发布按钮 + badge 支持）
- `src/components/ui/MainLayout.vue` — 布局组件（slot + AppTabBar，统一处理 safe-area）
- `src/components/ui/icons/` 目录下的 TabBar 图标：
  - `HomeIcon.vue`（有 filled / outline 两态）
  - `BagIcon.vue`（市集）
  - `PlusIcon.vue`（发布，复用已存在则引用）
  - `MsgIcon.vue`（消息）
  - `MeIcon.vue`（我）
  - 图标源从 `design/design-system.jsx` 扒（shared-components.jsx 可能也有）

### 新页面（5 个 tab 页壳 + 1 发布页占位）
- `src/pages/home/home.vue` — 首页壳（内部三 Tab：关注/发现/附近，**只做顶部结构 + 瀑布流占位 "Phase 4 实现"**）
- `src/pages/market/market.vue` — 市集占位（`<!-- TODO(design): 待设计师细化 市集首页 -->`）
- `src/pages/message/message.vue` — 消息占位（同上）
- `src/pages/mine/mine.vue` — 我占位（同上）
- `src/pages/publish/publish.vue` — 发布页占位（独立路由，不在 tabBar）

### 路由
- `src/pages.json` 更新：tabBar 配置 + 5 新路由 + 发布页路由
- 登录成功后的 `reLaunch` 目标从 `/pages/index/index` 改为 `/pages/home/home`
- Splash 的跳转同步更新

### 废弃
- `src/pages/index/index.vue`（原占位，已废弃）— 保留 404 重定向到首页，或直接删除

---

## 执行步骤

1. 读 `docs/uniapp-gotchas.md` 全部 15 条，特别关注：
   - **#5 `position: fixed` + transform 父元素**（TabBar 悬浮必踩）
   - **#12 uview-plus 全局污染**（可能影响 tabBar 自定义样式）
   - **#13 小程序页面栈 10 层 navigateSafe**（发布按钮 navigate 可能耗尽页面栈）
   - **#14 条件编译**（`custom: true` 可能在 H5 需要条件编译兜底）
   - **#15 生命周期跨端**（tab 切换触发 onShow 而非 onActivated）
2. 从设计稿扒 5 个图标 SVG（可能 shared-components.jsx 有定义，没有就参考 design-system.jsx 的 PawIcon 风格自制）
3. 实现 `AppTabBar.vue`（毛玻璃 + 悬浮按钮 + badge + active 高亮）
4. 实现 `MainLayout.vue`（slot + AppTabBar + safe-area）
5. 实现 5 个 tab 页（home 有内部三 tab 骨架，其他是占位）
6. `pages.json` 加 tabBar 配置 + 新路由
7. 改 Splash / Login reLaunch 目标
8. **视觉验证（按 dispatch-template 方案 A）**：
   - 启动 dev:h5
   - 请用户截图 home / market / message / mine 四个 tab 的 TabBar 渲染
   - 点击切换 tab 验证 active 状态切换
   - 点击发布按钮验证 navigate 到发布页
9. 报告按 dispatch-template 10 项格式

---

## 验收 Harness

### Functional
- 5 个 tab 页 + 发布页路由注册
- 4 个 tab 页都用 `MainLayout` 包裹，TabBar 实例一致
- TabBar 悬浮毛玻璃效果（截图可见）
- 发布按钮悬浮向上突出 18px
- 消息 tab 默认显示 badge=3（mock）
- active 高亮切换正确
- 点击发布按钮 navigate 到 publish 页
- 从 Login 成功后跳 home 而非 index

### Quality
- 所有颜色 / 字号 / 阴影 / 圆角来自 tokens（检查 rgba 硬编码已用 `rgba($color-primary, N)` 函数）
- AppTabBar 是**唯一**一份 TabBar 代码，全局复用（grep 确认别处没有重复的 tabBar 样式）
- MainLayout 布局正确处理 safe-area-inset-bottom（iPhone 刘海屏）
- TypeScript 严格模式零新错
- 触发并处理 gotchas #5 / #12 / #13 / #14 / #15（按原则 9 报告必须引用编号）

### Delivery（视觉验证按 dispatch-template 方案 A）
- 启动 dev:h5 + 用户在 iPhone 13 Pro 视口分别截图 4 个 tab 页的 TabBar
- 贴设计稿截图对比
- reviewer 独审通过
- git commit + push

**指标优先级**：`tab_bar_consistency > pixel_match > token_alignment > correctness`

---

## 风险与预案

| 风险 | 预案 |
|------|------|
| `backdrop-filter` 在小程序端不支持 | 条件编译 `#ifdef MP-WEIXIN` 回退为半透明纯色（rgba），记入 gotchas |
| `pages.json` tabBar `custom: true` 在 H5 仍渲染底部原生条 | worker 在 H5 / 小程序端分别验证，必要时 CSS `display: none` 藏掉 |
| 发布按钮 `marginTop: -18` 溢出 TabBar 容器 | 容器 `overflow: visible`（确认） |
| 用户点击 tab 图标外围导致不响应 | 整个 tab 区域（含 label）都有 `@tap`，flex 撑满 |
| `uni.switchTab` 必须 pagePath 在 tabBar list 里 | 严格维护 list；publish 不在里面，用 navigateTo |
| 页面栈 10 层（gotchas #13）| 发布按钮用封装的 `navigateSafe()` |

---

## 结项交付物

- `AppTabBar.vue` + `MainLayout.vue` + 5 个图标组件
- `pages/home/home.vue`（含三 tab 骨架 + 瀑布流占位）
- `pages/market/market.vue` / `pages/message/message.vue` / `pages/mine/mine.vue`（占位）
- `pages/publish/publish.vue`（占位）
- `pages.json` 更新
- `pages/splash/splash.vue` + `pages/login/login.vue` + `pages/login/phone-login.vue` 的跳转目标更新
- 视觉验证截图用户确认通过
- Reviewer 独审 PASS
- git commit + push

---

## 后续衔接

Phase 3 完成后 → **Phase 4**：首页瀑布流（关注/发现/附近三 feed + WaterFall 虚拟化 + NoteCard + TopicPill 话题胶囊）

---

## 实验账本

| round | persona | 假设 | 改动 | 验收 | status | decision | notes |
|-------|---------|------|------|------|--------|---------|-------|
| 1a | frontend-developer (worker 初次派发) | 按派发单直接实现 14 个产出 | 无（worker 误读 CLAUDE.md 规则 5/6 为自己角色，停下来问"我是主线程还是 sub-agent"） | 无产出 | abort | 主线程重新派发 + 澄清 | **meta-gotcha**：sub-agent 读项目 CLAUDE.md 时会看到"plan before execute / 走 autoresearch-router"等规则，误以为自己需要再做一轮 plan + dispatch。需要在 dispatch-template.md 加标准开场：**"YOU ARE the worker, not the main thread. 规则 5/6 是给主线程的，不是给你。直接执行。"** |
| 1b | frontend-developer (worker 重派) | 同上 + 角色澄清 | 14 新文件（AppTabBar / MainLayout / 5 图标 / 5 tab 页 / publish / navigate.ts）+ 4 微改（pages.json / Splash / Login / Phone-Login） | worker 自报：gotchas #5/#11/#12/#13/#14 全处理；TS 0 新错；mp-weixin 构建 exit=0 | success 自报 | 待 Round 2 reviewer 验 + 用户视觉验收 | home 默认 tab=discover；发布按钮走 navigateSafe；message badge=3 mock；dev server @ port 5175 保持运行 |
| 2a | reality-checker (reviewer) | 代码层审查：grep 验证 TabBar 唯一性 / gotchas 证据 / 跳转同步 / 实跑 tsc + build | 11 项报告完成 | 均分 4.43/5：TabBar 一致性 5 / gotchas 4 / 视觉规格 3 / pages.json 5 / 跳转 5 / TS+跨端 5 / 回归 4 | success | NEEDS WORK（1 硬伤：发布按钮 margin-top -72rpx 应为 -36rpx，2× 偏差） | 所有 gotchas 处理到位；mp-weixin 实测 build exit=0；只差 1 字符 |
| 2a' | 主 agent | 修复 reviewer 发现的 margin-top 硬伤 | AppTabBar.vue L220: -72rpx → -36rpx | 修复完成 | success | keep pending 用户视觉 | 1 字符修复 |
| 2b | 用户（视觉验收 Round 1） | 视觉层验证 | 用户发现 2 个交互问题：1）顶部三 Tab 不支持左右滑；2）瀑布流滚动时顶部导航栏不应跟着滚 | 用户反馈 | success | 需要修 | Worker 对 UX 规范理解不足，设计稿没明写"左右滑 + 固定顶部"但是小红书型产品常识 |
| 3a | 主 agent | home.vue 改造：顶部栏固定 + swiper 左右滑 + 每 tab 独立 scroll-view | 重写 home.vue 布局（flex column + swiper + scroll-view）| Vite 热重载 + 用户再验收 | success | 用户口头 PASS | 沉淀 gotcha #16 |
| 3b | 主 agent | 沉淀 meta-gotcha：sub-agent 角色误读 | 新增 gotcha #17 + dispatch-template.md 顶部加"你的角色"固定开场 | grep "你的角色" 命中 | success | keep | 避免未来 Phase 再踩 |
| 2b' | 用户（视觉验收 Round 2） | 复验交互修正后效果 | "目前看起来还可以" | 口头通过 | success | **keep** | 遗留：部分设计细节留给后续迭代 |
