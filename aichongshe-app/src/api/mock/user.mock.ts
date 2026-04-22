/**
 * 用户相关 mock 数据。
 * 必须满足 src/types/user.ts 的类型契约。
 */
import type { UserProfile } from '@/types/user';

export const mockUserProfile: UserProfile = {
  id: '1800000000000000001',
  nickname: '毛球侦探',
  avatar: 'https://picsum.photos/seed/pet-detective/200',
  bio: '两只橘猫 × 一位铲屎官｜记录生活里的软柿子日常 🧶',
  followCount: 128,
  fansCount: 1436,
  likeCount: 9287,
  gender: 2,
  createdAt: '2025-10-08T12:34:56.000Z',
};

/**
 * 模拟异步延迟，贴近真实网络场景。
 */
export function delay<T>(data: T, ms = 200): Promise<T> {
  return new Promise((resolve) => setTimeout(() => resolve(data), ms));
}
