# components/ui — 设计稿特色组件层

职责：承载设计稿里 uview-plus 无法覆盖的自研组件，严格对齐 latte 主题。

原则：
- 本层自己写，不继承 uview-plus
- 可以引用 `base/`（极少需要），不能引用 `biz/`
- 和 latte 主题强耦合（颜色、圆角、阴影 token 严格对齐 `styles/tokens.scss`）
- 例如：`NoteCard` 笔记卡、`WaterFall` 瀑布流、`TopicPill` 话题胶囊、`FloatingTabBar` 悬浮 TabBar、`UserChip`、`GlassButton`

当前状态：Phase 0 占位，Phase 1 开始按设计稿落组件。
