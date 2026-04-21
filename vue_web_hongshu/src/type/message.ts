export interface CountMessage {
  chatCount: number
  productChatCount: number
  likeOrCollectionCount: number
  commentCount: number
  followCount: number
}

export enum MessageType {
  LIKE = 0,
  COMMENT = 1,
  FOLLOW = 2,
  CHAT = 3,
  PRODUCT = 4
}

export interface TabItem {
  type: MessageType
  title: string
  countKey: keyof CountMessage
}

export const MESSAGE_TABS: TabItem[] = [
  {
    type: MessageType.CHAT,
    title: '闲宝消息',
    countKey: 'chatCount'
  },
  {
    type: MessageType.PRODUCT,
    title: '闲宝消息',
    countKey: 'productChatCount'
  },
  {
    type: MessageType.COMMENT,
    title: '评论和@',
    countKey: 'commentCount'
  },
  {
    type: MessageType.LIKE,
    title: '赞和收藏',
    countKey: 'likeOrCollectionCount'
  },
  {
    type: MessageType.FOLLOW,
    title: '新增关注',
    countKey: 'followCount'
  }
]