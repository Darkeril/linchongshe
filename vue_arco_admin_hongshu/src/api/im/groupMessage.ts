import request from '@/utils/request';
import axios from '@/api/interceptor';

/** 群消息查询参数 */
export interface ImGroupMessageQueryParams {
  groupId?: number;
  sendId?: number;
  content?: string;
  type?: number;
  pageNum?: number;
  pageSize?: number;
}

/** 群消息 VO */
export interface ImGroupMessageVo {
  id: number;
  groupId?: number;
  groupName?: string;
  sendId?: number;
  sendUserName?: string;
  sendHeadImage?: string;
  sendHeadImageThumb?: string;
  sendNickName?: string;
  content?: string;
  type?: number;
  status?: number;
  sendTime?: string;
}

/** 分页表格返回 */
export interface TableDataInfo<T> {
  code: number;
  msg: string;
  rows: T[];
  total: number;
}

/** 群消息列表（分页） */
export function getImGroupMessageList(params: ImGroupMessageQueryParams) {
  return axios({
    url: '/im/groupMessage/list',
    method: 'get',
    params,
  });
}

/** 群消息详情 */
export function getImGroupMessageInfo(id: number) {
  return request<{ code: number; data: ImGroupMessageVo }>({
    url: `/im/groupMessage/${id}`,
    method: 'get',
  });
}

/** 按天群消息量 */
export function getImGroupMessageDailyCount(days?: number) {
  return request<{ code: number; data: Array<{ date: string; count: number }> }>({
    url: '/im/groupMessage/dailyCount',
    method: 'get',
    params: { days: days ?? 7 },
  });
}
