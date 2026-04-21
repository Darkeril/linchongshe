import { request } from '@/utils/request';

export interface XiaohongshuSpiderParams {
  keyword: string;
  type?: 'image' | 'video' | 'note';
  sortType?: number; // 排序方式：0-综合排序, 1-最新, 2-最多点赞, 3-最多评论, 4-最多收藏
  noteTime?: number; // 笔记时间：0-不限, 1-一天内, 2-一周内, 3-半年内
  page?: number;
  pageSize?: number;
  cookie?: string; // Cookie值（可选，用于登录后的请求）
}

export interface XiaohongshuItem {
  id: string;
  title: string;
  cover: string;
  images?: string[];
  video?: string;
  author: string;
  authorId: string;
  url: string;
  content: string;
  publishTime: string;
  likeCount?: number;
  collectionCount?: number;
  commentCount?: number;
  viewCount?: number;
  type: 'note' | 'image' | 'video';
}

export interface XiaohongshuSpiderResponse {
  code: string | number;
  data: XiaohongshuItem[];
  message?: string;
}

// 小红书爬虫接口
export function xiaohongshuSpider(params: XiaohongshuSpiderParams) {
  return request<XiaohongshuSpiderResponse>({
    url: '/web/spider/xiaohongshu',
    method: 'post',
    data: params,
  });
}

// 下载视频到服务器并上传到OSS（直接调用file模块）
export function downloadXiaohongshuVideo(url: string, cookie?: string) {
  const params = new URLSearchParams();
  params.append('url', url);
  if (cookie) {
    params.append('cookie', cookie);
  }
  return request<{ code: string | number; data: { url: string; name: string }; msg?: string }>({
    url: `/file/xiaohongshu/video/download?${params.toString()}`,
    method: 'post',
  });
}

// 下载图片到服务器并上传到OSS（直接调用file模块）
export function downloadXiaohongshuImage(url: string, cookie?: string) {
  const params = new URLSearchParams();
  params.append('url', url);
  if (cookie) {
    params.append('cookie', cookie);
  }
  return request<{ code: string | number; data: { url: string; name: string }; msg?: string }>({
    url: `/file/xiaohongshu/image/download?${params.toString()}`,
    method: 'post',
  });
}

// 上传文件到OSS（直接调用file模块）
export function uploadFile(file: File) {
  const formData = new FormData();
  formData.append('file', file);
  return request<{ code: string | number; data: { url: string; name: string }; msg?: string }>({
    url: '/file/upload',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
  });
}

// 批量上传文件到OSS（直接调用file模块）
export function uploadBatchFiles(files: File[]) {
  const formData = new FormData();
  files.forEach((file) => {
    formData.append('files', file); // 后端参数名是 files（数组）
  });
  return request<{ code: string | number; data: Array<{ url: string; name: string }>; msg?: string }>({
    url: '/file/batch/upload',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
  });
}
