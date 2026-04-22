# UniApp 开发陷阱清单

**最近更新**：2026-04-22（Round 2 修订：新增 #13/#14/#15，修正 #11 关于 300ms 延迟的表述）
**条目数**：15
**维护**：每次 Phase 踩到新坑必须追加条目

## 总览

这份文档是爱宠社 UniApp 项目的**跨端陷阱知识库**。Worker 每次派发前必读，踩到新坑必须 PR 进来。

每条条目固定七栏：**场景 / 现象 / 根因 / 正确做法 / 反例 / 影响范围 / 首次发现**。正确做法与反例都是可直接 copy-paste 的代码片段。影响范围标 H5 / 小程序（微信/抖音）/ App（含 nvue）。

---

## 已踩过的坑

### 1. `<button>` 默认样式覆盖

**场景**：登录页全宽主按钮 / 手机号登录按钮等使用 UniApp 原生 `<button>`。
**现象**：按钮宽度不跟父容器（仅 200rpx 左右）、右侧冒出一圈边框、文字被 padding 撑高、hover-class 无效。
**根因**：UniApp `<button>` 编译后默认 `display: inline-block`、`min-width: ~150rpx`、`padding: 0 24rpx`、`::after` 画 1px 边框用于小程序「按压态」降级、`line-height` 不为 1。移动端容器弹性不足时直接溢出。
**正确做法**：
```scss
.login__btn {
  width: 100%;
  box-sizing: border-box;
  padding: 0;
  margin: 0;
  border: none;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  white-space: nowrap;
  overflow: hidden;

  &::after {
    display: none;
    border: none;
  }
}
```
**反例**：
```scss
// 只写视觉样式，保留 UniApp 默认 inline-block + ::after → 溢出 / 重边框
.login__btn {
  background: linear-gradient(135deg, #f29556, #d96a3a);
  color: #fff;
  border-radius: 52rpx;
}
```
**影响范围**：H5 / 微信小程序 / 抖音小程序 / App（nvue 不适用，nvue 无 ::after）
**首次发现**：Phase 2 LoginScreen（commit `45db131`）

---

### 2. `<input>` 跨端默认高度 / 内边距 / placeholder 样式差异

**场景**：任何表单输入（手机号、验证码、搜索框）。
**现象**：同一套 SCSS 在 H5 显示正常，小程序里 input 高度被压到 48rpx，App 端 placeholder 颜色变浅灰无法覆盖。
**根因**：H5 走浏览器默认 `<input>`（高度约 42px、有 1px 边框）；微信小程序原生 input 无默认高度但有 14rpx 上下 padding；App(iOS) placeholder 颜色由 `placeholder-style` 属性控制，不是 CSS。
**正确做法**：显式固定高度 + 清零 padding + placeholder 走属性。
```vue
<input
  v-model="phone"
  class="form__input"
  type="number"
  placeholder="请输入手机号"
  placeholder-style="color:#B8A08E;font-size:28rpx;"
/>
<style lang="scss">
.form__input {
  width: 100%;
  height: 96rpx;
  padding: 0 24rpx;
  box-sizing: border-box;
  border: 2rpx solid #e8d9c9;
  border-radius: 16rpx;
  font-size: 32rpx;
  line-height: normal;
  background: #fff;
}
</style>
```
**反例**：
```vue
<!-- placeholder 颜色改不动；小程序端被压扁 -->
<input v-model="phone" placeholder="请输入手机号" class="form__input" />
<style>.form__input { padding: 10px; }</style>
```
**影响范围**：H5 / 小程序 / App
**首次发现**：预见（参考 uview-plus issue 历史）

---

### 3. `<text>` 不能嵌套 block 元素

