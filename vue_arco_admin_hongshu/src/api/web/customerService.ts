import axios from 'axios';

export interface CustomerServiceRecord {
    id?: string;
    uid?: string; // 用户ID
    userId?: string;
    username?: string;
    nickname?: string;
    avatar?: string;
    content?: string; // 最后一条消息内容
    timestamp?: number; // 最后一条消息时间戳
    count?: number; // 未读消息数
    lastMessage?: string;
    lastMessageTime?: string;
    unreadCount?: number;
    createTime?: string;
}

export interface CustomerServiceMessage {
    id?: string;
    sendUid?: string;
    acceptUid?: string;
    content?: string;
    msgType?: number;
    chatType?: number;
    timestamp?: number;
    createTime?: string;
    sendUser?: {
        id?: string;
        username?: string;
        avatar?: string;
    };
    acceptUser?: {
        id?: string;
        username?: string;
        avatar?: string;
    };
}

// 获取所有与客服聊天的用户列表
export function getCustomerServiceUserList(query: any) {
    return axios({
        url: '/web/im/chat/getCustomerServiceUserList',
        method: 'get',
        params: query
    });
}

// 获取客服聊天记录
export function getCustomerServiceChatRecord(currentPage: number, pageSize: number, userId: string) {
    return axios({
        url: `/web/im/chat/getCustomerServiceChatRecord/${currentPage}/${pageSize}`,
        method: 'get',
        params: { userId }
    });
}

// 客服发送消息
export function sendCustomerServiceMessage(message: any) {
    return axios({
        url: '/web/im/chat/sendCustomerServiceMessage',
        method: 'post',
        data: message
    });
}

// 清除客服未读消息数
export function clearCustomerServiceMessageCount(userId: string) {
    return axios({
        url: '/web/im/chat/clearMessageCount',
        method: 'get',
        params: {
            sendUid: userId, // 用户ID
            type: 0, // 私聊类型
            productId: null,
            groupChatId: null
        }
    });
}

// 获取AI客服开关状态
export function getAICustomerServiceStatus() {
    return axios({
        url: '/web/im/chat/getAICustomerServiceStatus',
        method: 'get'
    });
}

// 更新AI客服开关状态
export function updateAICustomerServiceStatus(enabled: boolean) {
    return axios({
        url: '/web/im/chat/updateAICustomerServiceStatus',
        method: 'post',
        data: { enabled }
    });
}

