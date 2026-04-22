/**
 * 后端响应通用结构。对齐 springcloud_hongshu。
 * 字段后续需要由 API 盘点阶段确认是否吻合，当前按大多数 Java 后端约定定义。
 */
export interface ApiResponse<T = unknown> {
  code: number;
  message: string;
  data: T;
}

/**
 * 分页响应通用结构。
 */
export interface PaginationResult<T> {
  list: T[];
  total: number;
  page: number;
  pageSize: number;
  hasMore: boolean;
}
