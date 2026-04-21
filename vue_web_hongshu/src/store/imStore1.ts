import { defineStore } from "pinia";
import { ref } from "vue";
import { store } from "@/store";
import { Socket } from "socket.io-client"; 


// 使用setup模式定义
export const imStore = defineStore("imStore", () => {
  const userList = ref<Array<any>>([]);

  const productChatList = ref<Array<any>>([]); 

  const message = ref<any>({});

  const socket = ref<Socket | null>(null); // 添加socket状态

  const countMessage = ref({
    chatCount: 0,
    productChatCount: 0,
    likeOrCollectionCount: 0,
    commentCount: 0,
    followCount: 0,
  });

    // 添加socket相关方法
    const setSocket = (socketInstance: Socket | null) => {
      socket.value = socketInstance;
    };

  const setUserList = (data: Array<any>) => {
    userList.value = data;
  };

  const setProductChatList = (data: Array<any>) => {
    productChatList.value = data;
  };

  const setCountMessage = (data: any) => {
    countMessage.value = data;
  };

  const setMessage = (data: any) => {
    message.value = data;
  };

  return { 
    userList,
    productChatList,
    countMessage, 
    message, 
    socket,          
    setSocket,     
    setUserList, 
    setProductChatList, 
    setCountMessage, 
    setMessage };
});

export function useImStore() {
  return imStore(store);
}
