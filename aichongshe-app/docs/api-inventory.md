# 爱宠社后端 API 全量盘点

> 盘点时间：2026-04-22  
> 盘点对象：`springcloud_hongshu/`（Spring Cloud 微服务）  
> 盘点视角：backend-architect（架构/模块）+ api-tester（接口/字段/风险）  
> 前端参照：`design/screens-part1.jsx` ~ `screens-part12.jsx`（爱宠社 App 设计稿）

---

## 概览

- **总 Controller 文件数**：121（含 BaseController / 注释掉的 OssController / AI 子项目）
- **本次纳入盘点的有效 Controller**：约 105（剔除 BaseController 抽象类、AI 子项目 base 包）
- **业务域模块数（顶层）**：7（auth / gateway / web / idle / system / job / file / ai）
- **总 API 数量（估算）**：≈ 520+
  - App 端（移动端前端直接调用）：**≈ 165**
  - Web 端（PC Web 前端调用）：≈ 95
  - Sys/管理端（Admin 后台调用）：≈ 230
  - AI 子项目（独立对外）：≈ 60
- **App 端接口可用性分布**：
  - ✅ 可直接用：**≈ 95**
  - ⚠️ 需改造：**≈ 45**
  - ❌ 设计稿需要但未实现：**≥ 25**（详见末节）

---

## 技术栈

| 维度 | 选型 |
|------|------|
| JDK | Java 17 |
| Spring Boot | 3.2.6 |
| Spring Cloud | 2023.0.3 |
| Spring Cloud Alibaba | 2023.0.1.2（Nacos 注册/配置中心、Sentinel 限流、Seata） |
| 网关 | `hongshu-gateway`（WebFlux/Reactive，端口 8080） |
| 认证 | 双轨：`hongshu-auth` 使用 JWT（jjwt 0.9.1）；`hongshu-modules/hongshu-web` 使用自研 `IWebAuthUserService` + `@NoLoginIntercept` 注解（自定义 JwtUtilss） |
| ORM | MyBatis-Plus 3.5.5 + PageHelper 2.0.0 |
| 数据库 | MySQL（dynamic-datasource 多数据源） |
| 缓存 | Redis（`hongshu-common-redis`） |
| 消息队列 | RocketMQ 2.2.3（点赞/收藏 V2 走 Redis+RocketMQ） |
| 搜索引擎 | Elasticsearch 8.11.4（笔记/商品搜索） |
| 对象存储 | 阿里云 OSS / MinIO 8.2.2 / 七牛 7.7.0（多云可配） |
| 大模型 | 通义千问（文案生成） |
| 文档 | Springdoc OpenAPI 2.3.0 |
| 支付 | 支付宝 + 微信支付（含回调 notify） |
| 短信 | 自建 SmsAsyncService（异步） |
| 微信 | 小程序登录 / 公众号 JS-SDK / 扫码登录 |

---

## 模块：hongshu-auth（平台级 JWT 认证）

**源码位置**：`hongshu-auth/src/main/java/com/hongshu/auth/`

### 模块职责
面向 **管理后台 / 系统用户** 的 JWT 认证中心，基于 `TokenService` + `SysLoginService`。与 App/Web 前台登录是两套体系。

### API 列表

#### ⚠️ `POST /login` - 后台用户登录
- **Controller**: `com.hongshu.auth.controller.TokenController#login`
- **入参**: body `LoginBody { username, password }`
- **返回**: `R<{access_token, expires_in, ...}>`
- **认证**: 匿名
- **前端用途**: 管理后台登录；**App 前台不使用**（App 用 `/app/auth/login`）

#### ✅ `DELETE /logout` - 后台用户登出
- **Controller**: `TokenController#logout`
- **入参**: Header `Authorization`
- **返回**: `R<?>`

#### ✅ `POST /refresh` - 刷新 Token
- `TokenController#refresh`，Header Authorization，返回 R<?>

#### ⚠️ `POST /register` - 后台用户注册
- `TokenController#register`，body `RegisterBody { username, password }`
- **改造建议**: App 端注册不要走这里，使用 `/app/auth/register`

---

## 模块：hongshu-modules/hongshu-web（核心社区：笔记/评论/点赞/关注/聊天/用户）

**源码位置**：`hongshu-modules/hongshu-web/src/main/java/com/hongshu/web/controller/`

### 模块职责
爱宠社 **社区主域** —— 笔记、评论、点赞收藏、关注、聊天（私聊/群聊）、话题标签、搜索、用户资料、通知公告、AI 文案生成、第三方（微信/短信）、反馈。三层命名空间：
- `/app/**` ：移动端（App / 小程序）
- `/**` （无前缀）：PC Web 前端
- `/` 下混合：管理端 `sys/` 使用

---

### 2.1 子模块：App - 认证登录（`AppAuthController`）

**Controller**: `com.hongshu.web.controller.app.AppAuthController`  
**RequestMapping**: `/app/auth`  
**前端用途**：登录/注册/忘记密码/微信登录（screens-part1 Login）

| Status | Method | Path | 方法名 | 入参 | 返回 | 认证 |
|---|---|---|---|---|---|---|
| ✅ | POST | `/app/auth/login` | login | body `AuthUserDTO { username, password, terminal }` | `{accessToken, refreshToken, user}` | 匿名 |
| ✅ | POST | `/app/auth/loginByCode` | loginByCode | body `AuthUserDTO { phone, smsCode }` | 同上 | 匿名 |
| ✅ | POST | `/app/auth/sendCode` | sendVerifyCode | query `phone`, `captchaVerification` | `{sent: true}` | 匿名 |
| ⚠️ | POST | `/app/auth/demoLogin` | demoLogin | body `AuthUserDTO` | 同 login | 匿名 —— **生产环境需开关** |
| ✅ | GET | `/app/auth/checkToken` | checkToken | Header token | `R.ok()` | 需要登录 |
| ⚠️ | GET | `/app/auth/getUserInfoByToken` | getUserInfoByToken | query `accessToken` | `WebUser` | **敏感：token 不该走 query，应走 Header** |
| ✅ | POST | `/app/auth/register` | register | body `AuthUserDTO` | `WebUser` | 匿名 |
| ✅ | POST | `/app/auth/isRegister` | isRegister | body `AuthUserDTO` | `boolean` | 登录 |
| ⚠️ | GET | `/app/auth/loginOut` | loginOut | query `userId` | `R.ok` | **应用 POST，且不从参数传 userId（从 token 取）** |
| ✅ | POST | `/app/auth/updatePassword` | updatePassword | body `AuthUserDTO` | `boolean` | 登录 |
| ⚠️ | GET | `/app/auth/refreshToken` | refreshToken | query `refreshToken` | `{accessToken, ...}` | **refreshToken 放 query 不安全，应 POST body** |
| ✅ | POST | `/app/auth/wechatLogin` | wechatLogin | body `WeChatLoginDTO { code, encryptedData, iv }` | 同 login | 匿名 |

