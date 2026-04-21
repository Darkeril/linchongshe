# 爱宠社 - 宠物社区全栈项目

基于 Spring Cloud 微服务 + 多端前端的宠物社区平台。

## 项目结构

```
├── springcloud_hongshu/       # 后端微服务（Spring Cloud）
│   ├── hongshu-gateway        # 网关服务
│   ├── hongshu-auth           # 认证中心
│   ├── hongshu-modules/
│   │   ├── hongshu-web        # 社区业务（笔记、IM、评论）
│   │   ├── hongshu-system     # 系统管理
│   │   ├── hongshu-file       # 文件服务
│   │   ├── hongshu-ai         # AI 对话服务
│   │   ├── hongshu-idle       # 闲置交易服务
│   │   ├── hongshu-job        # 定时任务
│   │   └── hongshu-monitor    # 监控服务
│   └── hongshu-common/        # 公共模块
│
├── uniapp_hongshu/            # 移动端 App（UniApp Vue2）
├── vue_web_hongshu/           # 用户端 Web（Vue3 + Element Plus）
├── vue_arco_admin_hongshu/    # 管理后台（Vue3 + Arco Design）
└── vue_ai_hongshu/            # AI 对话端（Vue3 + Naive UI）
```

## 技术栈

### 后端
| 技术 | 版本 |
|------|------|
| Spring Boot | 3.2.6 |
| Spring Cloud | 2023.0.3 |
| Spring Cloud Alibaba | 2023.0.1.2 |
| JDK | 17 |
| MySQL | 8.0 |
| Redis | 7.x |
| Nacos | 2.4.3 |
| ElasticSearch | 7.x |

### 前端
| 端 | 框架 | 说明 |
|----|------|------|
| 移动端 | UniApp + Vue 2 | Android / iOS / H5 / 小程序 |
| 用户 Web | Vue 3 + Element Plus | PC 浏览器端 |
| 管理后台 | Vue 3 + Arco Design | 后台管理系统 |
| AI 对话 | Vue 3 + Naive UI | AI 智能助手 |

## 功能模块

- **社区动态** — 图文/视频笔记发布、瀑布流、点赞收藏评论、ES 全文搜索
- **即时通讯** — WebSocket 私聊、群聊、消息通知
- **闲置交易** — 宠物用品二手市场、购物车、订单
- **AI 对话** — 集成大模型智能问答
- **用户体系** — 注册登录、关注粉丝、个人主页
- **管理后台** — 用户管理、内容审核、数据统计

## 部署

Docker Compose 一键部署，包含 MySQL、Redis、Nacos、ES 及 9 个微服务。

## 分支说明

| 分支 | 说明 |
|------|------|
| `main` | 全量源码（前端 + 后端） |
| `uniapp-mobile` | 仅移动端 UniApp 代码 |
