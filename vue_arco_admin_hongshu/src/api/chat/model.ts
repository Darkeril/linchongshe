import request from '@/utils/request';

export interface PageQuery {
  current?: number;
  size?: number;
  [key: string]: any;
}

export interface ChatGptModelPayload {
  id?: number;
  name: string;
  model?: string;
  icon?: string;
  version?: string;
  status?: number;
  sort?: number;
}

export function pageModel(params: PageQuery) {
  return request({ url: '/chat/gpt/model/page', method: 'get', params });
}

/** 大模型下拉等：返回列表 */
export function listModel(params?: Record<string, unknown>) {
  return request({ url: '/chat/gpt/model/list', method: 'get', params });
}

export function getModel(id: number | string) {
  return request({ url: `/chat/gpt/model/${id}`, method: 'get' });
}

export function saveModel(data: ChatGptModelPayload) {
  return request({ url: '/chat/gpt/model', method: 'post', data });
}

export function updateModel(data: ChatGptModelPayload) {
  return request({ url: '/chat/gpt/model', method: 'put', data });
}

export function delModel(id: number | string) {
  return request({ url: `/chat/gpt/model/${id}`, method: 'delete' });
}