**风险点**：敏感 token 以 query 传递、GET 做登出等写操作，不符合规范。

---

### 2.2 子模块：App - 用户资料（`AppUserController` + `AppUserSecurityController`）

**Controller**: `AppUserController`（`/app/user`）、`AppUserSecurityController`（`/app/user`）  
**前端用途**：个人中心 / 编辑资料 / 账号安全（screens-part5 Profile、part7 EditProfile / Settings）

| Status | Method | Path | 描述 | 入参 | 备注 |
|---|---|---|---|---|---|
| ✅ | GET | `/app/user/getTrendByUser/{currentPage}/{pageSize}` | 获取用户的动态（笔记）列表 | query `userId`, `type`, `status` | 返回 `Page<NoteSearchVo>` |
| ✅ | GET | `/app/user/getUserById` | 获取用户信息 | query `userId` | `WebUser` |
| ⚠️ | POST | `/app/user/updateUser` | 更新用户信息（含头像 multipart） | `avatarFile` + `user` JSON | **multipart+JSON 混用前端 UniApp 不友好，建议拆分** |
| ✅ | POST | `/app/user/updateAvatarUrl` | 更新头像 URL | body `UserAppDTO` | |
| ✅ | POST | `/app/user/updateAvatar` | 直接上传头像文件 | multipart `file`, `userId` | |
| ✅ | POST | `/app/user/updateUsername` | 修改昵称 | body `UserAppDTO` | |
| ✅ | POST | `/app/user/updateDes` | 修改个性签名 | body `UserAppDTO` | **命名缩写不规范，应 `updateDescription`** |
| ✅ | POST | `/app/user/updateTags` | 修改标签（兴趣） | body `UserAppDTO` | |
| ✅ | POST | `/app/user/updateBackgroundImage` | 修改背景图 URL | body | |
| ✅ | POST | `/app/user/updateBackgroundImageFile` | 上传背景图文件 | multipart | |
| ✅ | POST | `/app/user/updateSex` | 修改性别 | body | |
| ✅ | POST | `/app/user/updateBirthday` | 修改生日 | body | |
| ✅ | POST | `/app/user/updateArea` | 修改所在地 | body | |
| ✅ | GET | `/app/user/getUserByKeyword/{currentPage}/{pageSize}` | 搜索用户 | query `keyword` | `Page<WebUser>` |
| ✅ | GET | `/app/user/saveUserSearchRecord` | 保存搜索记录 | query `keyword` | |
| ⚠️ | GET | `/app/user/getUserList` | 会员列表（？App 不该用） | WebUser | **可能是管理端接口错放到 app 包** |

**AppUserSecurityController**（`/app/user`，兼容历史路径）：

| Status | Method | Path | 描述 |
|---|---|---|---|
| ✅ | GET | `/app/user/getUserIsBindThird` | 获取第三方绑定状态 |
| ✅ | POST | `/app/user/updatePhoneNumber` | 更新绑定手机号（query phoneNumber + smsCode） |
| ✅ | POST | `/app/user/resetPasswordByPhone` | 手机号重置密码（匿名） |
| ✅ | POST | `/app/user/resetPasswordByOld` | 原密码修改密码（UserPasswordOldDTO） |
| ✅ | POST | `/app/user/cancelAccount` | 注销账号（软禁用） |

---

### 2.3 子模块：App - 笔记（`AppNoteController` + `AppNoteBehaviorController`）

**Controller**: `AppNoteController`（`/app/note`）  
**前端用途**：笔记详情（part2 NoteDetail）、发布（part3 Publish）、我的笔记（part5 Profile）

| Status | Method | Path | 描述 | 备注 |
|---|---|---|---|---|
| ✅ | GET | `/app/note/getNoteById` | 获取笔记详情 | query `noteId`；匿名；返回 `NoteVo` |
| ⚠️ | POST | `/app/note/saveNoteByDTO` | 发布笔记（multipart：requestId + noteData JSON + coverFile + uploadFiles） | 前端要处理 multipart，**图片数量限制需文档化** |
| ⚠️ | POST | `/app/note/saveNote` | 发布笔记（纯 JSON，文件需先走 OSS 上传） | 推荐走这个，multipart 那个可逐步淘汰 |
| ✅ | POST | `/app/note/deleteNoteByIds` | 批量删除笔记 | body `List<String> noteIds` |
| ⚠️ | POST | `/app/note/updateNoteByDTO` | 更新笔记（multipart） | 同 saveNoteByDTO 问题 |
| ✅ | GET | `/app/note/getHotPage/{currentPage}/{pageSize}` | 获取热门笔记分页 | `Page<NoteVo>` |
| ⚠️ | GET | `/app/note/pinnedNote` | 置顶笔记 | query `noteId`；**应 POST，改写操作不该 GET** |

**AppNoteBehaviorController**（`/app/noteBehavior`）—— 埋点上报：

| Status | Method | Path | 描述 |
|---|---|---|---|
| ✅ | POST | `/app/noteBehavior/report` | 单条上报（`NoteBehaviorReportDTO { eventType, nid, uid, scene, durationSec, completed }`） |
| ✅ | POST | `/app/noteBehavior/reportBatch` | 批量上报 |

---

### 2.4 子模块：App - 评论（`AppCommentController`）

`/app/comment`

