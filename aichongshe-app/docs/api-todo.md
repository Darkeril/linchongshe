# 爱宠社 API 盘点 TODO

> 本文档记录每个页面/模块所需的后端接口盘点结果，是后端开发的参考依据，也是前端 mock 数据结构的对照。
>
> **约定**：每开一个新页面前，先来此处登记 API 盘点，经用户确认后才开始编码。

---

## 模板（新增页面时复制使用）

```markdown
## [页面名] - 页面标题

对应设计稿：design/screens-partX.jsx
对应 store：src/stores/xxx.ts
对应 types：src/types/xxx.ts

### 可直接用
- `GET /api/xxx/{id}` - 功能描述
  - 来源：`springcloud_hongshu/xxx-service/src/main/java/.../XxxController.java`

### 需要改造
- `POST /api/xxx/{id}/like`
  - 现状：只返回 success
  - 需求：额外返回当前点赞总数（用于乐观更新校准）

### 未实现
- 接口名（设计稿 screens-partX 有但后端无）
```

---

## 0. 用户 Profile（示例 / Splash 验证用）

对应 service：`src/api/services/user.ts`
对应 types：`src/types/user.ts`

### 未实现（占位，待 API 盘点阶段正式确认）
- `GET /api/user/profile` — 获取当前登录用户资料
  - 前端期望返回结构：`UserProfile`（见 `src/types/user.ts`）
  - 说明：当前仅 mock 阶段可用，真实接口路径与字段以后端盘点结果为准

---

## 7. Publish - 发布笔记页

对应设计稿：`design/screens-part3.jsx`
对应 service：`src/api/services/note.ts`
对应 types：`src/types/note.ts`
对应 mock：`src/api/mock/note.mock.ts`

### 可直接参考
- `POST /app/note/saveNote`
  - 功能：保存图文笔记（JSON NoteAppDTO + requestId）
  - 来源：`springcloud_hongshu/hongshu-modules/hongshu-web/src/main/java/com/hongshu/web/controller/app/AppNoteController.java`
- `POST /app/note/saveNoteByDTO`
  - 功能：multipart 保存图文笔记，包含 `coverFile` 与 `uploadFiles`
  - 来源：同上
- `POST /oss/upload`
  - 功能：单文件上传
  - 来源：`springcloud_hongshu/hongshu-framework/src/main/java/com/hongshu/common/oss/controller/OssUploadController.java`
- `POST /oss/uploadBatch`
  - 功能：多文件上传，字段名 `uploadFiles`
  - 来源：同上

### 需要改造 / 确认
- 前端当前 `request()` 只封装了 JSON `uni.request`；真实图片发布需要新增 `uni.uploadFile` adapter。
- `saveNote` 目前是 `@RequestParam("requestId")` + `@RequestBody NoteAppDTO`，前端 JSON 版需要确认网关和后端是否接受 `?requestId=...`。
- `saveNoteByDTO` 更贴近发布页需求，但需要 multipart 字段契约确认：`noteData` 是 JSON 字符串还是表单字段对象。
- `NoteAppDTO.tags` 类型在真实链路中需要确认，前端 mock 当前按 `string[]` 建模。
- 发布成功响应需要确认是否返回新笔记 id；前端期望 `PublishNoteResult = { id, status }`。

### 本期实现
- Phase 7 先做 mock-first 发布闭环：选择图片、填写标题正文、保存草稿、发布成功跳新笔记详情。
- 非 mock 模式暂按 `/app/note/saveNote?requestId=...` JSON 形态预留，真实上传待后端契约确认后补齐。
