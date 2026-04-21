import request from '@/utils/request';

export interface PageQuery {
  current?: number;
  size?: number;
  [key: string]: any;
}

export function pageOrder(params: PageQuery) {
  return request({
    url: '/chat/gpt/order/page',
    method: 'get',
    params,
  });
}

export function delOrder(id: number | string) {
  return request({
    url: `/chat/gpt/order/${id}`,
    method: 'delete',
  });
}






