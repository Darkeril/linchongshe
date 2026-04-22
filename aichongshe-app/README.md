# aichongshe-app

爱宠社移动端 v2（图文社区，对标小红书/得物）。

## 技术栈

- **框架**：UniApp + Vue3 `<script setup>` + TypeScript
- **渲染模式**：Vue 页面 + nvue 混合（核心展示页 nvue，表单/设置页 vue）
- **状态管理**：Pinia
- **UI 组件库**：uview-plus（基础控件）+ 自研 `components/ui/`（设计稿特色组件）
- **数据层**：假数据先行，通过 `VITE_USE_MOCK` 环境变量切换真假 API
- **主题**：latte 暖咖温润（焦糖 `#C97B4A` / 奶茶米 `#F4EDE2`）

四端覆盖：iOS / Android / 微信小程序 / 抖音小程序（Web 端非当前优先级）。

## 目录结构

```
src/
├── pages/                  ← 页面（pages.json 注册）
├── components/
│   ├── base/              ← uview-plus 控件 + 样式覆盖
│   ├── ui/                ← 设计稿特色组件（自研）
│   └── biz/               ← 业务组件（组合 base 和 ui）
├── api/
│   ├── services/          ← 业务 API（真/假同签名）
│   ├── mock/              ← 假数据（符合 types/ 契约）
│   └── request.ts         ← VITE_USE_MOCK 开关
├── stores/                ← Pinia store
├── types/                 ← TS 类型（按真实后端接口结构定义）
├── styles/
│   ├── tokens.scss        ← SCSS 设计 token
│   ├── tokens.ts          ← TS 设计 token（与 scss 同源同值）
│   └── global.scss        ← 全局样式 + uview-plus 主题覆盖
└── utils/
docs/
└── api-todo.md            ← API 盘点清单（新页面开发前必填）
```

### 组件分层规则

- 业务页面只能引用 `biz/`，或直接用 `ui/` / `base/`，**不能跨层**
- 设计稿里有辨识度的、uview-plus 没有的组件，一律放 `ui/` 自己写
- 每层各有一份 README 说明职责（见 `src/components/*/README.md`）

## 开发

```bash
# H5 本地预览（默认走 mock）
npm run dev:h5

# 微信小程序
npm run dev:mp-weixin

# 抖音小程序
npm run dev:mp-toutiao

# App（iOS / Android）
# 使用 HBuilderX 打开项目，选择「运行 → 运行到手机或模拟器」
```

## 环境变量

- `.env.development` → `VITE_USE_MOCK=true`（本地默认走 mock）
- `.env.production` → `VITE_USE_MOCK=false`（真实后端）

## 开发原则速览

详见仓库根目录 `CLAUDE.md` 第三、四章。核心要点：

1. **像素级还原设计稿**：不接受"颜色改了、布局没对"这种半吊子还原
2. **API 盘点先行**：新页面开发前必须先出 `docs/api-todo.md` 清单
3. **按用户动线推进**：Splash → 登录 → 首页 → 详情页 → 个人主页 …
4. **组件分层**：base / ui / biz 三层不能跨层
5. **Mock 数据架构**：service 级别真假切换，类型契约统一
