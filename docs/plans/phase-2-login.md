# Phase 2：登录流程

- **创建日期**：2026-04-22
- **状态**：✅ 已完成（见提交 45db131：登录流程 + 登录态骨架）
- **执行方式**：autoresearch-router 派发
- **前置依赖**：Phase 0 / 0.5 / 1 已完成

---

## 目标

1. 还原 `design/screens-part1.jsx` 的 `LoginScreen`（登录方式选择页）到 `aichongshe-app/src/pages/login/login.vue`
2. 补一个**手机号登录页**（设计稿缺失，按原则 1 分支 Claude 出 V1 + `TODO(design)` 标注）
3. 搭建**登录态管理骨架**（Pinia store `user` 补完，登录成功后 reLaunch 到首页）
4. Splash 改为：有登录态直接跳首页，无登录态跳 Login

---

## 设计稿规格（LoginScreen，screens-part1.jsx 行 166-275）

### 布局分层

```
Login 根容器（100% × 100%，bg 底色）
├─ StatusLine（顶部状态栏，ink 色）
├─ 顶部柔和背景块 (absolute, top -60 left/right -40, height 340)
│   └─ radial-gradient(ellipse at 50% 60%, primarySoft → bg 70%)
├─ 浮动宠物头像群 (absolute, top 70, height 280, pointerEvents none)
│   ├─ PetAvatar variant=dog, size=56, top 30 left 40, rotate -10°
│   ├─ PetAvatar variant=cat, size=64, top 10 right 50, rotate 12°
│   ├─ PetAvatar variant=mochi, size=44, top 140 left 20, rotate -5°
│   ├─ PetAvatar variant=puppy, size=48, top 150 right 30, rotate 8°
│   └─ PawIcon size=22 primary 色, top 110 left 40%, opacity 0.35, rotate -20°
├─ Logo + 品牌 (absolute, top 210, 居中, gap 12, z 2)
│   ├─ 圆形徽标 72×72, radius 50%
│   │    ├─ 135° 渐变 primary → primaryInk
│   │    ├─ 阴影 0 10px 28px primary@55
│   │    └─ 中央 PawIcon 34px 白色
│   └─ "爱宠社" Baloo 2/ZCOOL KuaiLe, weight 700, size 28, letter-spacing 1.5
├─ 欢迎文案 (absolute, top 360, left/right 32, 居中)
│   ├─ "欢迎回来 🐾" size 22 weight 700 ink 色, margin-bottom 8
│   └─ 副文案 size 14 inkSoft, line-height 1.6
│      "宠友都在这里，等你一起分享 / 萌瞬间、闲置好物和日常治愈"
├─ 按钮组 (absolute, top 470, left/right 28, flex 列, gap 12)
│   ├─ 微信一键登录（主按钮）
│   │    ├─ 高 52, radius radius×1.6 (= 16×1.6 ≈ 26)
│   │    ├─ 135° 渐变 primary → primaryInk
│   │    ├─ 文字白色 size 16 weight 600
│   │    ├─ 阴影 0 8px 20px primary@55 + inset 0 1px 0 white@30
│   │    └─ 微信图标 18px（inline SVG）+ "微信一键登录"
│   └─ 手机号登录 / 注册（次按钮）
│        ├─ 高 52, radius radius×1.6
│        ├─ 背景 surface, 文字 primary, 边框 1.5px primarySoft
│        ├─ size 16 weight 600
│        └─ 阴影 0 4px 12px primary@15
└─ 底部协议 (absolute, bottom 40, 居中, gap 8, size 11 inkMuted)
    ├─ Checkbox 14×14 radius 7
    │    ├─ 未选：border 1.5px inkFaint, 背景透明
    │    └─ 已选：border+背景 primary, 中央白色对勾 SVG
    └─ "已阅读并同意 《用户协议》 和 《隐私政策》"
         （协议名 primary 色可点击）
```

---

## 新增组件

### `src/components/ui/PetAvatar.vue`
- 参考 `design/design-system.jsx` 行 220-280 的 `PetAvatar` + `PetAvatarShape`
- props：`variant: 'dog' | 'cat' | 'mochi' | 'puppy'`、`size: number`、`bg?: string`
- 4 个 variant 的形状从 `PetAvatarShape` 源码还原（SVG）
- 不硬编码颜色，`bg` 默认从 tokens 拉主色软色，允许父组件覆盖
- 放 `components/ui/` 层

### `src/components/ui/AppButton.vue`（可选但推荐）
- 按钮样式在 Login 只是第一次出现，但后续各页都会用
- 支持 variant：`primary`（渐变）、`outline`（描边）、`ghost`（透明）
- 这样比直接在页面里写按钮样式更可维护
- 可以推迟到 Phase 3 首次遇到第二种按钮样式时再抽

---

## 新增页面（无设计稿，按原则 1 分支 Claude 出 V1）

### `src/pages/login/phone-login.vue`（手机号登录）
- **无设计稿**，Claude 按 latte 主题出 V1
- 代码根节点标注 `<!-- TODO(design): 待设计师细化 手机号登录页 -->`
- 内容：
  - 返回按钮（左上）
  - 标题 "手机号登录"
  - 手机号输入框（国家区号 +86 前缀 + 11 位数字框）
  - 验证码输入框（6 位 + 右侧"获取验证码"按钮，60s 倒计时）
  - "登录 / 注册" 主按钮（与登录页主按钮同款渐变）
  - 其他登录方式切换（底部小字链接）
