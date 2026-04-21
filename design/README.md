# 爱宠社 · 重新设计稿

「暖咖温润」方向的完整高保真设计稿 —— **9 大分组 / 40 屏** 全流程。

## 📁 文件说明

| 文件 | 作用 |
|---|---|
| `爱宠社重新设计.html` | 入口文件，React + Babel 内联 JSX 方式挂载所有屏幕 |
| `design-system.jsx` | 设计 token：色板 `PALETTES`、字体栈 `TYPE` |
| `design-canvas.jsx` | 可平移缩放的画布容器（仅设计稿浏览用，开发时不需要） |
| `ios-frame.jsx` | iOS 设备框架（仅设计稿浏览用） |
| `shared-components.jsx` | 通用组件：`TopBar` / `StatusLine` / `Tag` / `SectionTitle` / `PetAvatar` / `PetPlaceholder` / `PawIcon` / `HeartIcon` / `ShareIcon` |
| `screens-part1.jsx` ~ `part12.jsx` | 40 屏具体页面源码 |

## 🎨 设计 token（见 `design-system.jsx`）

### 主色板（latte 暖咖温润）
```js
primary:      '#c97b4a'   // 主色 · 烘焙咖
primaryInk:   '#9c5a31'   // 深色主色
primarySoft:  '#f5e4d3'   // 浅色背景
accent:       '#d9956a'   // 辅色 · 蜜桃
warn:         '#e07a5a'   // 强调/警告
bg:           '#f0eee9'   // 页面底色
surface:      '#ffffff'   // 卡片
surfaceDim:   '#f5efe6'   // 次级卡片
ink:          '#2a2017'   // 正文
inkSoft:      '#5a4a3a'
inkMuted:     '#8a7a66'
inkFaint:     '#b5a694'
divider:      '#ebe3d4'
```

### 字体
```js
TYPE.brand: 'Baloo 2, Noto Sans SC'   // 品牌/大标题
TYPE.body:  'Noto Sans SC'            // 正文
TYPE.num:   'Baloo 2'                 // 数字
```

### 常用数值
- 圆角 `radius`：小=10 / 中=16 / 大=22（默认 16）
- 屏幕尺寸：360 × 740（iPhone 13 mini 左右）
- 底部 TabBar 高度：约 76px（含安全区）
- 顶部 TopBar 高度：92px（含 StatusBar）

## 🗂 40 屏索引

### ① 入口 · Onboarding（part1）
- `SplashScreen` 启动页
- `LoginScreen` 登录

### ② 主体 · 浏览与创作（part1-2, 9）
- `HomeScreen` 首页瀑布流
- `SearchScreen` 搜索
- `NoteDetailScreen` 笔记详情
- `CommentsScreen` 评论页
- `TopicScreen` 话题页
- `PublishScreen` 发布

### ③ 宠物档案（part9）
- `AddPetScreen` 添加宠物
- `PetProfileScreen` 宠物主页
- `VaccineScreen` 疫苗/健康记录

### ④ 商业 · 市集与交易（part3, 8）
- `MarketScreen` 市集
- `ProductDetailScreen` 商品详情
- `CartScreen` 购物车
- `CheckoutScreen` 结算
- `OrdersScreen` 订单列表
- `OrderDetailScreen` 订单详情

### ⑤ 社交（part5, 7）
- `MessagesScreen` 消息列表
- `ChatScreen` 一对一聊天
- `NotificationsScreen` 互动通知
- `FollowListScreen` 关注/粉丝

### ⑥ 个人与设置（part4, 6, 7）
- `ProfileScreen` 个人主页
- `EditProfileScreen` 编辑资料
- `AddressBookScreen` 地址簿
- `CouponsScreen` 卡券
- `SettingsScreen` 设置
- `EmptyStatesScreen` 空态合集

### ⑦ 发现 · 引导与内容形态（part10）
- `OnboardingScreen` 4 步引导
- `ExploreScreen` 分类发现
- `ReelsScreen` 竖版短视频
- `NearbyScreen` 附近地图
- `LiveScreen` 直播间
- `SignInScreen` 签到 + 任务中心

### ⑧ 增值 · 收藏与交易闭环（part11）
- `FavoritesScreen` 收藏夹
- `HistoryScreen` 浏览历史
- `ShippingScreen` 物流轨迹
- `WalletScreen` 我的钱包
- `SupportScreen` AI 客服

### ⑨ 社区 · 公益与特色（part12）
- `PetDateScreen` Tinder 风宠物交友
- `AdoptScreen` 领养中心
- `LostScreen` 走失播报
- `AlbumScreen` 宠物相册
- `VIPScreen` 会员中心
- `AboutScreen` 关于

## 🚀 本地查看设计稿

```bash
# 用任意静态服务器起一个（必须走 HTTP，不能直接 file://，否则 Babel 读不到 JSX）
npx serve design/
# 打开 http://localhost:3000/爱宠社重新设计.html
```

## 🛠 给 Claude Code 的实现建议

1. **技术栈建议**：React + TypeScript + Tailwind CSS / Emotion
   - 把 `PALETTES.latte` 对应到 Tailwind 的 `theme.extend.colors`
   - 字体通过 `next/font` 或 `@fontsource` 引入 Baloo 2 + Noto Sans SC

2. **组件化路径**：
   - `shared-components.jsx` 里的组件（TopBar / Tag / PetAvatar / StatusLine / SectionTitle / Icon 系列）→ 直接 1:1 复刻成 `src/components/ui/`
   - `PetPlaceholder` 实际开发时替换成 `<img>`（接后端图片）
   - 每个 Screen 拆成独立页面/路由

3. **占位图**：
   - 所有 `<PetPlaceholder>` 是 CSS 画的占位，生产环境换成真实图片 CDN
   - `<PetAvatar variant="...">` 同理

4. **还原度保证**：
   - 所有尺寸、颜色、间距都在 JSX 里精确写死，**直接抄**即可
   - 布局基本是 `position: absolute` + 显式 top/left —— 因为是设计稿追求像素对齐；实际实现可以改用 flex/grid 让它响应式

5. **状态管理**：
   - 设计稿里的 `useState` 只是演示切换（Tab、折叠），开发时对接真实数据源

6. **需要后端的点**：
   - 登录 / OAuth
   - 笔记 CRUD + 瀑布流分页
   - 商品 / 订单 / 支付 / 物流
   - 私信 IM
   - LBS 附近（NearbyScreen）
   - 直播推拉流（LiveScreen）
   - AI 客服（SupportScreen）

## 📝 关于 Tweaks 面板

`爱宠社重新设计.html` 顶部有个 Tweaks 面板，可切换 3 个色板（latte / peach / cream）和圆角大小。实际开发时只用 `latte` 即可，其他色板是方向探索备份。

---

🐾 祝开发顺利！
