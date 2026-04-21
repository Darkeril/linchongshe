import { request } from '@/utils/request';

export interface OrderRecord {
  id?: number | string;
  orderNumber?: string;
  productId?: number | string;
  productTitle?: string;
  productCover?: string;
  buyerId?: number | string;
  buyerName?: string;
  sellerId?: number | string;
  sellerName?: string;
  actualBuyMoney?: number | string;
  originalPrice?: number | string;
  dealStatus?: number; // 交易状态：0-待交易，1-已交易，2-已取消
  orderStatus?: number; // 支付状态：0-待支付，1-已支付，2-已退款
  payType?: string; // 支付类型（1-微信支付，2-支付宝支付）
  payTypeName?: string; // 支付类型名称
  createTime?: string;
  updateTime?: string;
  remark?: string;
  address?: string;
  phone?: string;
}

export interface OrderListParams {
  current?: number;
  pageSize?: number;
  orderNumber?: string;
  dealStatus?: number;
  orderStatus?: number;
  payType?: string;
  buyerName?: string;
  sellerName?: string;
  dateRange?: string[];
}

export interface OrderListRes {
  code: number;
  data: { list: OrderRecord[]; total: number } | OrderRecord[];
  msg?: string;
}

// 获取订单列表
export function getOrderList(params: OrderListParams) {
  return request<OrderListRes>({
    url: '/idle/order/list',
    method: 'get',
    params,
  });
}

// 获取订单详情
export function getOrder(id: number | string) {
  return request<{ code: number; data: OrderRecord }>({
    url: `/idle/order/${id}`,
    method: 'get',
  });
}

// 删除订单
export function deleteOrder(id: number | string) {
  return request({
    url: `/idle/order/${id}`,
    method: 'delete',
  });
}

// 批量删除订单
export function deleteBatchOrder(ids: Array<number | string>) {
  return request({
    url: '/idle/order/batch',
    method: 'delete',
    data: { ids },
  });
}

// 更新订单状态
export function updateOrderStatus(id: number | string, status: number) {
  return request({
    url: `/idle/order/${id}/status`,
    method: 'put',
    data: { status },
  });
}

// 取消订单
export function cancelOrder(id: number | string) {
  return request({
    url: `/idle/order/${id}/cancel`,
    method: 'put',
  });
}

// 导出订单
export function exportOrder(params: OrderListParams) {
  return request({
    url: '/idle/order/export',
    method: 'get',
    params,
    responseType: 'blob',
  });
}

// 获取订单统计
export function getOrderStats(params?: any) {
  return request({
    url: '/idle/order/stats',
    method: 'get',
    params,
  });
}