| Status | Method | Path | 描述 |
|---|---|---|---|
| ⚠️ | GET | `/app/comment/getOneCommentByNoteId/{currentPage}/{pageSize}` | 一级评论分页 | 命名 `getOneCommentByNoteId` → `getTopComments` 更清晰 |
| ✅ | GET | `/app/comment/getCommentById` | 单条评论详情 |
| ✅ | POST | `/app/comment/saveCommentByDTO` | 发表评论（`CommentDTO`） |
| ✅ | POST | `/app/comment/syncCommentByIds` | 批量同步评论集 |
| ✅ | GET | `/app/comment/getTwoCommentByOneCommentId/{currentPage}/{pageSize}` | 二级评论分页 |
| ✅ | GET | `/app/comment/getNoticeComment/{currentPage}/{pageSize}` | 评论通知列表（消息页 part5） |
| ✅ | GET | `/app/comment/getCommentWithCommentByNoteId/{currentPage}/{pageSize}` | 带二级的一级评论列表 |
| ✅ | GET | `/app/comment/getCommentWithCommentByProductId/{currentPage}/{pageSize}` | 商品评论 |
| ✅ | GET | `/app/comment/scrollComment` | 定位到指定评论（跳转锚点） |
| ⚠️ | GET | `/app/comment/deleteCommentById` | 删除评论 | **应为 DELETE 或 POST** |

---

### 2.5 子模块：App - 点赞收藏（`AppLikeOrCollectionController`）

`/app/likeOrCollection` —— V2 版本走 Redis + RocketMQ，高并发优化

| Status | Method | Path | 描述 |
|---|---|---|---|
| ✅ | POST | `/app/likeOrCollection/likeOrCollectionByDTO` | 点赞/收藏/取消（body `LikeOrCollectionDTO { targetId, targetType, actionType }`） |
| ✅ | POST | `/app/likeOrCollection/isLikeOrCollection` | 查询点赞收藏状态（Redis 缓存） |
| ✅ | GET | `/app/likeOrCollection/getNoticeLikeOrCollection/{currentPage}/{pageSize}` | 点赞收藏通知（消息页） |

---

### 2.6 子模块：App - 关注（`AppFollowerController`）

`/app/follower`

| Status | Method | Path | 描述 |
|---|---|---|---|
| ✅ | GET | `/app/follower/getFollowTrend/{currentPage}/{pageSize}` | **首页「关注 Tab」动态流**（screens-part2 Home 关注 Tab） |
| ✅ | GET | `/app/follower/getFollowList/{currentPage}/{pageSize}` | 关注列表（可选 userId） |
| ✅ | GET | `/app/follower/getFollowUser` | 获取关注用户（头像横滚用） |
| ✅ | GET | `/app/follower/getFriend/{currentPage}/{pageSize}` | 好友（关注+粉丝，type 区分） |
| ⚠️ | GET | `/app/follower/followById` | 关注用户（query `followerId`） | **写操作应 POST** |
| ✅ | GET | `/app/follower/isFollow` | 是否已关注 |
| ✅ | GET | `/app/follower/getNoticeFollower/{currentPage}/{pageSize}` | 新粉丝通知 |

---

### 2.7 子模块：App - 聊天私聊（`AppChatController`）

`/app/im/chat`

| Status | Method | Path | 描述 |
|---|---|---|---|
| ✅ | POST | `/app/im/chat/sendMsg` | 发送消息（body `Message { sendUid, acceptUid, content, chatType, msgType, groupChatId }`）；**匿名但内部鉴权** |
| ✅ | GET | `/app/im/chat/getAllChatRecord/{currentPage}/{pageSize}` | 聊天记录分页（私聊+群聊 acceptUid / groupChatId 二选一） |
| ✅ | GET | `/app/im/chat/getChatUserList` | 会话列表（对应消息 Tab） |
| ✅ | GET | `/app/im/chat/getCountMessage` | 未读总数（Tab 角标） |
| ❌ | GET | `/app/im/chat/deleteMsg` | **返回 null（未实现）** |
| ❌ | GET | `/app/im/chat/deleteAllChatRecord` | **未实现** |
| ❌ | GET | `/app/im/chat/deleteChatUser` | **未实现** |
| ✅ | GET | `/app/im/chat/clearMessageCount` | 清除会话未读 |
| ⚠️ | * | `/app/im/chat/closeChat/{sendUid}` | 关闭聊天（`@RequestMapping` 无 method 限制，任意方法都能触发） |

**风险**：三个 delete 接口是 stub；未打码的 `@RequestMapping` 无方法约束。

---

### 2.8 子模块：App - 群聊（`AppChatGroupController`）

`/app/im/group`

| Status | Method | Path | 描述 |
|---|---|---|---|
| ✅ | POST | `/app/im/group/create` | 创建群聊（`CreateGroupDTO`） |
| ⚠️ | POST | `/app/im/group/join/{groupChatId}` | 加入群聊 |
| ⚠️ | POST | `/app/im/group/quit/{groupChatId}` | 退出群聊 |
| ✅ | POST | `/app/im/group/dissolve/{groupChatId}` | 解散群聊（仅群主） |
| ✅ | POST | `/app/im/group/removeMember` | 移除成员（query groupChatId + userId） |
| ✅ | GET | `/app/im/group/myList` | 我的群聊列表 |
| ✅ | GET | `/app/im/group/detail/{groupChatId}` | 群聊详情 |
| ✅ | GET | `/app/im/group/members/{groupChatId}` | 群成员列表 |
| ✅ | POST | `/app/im/group/updateInfo` | 更新群信息（query 参数，**建议改 body**） |
| ✅ | POST | `/app/im/group/setNickname` | 设置群昵称 |
| ✅ | POST | `/app/im/group/setMute` | 免打扰 |

---

### 2.9 子模块：App - 专辑（收藏夹）（`AppAlbumController`）

`/app/album` —— 对应 screens-part11 Favorites

