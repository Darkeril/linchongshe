# 爱宠社 UI 重设计路线图

> 原则：**按用户视觉路径走** —— 用户先看到哪个界面就先改哪个。
>
> 设计稿来源：`design/` 目录下 12 个 JSX 文件，共 44 个页面设计。
>
> 当前分支：`feature/phase1-ui-redesign`

---

## Phase 1 — 第一印象 ✅ 已完成

> 用户打开 App 最先看到的：启动页 → 登录 → TabBar

| 步骤 | 页面 | 文件 | 设计稿 | 状态 |
|------|------|------|--------|------|
| 0 | 设计系统基础 | `static/styles/theme.scss`, `App.vue` | `design-system.jsx` | ✅ |
| 1 | App 图标 | `static/logoImage.png`, `unpackage/res/icons/*` | `design-system.jsx` PawIcon | ✅ |
| 2 | 闪屏页 | `pages/splash/splash.vue` | `screens-part1.jsx` SplashScreen | ✅ |
| 3 | 登录方式页 | `pkg-auth/pages/login/loginMethod.vue` | `screens-part1.jsx` LoginScreen | ✅ |
| 4 | 密码/验证码登录页 | `pkg-auth/pages/login/login.vue` + `css/main.css` | — (配色迁移) | ✅ |
| 5 | 底部 TabBar | `components/custom-tabbar/CustomTabBar.vue` | `screens-part1.jsx` AppTabBar | ✅ |
| — | 登录组件 | `watch-button.vue`, `watch-input.vue` | — (配色迁移) | ✅ |
| — | SVG 图标资源 | `static/icons/` (12个), `static/pet-avatars/` (4个) | `design-system.jsx` | ✅ |
| BUG | 独立分包刷新背景色丢失 | `pages.json` globalStyle + JS fixPageBackground | — | ✅ |

---

## Phase 2 — 核心内容浏览

> 登录成功后用户第一件事：看首页信息流，点开感兴趣的笔记。

| 页面 | 现有文件 | 设计稿 | 说明 |
|------|----------|--------|------|
| **首页** | `pages/index/index.vue` | `screens-part1.jsx` HomeScreen | 瀑布流信息流，关注/发现/附近 三个 Tab |
| 首页-发现 | `components/index-pages/DiscoverPage.vue` | 同上 | 发现子页 |
| 首页-关注 | `components/index-pages/FollowPage.vue` | 同上 | 关注子页 |
| 首页-附近 | `components/index-pages/NearbyPage.vue` | 同上 | 附近子页 |
| **笔记详情** | `pkg-detail/pages/notesDetail/notesDetail.vue` | `screens-part2.jsx` NoteDetailScreen | 图文详情+评论区 |
| 笔记视频详情 | `pkg-detail/pages/notesDetail/noteVideoD.vue` | 同上 (视频版) | 视频笔记播放 |
| 瀑布流组件 | `components/OptimizedWaterFall.vue` | — | 卡片样式更新 |
| 笔记卡片组件 | `components/NotesListItem.vue` | — | 卡片样式更新 |
| 骨架屏 | `components/SkeletonScreen.vue` | — | 配色迁移 |

**改动重点：**
- 首页顶部导航栏 → 设计稿 TopBar 样式（搜索框+头像）
- 瀑布流卡片 → 圆角加大、阴影柔化、配色暖化
- 笔记详情页 → 整体布局重写，评论区样式
- 页面背景 `#ffffff` → `var(--acs-bg)` 或 `#F4EDE2`

---

## Phase 3 — 内容创作与搜索

> 用户想发帖，或想搜索特定内容。

