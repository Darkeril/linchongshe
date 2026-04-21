import axios from '@/api/interceptor';

export interface HttpResponse<T = unknown> {
  msg: string;
  code: number;
  data?: T;
  rows?: T;
}

export interface RequestConfig {
  url: string;
  method: 'get' | 'post' | 'put' | 'delete';
  params?: any;
  data?: any;
  headers?: any;
}

export function request<T = any>(config: RequestConfig): Promise<HttpResponse<T>> {
  return axios(config);
}

export default request;








