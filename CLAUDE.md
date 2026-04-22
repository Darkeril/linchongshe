# 爱宠社 linchongshe 项目规则

本文件是 **项目级** CLAUDE.md，会与全局 `~/.claude/CLAUDE.md` 叠加生效。
全局规则定义通用行为（计划先行、外科手术式改动、autoresearch-router 执行等），本文件定义爱宠社项目特有的约定。

---

## 一、项目定位

爱宠社（AiChongShe）是一个**图文社区类商业产品**，对标小红书 / 得物等内容社区。
- 商业级项目，运营侧已就位，有流量保障
- 需要**商业级体验和优化**，不是个人作品或 MVP
- 开发周期：**6 个月**（Claude Code / Codex 全程辅助）

---

## 二、仓库结构

```
linchong_src/
├── design/                    ← 设计稿（JSX 格式 + 参考图）
├── aichongshe-app/            ← 移动端 v2（本项目主战场，从零新建）
├── uniapp_hongshu/            ← 移动端老版（保留参考，不再改动）
├── vue_web_hongshu/           ← Web 前端（后期再做）
├── vue_arco_admin_hongshu/    ← 管理后台
├── vue_ai_hongshu/            ← AI 应用
└── springcloud_hongshu/       ← Spring Cloud 后端
```

**老目录 `uniapp_hongshu` 不再接受改动**，所有移动端开发在 `aichongshe-app` 进行。

---

## 三、移动端技术栈（aichongshe-app）

### 锁定技术选型

| 层 | 方案 |
|----|------|
| 框架 | UniApp（Vue3 + `<script setup>` + TypeScript） |
| 渲染模式 | **Vue 页面 + nvue 混合**：核心展示页用 nvue（真原生渲染），表单/设置页用 vue |
| 状态管理 | Pinia |
| UI 组件库 | **uview-plus**（基础控件）+ 自研 `components/ui/`（设计稿特色组件） |
| 类型系统 | TypeScript 全量覆盖 |
| 数据层 | 假数据先行，通过 `VITE_USE_MOCK` 环境变量切换真假 API |

### 不选的路线及原因

- ❌ **uni-app X (uvue) 全栈**：2025 年 iOS 端生态未完全成熟，商业项目不押宝
- ❌ **Flutter / React Native**：都不能覆盖微信/抖音小程序
- ❌ **原生双栈（Swift + Kotlin）**：6 个月 + 小团队做不完四端

### 四端覆盖

**一开始就必须同步兼容**：
- iOS（原生打包）
- Android（原生打包）
- 微信小程序
- 抖音小程序

**Web 端不在当前优先级内**。

### 性能优化要求（P0 级，不可妥协）

1. **瀑布流等列表必须虚拟化**，禁止无脑 `v-for` 渲染长列表
2. **图片管线**：CDN 自适应 WebP/AVIF + 按 DPR 下发 + 列表缩略图/详情高清分级
3. **骨架屏 + 乐观更新**：点赞/关注等交互本地先变化，不等接口
4. **动画走 CSS transform**，禁止 `setInterval` 驱动动画
5. **分包加载**：非首屏模块必须分包

---

## 四、核心开发原则

### 原则 1：像素级还原设计稿（不可妥协）

> 如果做不到还原，设计稿就没有任何意义。

**要求**：
- **颜色、配色、阴影、圆角、边距、字号、字重** 严格对照设计稿
- **按钮位置、图标选择、布局结构** 严格对照设计稿
- **交互状态（hover/active/disabled/loading）** 严格对照设计稿
- **动效时长、缓动曲线** 严格对照设计稿

**工作方式**：
- 每完成一个页面，**必须贴设计稿截图与实现截图的对比**，用户逐一确认通过才算完成
- 设计稿哪里不清楚 → **停下来问**，**不猜测，不"大概差不多"**
- 发现设计稿自身有问题（比如尺寸矛盾、缺状态）→ 先提出，等用户明确后再动手
- 不接受"颜色改了、布局没对"这种半吊子还原

**参考资料**：
- `design/design-system.jsx` — 设计系统变量（颜色、间距、字号）
- `design/shared-components.jsx` — 通用组件规格
- `design/screens-part1.jsx` ~ `screens-part12.jsx` — 各屏幕设计稿
- `design/ios-frame.jsx` / `design/design-canvas.jsx` — 画布框架
- `design/_debug.jpg` / `design/uploads/` — 参考图

