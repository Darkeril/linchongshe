# Worker 派发 Prompt 模板

**最近更新**：2026-04-22（Round 2 修订：
- 视觉验证流程由 `curl` 拉 SPA HTML 改为"截图验证（主）+ Playwright 可选（辅）"，因 UniApp H5 是 SPA hash 路由，curl 拿不到子路由实际 DOM；
- 报告项从 8 项扩为 10 项，新增"回归检查"与"跨端 sanity check"；
- 在"硬性约束"新增"报告必须引用 gotchas 条目编号"规则）

所有 autoresearch-router 派发的 worker（实现类任务）必须按本模板写 prompt。主线程每次 dispatch 前复制模板 → 填具体内容 → 提交给 Agent 工具。

## 模板

````markdown
## Persona Behavior 注入
<!-- 从 persona-behaviors.md 抓取对应 persona 的完整行为指令粘贴于此。
     通常是 frontend-developer / nvue-specialist / form-specialist 之一。 -->

---

## 必读（按顺序）

1. **`docs/uniapp-gotchas.md`** — UniApp 已知陷阱清单（**强制**，每次派发必读）
2. **本 Phase 计划文档** `docs/plans/phase-<N>-<主题>.md`
3. **`CLAUDE.md`** 相关原则（像素级还原、组件分层 base/ui/biz、API 规范、货币体系）
4. **设计稿源文件**：
   - `design/design-system.jsx`（tokens）
   - `design/shared-components.jsx`（通用控件）
   - `design/screens-part<X>.jsx`（本任务页面）

---

## 任务目标

<!-- 一句话说明本次要交付什么页面 / 组件 / 功能；指向设计稿对应屏幕名。 -->

## 产出清单

| 路径 | 职责 |
|------|------|
| `aichongshe-app/src/pages/xxx/xxx.vue` | 页面入口 |
| `aichongshe-app/src/components/ui/XxxCard.vue` | 新增 UI 组件 |
| `aichongshe-app/src/api/mock/xxx.mock.ts` | Mock 数据 |
| `aichongshe-app/src/types/xxx.ts` | 类型定义 |
<!-- 每项必填：路径 + 该文件的唯一职责 -->

## 硬性约束

- **像素级还原**：颜色 / 间距 / 字号 / 圆角 / 阴影严格对齐设计稿，不猜。
- **所有尺寸走 `rpx`**，颜色 / 字号走 `src/styles/tokens.scss` 变量，严禁硬编码。
- **组件分层**：只能引用 `components/base|ui|biz/`，不跨层；uview-plus 原生组件外层包 `base/`。
- **点击事件**：全项目统一用 `@tap`（风格约定，与移动端触摸语义对齐；注意 gotchas #11 —— `@tap` 不会"更快"，别再传"消除 300ms 延迟"的错误说法）。
- **`<button>` 必须按 gotchas #1 覆盖默认样式**；优先 `<view @tap>` 替代。
- **API 规范**：HTTP 方法语义严格（读 GET / 写 POST|PUT|PATCH|DELETE），token 走 Header，禁止 URL query。
- **Mock 优先**：所有接口都先出 mock，符合后端 TS 类型。
- **无设计稿页面** → 根元素标 `<!-- TODO(design): 待设计师细化 [页面名] -->`。
- **报告必须引用 gotchas 条目编号**（**重要**）：报告第 4 项中必须以编号形式引用 `docs/uniapp-gotchas.md` 的具体条目（例如"触发 #1 `<button>` 默认样式，按正确做法覆盖 `width: 100%` + `::after { display: none }`；触发 #11 `@tap` 风格约定，所有点击已用 `@tap`"），或**明确声明"本次实现未触发任何 gotchas 条目"**。报告里既没有编号引用也没有明确声明 → **主线程视为未读 gotchas，整次派发判定未完成，退回重做**。
- **跨端导航必须走 `navigateSafe()` 封装**（gotchas #13）：禁止散落 `uni.navigateTo`。
- **条件编译指令格式严格**（gotchas #14）：大小写、注释符、平台名拼写必须准确，否则静默失效。

