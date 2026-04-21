import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { store } from "@/store";
import { Socket } from "socket.io-client"; 

// 消息类型枚举
export enum MessageType {
  LIKE = 0,
  COMMENT = 1,
  FOLLOW = 2,
  CHAT = 3,
  PRODUCT = 4
}

// 消息计数接口
export interface MessageCount {
  chatCount: number;
  productChatCount: number;
  likeOrCollectionCount: number;
  commentCount: number;
  followCount: number;
}

// 消息类型到计数属性的映射
const messageTypeToCountKey: Record<MessageType, keyof MessageCount> = {
  [MessageType.CHAT]: 'chatCount',
  [MessageType.PRODUCT]: 'productChatCount',
  [MessageType.COMMENT]: 'commentCount',
  [MessageType.LIKE]: 'likeOrCollectionCount',
  [MessageType.FOLLOW]: 'followCount'
};

export const imStore = defineStore("imStore", () => {
  // 状态定义
  const userList = ref<Array<any>>([]);
  const noteChatList = ref<Array<any>>([]); 
  const productChatList = ref<Array<any>>([]); 
  const message = ref<any>({});
  const socket = ref<Socket | null>(null);

  const countMessage = ref<MessageCount>({
    chatCount: 0,
    productChatCount: 0,
    likeOrCollectionCount: 0,
    commentCount: 0,
    followCount: 0,
  });

  // Socket 相关方法
  const setSocket = (socketInstance: Socket | null) => {
    socket.value = socketInstance;
  };

  // 用户列表相关方法
  const setUserList = (data: Array<any>) => {
    userList.value = data;
  };

  // 聊天列表相关方法
  const setNoteChatList = (data: Array<any>) => {
    noteChatList.value = data;
  };

  const setProductChatList = (data: Array<any>) => {
    productChatList.value = data;
  };

  // 消息相关方法
  const setMessage = (data: any) => {
    message.value = data;
  };

  // 消息计数相关方法
  const setCountMessage = (data: Partial<MessageCount>) => {
    const newCountMessage = { ...countMessage.value };
    
    Object.entries(data).forEach(([key, value]) => {
      if (key in newCountMessage) {
        newCountMessage[key as keyof MessageCount] = Math.max(0, Number(value) || 0);
      }
    });

    countMessage.value = newCountMessage;
  };

  const updateMessageCounts = (counts: Partial<MessageCount>) => {
    const validatedCounts = Object.entries(counts).reduce((acc, [key, value]) => ({
      ...acc,
      [key]: Math.max(0, Number(value) || 0)
    }), {} as Partial<MessageCount>);

    setCountMessage(validatedCounts);
  };

  const getMessageCount = (type: MessageType): number => {
    const key = messageTypeToCountKey[type];
    return countMessage.value[key] || 0;
  };

  const resetMessageCount = (messageType: MessageType) => {
    const key = messageTypeToCountKey[messageType];
    if (key) {
      const newCountMessage = { ...countMessage.value };
      newCountMessage[key] = 0;
      countMessage.value = newCountMessage;
    }
  };

  const resetAllMessageCounts = () => {
    countMessage.value = {
      chatCount: 0,
      productChatCount: 0,
      likeOrCollectionCount: 0,
      commentCount: 0,
      followCount: 0
    };
  };

  const incrementMessageCount = (type: MessageType, amount: number = 1) => {
    const key = messageTypeToCountKey[type];
    if (key) {
      const newCountMessage = { ...countMessage.value };
      newCountMessage[key] = Math.max(0, (newCountMessage[key] || 0) + amount);
      countMessage.value = newCountMessage;
    }
  };

  const decrementMessageCount = (type: MessageType, amount: number = 1) => {
    incrementMessageCount(type, -amount);
  };

  // 单独更新各类消息计数的方法
  const updateChatCount = (count: number) => {
    countMessage.value.chatCount = Math.max(0, count);
  };

  const updateProductChatCount = (count: number) => {
    countMessage.value.productChatCount = Math.max(0, count);
  };

  const updateLikeCount = (count: number) => {
    countMessage.value.likeOrCollectionCount = Math.max(0, count);
  };

  const updateCommentCount = (count: number) => {
    countMessage.value.commentCount = Math.max(0, count);
  };

  const updateFollowCount = (count: number) => {
    countMessage.value.followCount = Math.max(0, count);
  };

  // 计算属性
  const totalUnreadCount = computed(() => 
    Object.values(countMessage.value).reduce((sum, count) => sum + count, 0)
  );

  const chatUnreadCount = computed(() => countMessage.value.chatCount);
  const productUnreadCount = computed(() => countMessage.value.productChatCount);
  const commentUnreadCount = computed(() => countMessage.value.commentCount);
  const likeUnreadCount = computed(() => countMessage.value.likeOrCollectionCount);
  const followUnreadCount = computed(() => countMessage.value.followCount);

  return {
    // 状态
    userList,
    noteChatList,
    productChatList,
    countMessage,
    message,
    socket,

    // Socket 方法
    setSocket,

    // 列表相关方法
    setUserList,
    setNoteChatList,
    setProductChatList,

    // 消息相关方法
    setMessage,
    setCountMessage,

    // 消息计数相关方法
    updateMessageCounts,
    getMessageCount,
    resetMessageCount,
    resetAllMessageCounts,
    incrementMessageCount,
    decrementMessageCount,

    // 单独更新计数方法
    updateChatCount,
    updateProductChatCount,
    updateLikeCount,
    updateCommentCount,
    updateFollowCount,

    // 计算属性
    totalUnreadCount,
    chatUnreadCount,
    productUnreadCount,
    commentUnreadCount,
    likeUnreadCount,
    followUnreadCount
  };
});

// 导出 store hook
export function useImStore() {
  return imStore(store);
}