| Status | Method | Path | 描述 |
|---|---|---|---|
| ✅ | GET | `/app/album/getAlbumByUserId/{currentPage}/{pageSize}` | 用户专辑分页 |
| ✅ | POST | `/app/album/saveAlbum` | 新建专辑（`AlbumDTO`） |
| ✅ | GET | `/app/album/getAlbumById` | 专辑详情 |
| ⚠️ | GET | `/app/album/deleteAlbum` | 删除专辑 | 应 DELETE |
| ✅ | POST | `/app/album/updateAlbum` | 更新专辑 |

**缺失**：把笔记加入/移出专辑的接口未暴露在 App 端（Web 端 `/albumNoteRelation` 有）。

---

### 2.10 子模块：App - 标签（`AppTagController`）

`/app/tag`

| Status | Method | Path | 描述 |
|---|---|---|---|
| ✅ | GET | `/app/tag/getHotTagList/{currentPage}/{pageSize}` | 热门标签（首页话题 part2） |
| ✅ | GET | `/app/tag/getTagByKeyword/{currentPage}/{pageSize}` | 关键词搜标签 |
| ✅ | GET | `/app/tag/getTagById` | 标签详情 |
| ✅ | GET | `/app/tag/getNoteByTagId/{currentPage}/{pageSize}` | 标签下笔记（对应 part2 话题页） |

---

### 2.11 子模块：App - 分类（`AppCategoryController`）

`/app/category`

| Status | Method | Path | 描述 |
|---|---|---|---|
| ✅ | GET | `/app/category/getCategoryTreeData` | 分类树（匿名） |

---

### 2.12 子模块：App - 搜索 ES（`AppEsNoteController` + `AppEsRecordController`）

`/app/es/note`：

| Status | Method | Path | 描述 |
|---|---|---|---|
| ✅ | POST | `/app/es/note/getNoteByDTO/{currentPage}/{pageSize}` | 搜索笔记（匿名，`EsNoteDTO { keyword, category, sortBy, ... }`） |
| ✅ | POST | `/app/es/note/getCategoryAgg` | 聚合分类（分面搜索） |
| ✅ | GET | `/app/es/note/getRecommendNote/{currentPage}/{pageSize}` | 推荐笔记（匿名） |
| ✅ | GET | `/app/es/note/getRecommendUser/{currentPage}/{pageSize}` | 推荐用户 |
| ✅ | GET | `/app/es/note/getVideoNote/{currentPage}/{pageSize}` | 视频笔记（对应 part10 Reels） |
| ✅ | GET | `/app/es/note/getHotNote/{currentPage}/{pageSize}` | 热榜 |
| ✅ | GET | `/app/es/note/getNotesNearBy/{currentPage}/{pageSize}` | 附近笔记（对应首页「附近」Tab） |
| ⚠️ | POST/DELETE | `/app/es/note/addNote` / `updateNote` / `deleteNote/{noteId}` / `addNoteBulkData` / `delNoteBulkData` / `refreshNoteData` | **索引维护接口不应暴露在 App 端命名空间** |

`/app/es/record` —— 搜索历史：

| Status | Method | Path | 描述 |
|---|---|---|---|
| ✅ | GET | `/app/es/record/getRecordByKeyWord` | 联想搜索历史 |
| ✅ | GET | `/app/es/record/getHotRecord` | 热门搜索 |
| ✅ | POST | `/app/es/record/addRecord` | 新增搜索记录 |
| ✅ | POST | `/app/es/record/clearRecord` | 清除一条 |
| ✅ | POST | `/app/es/record/clearAllRecord` | 清空全部 |

---

### 2.13 子模块：App - 轻量推荐（`LightweightRecommendController`）

`/api/recommend/lightweight` —— **四维召回**：标签 40% + 分类 30% + 关注 20% + 热门 10%

| Status | Method | Path | 描述 |
|---|---|---|---|
| ✅ | GET | `/api/recommend/lightweight/notes/{currentPage}/{pageSize}` | **首页「发现」Tab** 核心推荐接口 |
| ✅ | GET | `/api/recommend/lightweight/notes/user/{userId}/{currentPage}/{pageSize}` | 指定用户推荐（调试用） |
| ✅ | GET | `/api/recommend/lightweight/health` | 健康检查 |

---

### 2.14 子模块：App - 浏览记录（`AppBrowseRecordController`）

`/app/browseRecord`

| Status | Method | Path | 描述 |
|---|---|---|---|
| ⚠️ | `*` | `/app/browseRecord/getAllBrowseRecordByUser/{page}/{limit}` | 浏览历史（对应 part11 History） |
| ⚠️ | `*` | `/app/browseRecord/addBrowseRecord` | 上报浏览 |
| ⚠️ | `*` | `/app/browseRecord/delRecord/{uid}` | 删除浏览记录（body List） |

**风险**：三个接口都用 `@RequestMapping` 未限定 method。

---

### 2.15 子模块：App - 通知公告（`AppNoticeController`）

`/app/notice` —— 有两个 **同路径冲突** 的 Controller（hongshu-web 与 hongshu-system 各一份）

| Status | Method | Path | 描述 | 问题 |
|---|---|---|---|---|
| ⚠️ | GET | `/app/notice/list/{currentPage}/{pageSize}` | 通知列表 | **hongshu-web 里走 RemoteNoticeService；hongshu-system 里直接查 DB，二者路径完全一样，启动时会冲突/取决于 Nacos 路由** |
| ⚠️ | GET | `/app/notice/{noticeId}` | 通知详情 | 同上冲突 |

---

### 2.16 子模块：App - 第三方（`AppThirdController`）

`/app/third`（兼容 `/web/app/third`）

| Status | Method | Path | 描述 |
|---|---|---|---|
| ✅ | GET | `/app/third/getWeChatJSSDKConfig` | 微信 JS-SDK 签名 |
| ⚠️ | GET | `/app/third/sendBindPhoneSms` | 发送绑定手机号验证码 | **返回特殊 code 20010** |
| ⚠️ | GET | `/app/third/sendResetPhoneSms` | 发送重置手机号验证码 | 同上 |
| ⚠️ | POST | `/app/third/checkBindSmsCode` | 校验绑定验证码 | **返回特殊 code 20020** |
| ⚠️ | POST | `/app/third/checkResetSmsCode` | 校验重置验证码 | 同上 |
| ✅ | POST | `/app/third/uploadAudio` | 上传语音文件（multipart） |