- 表单校验：手机号 11 位 / 验证码 6 位
- mock 层：`src/api/services/auth.ts` 提供 `requestSmsCode / loginByPhone` 假接口

---

## 登录态管理骨架

### `src/stores/user.ts` 补完
- State：`userInfo: UserProfile | null`、`accessToken: string | null`、`isLogin: computed`
- Actions：`login(credentials)`、`logout()`、`fetchProfile()`、`refreshToken()`
- 持久化：通过 `uni.setStorageSync` / `uni.getStorageSync`（跨端兼容）
- 初始化：在 App `onLaunch` 中从 storage 恢复

### `src/api/services/auth.ts`（新）
- `loginByWechat(code: string)` → mock 直接返回成功 token
- `loginByPhone(phone: string, code: string)` → mock 校验 1234 通过
- `requestSmsCode(phone: string)` → mock 返回成功
- `logout()` → 清除 token

### `src/api/mock/auth.mock.ts`（新）
- 对应假数据

### `src/types/auth.ts`（新）
- `LoginCredentials`、`LoginResponse`、`SmsRequest` 等 TS 类型

### `src/utils/auth.ts`（新）
- `hasToken(): boolean` / `getToken()` / `setToken()` / `clearToken()`
- 上层工具函数

---

## Splash 调整

现有 Splash 是 2 秒无条件 reLaunch 到 `/pages/index/index`。
**调整为**：2 秒后判断登录态
- 有 token → `/pages/index/index`
- 无 token → `/pages/login/login`

---

## 登录拦截中间件（本 Phase 建立最小版）

### `src/composables/useAuthGuard.ts`（新）
- 导出 `requireAuth(action: () => void, options?)` 函数
- 当前简化版：未登录直接 reLaunch 到 Login 页（不实现 Sheet 弹窗和动作回放，那是 Phase 首页/详情时补）
- **TODO(Phase X)**：升级为底部 Sheet 弹窗 + 动作回放（按原则 6 完整实现）

---

## 执行步骤

1. 新增组件 `PetAvatar.vue` — 从设计稿复刻 4 variant SVG 形状
2. 还原 Login 登录方式选择页（`src/pages/login/login.vue`）
3. 写手机号登录页 V1（`src/pages/login/phone-login.vue`）
4. 搭建登录态骨架（`stores/user.ts` + `api/services/auth.ts` + `api/mock/auth.mock.ts` + `types/auth.ts` + `utils/auth.ts`）
5. 补 `useAuthGuard.ts` 最小版
6. 修改 Splash 的跳转逻辑（依据登录态）
7. `pages.json` 注册 login 和 phone-login 路由
8. 本地启动 `npm run dev:h5` 验证：
   - Splash → Login（首次）
   - 微信登录 → 成功 → 首页
   - 手机号登录 → 输入 13800000000 + 1234 → 成功 → 首页
   - 再次启动 → 有 token → Splash → 首页（跳过 Login）

---

## 验收 Harness

### Functional
- Login 页像素级对比 `screens-part1.jsx` LoginScreen
- Phone-Login 页能看，latte 主题风格不违和（无设计稿 V1）
- 微信登录 mock 成功 → Pinia 有 userInfo + storage 有 token
- 手机号登录 mock 流程通（区号 +86 不可编辑、验证码 60s 倒计时、校验错误提示）
- 刷新 H5 后 Splash 判断登录态跳转正确

### Quality
- 所有颜色/字号/间距从 tokens 引用（不硬编码）
- PetAvatar 是独立组件，4 variant SVG 还原
- 按钮动画流畅（按下缩小/透明度，不是死的）
- TS 严格模式无新错
- `TODO(design)` 标注在无设计稿页面根节点

### Delivery
- 截图对比设计稿给用户验收
- commit + push

**指标优先级**：`pixel_match > token_alignment > correctness > code_cleanliness`

---

## 风险与预案

| 风险 | 预案 |
|------|------|
| `PetAvatar` 的 SVG 形状复杂（可能很长） | 完整保留源 SVG path，不简化；如果极复杂可存为独立 .svg 资源用 image 引用 |
| 微信一键登录 mock 不够真实 | 按 CLAUDE.md 原则 5（全量 mock），假数据返回固定 UserProfile 即可 |
| 小程序端 SVG 支持 | Phase 1 已用 inline SVG 无问题，本 Phase 延续同策略 |
| 动作回放未实现会不会阻塞 | 不会，本 Phase 简化为 redirect；真正动作回放到点赞/收藏/关注 Phase 补 |

---

## 结项交付物

- `src/pages/login/login.vue`（还原）
- `src/pages/login/phone-login.vue`（V1 新增 + TODO(design) 标注）
- `src/components/ui/PetAvatar.vue`
- `src/composables/useAuthGuard.ts`
- `src/stores/user.ts`（补完）
- `src/api/services/auth.ts` + `src/api/mock/auth.mock.ts` + `src/types/auth.ts` + `src/utils/auth.ts`
- `src/pages/splash/splash.vue`（修改跳转逻辑）
- `pages.json`（新路由注册）
- commit + push

---

## 后续衔接

Phase 2 完成后 → Phase 3：首页三 Tab（关注/发现/附近）+ 底部 TabBar + 瀑布流卡片
