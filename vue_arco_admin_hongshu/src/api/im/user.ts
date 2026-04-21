import request from '@/utils/request';
import axios from '@/api/interceptor';

/** 用户列表查询参数 */
export interface ImUserQueryParams {
  userName?: string;
  nickName?: string;
  activeRange?: 'daily' | 'monthly';
  pageNum?: number;
  pageSize?: number;
}

/** 用户 VO */
export interface ImUserVo {
  id: number;
  userName?: string;
  nickName?: string;
  phone?: string;
  hsId?: number;
  headImage?: string;
  headImageThumb?: string;
  sex?: number;
  signature?: string;
  lastLoginTime?: string;
  createdTime?: string;
  isBanned?: boolean;
  reason?: string;
}

/** 分页表格返回 */
export interface TableDataInfo<T> {
  code: number;
  msg: string;
  rows: T[];
  total: number;
}

/** 封禁参数 */
export interface ImUserBanDto {
  id: number;
  reason?: string;
}

/** 解封参数 */
export interface ImUserUnbanDto {
  id: number;
}

/** 用户列表（分页）- 与会员管理一致使用 axios 直调 */
export function getImUserList(params: ImUserQueryParams) {
  return axios({
    url: '/im/sys/user/list',
    method: 'get',
    params,
  });
}

/** 用户详情 */
export function getImUserInfo(id: number) {
  return request<{ code: number; data: ImUserVo }>({
    url: `/im/sys/user/${id}`,
    method: 'get',
  });
}

/** 封禁用户 */
export function banImUser(data: ImUserBanDto) {
  return request({
    url: '/im/sys/user/ban',
    method: 'put',
    data,
  });
}

/** 解封用户 */
export function unbanImUser(data: ImUserUnbanDto) {
  return request({
    url: '/im/sys/user/unban',
    method: 'put',
    data,
  });
}

/** 导出用户列表（Excel） */
export function exportImUserList(params?: {
  userName?: string;
  nickName?: string;
  activeRange?: 'daily' | 'monthly';
}) {
  return axios({
    url: '/im/sys/user/export',
    method: 'post',
    data: params ?? {},
    responseType: 'blob',
  });
}

/** 总用户数 */
export function getImUserTotalCount() {
  return request<{ code: number; data: number }>({
    url: '/im/sys/user/totalCount',
    method: 'get',
  });
}

/** 活跃统计（日活、周活、月活） */
export function getImUserActiveStats() {
  return request<{ code: number; data: { dailyActive: number; weeklyActive: number; monthlyActive: number } }>({
    url: '/im/sys/user/activeStats',
    method: 'get',
  });
}

/** 按天注册数（首页图表） */
export function getImUserDailyRegistrationCount(days?: number) {
  return request<{ code: number; data: Array<{ date: string; count: number }> }>({
    url: '/im/sys/user/dailyRegistrationCount',
    method: 'get',
    params: { days: days ?? 7 },
  });
}

/** IM 用户下拉搜索（按用户名/昵称） */
export function searchImUserByName(name: string) {
  return request<{ code: number; data: ImUserVo[] }>({
    url: '/im/sys/user/findByName',
    method: 'get',
    params: { name },
  });
}
