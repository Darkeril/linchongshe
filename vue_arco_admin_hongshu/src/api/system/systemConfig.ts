import { request } from '@/utils/request';

export interface SystemConfigRecord {
  id: number;
  configName: string;
  configKey: string;
  configValue: string;
  configType: string;
  createTime: string;
}

export interface SystemConfigParams extends Partial<SystemConfigRecord> {
  current: number;
  pageSize: number;
}

export function getSystemConfigList(params: SystemConfigParams) {
  return request<{
    list: SystemConfigRecord[];
    total: number;
  }>({
    url: '/system/config/list',
    method: 'get',
    params,
  });
}

export function getSystemConfig(id: number) {
  return request<SystemConfigRecord>({
    url: `/system/config/${id}`,
    method: 'get',
  });
}

export function createSystemConfig(data: Partial<SystemConfigRecord>) {
  return request<SystemConfigRecord>({
    url: '/system/config',
    method: 'post',
    data,
  });
}

export function updateSystemConfig(id: number, data: Partial<SystemConfigRecord>) {
  return request<SystemConfigRecord>({
    url: `/system/config/${id}`,
    method: 'put',
    data,
  });
}

export function deleteSystemConfig(id: number) {
  return request({
    url: `/system/config/${id}`,
    method: 'delete',
  });
}

export function refreshCache() {
  return request({
    url: '/system/config/refreshCache',
    method: 'delete',
  });
}


