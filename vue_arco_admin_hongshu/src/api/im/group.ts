import axios from '@/api/interceptor';

/** 群组列表查询参数 */
export interface ImGroupQueryParams {
  name?: string;
  ownerId?: number;
  pageNum?: number;
  pageSize?: number;
}

/** 群组 VO */
export interface ImGroupVo {
  id: number;
  name?: string;
  ownerId?: number;
  ownerUserName?: string;
  headImage?: string;
  headImageThumb?: string;
  notice?: string;
  dissolve?: boolean;
  isBanned?: boolean;
  reason?: string;
  createdTime?: string;
  memberCount?: number;
}

/** 分页表格返回 */
export interface TableDataInfo<T> {
  code: number;
  msg: string;
  rows: T[];
  total: number;
}

/** 封禁参数 */
export interface ImGroupBanDto {
  id: number;
  reason?: string;
}

/** 解封参数 */
export interface ImGroupUnbanDto {
  id: number;
}

/** 群组列表（分页）- 与会员管理一致使用 axios 直调 */
export function getImGroupList(params: ImGroupQueryParams) {
  return axios({
    url: '/im/sys/group/list',
    method: 'get',
    params,
  });
}

/** 群组详情 */
export function getImGroupInfo(id: number) {
  return axios({
    url: `/im/sys/group/${id}`,
    method: 'get',
  });
}

/** 封禁群组 */
export function banImGroup(data: ImGroupBanDto) {
  return axios({
    url: '/im/sys/group/ban',
    method: 'put',
    data,
  });
}

/** 解封群组 */
export function unbanImGroup(data: ImGroupUnbanDto) {
  return axios({
    url: '/im/sys/group/unban',
    method: 'put',
    data,
  });
}

/** 总群组数 */
export function getImGroupTotalCount() {
  return axios({
    url: '/im/sys/group/totalCount',
    method: 'get',
  });
}

/** 群成员列表（分页） */
export function getImGroupMemberList(params: {
  groupId: number;
  userNickName?: string;
  remarkNickName?: string;
  pageNum?: number;
  pageSize?: number;
}) {
  return axios({
    url: '/im/sys/group/member/list',
    method: 'get',
    params,
  });
}
