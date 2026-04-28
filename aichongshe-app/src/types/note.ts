/**
 * 笔记（瀑布流卡片）类型定义。
 * Phase 4 只覆盖列表场景；详情页类型放到 Phase 5。
 *
 * 字段对齐后端约定（原则 2 API 盘点）：
 * - id：后端 Long / String 统一前端按 string 处理
 * - variant：前端视觉用，后端未来若没有这个字段，由前端按 userId hash 决定
 * - h：封面高度（px），设计稿瀑布流卡片高度不等，用以实现错落；真实后端返回应为封面原图高度，
 *      前端按宽高比换算；Phase 4 mock 直接给出目标高度
 */

/** 萌宠占位图 variant（与 PetAvatar 保持一致） */
export type PetVariant = 'dog' | 'cat' | 'mochi' | 'puppy';

/** 首页 feed 类型 */
export type NoteFeedId = 'follow' | 'discover' | 'nearby';

/** 瀑布流单条笔记 */
export interface NoteListItem {
  /** 笔记 id（后端 Long，前端字符串化） */
  id: string;
  /** 标题（2 行截断） */
  title: string;
  /** 真实封面 URL；为空时使用 PetPlaceholder */
  coverUrl?: string;
  /** 封面宠物占位 variant */
  variant: PetVariant;
  /** 可选分类标签（标签胶囊，例如「金毛日记」） */
  tag?: string;
  /** 封面渲染高度（px） */
  h: number;
  /** 作者昵称 */
  user: string;
  /** 作者头像 variant */
  avatarVariant: PetVariant;
  /** 点赞数 */
  likes: number;
  /** 当前登录用户是否已点赞 */
  liked: boolean;
}

/** 列表请求参数（对齐后端 POST 入参） */
export interface NoteListParams {
  feed: NoteFeedId;
  page: number;
  size: number;
}

/** 列表响应 */
export interface NoteListResult {
  list: NoteListItem[];
  hasMore: boolean;
  page: number;
}

/** 点赞响应 */
export interface LikeNoteResult {
  liked: boolean;
  likes: number;
}

// ─────────────────────────────────────────────────────────────
// 笔记详情（Phase 5a 新增）
// ─────────────────────────────────────────────────────────────

/**
 * 笔记详情中的单张图片。
 * Phase 5a 图片内容由 PetPlaceholder 占位渲染（真实 url 暂为空字符串）；
 * 当后端接入 CDN 图片时，url 将被填充，前端优先使用真实 url。
 */
export interface NoteImage {
  /** 真实图片 URL；Phase 5a 为空字符串，由 PetPlaceholder 兜底 */
  url: string;
  /** 占位图 variant（决定风格） */
  variant: PetVariant;
  /** 占位图 seed（保证稳定渲染） */
  seed: string | number;
}

/** 笔记作者（详情页顶部展示） */
export interface NoteAuthor {
  id: string;
  name: string;
  avatarVariant: PetVariant;
  followed: boolean;
}

/** 笔记详情（含图文 + 元信息 + 作者） */
export interface NoteDetail extends NoteListItem {
  /** 图文列表（1-9 张） */
  images: NoteImage[];
  /** 正文 */
  content: string;
  /** 话题标签（不带 # 前缀，由组件自行拼接） */
  topics: string[];
  /** 作者信息 */
  author: NoteAuthor;
  /** 收藏数 */
  saves: number;
  /** 当前登录用户是否已收藏 */
  saved: boolean;
  /** 评论数 */
  comments: number;
  /** 人类可读的编辑时间描述（Phase 5a 伪随机；后续迁到时间戳） */
  editedAt: string;
  /** 发布所在城市（纯展示） */
  fromCity: string;
}

/** 收藏切换响应 */
export interface SaveNoteResult {
  saved: boolean;
  saves: number;
}

/** 关注切换响应 */
export interface FollowResult {
  followed: boolean;
}

// ─────────────────────────────────────────────────────────────
// 发布笔记（Phase 7 新增）
// ─────────────────────────────────────────────────────────────

/** 发布页选中的单张图片 */
export interface PublishImageItem {
  /** 本地临时路径或上传后的 CDN URL */
  url: string;
  /** mock / 占位渲染用宠物变体 */
  variant: PetVariant;
  /** 占位渲染稳定 seed */
  seed: string | number;
}

/** 发布笔记入参 */
export interface PublishNotePayload {
  title: string;
  content: string;
  topics: string[];
  images: PublishImageItem[];
  location?: string;
  petName?: string;
  mentionedUsers?: string[];
  relatedProducts?: string[];
}

/** 发布笔记结果 */
export interface PublishNoteResult {
  id: string;
  status: 'published';
}
