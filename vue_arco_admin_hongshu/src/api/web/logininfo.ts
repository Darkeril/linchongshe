import { request } from '@/utils/request';

export interface LogininfoRecord {
  id: number;
  userName: string;
  ipaddr: string;
  loginLocation: string;
  browser: string;
  os: string;
  status: number;
  msg: string;
  loginTime: string;
}

export interface LogininfoParams extends Partial<LogininfoRecord> {
  current: number;
  pageSize: number;
}

export function getLogininfoList(params: LogininfoParams) {
  return request<{
    list: LogininfoRecord[];
    total: number;
  }>({
    url: '/web/logininfo/list',
    method: 'get',
    params,
  });
}

export function getLogininfo(id: number) {
  return request<LogininfoRecord>({
    url: `/web/logininfo/${id}`,
    method: 'get',
  });
}

export function deleteLogininfo(id: number) {
  return request({
    url: `/web/logininfo/${id}`,
    method: 'delete',
  });
}

export function cleanLogininfo() {
  return request({
    url: '/web/logininfo/clean',
    method: 'delete',
  });
}

export function exportLogininfo() {
  return request({
    url: '/web/logininfo/export',
    method: 'get',
    responseType: 'blob',
  });
}