**风险**：自定义 20010/20020 特殊 code，前端需要 hardcode 识别，不符合统一错误码规范。

---

### 2.17 子模块：App - 通义千问 AI 文案（`AppTongYiContentController`）

`/app/tongyi/content`

| Status | Method | Path | 描述 |
|---|---|---|---|
| ✅ | POST | `/app/tongyi/content/generate` | 根据图片/视频 URL 生成文案（`TongYiContentRequestDTO`） |
| ✅ | POST | `/app/tongyi/content/generate-from-images` | 上传图片直接生成（multipart） |
| ✅ | POST | `/app/tongyi/content/generate-from-video` | 上传视频生成 |
| ✅ | POST | `/app/tongyi/content/generate-titles` | 仅生成标题 |

---

### 2.18 子模块：OSS 文件上传（`OssUploadController`）

`/oss`

| Status | Method | Path | 描述 |
|---|---|---|---|
| ✅ | POST | `/oss/upload` | 单文件上传（multipart `file`） |
| ✅ | POST | `/oss/uploadBatch` | 批量上传（multipart `uploadFiles`） |

---

### 2.19 子模块：Web 端（PC 网页）

路径全部为 **无 `/app` 前缀**，例如 `/auth/login`、`/note/getNoteById`、`/user/getUserById`、`/follower/followById`、`/comment/saveCommentByDTO`、`/likeOrCollection/...`、`/im/chat/...`、`/album/...`、`/tag/...`、`/category/...`、`/es/note/...`、`/es/record/...`、`/captcha/...`、`/feedback/...`、`/tongyi/content/...`、`/spider/xiaohongshu*`（小红书爬虫）、`/albumNoteRelation/...`、`/tagNoteRelation/...`。

功能与 App 端高度重合，**字段一致但路径 App/Web 双份**。Web 特有接口：
- `WebAuthController`：多了 `/auth/qrcode`、`/auth/qrcode/scan`、`/auth/qrcode/confirm`、`/auth/qrcode/status`、`/auth/qrcode/login`、`/auth/wechat/authUrl`、`/auth/wechat/callback`、`/auth/wechat/getLoginInfo`（PC 扫码、微信网页授权）
- `WebChatController`：多了客服相关 `getCustomerServiceUserList`、`getCustomerServiceChatRecord/{c}/{s}`、`sendCustomerServiceMessage`、`getAICustomerServiceStatus`、`updateAICustomerServiceStatus`
- `WebXiaohongshuSpiderController`：小红书爬虫（解析链接）
- `WebFeedbackController`：`/feedback/submit`、`/feedback/list/{c}/{s}`、`/feedback/like/{id}`、`/feedback/plannedFeatures`
- `WebCaptchaController`：`/captcha/type`、`/captcha/get`、`/captcha/check`、`/captcha/verify`

**App 端可复用 Web 的情况**：如果 App 有需要而 `/app/*` 未实现的功能（比如举报 / 话题详情页完整数据），可考虑让 App 直接调 Web 端路径作为过渡。

---

### 2.20 子模块：管理端（`sys/` 前缀）

Controller：`SysNavbarController`、`SysStatisticsController`、`SysFeedbackController`、`SystemNotificationController`、`SysChatGroupController`、`SysTagController`、`SysAlbumController`、`SysCommentController`、`SysNoteController`、`SysMemberController`、`SysLoginInforController`、`SysDashboardController`、`SysDataCenterAnalyticsController`、`SysDataCenterFanProfileController`、`SysSystemConfigController`、`SystemConfigController`（大部分注释掉了）。

**与 App 端前端完全无关**。典型路径：`/note/allList`、`/note/unAuditList`、`/note/auditNote`、`/member/list`、`/comment/list`、`/dataCenter/analytics/*`、`/dataCenter/fanProfile/*`、`/system/config/*`（含 OSS/SMS/支付宝/微信支付/验证码/高德/黑名单等各种运营配置）。

**App 需要但可能从这里消费**：
- `/system/config/websiteConfig` / `/system/config/captchaConfig` / `/system/config/carousel`（运营位轮播图）—— App 启动页、首页 Banner 可能要用

### 2.21 子模块：审计（`WebAuditLogController`）

`/auditLog`——运营审计日志查询（App 端不用）

### 2.22 子模块：监控（`CacheController` / `ServerController`）

`/monitor/cache`、`/monitor/server`（仅运维使用）

---

## 模块：hongshu-modules/hongshu-idle（闲置交易 / 商品 / 订单 / 支付）

**源码位置**：`hongshu-modules/hongshu-idle/src/main/java/com/hongshu/idle/controller/`

### 模块职责
**市集商品域** —— 闲置/商品发布、分类、搜索、收藏、订单、支付（支付宝+微信）、地址、推荐。对应 screens-part4 Market / ProductDetail、part8 Cart / Checkout / Orders / OrderDetail。

### App 端路径

| Controller | 前缀 | 关键接口 |
|---|---|---|
| `AppProductController` | `/app/product` | `getProductById`, `getProduct/{c}/{s}`（搜索 body EsProductDTO）, `getProductByKeyword/{c}/{s}`, `saveProductByDTO`（multipart）, `saveProduct`（JSON）, `updateProductByDTO`, `pinnedProduct`, `idleCount`（⚠️ `pinnedProduct` 用 GET） |
| `AppEsProductController` | `/app/es/product` | `getProductByDTO/{c}/{s}`, `recommendProduct/{c}/{s}`, `getVideoProduct/{c}/{s}` |
| `AppCollectionController` | `/app/collection` | `collectionByDTO`, `isCollection`, `getNoticeCollection/{c}/{s}` |
| `AppCategoryController` | `/app/category` | `getCategoryTreeData`（**与 hongshu-web 的 `/app/category/getCategoryTreeData` 路径完全冲突**，需网关路由区分） |
| `AppPaymentOrderController` | `/app/payment/order` | `createPayment`（GET query productOrderId, payType）、`getPaymentOrder` |
| `AppPaymentPayController` | `/app/payment/pay` | `createPaymentPay`, `finish`, `cancel`, `status`, `updateOrderStatus`, `alipay/notify`, `wechat/notify`（回调匿名） |
| `LightweightProductRecommendController` | `/api/recommend/product/lightweight` | `products/{c}/{s}`, `products/user/{userId}/{c}/{s}`, `health` |

