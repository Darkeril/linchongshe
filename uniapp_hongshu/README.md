# 爱宠社

基于 UniApp (Vue2) 开发的宠物社区 App，支持 Android / iOS / H5 / 微信小程序多端运行。

## 功能模块

- **社区动态** — 图文/视频笔记发布、瀑布流浏览、点赞收藏评论
- **即时通讯** — WebSocket 私聊、群聊、消息通知
- **闲置交易** — 宠物用品二手交易、购物车、订单管理
- **AI 对话** — 集成 AI 助手，智能问答
- **个人中心** — 关注/粉丝、浏览记录、草稿箱、地址管理

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端框架 | UniApp + Vue 2 |
| UI 组件 | uView UI |
| 状态管理 | Vuex |
| 网络请求 | 自封装 axios（含 token 无感刷新） |
| 即时通讯 | WebSocket |
| 地图服务 | 高德地图 SDK |
| 打包工具 | HBuilderX 云打包 |

## 后端架构

Spring Cloud 微服务（9 个服务）：Gateway、Auth、Web、System、File、AI、Idle、Job、Monitor

基础设施：MySQL + Redis + Nacos + ElasticSearch，Docker Compose 一键部署

## 项目结构

```
├── apis/              # API 接口定义
├── components/        # 公共组件
├── config/            # 全局配置
├── pages/             # 主包页面（首页）
├── pkg-ai/            # AI 对话模块
├── pkg-auth/          # 登录注册模块
├── pkg-chat/          # 聊天模块
├── pkg-detail/        # 笔记/视频/闲置详情
├── pkg-main/          # 主功能（闲置市场、分类、我的）
├── pkg-mine/          # 个人中心（订单、地址、草稿等）
├── pkg-msg/           # 消息中心
├── pkg-others/        # 通知、支付等
├── pkg-profile/       # 个人资料编辑
├── pkg-publish/       # 发布笔记/闲置
├── pkg-search/        # 搜索
├── static/            # 静态资源（Logo、图标）
├── utils/             # 工具函数
├── manifest.json      # App 配置
├── pages.json         # 路由与页面配置
└── .env.js.example    # 环境配置模板
```

## 快速开始

1. 复制环境配置：`cp .env.js.example .env.js`，填入服务器地址
2. 安装依赖：`npm install`
3. HBuilderX 打开项目 → 运行到浏览器 / 模拟器 / 真机
4. 打包：发行 → 原生 App-云打包 → Android / iOS