## 视觉验证流程（强制）

> **为什么不再用 curl**：UniApp H5 是 Vite + Vue3 SPA，默认 hash 路由（`/#/pages/xxx`）。`curl http://localhost:5173/#/pages/login/login` **只能拿到首页骨架 HTML**（`<div id="app"></div>` 之类），子路由的真实 DOM 要等 JS 在浏览器端路由后 mount。Round 1 模板用 `curl + grep` 的验证步骤**形同虚设**——Phase 2 按钮溢出的坑当初就是 curl 查不出来的。

完成实现后**必须**选以下方案之一执行，并在报告中贴证据。**派发时主线程在下方勾选**走哪种（默认 A，复杂动效/难复现的 bug 才启用 B）：

- [x] **方案 A：真实浏览器截图验证（默认）**
- [ ] **方案 B：Playwright headless DOM dump（可选加强）**

### 方案 A：真实浏览器截图验证（主，默认走这条）

1. Worker 启动 `dev:h5`：
   ```bash
   cd aichongshe-app && npm run dev:h5 > /tmp/dispatch-verify.log 2>&1 &
   echo $! > /tmp/dispatch-dev.pid
   ```
2. 等待编译完成（`tail -f /tmp/dispatch-verify.log` 见到 `Local: http://localhost:5173/`）。
3. Worker 在报告里**明确告知主线程**："请在 Chrome DevTools 切到 iPhone 13 Pro（390×844 DPR3）视口，访问 `http://localhost:5173/#/pages/<path>`，截图本页面并贴到对话中"。
4. 用户（或主线程代用户）执行截图并回传。Worker 拿到截图后在报告里**逐一比对设计稿**：颜色 / 间距 / 控件位置 / 字号是否一致。
5. 关停 dev：`kill $(cat /tmp/dispatch-dev.pid)`。

**为什么这样设计**：零依赖，用户任何时候可以自己验；截图是**真实渲染结果**，比任何 DOM dump 都直观；不会被 "HTML 有这个 class 但视觉上其实是错的"（例如被父元素 `overflow: hidden` 裁掉）这种情况骗过。

### 方案 B：Playwright headless DOM dump（辅，按需启用）

适用场景：当截图一眼看不出问题（例如事件绑定、ARIA、动态样式）需要机器可验证时启用。

1. Worker 在 `aichongshe-app` 下安装 Playwright（如未安装）：
   ```bash
   npm install -D @playwright/test
   npx playwright install chromium
   ```
2. 启动 dev:h5（同方案 A 步骤 1）。
3. 写临时脚本 `/tmp/visual-verify.mjs`：
   ```js
   import { chromium } from 'playwright'
   const browser = await chromium.launch()
   const ctx = await browser.newContext({ viewport: { width: 390, height: 844 }, deviceScaleFactor: 3 })
   const page = await ctx.newPage()
   await page.goto('http://localhost:5173/#/pages/login/login', { waitUntil: 'networkidle' })
   await page.waitForTimeout(500)  // 兜底等 Vue mount
   const html = await page.content()
   await page.screenshot({ path: '/tmp/visual-verify-login.png', fullPage: true })
   console.log(html)
   await browser.close()
   ```
4. 运行并保存产物：
   ```bash
   node /tmp/visual-verify.mjs > /tmp/visual-verify-login.html
   ```
5. 对关键控件 grep：
   ```bash
   grep -E 'class="[^"]*login__btn' /tmp/visual-verify-login.html
   ```
6. 报告里附：Playwright 脚本链接 + `/tmp/visual-verify-<page>.png` 截图链接 + DOM 片段。

**方案 B 注意**：爱宠社目前未装 Playwright，首次启用需要装依赖，派发前主线程确认用户同意。

### 不论走 A 还是 B，以下红线一致