**场景**：富文本、徽章拼接（`<text>￥<text>99</text>.9</text>`）或给文字套 `<view>` 想做 flex。
**现象**：小程序直接报编译错误 "text should not contain block-level child"；nvue 下整段文字不渲染。
**根因**：UniApp 规范：`<text>` 是 inline，其后代只能是 `<text>` 或纯字符；block 元素（`<view>`、`<image>`）必须反过来由 `<view>` 包 `<text>`。
**正确做法**：
```vue
<view class="price">
  <text class="price__symbol">￥</text>
  <text class="price__int">99</text>
  <text class="price__decimal">.9</text>
</view>
```
**反例**：
```vue
<text class="price">
  ￥<view class="price__int">99</view>.9   <!-- 小程序编译报错 -->
</text>
```
**影响范围**：微信小程序 / 抖音小程序 / App-nvue（H5 容忍但表现不一致）
**首次发现**：预见

---

### 4. `<scroll-view>` 必须显式高度 + `overflow: hidden` 祖先

**场景**：首页信息流、评论列表、分类横滑 Tab。
**现象**：列表不滚动 / 滚动到底部却还有内容未渲染 / iOS 惯性丢失。
**根因**：`<scroll-view>` 靠自身 `height` 计算可视区；未设高度时小程序回落到 0。滚动方向为横时必须 `scroll-x` 且内部元素 `display: inline-block`。
**正确做法**：
```vue
<scroll-view
  class="feed"
  scroll-y
  :enhanced="true"
  :show-scrollbar="false"
  @scrolltolower="loadMore"
>
  <NoteCard v-for="n in list" :key="n.id" :data="n" />
</scroll-view>
<style lang="scss">
.feed {
  height: calc(100vh - 176rpx); // 显式高度！不要用 flex:1
  box-sizing: border-box;
}
</style>
```
**反例**：
```vue
<!-- 父 flex:1 传不下来高度，小程序列表不滚 -->
<view style="display:flex;flex-direction:column;height:100vh">
  <scroll-view scroll-y style="flex:1"><view v-for="..."/></scroll-view>
</view>
```
**影响范围**：微信小程序 / 抖音小程序（H5/App 相对容忍）
**首次发现**：预见

---

### 5. `position: fixed` 在 `transform` 祖先上失效

**场景**：底部 FloatingTabBar / 发布悬浮按钮 / 全局 Toast。
**现象**：TabBar 跟随页面一起滚，或出现在父容器坐标系而非视口底部。
**根因**：CSS 规范：任何祖先含 `transform / filter / perspective` 都会创建新的 containing block，导致 `position: fixed` 以该祖先为参考。常见肇事者：页面过渡动画、列表 cell `transform: translateZ(0)` 强制硬件加速。
**正确做法**：把 fixed 元素挂到页面根下，且确保从根到它的链路上没有 transform；动画用 `opacity + translate` 时用单独包装层。
```vue
<template>
  <view class="page">
    <view class="page__content"><!-- 页面主体 --></view>
  </view>
  <!-- TabBar 与 .page 平级，而非放进 page 内 -->
  <FloatingTabBar class="app-tabbar" />
</template>
<style lang="scss">
.app-tabbar { position: fixed; left: 0; right: 0; bottom: 0; z-index: 99; }
</style>
```
**反例**：
```vue
<view class="page" style="transform: translateZ(0)"><!-- 陷阱来源 -->
  <FloatingTabBar style="position:fixed;bottom:0" />
</view>
```
**影响范围**：H5 / 小程序 / App-vue（nvue 用 `position:sticky` 或原生 absolute 规则）
**首次发现**：预见

---

### 6. `rpx` 在大屏 / 折叠屏 / iPad 上失控

**场景**：设计稿基于 375 设计稿（`750rpx = 375pt`），iPad 或横屏下使用。
**现象**：按钮高 104rpx 在 iPad Pro（1024 宽）上被放大到 ~140px，字号被撑得失比例，间距撕裂。
**根因**：`rpx` 以屏幕物理宽线性换算，宽度越大单位越大。UniApp 官方上限在 H5 为 960rpx 容器，超出后按 px 处理，但小程序无此兜底。
**正确做法**：关键尺寸做上限封顶；字号改用 `px` 或 `$size-*` token 混合。
```scss
// tokens.scss
@function rpx-max($rpx, $max-px) {
  @return min(#{$rpx}rpx, #{$max-px}px);
}
.btn { height: rpx-max(104, 56); font-size: 30rpx; } // 字号小幅波动可接受
// 容器加 max-width 把布局夹住
.page { max-width: 750rpx; margin: 0 auto; }
```
**反例**：
```scss
// iPad 下按钮 140px 高，内容区被撑爆
.btn { height: 104rpx; font-size: 40rpx; }
.page { width: 100%; }
```
**影响范围**：H5（横屏 / iPad / PC 模拟器）/ App（Pad / 折叠屏）
**首次发现**：预见

