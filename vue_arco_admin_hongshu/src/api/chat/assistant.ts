import request from '@/utils/request';

export interface PageQuery {
  current?: number;
  size?: number;
  [key: string]: any;
}

export function pageAssistant(params: PageQuery) {
  return request({
    url: '/chat/gpt/assistant/page',
    method: 'get',
    params,
  });
}

export function delAssistant(id: number | string) {
  return request({
    url: `/chat/gpt/assistant/${id}`,
    method: 'delete',
  });
}