### 「无 /app 前缀」路径（应为 Web 端或旧移动端共用，**App 可能兼容调用**）

| Controller | 前缀 | 关键接口 |
|---|---|---|
| `IdleProductController` | `/product` | 同 AppProductController 复制 |
| `IdleProductOrderController` | `/product/order` | `createOrder`（POST body `ProductOrderDTO`）, `detail`（GET query orderNumber）, `getOrderDetailById/{orderId}`, `getOrdersByUser/{c}/{s}`（query userId, orderStatus）, `cancelOrder/{orderId}` |
| `IdleUserAddressController` | `/address` | `addressList`（query uid）, `getAddress`（query id）, `{id}`(GET) 、`POST /address`（新增，RESTful）、`saveAddress`/`updateAddress`（旧接口）、`PUT /{id}`、`DELETE /{id}`、`deleteAddress`、`PUT setDefault/{id}` |
| `IdleEsProductController` | `/es/product` | `getProductByDTO/{c}/{s}`, `recommendProduct/{c}/{s}`, `getHotProduct/{c}/{s}`, `getVideoProduct/{c}/{s}`, `addProduct`/`updateProduct`/`deleteProduct/{productId}` |
| `IdleCollectionController` | `/collection` | 同 app 版 |
| `IdleCategoryController` | `/category` | `getCategoryTreeData`, `getCategoryAgg` |
| `IdlePaymentOrderController` | `/payment/order` | 同 app 版 |
| `IdlePaymentPayController` | `/payment/pay` | 同 app 版 |

### 管理端

| Controller | 前缀 | 功能 |
|---|---|---|
| `SysIdleProductController` | `/product`（sys） | 商品审核 |
| `SysIdleProductOrderController` | `/order` | 订单管理 |
| `SysIdlePaymentPayController` | `/payment` | 支付管理 |
| `SysIdleNavbarController` | `/navbar` | 分类导航 |
| `SysSystemConfigController` | `/config` | 系统配置 |
| `RecommendController` | `/es/recommend` | ES 推荐（Old） |

### App 端订单/支付流程（设计稿 part8 对应）

前端下单推荐链路：
1. `POST /product/order/createOrder` → 得到 `orderNumber`
2. `GET /app/payment/order/createPayment?productOrderId=...&payType=2` → 得到 `paymentOrderId`
3. `GET /app/payment/pay/createPaymentPay?paymentOrderId=...` → 得到 `paymentPayId`
4. `POST /app/payment/pay/finish?paymentPayId=...&paymentMethod=...` → 得到支付二维码/表单
5. 前端轮询 `GET /app/payment/pay/status?paymentPayId=...`
6. 后端回调 `alipay/notify` 或 `wechat/notify`

**⚠️ 改造建议**：createOrder 走 `/product/order`（无 /app 前缀），其他走 `/app/payment`，前缀不统一，前端需特殊处理。建议统一为 `/app/order/*` 与 `/app/payment/*`。

---

## 模块：hongshu-modules/hongshu-system（用户/角色/权限/字典/公告等通用系统）

**源码位置**：`hongshu-modules/hongshu-system/`  
**职责**：RBAC 基础（`SysUser/SysRole/SysMenu/SysDept/SysPost`）、字典（`SysDict`）、操作日志、登录日志、通知公告（`SysNotice`）、在线用户、系统配置（`SysConfig`）。

**与 App 端关系**：
- `SysUserController`（`/user`）部分接口被 `hongshu-auth` / `remoteUser` Feign 间接使用
- `AppNoticeController`（`/app/notice`）**已在前面提过，与 hongshu-web 版本冲突**
- 其余 `/role`、`/menu`、`/dict/*`、`/operlog`、`/post`、`/dept`、`/online`、`/logininfor`、`/config`、`/user/profile` 都是管理后台专用。

App 端可能只需要：
- ✅ `GET /user/info/{username}`（Feign 内部调用）
- ✅ `GET /config/configKey/{configKey}`（读取动态配置）

---

## 模块：hongshu-modules/hongshu-job（定时任务）

`/job`、`/job/log`：**仅管理后台使用**，App 不涉及。

---

## 模块：hongshu-modules/hongshu-file（独立文件服务）

`SysFileController`：

| Method | Path | 描述 |
|---|---|---|
| POST | `/upload` | 单文件上传（Feign 内部常用） |
| POST | `/batch/upload` | 批量上传（并行，最多 10 个） |
| POST | `/upload/file` | 上传本地 File 对象（Feign 内部） |
| POST | `/batch/upload/file` | 批量上传 File |
| POST | `/xiaohongshu/video/download` | 下载小红书视频到 OSS |
| POST | `/xiaohongshu/image/download` | 下载小红书图片到 OSS |

App 端一般通过 `hongshu-web` 的 `/oss/upload` 间接使用。

---

## 模块：hongshu-modules/hongshu-ai（独立 AI 应用，对外 GPT/助手）

### 职责
这是一个 **独立对外的 AI/GPT 应用**（类似 ChatGPT Web），与爱宠社 App 主业务**耦合度低**。爱宠社 App 用到的 AI 能力是 `hongshu-web` 的 `/app/tongyi/content/*`，不是这个模块。

### 接口摘要

**App 端**（不是爱宠社 App）：

| Controller | 前缀 | 描述 |
|---|---|---|
| `app/LoginController` | `/app` | `POST /api/oauth/token` OAuth 登录 |
| `app/UserController` | `/app/user` | `GET`（我的）、`GET /model`、`PUT`、`POST /avatar`、`PUT /password/update`、`PUT /context`、`PUT /share` |
| `app/AppApiController` | `/app/api` | `POST /sms/send`、`GET /home/config`、`GET /content/agreement/{type}`、`GET /content/{type}`、`GET /content/detail/{id}`、`GET /assistant/type`、`GET /assistant`、`GET /assistant/{id}`、`GET /assistant/random`、`POST /chat` |
| `app/ChatController` | `/v1/chat` | OpenAI 兼容 chat/messages SSE |
| `app/OrderController` | `/app/order` | 订单（GPT 充值订单，非爱宠社交易订单） |

