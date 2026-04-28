/**
 * 笔记相关 API service（真/假同签名）。
 *
 * 严格按 CLAUDE.md 原则 7：
 * - 写操作用 POST；列表拉取虽然是读，但参数多、按后端习惯也用 POST
 * - Token 走 Header（Authorization: Bearer）
 * - URL 走 `/app/web/note/*` 命名空间
 */
import { isMock, request } from '@/api/request';
import {
  mockGetNoteDetail,
  mockGetNoteList,
  mockLikeNote,
  mockPublishNote,
  mockToggleFollow,
  mockToggleSave,
} from '@/api/mock/note.mock';
import { getToken } from '@/utils/auth';
import type {
  FollowResult,
  LikeNoteResult,
  NoteDetail,
  NoteListParams,
  NoteListResult,
  PublishNotePayload,
  PublishNoteResult,
  SaveNoteResult,
} from '@/types/note';

function getAuthHeader(): Record<string, string> {
  const token = getToken();
  return token ? { Authorization: `Bearer ${token}` } : {};
}

/** 拉笔记列表（按 feed 分页） */
export function getNoteList(params: NoteListParams): Promise<NoteListResult> {
  if (isMock()) {
    return mockGetNoteList(params);
  }
  return request<NoteListResult>({
    url: '/app/web/note/list',
    method: 'POST',
    data: { feed: params.feed, page: params.page, size: params.size },
    header: getAuthHeader(),
  });
}

/** 点赞 / 取消点赞（后端根据当前状态切换） */
export function likeNote(noteId: string): Promise<LikeNoteResult> {
  if (isMock()) {
    return mockLikeNote(noteId);
  }
  return request<LikeNoteResult>({
    url: `/app/web/note/${noteId}/like`,
    method: 'POST',
    header: getAuthHeader(),
  });
}

/** 拉笔记详情
 *  按 CLAUDE.md 原则 7：读操作必须 GET，路径 RESTful 带资源 id
 */
export function getNoteDetail(id: string): Promise<NoteDetail> {
  if (isMock()) {
    return mockGetNoteDetail(id);
  }
  return request<NoteDetail>({
    url: `/app/web/note/${encodeURIComponent(id)}`,
    method: 'GET',
    header: getAuthHeader(),
  });
}

/** 收藏 / 取消收藏 */
export function toggleSave(noteId: string): Promise<SaveNoteResult> {
  if (isMock()) {
    return mockToggleSave(noteId);
  }
  return request<SaveNoteResult>({
    url: `/app/web/note/${noteId}/save`,
    method: 'POST',
    header: getAuthHeader(),
  });
}

/** 关注 / 取消关注作者 */
export function toggleFollow(authorId: string): Promise<FollowResult> {
  if (isMock()) {
    return mockToggleFollow(authorId);
  }
  return request<FollowResult>({
    url: `/app/web/user/${authorId}/follow`,
    method: 'POST',
    header: getAuthHeader(),
  });
}

/** 发布图文笔记（Phase 7 mock-first）。
 *  真实后端当前有 `/app/note/saveNote`，但图片上传仍需 multipart/OSS 前置流程；
 *  非 mock 模式先按 JSON 版 saveNote 对齐，最终契约见 docs/api-todo.md。
 */
export function publishNote(payload: PublishNotePayload): Promise<PublishNoteResult> {
  if (isMock()) {
    return mockPublishNote(payload);
  }

  const requestId = `acs-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
  return request<PublishNoteResult>({
    url: `/app/note/saveNote?requestId=${encodeURIComponent(requestId)}`,
    method: 'POST',
    data: {
      title: payload.title,
      content: payload.content,
      urls: payload.images.map((img) => img.url),
      noteCover: payload.images[0]?.url ?? '',
      tags: payload.topics,
      noteType: 1,
      address: payload.location ?? '',
      relatedProducts: payload.relatedProducts ?? [],
    },
    header: getAuthHeader(),
  });
}
