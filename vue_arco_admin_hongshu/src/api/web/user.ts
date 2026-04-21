import axios from '@/api/interceptor';

export interface WebUserRecord {
  id: string;
  username: string;
  avatar?: string;
  [key: string]: any;
}

/** 解析 getUserByKeyword：拦截器已返回 { code, data: Page } */
export function unwrapUserKeywordPage(res: unknown): WebUserRecord[] {
  const r = res as { data?: { records?: WebUserRecord[]; rows?: WebUserRecord[] } } | null;
  if (!r?.data) return [];
  return r.data.records || r.data.rows || [];
}

/** 解析 getUserById：拦截器已返回 { code, data: WebUser } */
export function unwrapUserEntity(res: unknown): WebUserRecord | null {
  const r = res as { data?: WebUserRecord } | null;
  const u = r?.data;
  if (u && typeof u === 'object' && u.id != null) return u;
  return null;
}

// 根据关键词查询用户（分页）
export function getUserByKeyword(currentPage: number, pageSize: number, keyword: string) {
  return axios({
    url: `/web/user/getUserByKeyword/${currentPage}/${pageSize}`,
    method: 'get',
    params: { keyword },
  });
}

// 获取用户列表
export function getUserList(query: any) {
  return axios({
    url: '/web/user/getUserList',
    method: 'get',
    params: query,
  });
}

// 根据ID获取用户信息
export function getUserById(userId: string) {
  return axios({
    url: '/web/user/getUserById',
    method: 'get',
    params: { userId },
  });
}



