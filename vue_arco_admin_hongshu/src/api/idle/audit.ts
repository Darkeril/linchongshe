import { request } from '@/utils/request';

export interface ProductAuditRecord {
  id: number;
  name: string;
  userName: string;
  status: number;
  createTime: string;
}

export interface ProductAuditParams extends Partial<ProductAuditRecord> {
  current: number;
  pageSize: number;
}

export function getProductAuditList(params: ProductAuditParams) {
  return request<{
    list: ProductAuditRecord[];
    total: number;
  }>({
    url: '/idle/audit/list',
    method: 'get',
    params,
  });
}

export function getProductAudit(id: number) {
  return request<ProductAuditRecord>({
    url: `/idle/audit/${id}`,
    method: 'get',
  });
}

export function approveProduct(id: number) {
  return request({
    url: `/idle/audit/${id}/approve`,
    method: 'put',
  });
}

export function rejectProduct(id: number) {
  return request({
    url: `/idle/audit/${id}/reject`,
    method: 'put',
  });
}


