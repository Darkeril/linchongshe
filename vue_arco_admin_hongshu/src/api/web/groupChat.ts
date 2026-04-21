import axios from 'axios';

export interface GroupChatRecord {
    id?: string;
    groupName?: string;
    groupAvatar?: string;
    groupDescription?: string;
    ownerId?: string;
    ownerName?: string;
    ownerAvatar?: string;
    memberCount?: number;
    maxMemberCount?: number;
    status?: number;
    createTime?: string;
    lastMessage?: string;
    lastMessageTime?: string;
    unreadCount?: number;
}

export interface GroupChatMemberRecord {
    id?: string;
    userId?: string;
    username?: string;
    nickname?: string;
    avatar?: string;
    role?: number;
    groupNickname?: string;
    joinTime?: string;
}

// 查询群聊列表
export function listGroupChat(query: any) {
    return axios({
        url: '/web/chatGroup/list',
        method: 'get',
        params: query
    });
}

// 查询群聊详细
export function getGroupChat(id: string) {
    return axios({
        url: `/web/chatGroup/${id}`,
        method: 'get'
    });
}

// 获取群成员列表
export function getGroupMembers(groupId: string) {
    return axios({
        url: `/web/chatGroup/members/${groupId}`,
        method: 'get'
    });
}

// 删除/解散群聊
export function delGroupChat(ids: string[]) {
    return axios({
        url: `/web/chatGroup/${ids.join(',')}`,
        method: 'delete'
    });
}

// 移除群成员
export function removeMember(groupId: string, userId: string) {
    return axios({
        url: '/web/chatGroup/removeMember',
        method: 'post',
        params: {
            groupId,
            userId
        }
    });
}

