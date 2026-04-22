/**
 * 用户 API service。
 * 同一签名下根据 `isMock()` 切换真/假数据源。
 */
import { isMock, request } from '@/api/request';
import { delay, mockUserProfile } from '@/api/mock/user.mock';
import type { UserProfile } from '@/types/user';

/**
 * 获取当前登录用户的个人资料。
 * 真实接口路径 TODO：待 API 盘点确认，暂按 `/api/user/profile` 占位。
 */
export function getProfile(): Promise<UserProfile> {
  if (isMock()) {
    return delay(mockUserProfile);
  }
  return request<UserProfile>({
    url: '/api/user/profile',
    method: 'GET',
  });
}
