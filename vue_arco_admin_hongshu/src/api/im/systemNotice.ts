import request from '@/utils/request';
import axios from '@/api/interceptor';

export interface ImSystemNoticeQueryParams {
  content?: string;
  pageNum?: number;
  pageSize?: number;
}

export interface ImSystemNoticeRecord {
  id: number;
  content: string;
  scopeType: number;
  sendTime?: string | number;
  totalRecvCount?: number;
  readCount?: number;
  unreadCount?: number;
}

export interface ImSystemNoticeDetailRecord {
  noticeId: number;
  userId: number;
  userName?: string;
  nickName?: string;
  headImage?: string;
  readFlag?: number;
  readTime?: string | number;
}

export interface ImSystemNoticeSendPayload {
  content: string;
  recvIds?: number[];
}

export function getImSystemNoticeList(params: ImSystemNoticeQueryParams) {
  return axios({
    url: '/im/sys/systemNotice/list',
    method: 'get',
    params,
  });
}

export function getImSystemNoticeDetailList(
  noticeId: number,
  params: { pageNum?: number; pageSize?: number }
) {
  return axios({
    url: `/im/sys/systemNotice/detail/${noticeId}`,
    method: 'get',
    params,
  });
}

/**
 * 发送IM系统通知
 */
export function sendImSystemNotice(data: ImSystemNoticeSendPayload) {
  return request({
    url: '/im/sys/systemNotice/send',
    method: 'post',
    data,
  });
}

