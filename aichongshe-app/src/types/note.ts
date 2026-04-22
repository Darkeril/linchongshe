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
