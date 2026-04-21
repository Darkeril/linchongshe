import request from '@/utils/request';

export interface PageQuery {
  current?: number;
  size?: number;
  [key: string]: any;
}

export function pageRedemption(params: PageQuery) {
  return request({
    url: '/chat/gpt/redemption/page',
    method: 'get',
    params,
  });
}

export function delRedemption(id: number | string) {
  return request({
    url: `/chat/gpt/redemption/${id}`,
    method: 'delete',
  });
}

export function getRedemption(id: number | string) {
  return request({
    url: `/chat/gpt/redemption/${id}`,
    method: 'get',
  });
}

export function addRedemption(data: any) {
  return request({
    url: '/chat/gpt/redemption',
    method: 'post',
    data,
  });
}

export function updateRedemption(data: any) {
  return request({
    url: '/chat/gpt/redemption',
    method: 'put',
    data,
  });
}






