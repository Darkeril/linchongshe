import { request } from '@/utils/request';

export interface SpiderParams {
  currentPage: number;
  keyword: string;
}

export interface SpiderResponse {
  code: string;
  data: string[];
  message?: string;
}

// 图片爬虫
export function pictureSpider(params: SpiderParams) {
  return request<SpiderResponse>({
    url: '/web/spider/picture',
    method: 'get',
    params,
  });
}








