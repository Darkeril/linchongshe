# Phase 0：aichongshe-app 启动前准备

- **创建日期**：2026-04-22
- **状态**：✅ 已完成（见提交 3185202：新建 aichongshe-app v2 骨架并产出交互图谱和后端 API 盘点）
- **执行方式**：通过 `autoresearch-router` 技能驱动，子 agent 继承当前线程的模型与推理强度

---

## 目标

把 `aichongshe-app` 的骨架和基础设施搭好，让后续页面的**像素级还原**有一个干净、对齐 CLAUDE.md 规则的工作环境。

---

## 前置环境

| 项目 | 版本 / 状态 |
|------|-------------|
| Node | v25.9.0（过新，可能需降至 Node 20 LTS） |
| npm | 11.12.1 |
| HBuilderX | 5.07（2026.04） |
| 当前分支 | `feature/phase1-ui-redesign` |
| CLAUDE.md（项目级） | 已 commit：`8dc93db` |

---

## 技术决策锁定（Q1-Q5 用户答复）

1. **脚手架**：`@dcloudio/uni-cli` + Vite-TS 模板（非 HBuilderX GUI）
2. **HBuilderX**：已装，5.07 版
3. **新分支**：`feature/v2-aichongshe-app`（从 `feature/phase1-ui-redesign` 切出）
4. **API 盘点颗粒度**：**一次性**全量盘点（不是按页面按需盘点）
5. **设计 Token**：产出 `tokens.scss` + `tokens.ts` 双源同值

---

## 执行步骤

### 步骤 1：切分支

- 从 `feature/phase1-ui-redesign` 切出 `feature/v2-aichongshe-app`
- **验证**：`git branch` 显示新分支为当前分支

### 步骤 2：通读设计稿，输出「交互图谱 + 盲点清单」

- 阅读 `design/` 下：`design-system.jsx` / `shared-components.jsx` / `screens-part1~12.jsx`
- 输出 `design/interaction-map.md`：
  - 每个页面的入口、出口、主要控件、跳转关系
  - 设计稿无法推断的业务规则（用户需要补答的盲点清单）
- **验证**：文档覆盖 12 个 screens 的交互流 + 至少 20 条盲点提问

### 步骤 3：后端 API 一次性全量盘点

- 扫描 `springcloud_hongshu/` 所有 Controller / API 端点
- 输出 `aichongshe-app/docs/api-inventory.md`：
  - 模块分组（user / note / comment / like / follow / topic / message / ...）
  - 每个 API：`method`、`path`、入参、返回结构
  - 与设计稿功能交叉对比后，标注：✅ 可用 / ⚠️ 需改 / ❌ 未实现
- **验证**：文档覆盖所有 Controller，且能回答"设计稿里哪些功能后端还缺"

### 步骤 4：用 @dcloudio/uni-cli 创建 aichongshe-app

- 在 `linchong_src/` 下创建 `aichongshe-app/` 目录
- 使用官方 Vue3 + Vite + TS 模板（`uni-preset-vue#vite-ts`）
- 验证四端编译：H5 / 微信小程序 / 抖音小程序 / App
- **验证**：空项目 `uni run h5` 能启动不报错
- **风险**：Node v25 可能导致依赖安装失败，若失败则提示用户切 Node 20 LTS

### 步骤 5：搭目录结构（对齐 CLAUDE.md 第 4 章）

```
aichongshe-app/
├── src/
│   ├── pages/                  ← 页面（vue 或 nvue）
│   ├── components/
│   │   ├── base/              ← uview-plus 控件层
│   │   ├── ui/                ← 设计稿特色组件（latte 主题）
│   │   └── biz/               ← 业务组件
│   ├── api/
│   │   ├── services/          ← 业务 API（真假同签名）
│   │   ├── mock/              ← 假数据
│   │   └── request.ts         ← VITE_USE_MOCK 开关
│   ├── stores/                ← Pinia
│   ├── types/                 ← TS 接口类型（对齐后端）
│   ├── styles/
│   │   ├── tokens.scss        ← 设计 token（vue 页用）
│   │   └── tokens.ts          ← 设计 token（nvue 页用）
│   └── utils/
├── docs/
│   ├── api-inventory.md       ← 步骤 3 输出
│   └── api-todo.md            ← 后续按页面填
└── .env.development / .env.production
```

- **验证**：目录齐全，根目录 README 说明分层原则

### 步骤 6：Design Token 模块

- 从 `design/design-system.jsx` 提取 `color` / `spacing` / `radius` / `shadow` / `typography`
- 生成 `src/styles/tokens.scss` + `src/styles/tokens.ts`（同源同值）
- 配置 Vite 全局 SCSS 注入
- **验证**：
  - vue 页：`@use "@/styles/tokens.scss"` 能拿到 `$primary`
  - nvue 页：`import tokens from '@/styles/tokens'` 能拿到 `tokens.primary`

### 步骤 7：集成 uview-plus + latte 主题覆盖

- 安装 uview-plus，按官方文档接入 Vue3 + setup
- 用 tokens.scss 覆盖 uview-plus 主题变量
- **验证**：`<u-button type="primary">` 渲染为 latte 焦糖色 `#C97B4A`

### 步骤 8：Mock 数据开关骨架

- `src/api/request.ts` 实现 `VITE_USE_MOCK` 开关
- 写一个示例 service（如 `user.getProfile`）真假双实现
- **验证**：开关切换后 console 打印来源（mock / real）

### 步骤 9：公共 Layout + 路由骨架

- `App.vue` 初始化逻辑
- `pages.json` 配置空的 Splash 占位路由
- **验证**：`uni run h5` 打开默认进 Splash 空页面

### 步骤 10：Phase 0 结项

- 所有产出 git commit
- 推送到 `origin/feature/v2-aichongshe-app`
- **验证**：远程分支有完整 Phase 0 代码，用户能 pull 下来本地跑起

---

## 并行策略

- **步骤 2、3**（文档调研）与 **步骤 4-9**（工程搭建）可并行
- 文档产出耗时但不阻塞工程搭建
- 工程搭建需要的是环境和依赖

---

## 风险与预案

| 风险 | 预案 |
|------|------|
| Node v25 依赖安装报错 | 提示用户装 nvm + 切 Node 20 LTS |
| uview-plus 与 uni-preset-vue 版本冲突 | 查官方兼容矩阵，必要时锁依赖版本 |
| nvue 的 SCSS 变量无法直接用 | 已经通过 tokens.ts 双源方案规避 |
| 后端 API 命名/路径不规范 | api-inventory.md 如实记录，不篡改 |

---

## 结项交付物

- [x] `feature/v2-aichongshe-app` 分支推送到远端
- [x] `design/interaction-map.md` — 交互图谱 + 盲点清单
- [x] `aichongshe-app/docs/api-inventory.md` — 后端 API 全量盘点
- [x] `aichongshe-app/` — 完整可运行骨架
- [x] Phase 0 completion commit

---

## 后续衔接

Phase 0 完成后进入 **Phase 1：Splash 启动页**（按设计稿 `screens-part1` 像素级还原）。
Phase 1 计划另起文档 `docs/plans/phase-1-splash.md`。
