import axios from 'axios';

export interface NoticeRecord {
    createBy?: string;
    createTime?: string;
    updateBy?: string;
    updateTime?: any;
    remark?: string;
    noticeId?: number;
    noticeTitle?: string;
    noticeType?: string;
    noticeContent?: string;
    status?: string;
}

export interface NoticeTopRecord {
    noticeId: number;
    noticeTitle?: string;
    noticeType?: string;
    noticeContent?: string;
    createBy?: string;
    createTime?: string;
    isRead?: boolean;
}

export interface NoticeListTopBody {
    data?: NoticeTopRecord[];
    unreadCount?: number;
}

// 查询公告列表
export function listNotice(query: any) {
    return axios({
        url: '/system/notice/list',
        method: 'get',
        params: query
    })
}

// 查询公告详细
export function getNotice(noticeId: number) {
    return axios({
        url: `/system/notice/${noticeId}`,
        method: 'get'
    })
}

// 新增公告
export function addNotice(data: NoticeRecord) {
    return axios({
        url: '/system/notice',
        method: 'post',
        data
    })
}

// 修改公告
export function updateNotice(data: NoticeRecord) {
    return axios({
        url: '/system/notice',
        method: 'put',
        data
    })
}

// 删除公告
export function delNotice(noticeId: number) {
    return axios({
        url: `/system/notice/${noticeId}`,
        method: 'delete'
    })
}

export function listNoticeTop() {
    return axios.get<NoticeListTopBody>('/system/notice/listTop')
}

export function markNoticeRead(noticeId: number) {
    return axios({
        url: '/system/notice/markRead',
        method: 'post',
        params: { noticeId },
    })
}

export function markNoticeReadAll(ids: string) {
    return axios({
        url: '/system/notice/markReadAll',
        method: 'post',
        params: { ids },
    })
}