---

### 7. SCSS `@use` vs `@import` 预注入顺序

**场景**：`vite.config.ts` 里用 `additionalData` 全局注入 `@use 'src/styles/tokens.scss' as *;`。
**现象**：组件内再写 `@use '...'` 编译报错 "@use rules must be written before any other rules"；或变量在 `<style scoped>` 里 undefined。
**根因**：`@use` 规则必须出现在文件最顶部（在任何 CSS rule 之前）。uni-app/Vite 预注入时会把内容插在组件 `<style>` 顶部，若组件自己再 `@use` 第二次、或先写了 `@import`（允许混用但语义不同），就会破坏顺序。
**正确做法**：统一用 `@use`，组件内不再重复 import token；需要命名空间时 `as *`。
```ts
// vite.config.ts
css: {
  preprocessorOptions: {
    scss: {
      additionalData: `@use "@/styles/tokens.scss" as *;\n@use "@/styles/mixins.scss" as *;\n`,
    },
  },
},
```
```scss
// 组件 <style lang="scss"> —— 直接用变量即可，不要再写 @use / @import
.btn { background: $color-primary; }
```
**反例**：
```scss
<style lang="scss">
.btn { color: red; }
@use '@/styles/tokens.scss' as *;   // 报错：must be at top
</style>
```
**影响范围**：H5 / 小程序 / App（全平台编译期）
**首次发现**：预见

---

### 8. nvue 不支持的 CSS（grid / calc / 伪类 / 选择器）

**场景**：首页瀑布流、笔记详情页用 nvue 做真原生渲染。
**现象**：`display: grid` 无效、`width: calc(100% - 32rpx)` 被忽略、`:hover` / `::before` 完全不生效、后代选择器 `.a .b` 也不支持。
**根因**：nvue 走 Weex 渲染器，仅支持 flexbox + 单类选择器（`.class`），不支持 grid / calc / 伪类 / 伪元素 / 后代/子代选择器。
**正确做法**：nvue 页面一律 flex 布局 + 组件内用 class 单选择器；宽度写死或用 `flex: 1`；需要 `::before` 用真实 `<view>` 节点替代。
```vue
<!-- home.nvue -->
<template>
  <view class="col">
    <view class="card" v-for="n in left" :key="n.id">
      <image class="card-cover" :src="n.cover" mode="widthFix" />
      <text class="card-title">{{ n.title }}</text>
    </view>
  </view>
</template>
<style>
.col { flex: 1; }
.card { margin-bottom: 16rpx; background-color: #fff; border-radius: 16rpx; }
.card-cover { width: 100%; }
.card-title { font-size: 28rpx; color: #3a2618; padding: 16rpx; }
</style>
```
**反例**：
```vue
<style>
/* 全部在 nvue 下失效 */
.col { display: grid; grid-template-columns: 1fr 1fr; }
.card { width: calc(50% - 8rpx); }
.card:hover { opacity: 0.8; }
.card .card-title { color: red; }
</style>
```
**影响范围**：App-nvue（H5/小程序/App-vue 不受影响）
**首次发现**：预见

---

### 9. 样式作用域：`scoped` + `::v-deep` 正确用法