**Gpt 后台**：`/gpt/chat`、`/gpt/model`、`/gpt/openkey`、`/gpt/assistant`、`/gpt/assistantType`、`/gpt/redemption`、`/gpt/uploadConfig`、`/gpt/combo`、`/gpt/file`、`/gpt/order`、`/gpt/user`、`/gpt/agreement`、`/gpt/message`

**Sys**：`sys/base/config`、`sys/statistics`

⚠️ **重要冲突**：`/app/user` 在 hongshu-ai 与 hongshu-web 都存在，前缀完全一样，必须通过网关精确路由区分（或拆模块）。建议 hongshu-ai 的路径前缀改成 `/ai/app/*`。

---

## ❌ 未实现但设计稿需要的 API 清单

基于 screens-part1 ~ part12 扫描，以下功能**设计稿明确呈现但后端未提供专用接口**（或只能通过改造现有接口勉强拼凑）：

1. ❌ `POST /app/note/report` - 笔记举报（screens-part2 笔记详情右上菜单、part9 评论举报）
2. ❌ `POST /app/comment/report` - 评论举报（part9 Comments）
3. ❌ `POST /app/user/{userId}/report` - 用户举报
4. ❌ `POST /app/user/{userId}/block` / `DELETE /app/user/{userId}/block` - 拉黑/取消拉黑（part5 Profile 菜单、part7 Settings 黑名单）
5. ❌ `GET /app/user/blocklist` - 我的黑名单列表
6. ❌ `POST /app/album/addNote` / `removeNote` - 将笔记加入/移出专辑（part11 Favorites 新建收藏夹需要）；现在只有 Web 端 `/albumNoteRelation/*`
7. ❌ `POST /app/pet` 全族接口 - 宠物档案 CRUD（part9 PetProfile / AddPet）：新建宠物、列表、详情、更新、删除
8. ❌ `POST /app/pet/{petId}/vaccine` - 疫苗记录 CRUD（part9 Vaccine：record 列表/新增/修改，下次提醒）
9. ❌ `GET /app/pet/{petId}/health` - 体检报告、驱虫记录（part9）
10. ❌ `GET /app/topic/{topicId}` - 话题聚合页详情（part9 Topic）；现在只有 `/app/tag/*` 标签，**话题 = 标签? 需确认业务模型**
11. ❌ `POST /app/topic/follow` - 关注话题（类似关注用户）
12. ❌ `GET /app/nearby/users` - 附近的宠友（part10 Nearby、part12 PetDate）；现仅笔记 `getNotesNearBy`
13. ❌ `GET /app/nearby/shops` - 附近宠物店（part4 Market 顶部 "附近门店"）
14. ❌ `POST /app/pet/date` - 遛娃/宠友组队报名（part12 PetDate）
15. ❌ `GET /app/adopt/list` / `POST /app/adopt/apply` - 领养信息列表 & 申请领养（part12 Adopt）
16. ❌ `GET /app/lost/list` / `POST /app/lost/publish` - 寻宠/捡到发布（part12 Lost）
17. ❌ `GET /app/live/list` / `/app/live/{roomId}` - 直播间列表/详情（part10 Live）
18. ❌ `POST /app/signin` / `GET /app/signin/status` - 每日签到（part10 SignIn；part11 Wallet 积分）
19. ❌ `GET /app/wallet/balance` / `GET /app/wallet/transactions` - 钱包余额与明细（part11 Wallet）
20. ❌ `GET /app/points` / `POST /app/points/exchange` - 积分商城（part11 Wallet 兑换）
21. ❌ `GET /app/coupon/list` / `POST /app/coupon/claim` - 优惠券列表/领取（part7 Coupons）
22. ❌ `GET /app/cart` / `POST /app/cart/add` / `DELETE /app/cart/{itemId}` / `PUT /app/cart/{itemId}/qty` - 购物车（part8 Cart）；当前只有单一商品下单接口，**无购物车模型**
23. ❌ `POST /app/checkout/preview` - 结算预览（合并商品、运费、优惠券）（part8 Checkout）
24. ❌ `POST /app/order/{orderId}/confirm` - 确认收货（part8 OrderDetail）
25. ❌ `POST /app/order/{orderId}/review` - 评价订单（Controller 里有注释掉的 `/evaluate`）
26. ❌ `POST /app/order/{orderId}/refund` - 申请退款 / 售后
27. ❌ `GET /app/shipping/query` - 物流查询（part11 Shipping）
28. ❌ `GET /app/vip/info` / `POST /app/vip/subscribe` - VIP 会员中心与订阅（part12 VIP）
29. ❌ `GET /app/course/list` / `GET /app/course/{id}` - 课程：新手养猫 101、狗狗社交课、急救手册（part10 Explore）
30. ❌ `GET /app/banner/home` - 首页运营位 banner（现需从 `/system/config/carousel` 借用，管理端接口）
31. ❌ `GET /app/onboarding/config` - 启动引导图（part10 Onboarding；目前无接口）
32. ❌ `POST /app/device/push-token` - APNs/FCM 推送 token 注册
33. ❌ `GET /app/follower/mutual/{userId}` - 共同关注（个人主页展示常见功能）
34. ❌ `GET /app/note/{noteId}/related` - 相关笔记推荐（笔记详情底部）
35. ❌ `POST /app/share/note/{noteId}` - 生成分享海报/短链（部分前端用到）

---

## 风险与建议

### 1. 命名与 HTTP 方法不规范（高）
- 大量写操作使用 GET：`followById`、`deleteAlbum`、`deleteCommentById`、`pinnedNote`、`pinnedProduct`、`loginOut` 等。违反 REST 语义，存在 CSRF 与幂等性问题。
- 批量 `@RequestMapping` 未限定 method（`closeChat`、`addBrowseRecord`、`delRecord`、`getAllBrowseRecordByUser` 等），任意 method 都能命中。
- **建议**：移动端迁移时，后端应并行提供 `POST /app/v2/*` 的新路径，老路径保留兼容一个大版本。

