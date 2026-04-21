import { request } from '@/utils/request';

export interface PictureSortRecord {
  uid: string;
  name: string;
  isShow: number;
}

export interface PictureSortParams {
  pageSize?: number;
  currentPage?: number;
  isShow?: number;
}

export interface PictureSortListRes {
  code: string;
  data: {
    records: PictureSortRecord[];
    total: number;
  };
  message?: string;
}

// 获取图片分类列表
export function getPictureSortList(params: PictureSortParams) {
  return request<PictureSortListRes>({
    url: '/web/pictureSort/list',
    method: 'get',
    params,
  });
}













