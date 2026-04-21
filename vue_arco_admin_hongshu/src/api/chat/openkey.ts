import request from '@/utils/request';

export interface PageQuery {
  current?: number;
  size?: number;
  [key: string]: any;
}

/** 对应后端 OpenkeyCommand */
export interface ChatOpenkeyPayload {
  id?: number;
  appId?: string;
  appKey?: string;
  appSecret?: string;
  totalTokens?: number;
  usedTokens?: number;
  surplusTokens?: number;
  status?: number;
  model?: string;
  remark?: string;
}

export function pageOpenkey(params: PageQuery) {
  return request({ url: '/chat/gpt/openkey/page', method: 'get', params });
}

export function getOpenkey(id: number | string) {
  return request({ url: `/chat/gpt/openkey/${id}`, method: 'get' });
}

export function saveOpenkey(data: ChatOpenkeyPayload) {
  return request({ url: '/chat/gpt/openkey', method: 'post', data });
}

export function updateOpenkey(data: ChatOpenkeyPayload) {
  return request({ url: '/chat/gpt/openkey', method: 'put', data });
}

export function delOpenkey(id: number | string) {
  return request({ url: `/chat/gpt/openkey/${id}`, method: 'delete' });
}
