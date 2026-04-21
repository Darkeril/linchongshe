import { request } from '@/utils/request';

export interface IdleNavbarRecord {
  id?: number | string;
  name?: string;
  url?: string;
  status?: number; // 1 上线, 0 下线
  sort?: number;
  createTime?: string;
}

export interface IdleNavbarListParams {
  current?: number;
  pageSize?: number;
  name?: string;
  status?: number;
}

export interface IdleNavbarListRes {
  code: number;
  data: { list: IdleNavbarRecord[]; total: number };
  msg?: string;
}

// 列表
export function getIdleNavbarList(params: IdleNavbarListParams) {
  return request<IdleNavbarListRes>({
    url: '/idle/navbar/list',
    method: 'get',
    params,
  });
}

// 详情
export function getIdleNavbar(id: number | string) {
  return request<{ code: number; data: IdleNavbarRecord }>({
    url: `/idle/navbar/${id}`,
    method: 'get',
  });
}

// 新增
export function addIdleNavbar(data: FormData | IdleNavbarRecord) {
  return request({
    url: '/idle/navbar',
    method: 'post',
    data,
    headers: data instanceof FormData ? {} : { 'Content-Type': 'multipart/form-data' },
  });
}

// 编辑
export function updateIdleNavbar(data: FormData | IdleNavbarRecord) {
  return request({
    url: '/idle/navbar',
    method: 'put',
    data,
    headers: data instanceof FormData ? {} : { 'Content-Type': 'multipart/form-data' },
  });
}

// 删除
export function deleteIdleNavbar(id: number | string) {
  return request({
    url: `/idle/navbar/${id}`,
    method: 'delete',
  });
}

// 状态
export function updateIdleNavbarStatus(id: number | string, status: number) {
  return request({
    url: '/idle/navbar/updateStatus',
    method: 'post',
    params: { id, status },
  });
}