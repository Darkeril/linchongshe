/**
 * 笔记相关 API service（真/假同签名）。
 *
 * 严格按 CLAUDE.md 原则 7：
 * - 写操作用 POST；列表拉取虽然是读，但参数多、按后端习惯也用 POST
 * - Token 走 Header（Authorization: Bearer）
 * - URL 走 `/app/web/note/*` 命名空间
 */
import { isMock, request } from '@/api/request';
import { mockGetNoteList, mockLikeNote } from '@/api/mock/note.mock';
import { getToken } from '@/utils/auth';
import type {
  LikeNoteResult,
  NoteListParams,
  NoteListResult,
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
