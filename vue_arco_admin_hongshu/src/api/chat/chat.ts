import request from '@/utils/request';

export interface PageQuery {
  current?: number;
  size?: number;
  [key: string]: any;
}

export function pageChat(params: PageQuery) {
  return request({
    url: '/chat/gpt/chat/page',
    method: 'get',
    params,
  });
}

export function delChat(id: number | string) {
  return request({
    url: `/chat/gpt/chat/${id}`,
    method: 'delete',
  });
}

export function listChat(params?: Record<string, any>) {
  return request({
    url: '/chat/gpt/chat/list',
    method: 'get',
    params,
  });
}