| 页面 | 现有文件 | 设计稿 | 说明 |
|------|----------|--------|------|
| **发布笔记** | `pkg-publish/pages/publishNotes/publishNotes.vue` | `screens-part3.jsx` PublishScreen | 图片上传+文案编辑 |
| 发布闲置 | `pkg-publish/pages/publishIdle/publishIdle.vue` | — (参考 PublishScreen 风格) | 闲置商品发布 |
| **搜索页** | `pkg-search/pages/searchPage/searchPage.vue` | `screens-part6.jsx` SearchScreen | 搜索主页+热搜 |
| 搜索结果 | `pkg-search/pages/searchPage/searchDetailPage.vue` | 同上 | 搜索结果列表 |
| 商品搜索 | `pkg-search/pages/searchPage/productSearchPage.vue` | 同上 | 商品搜索 |
| **话题页** | `pkg-search/pages/topicIndex/topicIndex.vue` | `screens-part9.jsx` TopicScreen | 话题详情+讨论 |

**改动重点：**
- 发布页整体重写，图片选择区 → 设计稿的拖拽排序卡片风格
- 搜索页热搜标签 → 圆角标签 + 暖色系
- 话题页头部 → 渐变 Banner

---

## Phase 4 — 消息与社交

> 用户收到通知和私信，查看互动。

| 页面 | 现有文件 | 设计稿 | 说明 |
|------|----------|--------|------|
| **消息列表** | `pkg-main/pages/message/message.vue` | `screens-part5.jsx` MessagesScreen | 私信会话列表 |
| **聊天页** | `pkg-msg/pages/chat/chat.vue` | `screens-part5.jsx` ChatScreen | 一对一聊天 |
| **互动通知** | `pkg-others/pages/praiseAndCollect/praiseAndCollect.vue` | `screens-part7.jsx` NotificationsScreen | 赞/评论/关注 通知 |
| 评论页 | `pkg-msg/pages/comment/comment.vue` | `screens-part9.jsx` CommentsScreen | 评论列表 |
| 关注消息 | `pkg-others/pages/attentionMessageList/attentionMessageList.vue` | — | 新增关注通知 |
| 系统通知 | `pkg-others/pages/systemNoticeList/systemNoticeList.vue` | — | 系统消息 |
| **关注/粉丝** | `pkg-mine/pages/attentionAndFans/attentionAndFans.vue` | `screens-part7.jsx` FollowListScreen | 用户列表卡片 |
| 群聊 | `pkg-msg/pages/createGroup/`, `groupChatList/`, `groupDetail/` | — | 配色迁移 |

**改动重点：**
- 消息列表 → 设计稿的会话卡片样式（头像+最后消息+时间+未读徽标）
- 聊天气泡 → 暖色系气泡，发送方焦糖渐变
- 通知页 → 分类 Tab（赞和收藏/评论/关注）

---

## Phase 5 — 个人中心

> 用户管理自己的资料和设置。

| 页面 | 现有文件 | 设计稿 | 说明 |
|------|----------|--------|------|
| **我的主页** | `pkg-main/pages/mine/mine.vue` | `screens-part5.jsx` ProfileScreen | 个人主页（粉丝/关注/笔记统计） |
| 他人主页 | `pkg-mine/pages/mine/otherMine.vue` | 同上 (他人视角) | 他人个人主页 |
| **编辑资料** | `pkg-profile/pages/editData/editData.vue` | `screens-part7.jsx` EditProfileScreen | 昵称/简介/头像 |
| **设置** | `pkg-mine/pages/setting/setting.vue` | `screens-part7.jsx` SettingsScreen | 通知/隐私/账号 |
| 地址管理 | `pkg-mine/pages/addressManagement/addressManagement.vue` | `screens-part7.jsx` AddressBookScreen | 收货地址 CRUD |
| 地址编辑 | `pkg-mine/pages/addressEdit/addressEdit.vue` | 同上 | 地址表单 |
| 浏览记录 | `pkg-mine/pages/browseRecord/browseRecord.vue` | `screens-part11.jsx` HistoryScreen | 浏览历史 |
| 收藏夹 | `pkg-mine/pages/shoppingCart/shoppingCart.vue` | `screens-part11.jsx` FavoritesScreen | 收藏的笔记/商品 |
| 草稿箱 | `pkg-mine/pages/drafts/drafts.vue` | — | 配色迁移 |
| 骨架屏 | `components/ProfileSkeleton.vue` | — | 配色迁移 |

