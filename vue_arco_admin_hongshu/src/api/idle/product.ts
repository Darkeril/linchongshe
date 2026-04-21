import { request } from '@/utils/request';

export interface ProductRecord {
  id?: number | string;
  title?: string;
  author?: string;
  cover?: string;
  description?: string;
  productType?: number;
  auditStatus?: number;
  fromType?: number;
  cpid?: number | string;
  address?: string;
  count?: number;
  viewCount?: number;
  collectionCount?: number;
  commentCount?: number;
  sort?: number;
  createTime?: string;
  updateTime?: string;
  urls?: string;
  images?: string[];
  imageList?: string[];
  imageUrls?: string[];
  image1?: string;
  image2?: string;
  image3?: string;
  pictures?: string[];
  photoList?: string[];
  price?: string;
  originalPrice?: string;
}

export interface ProductListParams {
  current?: number;
  pageSize?: number;
  title?: string;
  author?: string;
  uid?: string | number;
  productType?: number;
  auditStatus?: number;
  fromType?: number;
  cpid?: number | string;
  dateRange?: string[];
}

export interface ProductListRes {
  code: number;
  data: { list: ProductRecord[]; total: number } | ProductRecord[];
  msg?: string;
}

// 获取商品列表
export function getProductList(params: ProductListParams) {
  return request<ProductListRes>({
    url: '/idle/product/list',
    method: 'get',
    params,
  });
}

// 查询未审核商品列表
export function unAuditListProduct(query: any) {
  return request({
    url: '/idle/product/unAuditList',
    method: 'get',
    params: query,
  });
}

// 获取商品详情
export function getProduct(id: number | string) {
  return request<{ code: number; data: ProductRecord }>({
    url: `/idle/product/${id}`,
    method: 'get',
  });
}

// 新增商品
export function addProduct(data: ProductRecord) {
  return request({
    url: '/idle/product',
    method: 'post',
    data,
    headers: { 'Content-Type': 'multipart/form-data' },
  });
}

// 修改商品
export function updateProduct(id: number | string, data: FormData) {
  return request({
    url: '/idle/product',
    method: 'put',
    data,
    headers: { 'Content-Type': 'multipart/form-data' },
  });
}

// 删除商品
export function deleteProduct(id: number | string) {
  return request({
    url: `/idle/product/${id}`,
    method: 'delete',
  });
}

// 批量删除商品
export function deleteBatchProduct(ids: Array<number | string>) {
  return request({
    url: '/idle/product/batch',
    method: 'delete',
    data: { ids },
  });
}

// 审核商品
export function auditProduct(data: { productId: string | number; auditType: string }) {
  return request({
    url: '/idle/product/auditProduct',
    method: 'put',
    data,
  });
}

// 批量审核商品
export function batchAuditProduct(data: { productIds: string; auditType: string }) {
  return request({
    url: '/idle/product/batchAuditProduct',
    method: 'put',
    data,
  });
}

// 刷新商品数据（ES重置）
export function refreshProductData() {
  return request({
    url: '/idle/product/refreshProductData',
    method: 'post',
  });
}

// 导出商品
export function exportProduct(params: ProductListParams) {
  return request({
    url: '/idle/product/export',
    method: 'get',
    params,
    responseType: 'blob',
  });
}

// 为了兼容性，添加 createProduct 别名
export const createProduct = addProduct;