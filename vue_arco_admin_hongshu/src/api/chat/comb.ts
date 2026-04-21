import request from '@/utils/request';

export interface PageQuery {
  current?: number;
  size?: number;
  [key: string]: any;
}

export function pageComb(params: PageQuery) {
  return request({
    url: '/chat/gpt/comb/page',
    method: 'get',
    params,
  });
}

export function delComb(id: number | string) {
  return request({
    url: `/chat/gpt/comb/${id}`,
    method: 'delete',
  });
}

export function updateComb(data: any) {
  return request({
    url: '/chat/gpt/comb',
    method: 'put',
    data,
  });
}