- **代码编译通过 ≠ 视觉正确**，未做视觉验证的 worker 产出视为未完成。
- Reviewer（reality-checker）必须独立审查 worker 的视觉验证证据是否真实可信（截图是否就是子页面、DOM 片段是否与 worker 的描述匹配）。

## 报告格式（固定 10 项，缺一即判定未完成）

1. **产出文件清单**（逐项 ✅，含行数概览）
2. **dev:h5 启动日志 tail**（`tail -30 /tmp/dispatch-verify.log`）
3. **视觉验证证据**：
   - 走方案 A：贴**截图**（用户回传的 iPhone 13 Pro 视口下子页面实际渲染截图）+ 1~2 段对比描述（"主按钮宽度 = 100%、圆角 52rpx、与设计稿一致"）
   - 走方案 B：贴 `/tmp/visual-verify-<page>.png` 截图 + DOM 片段（关键控件 class / inline style）+ Playwright 脚本路径
4. **触发的 gotchas 及处理方式**（**必须引用条目编号**，如"触发 #1 / #11 / #13"并说明如何规避；**未触发也必须明确写"无"**，否则视为未读 gotchas）
5. **关键决策列表**（选型 / 取舍 / 设计稿歧义处理）
6. **本次 Mock 数据清单**（列出新增 mock services / types / 假数据结构，逐项一行：路径 + 核心字段 + 数据规模，例如"`src/api/mock/note.mock.ts` - Note(id,title,cover,author) × 20 条模拟笔记列表"）
7. **风险提示**（已知但未处理的问题 / 跨端兼容未验证点）
8. **TypeScript 检查结果**（`cd aichongshe-app && npx tsc --noEmit` 输出，必须零错误）
9. **回归检查**：列出本次改动**可能影响**的已完成页面/组件，逐项说明为何不受影响（例如"本次只新增 `ui/PetAvatar.vue` 并在笔记详情页引用，未改 Splash / Login / HomeTabs 已有样式，视为无回归"）。如确有影响，需在报告里贴影响页面的视觉复测结果。
10. **跨端 sanity check**：至少跑**一次**其他端编译证明没破：
    - 小程序走 `cd aichongshe-app && npm run build:mp-weixin` 或 `npm run dev:mp-weixin`（贴 tail 30）
    - App 端走 `npm run build:app` 若环境支持
    - 最低要求：至少一条其他端编译通过（编译 error 数为 0，warning 可容忍）

## 遗留 TODO（含 `TODO(design)` 标注清单 + 后端依赖登记）

<!-- 作为报告附录单列一节，不计入 10 项。 -->

## 你不做

- 不 `git commit` / `git push`（主线程统一处理）
- 不改本 Phase 任务清单**外**的任何文件
- 不改 `CLAUDE.md` / `docs/uniapp-gotchas.md`（如踩到新坑，在报告"触发的 gotchas"章节建议新增条目，主线程决定是否收录）
- 不自行降级模型 / 推理强度
````

## 使用说明

1. 主线程每次派发时复制本模板（三反引号 code fence 内内容），填入 persona / 路径 / 任务描述 / 设计稿屏幕名。
2. **视觉验证方案** 在模板中勾选走 A 还是 B（默认 A，复杂交互才启用 B）。
3. 填写后立即用 Agent 工具派发（worker + reviewer 成对），Boot Block 必须完整打印。
4. 接收 worker 报告后：
   - **核对 10 项报告是否齐全**，任一缺失立即回退重补，不得接受不完整交付。
   - **核对报告第 4 项是否引用 gotchas 编号**（形如 `#1` / `#11` / `#13`）或明确声明"无触发"；只要既无编号又无声明 → 直接判定未读 gotchas，退回重做。
5. 若 worker 在"触发的 gotchas"栏建议新增条目，主线程审定后直接 PR 进 `docs/uniapp-gotchas.md`。
6. 派发模板自身有更新时，在本文档顶部加一行"最近更新：YYYY-MM-DD"并说明变更要点。