**场景**：覆盖 uview-plus 内部节点样式（如 `u-input` 的 `__inner`）、自研 `ui/` 组件给子组件改样式。
**现象**：`scoped` 下样式不生效；用旧 `/deep/` 或 `>>>` 在 Vue3 + small-program 编译报 warning；小程序里 `:deep()` 穿透失败。
**根因**：Vue3 scoped 推荐 `:deep()`，但 UniApp 小程序编译器对 `:deep()` 的支持依赖版本（HBuilderX ≥ 3.8）；选择器必须包外层本组件 class，否则打包丢失。
**正确做法**：统一使用 `:deep()`，外层必须带本组件 scope class。
```vue
<style lang="scss" scoped>
.login-form {
  :deep(.u-input__content__field-wrapper__field) {
    font-size: 32rpx;
    color: $color-text-ink;
  }
}
</style>
```
**反例**：
```vue
<style lang="scss" scoped>
/deep/ .u-input__inner { color: red; }       // Vue3 弃用
>>> .u-input__inner { color: red; }           // scss 不支持
:deep(.u-input__inner) { color: red; }        // 没带外层 class，小程序下可能编译丢失
</style>
```
**影响范围**：H5 / 小程序 / App-vue
**首次发现**：预见

---

### 10. 小程序 inline SVG 兼容性

**场景**：自研 PawIcon / 品牌 logo / 微信按钮图标，设计稿给的是 SVG。
**现象**：H5 里 `<svg>` 渲染正常；微信/抖音小程序下整块空白，控制台报 `unknown component svg`。
**根因**：小程序不认识 `<svg>` 标签，必须走 `<image>` + svg 文件 / base64，或转 iconfont。
**正确做法**：小程序端用 `<image>` 引用 svg（`src` 支持本地 `.svg` 或 data url）；H5/App 下可原样 inline。
```vue
<template>
  <!-- 全平台一致：image + svg 文件 -->
  <image
    class="icon-paw"
    src="/static/icons/paw.svg"
    mode="aspectFit"
  />
  <!-- 或 base64 便于动态换色（svg 内 currentColor 不支持小程序，改用 fill 预编译） -->
</template>
<style>.icon-paw { width: 48rpx; height: 48rpx; }</style>
```
**反例**：
```vue
<!-- 小程序端白屏 -->
<svg viewBox="0 0 24 24" class="icon">
  <path d="M12 2L2 22h20L12 2z" fill="currentColor" />
</svg>
```
**影响范围**：微信小程序 / 抖音小程序（H5/App-vue 支持 inline SVG）
**首次发现**：预见

---

### 11. `@click` vs `@tap`：语义与 hover-class 细节（更正：无 300ms 延迟）

> **修订记录**：Round 1 原条目声称 "`@click` 在小程序下有 300ms 延迟，在 nvue 下不触发"。经核实**不成立**，已修正。
>
> **核实结论**：
> - uni-app 官方文档事件处理章节明确 `@click` 和 `@tap` 为**同义别名**，编译到小程序端统一产出 `bindtap`。
> - 300ms 延迟是早期 H5 移动浏览器（iOS 5~8 时代）的 `click` 事件历史问题，主流 WebView 在 viewport `width=device-width` 下已消除；小程序的 `tap` 是原生触摸事件，不存在 300ms 延迟。
> - nvue 基于 Weex，`@click` 同样映射到 tap，不存在 "不触发" 的情况（事件冒泡规则与 vue 页面确有差异，但与 300ms 无关）。
>
> **因此本条目不再以"性能"为由推荐 `@tap`**，而是保留**语义一致性**和**hover-class 细节**两个真实的坑。

**场景**：任何点击交互——按钮、卡片、列表项。
**现象（真实的坑）**：
1. 项目同时出现 `@click` 和 `@tap`，代码风格不一致，review 时看不出哪种是主流。
2. 在 `<view @tap>` / `<view @click>` 上写 `hover-class="xxx-hover"` 无效——按下去没有视觉反馈。开发者误以为是事件问题，其实是 `hover-class` 属性只被 `<button>` 和部分原生组件支持。
3. nvue 下 `@tap` 事件对象结构和 vue 页面不同：`e.detail` / `e.currentTarget` 字段不全，读取时 undefined。

