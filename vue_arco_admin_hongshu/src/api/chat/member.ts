import request from '@/utils/request';

export interface PageQuery {
  current?: number;
  size?: number;
  [key: string]: any;
}

export function pageMember(params: PageQuery) {
  return request({
    url: '/chat/gpt/user/page',
    method: 'get',
    params,
  });
}

export function delMember(id: number | string) {
  return request({
    url: `/chat/gpt/user/${id}`,
    method: 'delete',
  });
}

export function updateMember(data: any) {
  return request({
    url: '/chat/gpt/user',
    method: 'put',
    data,
  });
}

export function updateMemberStatus(id: number | string, status: number) {
  return request({
    url: `/chat/gpt/user/${id}/status`,
    method: 'put',
    data: { status },
  });
}

export function delAllMember(ids: (number | string)[]) {
  return request({
    url: '/chat/gpt/user/batch',
    method: 'delete',
    data: { ids },
  });
}