**改动重点：**
- 个人主页头部 → 大圆角卡片，背景毛玻璃，统计数据横排
- 设置页 → 分组卡片式列表
- 编辑资料 → 圆角表单输入框

---

## Phase 6 — 市集电商

> 用户逛市集、买东西、管理订单。

| 页面 | 现有文件 | 设计稿 | 说明 |
|------|----------|--------|------|
| **市集首页** | `pkg-main/pages/idle/idle.vue` | `screens-part4.jsx` MarketScreen | 分类导航+热卖+推荐 |
| **商品详情** | `pkg-detail/pages/idleDetail/idleDetail.vue` | `screens-part4.jsx` ProductDetailScreen | 图片+评价+规格 |
| 商品视频 | `pkg-detail/pages/idleDetail/idleVideoD.vue` | 同上 | 视频版商品详情 |
| 分类页 | `pkg-main/pages/category/category.vue` | — | 配色迁移 |
| 全部分类 | `pkg-main/pages/categories/categories.vue` | — | 配色迁移 |
| 分类商品 | `pkg-main/pages/categoryProducts/categoryProducts.vue` | — | 配色迁移 |
| **购物车** | `pkg-mine/pages/shoppingCart/shoppingCart.vue` | `screens-part8.jsx` CartScreen | 商品列表+结算 |
| **确认订单** | `pkg-mine/pages/confirmOrder/confirmOrder.vue` | `screens-part8.jsx` CheckoutScreen | 地址+支付确认 |
| **订单列表** | `pkg-mine/pages/orders/orders.vue` | `screens-part8.jsx` OrdersScreen | 订单状态卡片 |
| **订单详情** | `pkg-mine/pages/orderDetail/orderDetail.vue` | `screens-part8.jsx` OrderDetailScreen | 物流+退货 |
| 支付 | `pkg-others/pages/payEntry/payEntry.vue` | — | 配色迁移 |
| 闲置瀑布流 | `components/idle-water-fall/idle-water-fall.vue` | — | 卡片样式更新 |
| 购物骨架屏 | `components/ShoppingSkeleton.vue` | — | 配色迁移 |

**改动重点：**
- 市集首页 → 横向分类滚动 + 瀑布流商品卡片
- 商品详情 → 大图轮播 + 规格选择弹窗
- 购物车 → 滑动删除 + 圆角卡片
- 订单卡片 → 状态标签+进度条

---

## Phase 7 — 宠物功能（新功能开发）

> 爱宠社的核心差异化：宠物档案管理。这些页面**目前不存在**，需要新建。

| 页面 | 文件（新建） | 设计稿 | 说明 |
|------|-------------|--------|------|
| **宠物主页** | `pkg-pet/pages/petProfile/petProfile.vue` | `screens-part9.jsx` PetProfileScreen | 宠物档案卡片 |
| **添加宠物** | `pkg-pet/pages/addPet/addPet.vue` | `screens-part9.jsx` AddPetScreen | 宠物信息表单 |
| **疫苗记录** | `pkg-pet/pages/vaccine/vaccine.vue` | `screens-part9.jsx` VaccineScreen | 健康记录时间线 |
| **新手引导** | `pages/onboarding/onboarding.vue` | `screens-part10.jsx` OnboardingScreen | 4 步功能介绍滑屏 |
| **AI 客服** | `pkg-ai/pages/chat/chat.vue` (已有框架) | `screens-part11.jsx` SupportScreen | 对话式客服 |
| **空态页面** | `components/EmptyState.vue` | `screens-part6.jsx` EmptyStatesScreen | 统一空态组件 |

**前置条件：**
- 需新建 `pkg-pet` 子包
- 需后端配合：宠物 CRUD API、疫苗记录 API
- pages.json 中注册新路由

---

## Phase 8 — 高级功能（新功能开发）

> 社区扩展功能，提升用户粘性。这些页面**全部需要新建**。

