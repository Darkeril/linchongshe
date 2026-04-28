/**
 * 请求入口：通过 VITE_USE_MOCK 切换走 mock 还是真实 HTTP。
 *
 * - VITE_USE_MOCK === 'true' → 返回 null（由 service 层走 mock 分支）
 * - 否则 → 返回 uni.request 包装结果
 *
 * 真假切换以「service 为粒度」，方便渐进迁移：
 * 每个 service 自己判断 `isMock()`，决定读 mock 数据还是调 request()。
 */

import type { ApiResponse } from '@/types/common';

/** 当前是否走 mock 数据 */
export function isMock(): boolean {
  return import.meta.env.VITE_USE_MOCK === 'true';
}

/** 基础 URL（真实请求使用） */
export const BASE_URL: string = import.meta.env.VITE_API_BASE_URL ?? '';

export interface RequestOptions {
  url: string;
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE';
  data?: Record<string, unknown> | unknown[];
  header?: Record<string, string>;
  timeout?: number;
}

/**
 * 统一 HTTP 请求封装（基于 uni.request，四端通用）。
 * mock 模式下不会走这里 —— service 会在调用前判断 `isMock()` 并直接返回 mock。
 */
export function request<T = unknown>(options: RequestOptions): Promise<T> {
  return new Promise((resolve, reject) => {
    uni.request({
      url: `${BASE_URL}${options.url}`,
      method: options.method ?? 'GET',
      data: options.data,
      header: {
        'Content-Type': 'application/json',
        ...(options.header ?? {}),
      },
      timeout: options.timeout ?? 10000,
      success: (res) => {
        const body = res.data as ApiResponse<T>;
        if (body && typeof body === 'object' && 'code' in body) {
          if (body.code === 0 || body.code === 200) {
            resolve(body.data);
          } else {
            reject(new Error(body.message || `请求失败 (code=${body.code})`));
          }
        } else {
          // 非标准后端响应：原样返回
          resolve(res.data as T);
        }
      },
      fail: (err) => {
        reject(new Error(err.errMsg || '网络请求失败'));
      },
    });
  });
}
