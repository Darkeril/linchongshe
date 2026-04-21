import { request } from '@/utils/request';

export interface AlbumItemRecord {
  id?: number;
  uid?: string;
  blogUid?: string;
  subjectUid?: string;
  sort?: number;
  blog?: {
    uid?: string;
    title?: string;
    author?: string;
    isOriginal?: number;
    photoList?: string[];
    blogSort?: {
      sortName?: string;
    };
    tagList?: Array<{
      content?: string;
    }>;
  };
}

export interface AlbumItemParams extends Partial<AlbumItemRecord> {
  current?: number;
  pageSize?: number;
  subjectUid?: string;
  title?: string;
  author?: string;
}

export interface AlbumItemListRes {
  code: string;
  data: {
    records: AlbumItemRecord[];
    total: number;
  };
  message?: string;
}

export interface AlbumItemListParams {
  subjectUid: string;
  pageSize?: number;
  currentPage?: number;
  title?: string;
  author?: string;
}

// 获取专辑项目列表
export function getSubjectItemList(params: AlbumItemListParams) {
  return request<AlbumItemListRes>({
    url: '/web/albumItem/list',
    method: 'get',
    params,
  });
}

// 编辑专辑项目（用于排序）
export function editSubjectItem(data: Array<{
  uid: string;
  blogUid: string;
  subjectUid: string;
  sort: number;
}>) {
  return request({
    url: '/web/albumItem/edit',
    method: 'post',
    data,
  });
}

// 批量删除专辑项目
export function deleteBatchSubjectItem(data: Array<{ uid: string }>) {
  return request({
    url: '/web/albumItem/deleteBatch',
    method: 'post',
    data,
  });
}

// 按创建时间排序
export function sortByCreateTime(params: URLSearchParams) {
  return request({
    url: '/web/albumItem/sortByCreateTime',
    method: 'post',
    data: params,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
  });
}








