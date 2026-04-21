import request from '@/utils/request';

export interface PageQuery {
  current?: number;
  size?: number;
  [key: string]: any;
}

export function pageAssistantType(params: PageQuery) {
  return request({
    url: '/chat/gpt/assistant-type/page',
    method: 'get',
    params,
  });
}

export function delAssistantType(id: number | string) {
  return request({
    url: `/chat/gpt/assistant-type/${id}`,
    method: 'delete',
  });
}