### 原则 2：API 盘点先行（每个新页面开发前必做）

**开发任何一个页面/模块前，必须先盘点后端接口**：

1. 查阅 `springcloud_hongshu/` 后端代码，列出当前页面涉及的 API：
   - ✅ **可直接用**：后端已实现，接口签名、返回结构与前端需求匹配
   - ⚠️ **需要改造**：后端已实现但需调整字段/入参/返回结构
   - ❌ **未实现**：设计稿有此功能，但后端未提供接口

2. 把盘点结果**整理到项目 TODO 文档**（统一放在 `aichongshe-app/docs/api-todo.md`），格式：

```markdown
## [页面名] - 笔记详情页

### 可直接用
- `GET /api/note/{id}` - 获取笔记详情

### 需要改造
- `POST /api/note/{id}/like`
  - 需要额外返回当前点赞总数，目前只返回 success

### 未实现
- 笔记举报接口（设计稿 screens-part5 有但后端无）
- 笔记收藏到自定义文件夹（设计稿 screens-part6 有但后端无）
```

3. 这份文档是后端开发的参考依据，也是前端 mock 数据结构的对照

### 原则 3：开发顺序按用户动线

> 按用户首次进入 App 的界面顺序来做，测试最方便。

**推进顺序**：

```
1. Splash（启动页）
2. 登录/注册流程
3. 首页（三 Tab：关注 / 发现 / 附近）+ 底部 TabBar
4. 笔记详情页
5. 个人主页（我的 / 他人）
6. 发布流程
7. 消息通知
8. 设置 / 次要页面
...
```

每一步**用户确认通过，再推进下一步**。

### 原则 4：组件分层

`components/` 目录遵循三层架构：

```
components/
├── base/      ← uview-plus 原生控件 + 少量样式覆盖（Button / Input / Popup / Toast）
├── ui/        ← 设计稿特色组件，自己写，对齐 latte 主题
│              (NoteCard, WaterFall, TopicPill, FloatingTabBar, UserChip, GlassButton)
└── biz/       ← 业务组件（组合 base 和 ui）
```

原则：
- 设计稿里有辨识度的、uview-plus 没有的组件，一律放 `ui/` 自己写
- 业务页面只能引用 `biz/` 或直接用 `ui/` / `base/`，**不能跨层**

### 原则 5：Mock 数据架构

```
src/
├── api/
│   ├── services/         ← 业务 API（真/假同签名）
│   ├── mock/             ← 假数据
│   └── request.ts        ← 根据 VITE_USE_MOCK 决定走真/假
├── stores/               ← Pinia
└── types/                ← TS 类型定义（对齐后端接口）
```

- 类型定义从一开始就按**真实后端接口结构**写，mock 数据也符合同一份 TS 类型
- 切换真假 API 以 service 为粒度，便于渐进迁移

---

## 五、Git 工作流

- 主分支：`main`
- UI 重设计分支：`feature/phase1-ui-redesign`（当前）
- 新项目 v2 开发建议单独开分支：`feature/v2-aichongshe-app`
- commit 信息用中文，简短描述改动动机（不是罗列改了什么）
- 老分支 `uniapp-mobile` 保留备查

---

## 六、团队与协作

团队计划：
- CTO（当前用户）
- 1 前端（熟练使用 Claude Code / Codex）
- 1 后端（熟练使用 Claude Code / Codex）
- 1 美术设计

所有成员都以 AI 辅助作为标配开发方式，因此**代码可读性、类型完整性、文档覆盖度要求高于普通项目**。

---

## 七、对 Claude 的特别约束

1. **不准假装还原**：颜色改了但布局没对齐 / 按钮位置错 / 图标不一致 —— 都视为未完成
2. **API 盘点不做完不准动手**：做新页面之前，先出 API 盘点文档，用户确认后再编码
3. **设计稿出现矛盾时不要默默选一个**：停下来问，把矛盾点摆出来让用户决定
4. **老目录不要再改**：`uniapp_hongshu/` 保持现状，所有新工作在 `aichongshe-app/`
5. **复杂任务走 autoresearch-router**（继承全局规则），子 agent 继承当前线程的模型和推理强度
