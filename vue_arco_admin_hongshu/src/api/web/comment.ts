import { request } from '@/utils/request';

export interface CommentRecord {
  id: number;
  content: string;
  userName: string;
  noteTitle: string;
  status: number;
  createTime: string;
}

export interface CommentParams extends Partial<CommentRecord> {
  current: number;
  pageSize: number;
}

export function getCommentList(params: CommentParams) {
  return request<{
    list: CommentRecord[];
    total: number;
  }>({
    url: '/web/comment/list',
    method: 'get',
    params,
  });
}

export function getComment(id: number) {
  return request<CommentRecord>({
    url: `/web/comment/${id}`,
    method: 'get',
  });
}

export function approveComment(id: number) {
  return request({
    url: `/web/comment/${id}/approve`,
    method: 'put',
  });
}

export function rejectComment(id: number) {
  return request({
    url: `/web/comment/${id}/reject`,
    method: 'put',
  });
}

export function deleteComment(id: number) {
  return request({
    url: `/web/comment/${id}`,
    method: 'delete',
  });
}

// 获取评论列表 (兼容旧接口)
export function listComment(params: any) {
  return request({
    url: '/web/comment/list',
    method: 'get',
    params,
  });
}

// 删除评论 (兼容旧接口)
export function delComment(ids: string | number) {
  return request({
    url: `/web/comment/${ids}`,
    method: 'delete',
  });
}

// 获取子评论列表（一级以下评论）
export function getCommentTreeList(params: any) {
  return request({
    url: '/web/comment/treeList',
    method: 'get',
    params,
  });
}