**根因**：
- `@click` = `@tap` 在 uni-app 里是别名，编译结果一致。选择哪个是**代码风格**问题而非性能问题。
- `hover-class` 是小程序 `<button>` 的原生属性，不是通用 CSS class 切换，`<view>` 上写了也不生效。要在 view 上做按压态需要用 `:active` CSS 伪类（小程序支持）或 JS 手动切 class。
- nvue 的事件体系走 Weex，事件 payload schema 与 vue 页面不是一一对应。

**正确做法**：
1. 团队约定一个即可（爱宠社项目规定一律用 `@tap`，原因是与移动端触摸语义对齐，代码读起来更"原生感"；但**这是风格约定不是性能要求**）。
2. 需要按压反馈的 view：用 CSS `:active` 或 `:hover:active`（小程序也认）或手动切 class，不要指望 `hover-class`。
3. nvue 页面读事件字段时先做空值保护。

```vue
<!-- 推荐：@tap + CSS :active 做按压态 -->
<view class="card" @tap="onCardTap">
  <text>笔记标题</text>
</view>

<!-- 需要 hover-class 时用 <button> 原生属性，不要加到 view 上 -->
<button class="acs-btn" hover-class="acs-btn--pressed" @tap="submit">提交</button>

<style lang="scss">
.card {
  background: #fff;
  transition: background 0.15s;
  &:active { background: #f7f3ee; }  // 小程序 / H5 / App-vue 均支持
}
</style>
```

**反例**：
```vue
<!-- hover-class 在 view 上无效，开发者误以为"小程序点击没反馈" -->
<view class="card" hover-class="card--pressed" @tap="onCardTap">...</view>

<!-- nvue 里直接解构 e.detail.x 可能拿到 undefined -->
<view @tap="(e) => console.log(e.detail.x)" />
```

**影响范围**：
- `@click`/`@tap` 别名：全端等价（只是风格约定）
- `hover-class` 限制：微信/抖音小程序
- nvue 事件对象差异：App-nvue

**首次发现**：Round 1 原条目（关于 300ms 延迟的说法错误），Round 2 reality-checker 指出后核实修正

---

### 12. uview-plus 全局样式污染

**场景**：引入 `uview-plus` 后页面内自定义 `.button` / `.input` / `.tag` 等 class。
**现象**：自己写的 `.tag` 被 uview-plus 的 `.u-tag / .tag` 样式覆盖，颜色 / padding 不对；小程序下表现更严重（因为小程序没有 scoped 隔离，global class 互相踩）。
**根因**：uview-plus 在 `uni.scss` / `uview-plus/theme.scss` 中注册了大量全局 class（部分无 `u-` 前缀，如 `.nav-bar`、`.tag`），小程序端 class 平铺到全局命名空间。
**正确做法**：自研组件 class 一律加项目前缀（如 `acs-` 爱宠社），并始终加 scoped；全局覆盖 uview-plus 变量走 `uni.scss` 而不是改 class。
```vue
<template>
  <view class="acs-tag acs-tag--primary">#猫咪</view>
</template>
<style lang="scss" scoped>
.acs-tag {
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
  &--primary { background: $color-primary-soft; color: $color-primary-ink; }
}
</style>
```
**反例**：
```vue
<!-- 小程序下被 uview-plus .tag 全局样式覆盖 -->
<view class="tag tag-primary">#猫咪</view>
<style scoped>.tag { background: #fff3e0; }</style>
```
**影响范围**：微信小程序 / 抖音小程序（H5/App-vue 受影响较轻）
**首次发现**：预见

---

### 13. 小程序页面栈 10 层限制 / `navigateTo` vs `redirectTo` vs `reLaunch`

