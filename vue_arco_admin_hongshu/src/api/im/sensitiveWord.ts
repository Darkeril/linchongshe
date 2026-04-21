import request from '@/utils/request';
import axios from '@/api/interceptor';

/** 敏感词列表查询参数 */
export interface ImSensitiveWordQueryParams {
  content?: string;
  enabled?: boolean;
  pageNum?: number;
  pageSize?: number;
}

/** 敏感词 VO */
export interface ImSensitiveWordVo {
  id: number;
  content?: string;
  enabled?: boolean;
  creator?: number;
  creatorName?: string;
  createTime?: string;
}

/** 敏感词新增/修改 BO */
export interface ImSensitiveWordBo {
  id?: number;
  content: string;
  enabled?: boolean;
}

/** 分页表格返回 */
export interface TableDataInfo<T> {
  code: number;
  msg: string;
  rows: T[];
  total: number;
}

/** 敏感词列表（分页） */
export function getImSensitiveWordList(params: ImSensitiveWordQueryParams) {
  return axios({
    url: '/im/sys/sensitiveWord/list',
    method: 'get',
    params,
  });
}

/** 敏感词详情 */
export function getImSensitiveWordInfo(id: number) {
  return request<{ code: number; data: ImSensitiveWordVo }>({
    url: `/im/sensitiveWord/${id}`,
    method: 'get',
  });
}

/** 新增敏感词 */
export function addImSensitiveWord(data: ImSensitiveWordBo) {
  return request({
    url: '/im/sensitiveWord',
    method: 'post',
    data,
  });
}

/** 修改敏感词 */
export function updateImSensitiveWord(data: ImSensitiveWordBo) {
  return request({
    url: '/im/sensitiveWord',
    method: 'put',
    data,
  });
}

/** 删除敏感词 */
export function deleteImSensitiveWord(ids: number[]) {
  return request({
    url: `/im/sensitiveWord/${ids.join(',')}`,
    method: 'delete',
  });
}

/** 启用/关闭敏感词 */
export function enableImSensitiveWord(data: { id: number; enabled: boolean }) {
  return request({
    url: '/im/sensitiveWord/enable',
    method: 'put',
    data,
  });
}
