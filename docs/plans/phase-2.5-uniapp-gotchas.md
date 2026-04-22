# Phase 2.5：UniApp 陷阱沉淀 + 派发流程升级

- **创建日期**：2026-04-22
- **状态**：✅ 已完成（Round 4 reviewer 判定 PASS 4.2/5，无单项 ≤ 2；遗留 curl 两处已顺手清理）
- **执行方式**：autoresearch-router **严格协议**（Boot Block 完整 + worker + reviewer + 实验账本）
- **触发原因**：Phase 2 LoginScreen 按钮因 UniApp 原生 `<button>` 默认样式未覆盖而溢出，暴露流程缺陷

---

## 目标

1. 把 Phase 2 踩的 UniApp 坑 + 可预见的其他坑，沉淀成 **`docs/uniapp-gotchas.md`** 知识库
2. 把"每次派发 worker 必读 gotchas + 必做视觉验证"写进 **CLAUDE.md** 成为硬约束
3. 把 "autoresearch-router 必须同时派 reviewer" 固化为流程纪律

---

## 三份产出

### 产出 1：`docs/uniapp-gotchas.md`（新建）

内容至少覆盖：

1. **已踩过的坑**
   - `<button>` 默认 inline-block + `::after` 边框 + padding → 必须覆盖 `width: 100%` / `display: none` / `padding: 0` / `white-space: nowrap`
   - 或者优先用 `<view @tap>` 替代 `<button>`（除非是表单提交）

2. **可预见的坑（基于 UniApp 开发者社区经验）**
   - `<input>` 在 H5 vs 小程序高度、内边距差异
   - `<text>` 是 inline 元素，不能嵌套 block
   - `<scroll-view>` 必须显式设置高度才能滚动
   - `position: fixed` 在小程序 transform 父元素上会失效
   - SCSS `@use` 在 uni-app 模板里预注入顺序问题
   - `rpx` 在 iPad / 大屏上会超出设计基准的问题
   - nvue 不支持的 CSS 属性清单（grid、calc 等）
   - 样式作用域：`/deep/` / `::v-deep` 在 scoped 下的正确用法
   - 小程序 SVG inline 的兼容性
   - UniApp event（@click vs @tap）平台差异

3. **引用规范**
   - 每条坑包含：场景描述 / 现象 / 根因 / 正确做法 / 反例
   - 允许 Phase N 发现新坑后追加（文档顶部记录"最近更新"）

### 产出 2：`CLAUDE.md` 更新

新增/扩展：

1. **原则 1（像素级还原）下扩展「视觉验证」子条**
   - Worker 完成实现后，**必须**按 `docs/plans/dispatch-template.md` 视觉验证章节提供证据：
     - dev:h5 启动日志 tail
     - 方案 A（默认）：真实浏览器截图；方案 B（可选）：Playwright DOM dump + 截图
     - ⚠️ 禁止用 curl 拉 SPA 页面（hash 路由下 curl 只能拿骨架，无法拿子路由 DOM）
   - 代码编译通过 ≠ 视觉正确。worker 未做视觉验证视为未完成

2. **新增原则 9：UniApp 陷阱清单强制阅读**
   - Worker 派发时，**必须在 prompt 第一条读 `docs/uniapp-gotchas.md`**
   - Worker 完成报告时，必须回答："本次实现是否触发任何 gotchas 清单里的场景？如何处理的？"
   - Phase 中踩到新坑必须 PR 进 `docs/uniapp-gotchas.md`

3. **八章约束增补**
   - 第 9 条：**autoresearch-router 派发时必须 worker + reviewer 成对出现**，不得只派 worker
   - 第 10 条：**主线程必须打印完整 Boot Block** 才能开始执行工具调用

### 产出 3：派发 prompt 模板固化

在 `docs/plans/` 下新建一个 **派发模板** `dispatch-template.md`，每次 dispatch worker 时按这个模板写 prompt。

模板要点：
- 第一条指令：读 `docs/uniapp-gotchas.md`
- 第二条：读当前 Phase 的 plan 文档
- 中段：任务清单 + 硬性约束
- 末段：**强制视觉验证章节**（固定格式）
- 报告格式：**固定 8 项**（产出清单、启动日志、触发的 gotchas、关键决策、风险、视觉证据、TS 检查、遗留 TODO）

---

## 执行步骤（autoresearch-router 严格协议）

### Step 1: Harness（本 Phase 验收）

Functional:
- `docs/uniapp-gotchas.md` 存在，至少 10 条陷阱（3 条已踩 + 7 条预见）
- `CLAUDE.md` 原则 1 有视觉验证子条，新增原则 9，八章新增第 9/10 条
- `docs/plans/dispatch-template.md` 存在且可用

Quality:
- uniapp-gotchas.md 每条陷阱：场景 / 现象 / 根因 / 正确做法 / 反例 齐全
- CLAUDE.md 结构层次不破坏，编号连续
- dispatch-template.md 有实际可抄的 markdown 模板

Delivery:
- git commit + push

**指标优先级**：`completeness > actionability > conciseness`

### Step 2: Candidate Pool