### 2. 路径命名空间冲突（高）
- `/app/notice`：hongshu-web 与 hongshu-system 两份同名 Controller，需网关精确路由
- `/app/user`：hongshu-web（`AppUserController` + `AppUserSecurityController`）与 hongshu-ai 都占用，前端调用混乱
- `/app/category`：hongshu-web（笔记分类）与 hongshu-idle（商品分类）冲突
- **建议**：通过网关路由（spring cloud gateway 的路径重写）或 **重命名**：`/app/community/*`、`/app/market/*`、`/app/system/*`、`/ai/app/*` 分开。

### 3. 认证方式双轨（中）
- `hongshu-auth`：Spring Security + JWT（jjwt）用于管理后台
- `hongshu-web`：自研 `JwtUtilss` + `@NoLoginIntercept` 用于 App / Web 前台
- **建议**：App 端只认 `hongshu-web` 的路径，避免交叉；管理后台接口不要暴露在公网前端。

### 4. 参数传递不安全（中）
- `/app/auth/getUserInfoByToken?accessToken=...`、`/app/auth/refreshToken?refreshToken=...`：敏感 token 放 query 会进日志/代理
- **建议**：改为 Header `Authorization`，或 POST body。

### 5. 统一错误码缺失（中）
- `Result.build(20010, ...)`、`Result.build(20020, ...)` 这种业务 code 只在 `AppThirdController` 出现
- `R.fail(msg)` 与 `Result.build(code, msg)` 两套返回体并存
- **建议**：制定统一错误码表（放 `hongshu-common-core`），前端使用 TS enum 对齐。

### 6. 分页参数不统一（中）
- 主流：PathVariable `{currentPage}/{pageSize}`
- 少数：`{page}/{limit}` (AppBrowseRecord)
- 管理后台：`PageParam` query 参数（`pageNum`/`pageSize`）
- **建议**：App 端统一 query `?page=&size=`（符合社区习惯），PathVariable 模式保留但废弃新增。

### 7. multipart + JSON 混用（中）
- `saveNoteByDTO`、`updateNoteByDTO`、`saveProductByDTO` 等把 `noteData` 作为字符串 JSON + 多文件一起 multipart 提交，UniApp / 小程序支持度差。
- **建议**：前端先调 `/oss/uploadBatch` 拿 URL，再调 `saveNote`（纯 JSON）；`saveNoteByDTO` 这类接口标记 deprecated。

### 8. 未实现功能（高，影响设计稿还原）
- 举报、拉黑、宠物档案、话题（Topic 与 Tag 关系）、购物车、优惠券、钱包、签到、领养、寻宠、直播、课程、VIP、推送 token —— 共 **≥ 25** 条设计稿需要的接口无后端。需与后端团队评估先后顺序。

### 9. 接口「僵尸」（低）
- `/app/im/chat/deleteMsg`、`deleteAllChatRecord`、`deleteChatUser` 返回 null，是未实现的 stub。
- `IdleProductOrderController` 有注释掉的 `/evaluate`。
- 建议：清理或补全。

---

## 附录：Controller 清单索引（按包）

- **hongshu-auth**: TokenController
- **hongshu-web/app**: AppAuthController, AppUserController, AppUserSecurityController, AppNoteController, AppNoteBehaviorController, AppCommentController, AppLikeOrCollectionController, AppFollowerController, AppChatController, AppChatGroupController, AppAlbumController, AppTagController, AppCategoryController, AppEsNoteController, AppEsRecordController, AppBrowseRecordController, AppNoticeController, AppThirdController, AppTongYiContentController
- **hongshu-web/web**: WebAuthController, WebUserController, WebNoteController, WebCommentController, WebFollowerController, WebAlbumController, WebAlbumNoteRelationController, WebTagController, WebTagNoteRelationController, WebCategoryController, WebChatController, WebCaptchaController, WebEsNoteController, WebEsRecordController, WebLikeOrCollectionController, WebFeedbackController, WebTongYiContentController, WebXiaohongshuSpiderController
- **hongshu-web/sys**: SysNavbarController, SysStatisticsController, SysFeedbackController, SystemNotificationController, SysChatGroupController, SysTagController, SysAlbumController, SysCommentController, SysNoteController, SysMemberController, SysLoginInforController, SysDashboardController, SysDataCenterAnalyticsController, SysDataCenterFanProfileController, SysSystemConfigController
- **hongshu-web/recommendation**: LightweightRecommendController
- **hongshu-web/monitor/audit**: CacheController, ServerController, WebAuditLogController, OssUploadController
- **hongshu-idle/app**: AppProductController, AppEsProductController, AppCategoryController, AppCollectionController, AppPaymentOrderController, AppPaymentPayController
- **hongshu-idle/idle**: IdleProductController, IdleProductOrderController, IdleEsProductController, IdleCategoryController, IdleCollectionController, IdlePaymentOrderController, IdlePaymentPayController, IdleUserAddressController
- **hongshu-idle/sys**: SysIdleProductController, SysIdleProductOrderController, SysIdlePaymentPayController, SysIdleNavbarController, SysSystemConfigController
- **hongshu-idle/recommendation**: LightweightProductRecommendController, RecommendController
- **hongshu-system**: SysUserController, SysRoleController, SysMenuController, SysDeptController, SysPostController, SysDictTypeController, SysDictDataController, SysNoticeController, SysOperlogController, SysLogininforController, SysUserOnlineController, SysProfileController, SysConfigController, app/AppNoticeController
- **hongshu-job**: SysJobController, SysJobLogController
- **hongshu-file**: SysFileController
- **hongshu-ai/app**: LoginController, UserController, AppApiController, ChatController, OrderController
- **hongshu-ai/gpt**: ModelController, OpenkeyController, OrderController, RedemptionController, AssistantController, AssistantTypeController, UploadConfigController, CombController, FileController, ChatController, ChatMessageController, UserController, AgreementController
- **hongshu-ai/sys**: BaseConfigController, StatisticsController
