import request from '@/utils/request';
import axios from '@/api/interceptor';

/** 私聊消息查询参数 */
export interface ImPrivateMessageQueryParams {
  sendId?: number;
  recvId?: number;
  content?: string;
  type?: number;
  status?: number;
  pageNum?: number;
  pageSize?: number;
}

/** 私聊消息 VO */
export interface ImPrivateMessageVo {
  id: number;
  sendId?: number;
  sendUserName?: string;
  sendHeadImage?: string;
  sendHeadImageThumb?: string;
  recvId?: number;
  recvUserName?: string;
  recvHeadImage?: string;
  recvHeadImageThumb?: string;
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

/** 私聊消息列表（分页） */
export function getImPrivateMessageList(params: ImPrivateMessageQueryParams) {
  return axios({
    url: '/im/sys/privateMessage/list',
    method: 'get',
    params,
  });
}

/** 私聊消息详情 */
export function getImPrivateMessageInfo(id: number) {
  return request<{ code: number; data: ImPrivateMessageVo }>({
    url: `/im/sys/privateMessage/${id}`,
    method: 'get',
  });
}

/** 按天私聊消息量 */
export function getImPrivateMessageDailyCount(days?: number) {
  return request<{ code: number; data: Array<{ date: string; count: number }> }>({
    url: '/im/sys/privateMessage/dailyCount',
    method: 'get',
    params: { days: days ?? 7 },
  });
}