**场景**：图文社区强互链，笔记详情 → 作者主页 → 笔记详情 → 话题聚合 → 笔记详情 → ...，极容易往栈里叠页。爱宠社对标小红书，详情/主页/话题/评论列表互相跳转是家常便饭。
**现象**：用户连续点了 10+ 次跳转后，**第 11 次 `uni.navigateTo` 静默失败**（微信）或抛错（抖音 / 部分版本），用户点了"没反应"但控制台只有一行 warning，极易被误认为是"接口卡住"。
**根因**：微信小程序官方规定页面栈**最多 10 层**（2023 后部分版本放宽但仍有上限，且抖音/其他小程序平台各有独立限制）；`uni.navigateTo` 跨过上限时不会自动降级。H5 和 App 本身无此限制，所以开发时在 H5 调试看不出问题。
**正确做法**：封装统一跳转函数 `navigateSafe()`，检查当前栈深度后决定用 `navigateTo` / `redirectTo` / `reLaunch`。项目中所有导航必须经过这个封装，禁止散落调用 `uni.navigateTo`。

```ts
// src/utils/navigate.ts
const STACK_LIMIT = 10  // 微信官方上限；保留 1 层给将来跳转，阈值取 9
const SAFE_THRESHOLD = STACK_LIMIT - 1

interface NavigateOptions {
  url: string
  /** 强制用 reLaunch（重置栈，跳首页流程时用） */
  reset?: boolean
  events?: Record<string, (...args: any[]) => void>
}

export function navigateSafe(options: NavigateOptions): Promise<void> {
  return new Promise((resolve, reject) => {
    if (options.reset) {
      uni.reLaunch({ url: options.url, success: () => resolve(), fail: reject })
      return
    }
    // getCurrentPages 在 H5/小程序/App 全端支持
    const depth = getCurrentPages().length
    if (depth >= SAFE_THRESHOLD) {
      // 栈快满 → 用 redirectTo 替换当前页（不入栈）
      uni.redirectTo({ url: options.url, success: () => resolve(), fail: reject })
    } else {
      uni.navigateTo({
        url: options.url,
        events: options.events,
        success: () => resolve(),
        fail: reject,
      })
    }
  })
}

// 业务侧使用
import { navigateSafe } from '@/utils/navigate'
await navigateSafe({ url: `/pages/note/detail?id=${noteId}` })
```

**反例**：
```ts
// 散落调用，无栈深度保护——在强互链路径上第 11 次点击必失败
uni.navigateTo({ url: `/pages/note/detail?id=${id}` })
uni.navigateTo({ url: `/pages/user/profile?id=${uid}` })
// 用户点到第 11 层，按钮无响应，仅控制台 warning
```

**影响范围**：微信小程序 / 抖音小程序（H5/App 无栈限制，但项目统一封装也避免跨端代码分叉）
**首次发现**：Phase 2.5（预见性沉淀；爱宠社社区强互链场景必踩）

---

### 14. 条件编译 `#ifdef` / `#endif` 平台分支静默失效

**场景**：vue/nvue 混合 + 四端覆盖，同一文件里写平台差异化代码（例如小程序走 `uni.login`、H5 走 web OAuth、App 走原生 SDK）。
**现象**：**编译不报错**，但那段平台专属代码在该平台上**根本没编译进去**（运行时缺逻辑，看起来"功能没做"）。开发者 grep 源码能看到代码，却怀疑编译器 bug——实际是指令格式错。极其隐蔽。
**根因**：uni-app 预编译器靠**精确字符串匹配**识别条件编译指令，不做模糊匹配也不报 warning。常见格式错误：
- 注释符号错：`.js/.ts/.vue <script>` 用 `// #ifdef`；`.scss/.css` 和 Vue `<style>` 用 `/* #ifdef */`；Vue `<template>` 用 HTML 注释 `<!-- #ifdef ... -->`。用错上下文对应的注释符 → 失效。
- 大小写错：必须全大写（`#ifdef H5`、`#ifdef MP-WEIXIN`），写 `#ifdef h5` 或 `#ifdef mp-weixin` 失效。
- 平台名拼错：`MP-WEIXIN` 不是 `MP-WECHAT`；`MP-TOUTIAO` 不是 `MP-DOUYIN`；`APP-PLUS` 不是 `APP`。
- 指令关键词错：必须 `#ifdef` / `#ifndef` / `#endif`，多写空格或换 `#if` 失效。
- `#ifdef` 和 `#endif` 之间漏配对或跨节点 → 部分版本沉默忽略整段。

