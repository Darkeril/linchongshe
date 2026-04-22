/**
 * 笔记评论类型定义（Phase 5b）。
 *
 * 2 层拍平约定：
 * - 一级评论（root）：`replyTo` undefined
 * - 二级回复（reply）：`replyTo` 指向"被回复的用户"（不是某条评论 id，简化）
 * - 回复的回复：仍然是二级，靠 `@用户名 ...` 文本表达层级
 */
import type { PetVariant } from '@/types/note';

/** 评论作者（精简版，不含关注关系等） */
export interface CommentUser {
  id: string;
  name: string;
  avatarVariant: PetVariant;
}

/** 单条评论（root 或 reply 共用） */
export interface CommentItem {
  id: string;
  noteId: string;
  author: CommentUser;
  /** 正文。回复可能含 "@张三 ..." 文本段 */
  text: string;
  likes: number;
  liked: boolean;
  /** 人类可读的发布时间（例如 "2 小时前"） */
  createdAt: string;
  /** 回复某人时填；一级评论 undefined */
  replyTo?: CommentUser;
}

/** 一级评论 + 其所有回复（2 层拍平） */
export interface CommentThread {
  root: CommentItem;
  /** 同一个 root 下的所有回复，按时间升序 */
  replies: CommentItem[];
}

/** 拉评论列表请求 */
export interface CommentListParams {
  noteId: string;
  page: number;
  size: number;
}

/** 评论列表响应 */
export interface CommentListResult {
  threads: CommentThread[];
  hasMore: boolean;
  page: number;
  total: number;
}

/** 发评论请求 */
export interface PostCommentParams {
  noteId: string;
  text: string;
  /** 回复某一级评论时填 root 的 id；不填则是发一级 */
  rootId?: string;
  /** 回复特定用户（回复某条 reply 时指向该 reply 的作者） */
  replyTo?: CommentUser;
}

/** 评论点赞响应 */
export interface LikeCommentResult {
  liked: boolean;
  likes: number;
}
