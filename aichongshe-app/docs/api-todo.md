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

（后续页面的 API 盘点条目由其它 worker 补充）