**正确做法**：严格按下表写。项目里建一个 `src/types/platform.d.ts` 把常用平台常量列出来方便查。

**平台标识对照表（uni-app 官方）**：

| 平台 | 标识 |
|------|------|
| H5（Web） | `H5` |
| App（vue 页面） | `APP-PLUS` |
| App（nvue 页面） | `APP-NVUE` / `APP-PLUS-NVUE` |
| 微信小程序 | `MP-WEIXIN` |
| 支付宝小程序 | `MP-ALIPAY` |
| 百度小程序 | `MP-BAIDU` |
| 字节跳动（抖音/头条）小程序 | `MP-TOUTIAO` |
| QQ 小程序 | `MP-QQ` |
| 快手小程序 | `MP-KUAISHOU` |
| 京东小程序 | `MP-JD` |
| 所有小程序通配 | `MP` |

**三种上下文的注释符**：

```ts
// ========== .ts / .js / <script setup> ==========
// #ifdef MP-WEIXIN
const loginApi = () => uni.login({ provider: 'weixin' })
// #endif

// #ifdef H5
const loginApi = () => window.location.assign('/oauth/start')
// #endif

// #ifdef MP-WEIXIN || MP-TOUTIAO
// 多平台用 || 连接
const isMini = true
// #endif

// #ifndef H5
// 非 H5 才执行
// #endif
```

```scss
/* ========== .scss / .css / <style> ========== */
.header {
  height: 88rpx;
  /* #ifdef MP-WEIXIN */
  padding-top: var(--status-bar-height);
  /* #endif */
}
```

```vue
<!-- ========== <template> ========== -->
<template>
  <view>
    <!-- #ifdef H5 -->
    <web-only-banner />
    <!-- #endif -->

    <!-- #ifdef MP-WEIXIN || MP-TOUTIAO -->
    <MiniProgramEntry />
    <!-- #endif -->
  </view>
</template>
```

**反例**：
```ts
// 全小写 → 静默失效，生产里那块代码根本不存在
// #ifdef mp-weixin
uni.login({ provider: 'weixin' })
// #endif

// 平台名拼错 → 失效
// #ifdef MP-WECHAT
uni.login()
// #endif

// 注释符用错：scss 里用了 // 注释
<style lang="scss">
// #ifdef H5   ← 错：scss 里应该是 /* #ifdef H5 */
.h5-only { display: block; }
// #endif
</style>
```

**排查技巧**：编译后到 `dist/dev/<平台>` 目录翻编译产物，如果你期望的代码段不存在 → 99% 是条件编译写错。

**影响范围**：全端（凡涉及跨端差异化代码）
**首次发现**：Phase 2.5（预见性沉淀；四端同步兼容的项目必踩）

---

### 15. 页面生命周期跨端差异（`onShow` / `onActivated` / nvue 时机）

**场景**：TabBar 切换回关注页需刷新动态；从详情页返回首页需回补新数据；埋点需在页面展示时触发。
**现象**：
- 同一段 `onShow` 代码在 H5 `TabBar` 切换时触发，小程序端切换时不触发（因为小程序 TabBar 页面切换机制不同）。
- 用 Vue 生态的 `onActivated`（keep-alive 复用时触发）写刷新逻辑——**全端都不触发**，因为 uni-app 没有 keep-alive 概念。
- nvue 页面 `onShow` 触发时机比 vue 页面早（渲染完成前就 fire），在 `onShow` 里 query DOM 拿不到节点。
- `onLoad` 在小程序 redirectTo 过来时会重新触发，navigateTo 回退时不触发——"页面已创建但未重新 onLoad"。

