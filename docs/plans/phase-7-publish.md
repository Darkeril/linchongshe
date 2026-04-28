# Phase 7：发布图文笔记（mock-first）

- **创建日期**：2026-04-28
- **状态**：✅ 已完成（mock-first 图文发布闭环；H5 / 微信小程序 / type-check 均通过）
- **前置依赖**：Phase 6 登录拦截升级完成
- **对应设计稿**：`design/screens-part3.jsx`
- **对应交互图**：`design/interaction-map.md` Publish 发布笔记页

---

## 目标

把 TabBar 中间发布入口从占位页升级为可用的图文发布流程：

1. 未登录点击发布入口时弹 `LoginSheet`，登录成功后继续进入发布页。
2. 发布页支持图片选择、预览、删除、封面标记和 1-9 张数量限制。
3. 支持标题、正文、话题、位置/宠物等发布元信息。
4. 支持保存草稿、取消二次确认。
5. mock 模式下发布成功后生成笔记详情，并跳转到新笔记详情页。

---

## API 盘点结论

### 当前可参考后端

- `POST /app/note/saveNote`
  - 来源：`springcloud_hongshu/hongshu-modules/hongshu-web/src/main/java/com/hongshu/web/controller/app/AppNoteController.java`
  - 现状：`requestId` 走 query/form 参数，正文走 `NoteAppDTO`。
- `POST /app/note/saveNoteByDTO`
  - 同 controller；multipart，包含 `coverFile` 和 `uploadFiles`。
- `POST /oss/upload`、`POST /oss/uploadBatch`
  - 来源：`OssUploadController.java`；可作为图片上传前置能力。

### 本 Phase 决策

前端请求封装当前只支持 JSON `uni.request`，还没有 multipart 上传封装；因此 Phase 7 先做 mock-first 完整体验，不假装真实上传已经打通。真实接入时需要补 `uni.uploadFile` / 批量上传 adapter，再把返回 URL 填入 `PublishNotePayload.images`。

---

## 实现范围

### 新增/扩展数据层

- `src/types/note.ts`
  - `PublishImageItem`
  - `PublishNotePayload`
  - `PublishNoteResult`
  - `NoteListItem.coverUrl`
- `src/api/services/note.ts`
  - `publishNote(payload)`
- `src/api/mock/note.mock.ts`
  - `mockPublishNote(payload)`：插入 discover feed，写入 detail cache。

### 修改 UI

- `src/pages/publish/publish.vue`
  - 按 `screens-part3.jsx` 做发布页主流程。
- `src/components/ui/AppTabBar.vue`
  - 发布入口接入 `useAuthGuard.requireAuth`。
- `src/components/ui/NoteGallery.vue`
  - `img.url` 存在时优先渲染真实图片。
- `src/components/ui/NoteCard.vue`
  - `coverUrl` 存在时优先渲染真实封面。

---

## 验收标准

- 未登录点击 TabBar 发布按钮弹登录面板，不直接跳页。
- 登录成功后进入 `/pages/publish/publish`。
- 发布页可选图、预览、删除；超过 9 张不可继续添加。
- 标题/正文/图片缺失时阻止发布并 toast 提示。
- `存草稿` 写入本地 storage；重新进入可恢复标题、正文、话题和图片路径。
- mock 发布成功后跳转新笔记详情页。
- `npm run type-check` 通过。
- `npm run build:h5` 通过。
- `npm run build:mp-weixin` 通过。

---

## 后续真实接口接入 TODO

1. 新增上传 service：基于 `uni.uploadFile` 对接 `/oss/uploadBatch` 或 `/app/note/saveNoteByDTO`。
2. 明确 `NoteAppDTO.tags` 的格式：字符串数组、逗号字符串，还是后端对象。
3. 明确 `saveNote` 返回值：新笔记 id、完整 note，或只返回 success。
4. 明确位置字段拆分：`province/city/district/address` 是否必填。
