import request from '@/utils/request';

export interface PageQuery {
  current?: number;
  size?: number;
  [key: string]: any;
}

export function pageChatMessage(params: PageQuery) {
  return request({
    url: '/chat/gpt/chat-message/page',
    method: 'get',
    params,
  });
}

export function delChatMessage(id: number | string) {
  return request({
    url: `/chat/gpt/chat-message/${id}`,
    method: 'delete',
  });
}