**根因**：
- uni-app 页面生命周期是对小程序原生生命周期的抽象封装，不是 Vue 组件生命周期。`onLoad` / `onShow` / `onReady` / `onHide` / `onUnload` 在不同平台对应原生钩子不同。
- `onActivated` / `onDeactivated` 是 Vue Router keep-alive 的概念，**uni-app 页面级不走 keep-alive**，这两个钩子在页面级无意义（组件级可用，但需要 Vue3 keep-alive 包裹）。
- nvue 基于 Weex，渲染是异步的原生 render，`onShow` 触发时 Weex 还在计算布局。
- 小程序页面切换机制（redirectTo / navigateBack / switchTab）与 Vue SPA 不同，某些钩子只在特定切换方式下触发。

**正确做法**：
1. **需要每次可见时刷新数据**：一律用 `onShow`。不要用 `onActivated`。
2. **只需初次进入时做的事**（如读路由参数）：用 `onLoad`。
3. **需要 DOM 已渲染才做的事**（如 scroll-view 定位、uni.createSelectorQuery）：用 `onReady`。nvue 上 `onReady` 也不一定是完全渲染，需配合 `setTimeout` 或 `nextTick`。
4. **TabBar 切换刷新**：onShow 里即可，但需注意小程序里 switchTab 到同 tab 不触发 onShow（是正常行为）。
5. **组件内的显示/隐藏**：用自定义 `visible` prop + `watch`，不要指望 `onActivated`。

**跨端生命周期对照表**：

| 钩子 | H5 | 微信小程序 | 抖音小程序 | App-vue | App-nvue | 触发时机 |
|------|----|-----------|-----------|---------|----------|----------|
| `onLoad` | ✅ | ✅ | ✅ | ✅ | ✅ | 页面初次加载（含 redirectTo 进入）；navigateBack 回退不触发 |
| `onShow` | ✅ | ✅ | ✅ | ✅ | ✅（时机偏早）| 页面显示；switchTab 到**其他** tab 触发，回到**当前** tab 不触发 |
| `onReady` | ✅ | ✅ | ✅ | ✅ | ⚠️ 不保证 DOM 完成 | 页面初次渲染完成 |
| `onHide` | ✅ | ✅ | ✅ | ✅ | ✅ | 页面隐藏（navigateTo / switchTab 离开）|
| `onUnload` | ✅ | ✅ | ✅ | ✅ | ✅ | 页面销毁（navigateBack / redirectTo / reLaunch 离开）|
| `onActivated` | ❌ 页面级 | ❌ | ❌ | ❌ | ❌ | uni-app 页面级无 keep-alive；**不要用** |
| `onPullDownRefresh` | 需 pageMeta | ✅ | ✅ | ✅ | ✅ | 下拉刷新 |
| `onReachBottom` | ✅ | ✅ | ✅ | ✅ | ⚠️ 要自行实现 | 滚动到底部 |

**正确代码**：
```vue
<script setup lang="ts">
import { onLoad, onShow, onReady } from '@dcloudio/uni-app'
import { ref } from 'vue'

const noteId = ref('')
const list = ref([])

// 初次加载：读参数
onLoad((query) => {
  noteId.value = query?.id ?? ''
})

// 每次显示：刷新数据（TabBar 切回、从详情页返回都会触发）
onShow(() => {
  fetchFollowingFeed()
})

// 渲染完成：做 DOM 相关操作
onReady(() => {
  const q = uni.createSelectorQuery()
  q.select('.feed').boundingClientRect(() => { /* ... */ }).exec()
})
</script>
```

**反例**：
```vue
<script setup lang="ts">
import { onActivated } from 'vue'

// ❌ 错：uni-app 页面不走 keep-alive，这个钩子全端不触发
onActivated(() => {
  fetchFollowingFeed()  // 永远不会执行
})

// ❌ 错：onLoad 里 query DOM，nvue 下节点还不存在
onLoad(() => {
  const el = uni.createSelectorQuery().select('.feed')  // nvue 下 undefined
})
</script>
```

**影响范围**：四端均受影响（各平台差异化行为不同）
**首次发现**：Phase 2.5（预见性沉淀；社区类 App TabBar 强刷新场景必踩）

---
