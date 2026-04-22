# components/biz — 业务组件层

职责：组合 `base/` 和 `ui/`，封装和业务强相关的组件。

原则：
- 可以引用 `base/` 与 `ui/`，不能反向被它们引用
- 和具体页面/接口/store 耦合的逻辑放在这里
- 例如：`NoteListSection`（关注/发现/附近的笔记列表）、`UserFollowButton`、`PublishEntry`

页面规则：
- 业务页面只能引用 `biz/`，或直接用 `ui/` / `base/`，**不能跨层**（避免 `ui/` 调 `biz/` 形成环）

当前状态：Phase 0 占位。
