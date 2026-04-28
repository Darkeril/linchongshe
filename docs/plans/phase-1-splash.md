# Phase 1：Splash 启动页像素级还原

- **创建日期**：2026-04-22
- **状态**：✅ 已完成（见提交 8747b50 / 081a659：Splash 像素级还原 + 本地字体接入）
- **执行方式**：通过 `autoresearch-router` 派发 worker 执行
- **前置依赖**：Phase 0、Phase 0.5 已完成

---

## 目标

把设计稿 `design/screens-part1.jsx` 里的 `SplashScreen` **像素级还原**到 `aichongshe-app/src/pages/splash/splash.vue`。

---

## 设计稿规格（来源：screens-part1.jsx 行 94-161）

### 布局层级

```
Splash 根容器（100% × 100%）
├─ 渐变背景：180° (surface 0% → bg 40% → primarySoft 100%)
├─ StatusLine（顶部状态栏，颜色 ink）
├─ 装饰爪印 × 4（PawIcon，不同尺寸/位置/旋转/透明度）
│   ├─ size 36, top 120, left 40, rotate -20°, opacity 0.7, color primarySoft
│   ├─ size 24, top 180, right 60, rotate 15°, opacity 0.5, color primarySoft
│   ├─ size 28, bottom 260, left 70, rotate 40°, opacity 0.25, color primary
│   └─ size 20, bottom 340, right 80, opacity 0.2, color primary
├─ 中央 Logo 组（absolute, top 32%, gap 20, 居中）
│   ├─ 圆形徽标 104×104, borderRadius 50%
│   │    ├─ 渐变 135° (primary → primaryInk)
│   │    ├─ 外阴影 0 14px 40px primary@55 + 内阴影 0 2px 6px white@40
│   │    └─ 中央 PawIcon 50px, 白色
│   └─ 文字组（居中）
│        ├─ "爱宠社" — Baloo 2 字体、weight 700、size 34、ink 色、字距 2、下 margin 8
│        └─ "和毛孩子一起，分享每一天" — size 14、inkSoft、字距 1
└─ 底部加载条（absolute, bottom 90, 水平居中, flex 列, gap 14）
    ├─ 加载条容器 44×4, radius 2, 背景 primarySoft, 内填充动画
    │    └─ 动画填充 60% 宽, primary 色, keyframes splashLoad 1.8s 无限
    └─ "LOADING · v2.0" — size 11, inkMuted, 字距 2
```

### 颜色 token 引用
- 背景：`bg / surface / primarySoft`
- 主色：`primary / primaryInk`
- 文字：`ink / inkSoft / inkMuted`

全部从 `src/styles/tokens.scss`（或 `tokens.ts`）引用，**禁止硬编码**。

### 字体
- "爱宠社" 用 **Baloo 2**（`TYPE.brand`）
- 其余用默认（`TYPE.body`）
- 如果 Baloo 2 字体文件未引入，**Phase 1 必须把 Web 字体加载或 fallback 配置好**

### 动画
- `@keyframes splashLoad`：`translateX(-100%) → translateX(80%) → translateX(180%)`，ease-in-out 1.8s 无限
- **必须用 CSS transform，不能用 JS 驱动**（CLAUDE.md 性能 P0 要求）

---

## 执行步骤

### 步骤 1：补 PawIcon 组件
- 查 `design/shared-components.jsx` 里的 `PawIcon` 实现
- 在 `src/components/ui/PawIcon.vue` 还原同等效果（SVG 或纯 CSS）
- 接受 props：`size` / `color` / `style`（含 rotation / position / opacity）
- 记录到组件分层：属于 `ui/`（设计稿特色组件）

### 步骤 2：字体接入
- Baloo 2 Web 字体接入（Google Fonts 或本地打包）
- 注意小程序端不能远程加载字体，需本地打包 ttf/woff2
- fallback：`-apple-system, BlinkMacSystemFont, sans-serif`

### 步骤 3：Splash 组件实现
- 替换现有 `src/pages/splash/splash.vue` 占位代码
- 严格按照上面布局规格写
- 所有颜色 / 字号 / 间距从 tokens 引用
- 动画用 `@keyframes` 写在 scoped style 里
- StatusLine 部分 UniApp 里对应 `uni.getSystemInfoSync()` 的状态栏高度，不需要重现文字，只要留出 safe-area

### 步骤 4：验证像素级还原
- H5 浏览器打开 `npm run dev:h5`，设 iPhone 13 Pro（390×844）视口
- 截图与设计稿 SplashScreen 对比
- 关键检查点：
  - [ ] 渐变方向和颜色梯度
  - [ ] 4 个装饰爪印位置/尺寸/旋转
  - [ ] Logo 渐变方向（135°）和阴影层次
  - [ ] "爱宠社" 字体是否为 Baloo 2 效果
  - [ ] 加载条位置和填充动画
  - [ ] 整体比例在 360×740 视口下对齐

### 步骤 5：Splash → 下一步跳转逻辑
- 2 秒后自动跳转到下一页（Phase 2 的登录页还没做，先跳到 `pages/index/index` 占位）
- 使用 `uni.reLaunch`（避免后退回 Splash）

### 步骤 6：提交
- git add / commit / push
- commit message："Phase 1: Splash 启动页像素级还原"

---

## 验收 Harness

### Functional
- `npm run dev:h5` 启动不报错
- Splash 页面在 H5 能显示
- 2 秒自动跳转到下一页
- 加载条动画跑起来

### Quality
- 无硬编码颜色 / 字号（全部来自 tokens）
- PawIcon 是独立组件放在 `components/ui/`
- TypeScript 无新增错误
- 动画 CSS 驱动（不是 JS）

### Delivery
- 截图对比（设计稿 vs 实现）让用户确认通过
- git commit + push

**指标优先级**：`pixel_match > correctness > token_alignment > code_cleanliness`

---

## 风险与预案

| 风险 | 预案 |
|------|------|
| Baloo 2 字体小程序端加载问题 | 本地打包，Phase 1 内解决 |
| PawIcon 在 shared-components.jsx 实现过于复杂 | 简化为 SVG path，形状贴近即可 |
| 移动端视口适配问题（rpx vs px） | UniApp 标准做法：rpx 单位 + tokens 自动适配 |
| nvue 不支持某些 CSS 动画 | Splash 用 vue 页面（不是 nvue），性能足够 |

---

## 结项交付物

- `src/pages/splash/splash.vue` — 像素级还原
- `src/components/ui/PawIcon.vue` — 爪印组件
- 字体资源（Baloo 2 本地 or 远程）
- 截图对比通过用户验收
- commit + push

---

## 后续衔接

Phase 1 完成后 → Phase 2：登录页（`screens-part1.jsx` 的 `LoginScreen`）