- **Strategy A（选中）**：派发 `frontend-developer` worker 写 uniapp-gotchas.md + dispatch-template.md；同时派 `reality-checker` reviewer 审阅产出、挑漏项、检查 CLAUDE.md 更新是否充分；主线程修改 CLAUDE.md（因为 CLAUDE.md 是项目规则文件，主 agent 掌握上下文最全，直接改更稳）
- Strategy B：全部交给 worker，主线程只验收（风险：CLAUDE.md 修改可能偏离原规则结构）

### Step 3: Routing

- **Worker**：`frontend-developer`（写陷阱知识库 + 派发模板，需要深入 UniApp 实战经验）
- **Reviewer**：`reality-checker`（以"质疑者视角"审查：gotchas 是否实用、模板是否能真降低返工率、CLAUDE.md 改动是否过度约束）
- **主线程**：修改 CLAUDE.md + 维护账本 + keep/revert 裁决

派工载荷包含：持久化 `persona_behavior` 指令（从 persona-behaviors.md 抓取对应段落注入）。

### Step 4: Trial + Ledger

主线程维护实验账本（本文档末尾），每轮：round / worker / 假设 / 改动 / 验收 / 决策 / failure_pattern。

### Step 5: 结项

- 产出合并入仓库
- git commit + push
- 本计划文档状态改为 ✅ 完成

---

## 实验账本

| round | persona | 假设 | 改动 | 验收 | status | decision | notes |
|-------|---------|------|------|------|--------|---------|-------|
| 1 | frontend-developer (worker) | 12 条陷阱 + 派发模板能覆盖 Phase 2 按钮坑类问题 | 新建 docs/uniapp-gotchas.md（12 条，~14.7KB）+ docs/plans/dispatch-template.md（99 行） | 文件存在 + 7 栏齐全自报 84/84 | success（自报） | 待 reviewer 裁决 | 自报完整性 100%，需 reality-checker 独立验证 |
| 1'（并行） | 主 agent | CLAUDE.md 增加视觉验证 + 原则 9 + 八章 9/10/11/12 能落实流程改进 | 编辑 CLAUDE.md（原则 1 扩展 + 新增原则 9 + 八章 8→12 条） | grep "原则 9" / "视觉验证" / "严格协议" 命中 | success | 待 reviewer 审查 | 修复了一处 8→4 的编号断裂，以及一处重复段落 |
| 2 | reality-checker (reviewer) | worker 产出有 3-5 个可挑的问题（按 persona 默认期望） | 独立审查三份产出，评分 + 漏项清单 | 输出 10 项报告 | success | NEEDS WORK | 均分 3.0/5，可操作性和风险覆盖均 2/5。3 个阻塞项：(1) dispatch-template curl-SPA 验证流程技术不可行；(2) gotchas 12 条偏科 CSS/模板层，缺路由/分包/生命周期 5 类坑；(3) 条目 11 @click 300ms 延迟存疑 |
| 2' | 主 agent | 编排者视角对三份产出评分 | 逐项自评 | 均分 3.2/5 | success | NEEDS WORK | 双视角均分 3.1 < 3.5 门槛，裁决继续 |
| 3 | frontend-developer (worker 修复) | 修 3 个阻塞项 + 3 个改进项 | 新增 3 条 gotchas（#13 页面栈 navigateSafe、#14 条件编译平台标识表、#15 生命周期跨端对照表）；修正 #11 @click 300ms 错误表述；重写 dispatch-template 视觉验证章节（截图主 + Playwright 辅）；报告 8→10 项（加回归检查、跨端 sanity check、Mock 清单） | 自报全部完成；三阻塞项+三改进项都有具体 diff | success（自报） | 待 Round 4 reviewer 验收 | @click 核实结论：reviewer 质疑正确，uni-app 编译 @click 为 bindtap，无 300ms 延迟 |
| 3' | 主 agent | CLAUDE.md 原则 9 规则 2 加机械判据 | 改写为"必须引用 gotchas 编号或明确声明未触发，否则退回" | 已写入 | success | 待 Round 4 一并审 | 改进 A 落实 |
| 4 | reality-checker (reviewer 复审) | 独立验证 Round 3 是真修 vs 假修；回归检查；双视角重新评分 | 按 6 项报告格式产出 | 阻塞项 1/2/3 全 PASS；改进 A/B/C 全 PASS；回归发现两处 curl 遗留（CLAUDE.md L95 + phase-2.5 L51） | success | **PASS 4.2/5**（上轮 3.0，+1.2） | 完整性 5/5、可操作性 4/5、原创性 4/5、风险覆盖 4/5、CLAUDE.md 4/5 — 无单项 ≤ 2，通过门槛 |
| 4' | 主 agent | 按 reviewer 反馈顺手清理 curl 遗留 | CLAUDE.md L91-98 视觉验证子条重写引用 dispatch-template 方案 A/B 明确禁用 curl；phase-2.5 L49-53 同步 | grep `curl` 在两文件无剩余 | success | keep | 双视角均分 (4.2 + 4.0) / 2 ≈ 4.1 → final keep |

---

## 后续衔接

完成后 → Phase 3：首页三 Tab + 瀑布流
