import axios from 'axios';

export interface AuditLogQueryParams {
  contentId?: string;
  contentType?: string;
  auditResult?: string;
  auditProvider?: string;
  startTime?: string;
  endTime?: string;
  pageNum: number;
  pageSize: number;
}

export interface AuditLog {
  id: string;
  contentId: string;
  contentType: string;
  auditResult: string;
  auditScore: number;
  auditReason: string;
  auditProvider: string;
  auditTime: string;
  createTime: string;
}

/**
 * 分页查询审核日志
 */
export function queryAuditLogList(params: AuditLogQueryParams) {
  return axios.post('/web/auditLog/list', params);
}

/**
 * 查询审核日志详情
 */
export function getAuditLogDetail(id: string) {
  return axios.get(`/web/auditLog/${id}`);
}
