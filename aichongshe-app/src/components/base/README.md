# components/base — uview-plus 控件层

职责：封装 uview-plus 的基础控件，做少量「对齐 latte 主题」的样式覆盖。

原则：
- 本层直接依赖 uview-plus，不依赖 `ui/` 和 `biz/`
- 只做「接 uview-plus 能力 + 套 latte 样式」，不做业务逻辑
- 例如：`BaseButton`（uview-plus `<u-button>` + 圆角/字号统一）、`BaseInput`、`BasePopup`、`BaseToast`

当前状态：Phase 0 占位，尚未落第一个组件。后续按页面实际需要逐个补。
