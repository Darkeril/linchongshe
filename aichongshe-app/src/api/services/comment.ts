/**
 * 笔记评论 API service（Phase 5b）。
 *
 * 严格按 CLAUDE.md 原则 7：
 * - 读 → GET；写 → POST
 * - Token 走 Header（Authorization: Bearer）
 * - URL 走 `/app/web/comment/*` 命名空间
 */
import { isMock, request } from '@/api/request';
import {
  mockGetComments,
  mockLikeComment,
  mockPostComment,
} from '@/api/mock/comment.mock';
import { getToken } from '@/utils/auth';
import type {
  CommentItem,
  CommentListParams,
  CommentListResult,
  LikeCommentResult,
  PostCommentParams,
} from '@/types/comment';

function getAuthHeader(): Record<string, string> {
  const token = getToken();
  return token ? { Authorization: `Bearer ${token}` } : {};
}

/** 拉评论列表（读 → GET） */
export function getComments(params: CommentListParams): Promise<CommentListResult> {
  if (isMock()) {
    return mockGetComments(params);
  }
  return request<CommentListResult>({
    url: `/app/web/comment/list?noteId=${encodeURIComponent(params.noteId)}&page=${params.page}&size=${params.size}`,
    method: 'GET',
    header: getAuthHeader(),
  });
}

/** 发评论（写 → POST） */
export function postComment(params: PostCommentParams): Promise<CommentItem> {
  if (isMock()) {
    return mockPostComment(params);
  }
  return request<CommentItem>({
    url: '/app/web/comment/create',
    method: 'POST',
    data: {
      noteId: params.noteId,
      text: params.text,
      rootId: params.rootId,
      replyToUserId: params.replyTo?.id,
    },
    header: getAuthHeader(),
  });
}

/** 点赞 / 取消点赞评论 */
export function likeComment(commentId: string): Promise<LikeCommentResult> {
  if (isMock()) {
    return mockLikeComment(commentId);
  }
  return request<LikeCommentResult>({
    url: `/app/web/comment/${encodeURIComponent(commentId)}/like`,
    method: 'POST',
    header: getAuthHeader(),
  });
}
