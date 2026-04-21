import request from "@/utils/request";

/**
 * 得到所有聊天的记录数量
 * @returns 聊天数量
 */
export const getCountMessage = () => {
  return request<any>({
    url: "/web/im/chat/getCountMessage", // mock接口
    method: "get",
  });
};

/**
 * 获取当前用户下所有聊天的用户信息
 * @returns 聊天的用户信息
 */
export const getChatUserList = () => {
  return request<any>({
    url: "/web/im/chat/getChatUserList", // mock接口
    method: "get",
  });
};

/**
 * 获取当前用户下所有聊天的用户信息
 * @returns 聊天的用户信息
 */
export const getProductChatMessages = () => {
  return request<any>({
    url: "/web/im/chat/productChatUserList", // mock接口
    method: "get",
  });
};

/**
 * 获取系统通知
 * @returns 系统通知内容
 */
export const getSystemNotification = () => {
  return request<any>({
    url: "/web/im/chat/getSystemNotification",
    method: "get",
  });
};

/**
 * C端：系统通知配置列表（管理端系统通知配置，非通知公告）
 */
export const getSystemNotificationConfigList = () => {
  return request<any>({
    url: "/web/im/chat/systemNotificationConfigList",
    method: "get",
  });
};

/**
 * 清除聊天数量
 * @param sendUid 发送方的用户id
 * @param type 类型
 * @returns success
 */
export const clearMessageCount = (sendUid:string, type:number, productId:string) => {
  return request<any>({
    url: "/web/im/chat/clearMessageCount", // mock接口
    method: "get",
    params:{
      sendUid,
      type,
      productId
    }
  });
};

/**
 * 获取所有的聊天记录
 * @param currentPage 分页
 * @param pageSize 分页数
 * @param acceptUid 接收方的用户id
 * @returns 聊天记录
 */
export const getAllChatRecord = (
  currentPage: number,
  pageSize: number,
  acceptUid: string
) => {
  return request<any>({
    url: `/web/im/chat/getAllChatRecord/${currentPage}/${pageSize}`, // mock接口
    method: "get",
    params: {
      acceptUid,
    },
  });
};

export const getProductChatRecord = (
  currentPage: number,
  pageSize: number,
  acceptUid: string,
  productId: string
) => {
  return request<any>({
    url: `/web/im/chat/productChatRecord/${currentPage}/${pageSize}`, // mock接口
    method: "get",
    params: {
      acceptUid,
      productId 
    },
  });
};

/**
 * 发送消息
 * @param data 消息实体
 * @returns success
 */
export const sendMsg = (data: any) => {
  return request<any>({
    url: "/web/im/chat/sendMsg", // mock接口
    method: "post",
    data: data,
  });
};