| 页面 | 文件（新建） | 设计稿 | 说明 |
|------|-------------|--------|------|
| **分类发现** | `pkg-main/pages/explore/explore.vue` | `screens-part10.jsx` ExploreScreen | 宠物品种/话题分类浏览 |
| **竖版短视频** | `pkg-main/pages/reels/reels.vue` | `screens-part10.jsx` ReelsScreen | 类抖音视频流 |
| **附近地图** | `pkg-main/pages/nearby/nearby.vue` | `screens-part10.jsx` NearbyScreen | 地理位置宠物动态 |
| **直播间** | `pkg-main/pages/live/live.vue` | `screens-part10.jsx` LiveScreen | 直播+评论+礼物 |
| **签到任务** | `pkg-main/pages/signIn/signIn.vue` | `screens-part10.jsx` SignInScreen | 每日签到+任务列表 |
| **宠物交友** | `pkg-pet/pages/petDate/petDate.vue` | `screens-part12.jsx` PetDateScreen | Tinder 风格卡片流 |
| **领养中心** | `pkg-pet/pages/adopt/adopt.vue` | `screens-part12.jsx` AdoptScreen | 待领养宠物列表 |
| **走失播报** | `pkg-pet/pages/lost/lost.vue` | `screens-part12.jsx` LostScreen | 失踪宠物公告 |
| **宠物相册** | `pkg-pet/pages/album/album.vue` | `screens-part12.jsx` AlbumScreen | 照片库 |
| **会员中心** | `pkg-mine/pages/vip/vip.vue` | `screens-part12.jsx` VIPScreen | 会员特权展示 |
| **我的钱包** | `pkg-mine/pages/wallet/wallet.vue` | `screens-part11.jsx` WalletScreen | 余额/交易/提现 |
| **物流轨迹** | `pkg-mine/pages/shipping/shipping.vue` | `screens-part11.jsx` ShippingScreen | 快递追踪 |
| **卡券管理** | `pkg-mine/pages/coupons/coupons.vue` | `screens-part7.jsx` CouponsScreen | 优惠券/会员卡 |
| **关于** | `pkg-mine/pages/about/about.vue` | `screens-part12.jsx` AboutScreen | 版本/隐私/介绍 |

**前置条件：**
- 大量后端 API 开发（直播、支付、地图、宠物社交匹配等）
- 第三方服务接入（地图 SDK、直播 SDK、支付 SDK）
- 可按优先级拆分为 8a / 8b / 8c 子阶段

---

## 总览

| Phase | 主题 | 页面数 | 类型 | 预计工作量 |
|-------|------|--------|------|-----------|
| **1** ✅ | 第一印象（启动/登录/TabBar） | 6 页 + 资源 | 重写 + 配色 | 已完成 |
| **2** | 核心浏览（首页/笔记详情） | 6 页 + 4 组件 | 重写 | ★★★★ 大 |
| **3** | 创作搜索（发布/搜索/话题） | 6 页 | 重写 | ★★★ 中 |
| **4** | 消息社交（消息/聊天/通知） | 8 页 | 重写 + 配色 | ★★★ 中 |
| **5** | 个人中心（主页/资料/设置） | 10 页 | 重写 + 配色 | ★★★ 中 |
| **6** | 市集电商（商品/购物车/订单） | 12 页 + 2 组件 | 重写 + 配色 | ★★★★ 大 |
| **7** | 宠物功能（档案/疫苗/引导） | 6 页 | **新建** | ★★★ 中 |
| **8** | 高级功能（短视频/直播/交友等） | 14 页 | **新建** | ★★★★★ 极大 |

**总计：44 个设计稿页面 → 8 个 Phase 覆盖全部**

---

## 注意事项

1. **Phase 2-6** 是对现有页面的 UI 重设计（改样式，不改业务逻辑）
2. **Phase 7-8** 是新功能开发（需要新建页面 + 后端 API）
3. 每个 Phase 完成后提 PR 合入 main，打 tag
4. 独立分包（`independent: true`）的页面需注意 CSS 变量不可用，必须用硬编码 fallback
5. 设计稿像素基准：360×740（iPhone 13 mini），1px = 2rpx
