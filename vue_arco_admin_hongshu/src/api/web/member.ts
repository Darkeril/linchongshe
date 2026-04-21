import axios from 'axios';

export interface MemberRecord {
    searchValue?: any;
    createBy?: any;
    createTime?: any;
    updateBy?: any;
    updateTime?: any;
    remark?: any;
    id: number | undefined;
    pid?: any;
    ancestors?: any;
    title?: string;
    sort?: any;
    normalCover?: string;
    phone?: any;
    email?: any;
    status?: any;
    delFlag?: any;
    parentName?: any;
    children?: any[];
}

// 查询会员列表
export function listMember(query: any) {
    return axios({
        url: '/web/member/list',
        method: 'get',
        params: query
    })
}

// 查询会员详细
export function getMember(id: number) {
    return axios({
        url: `/web/member/${id}`,
        method: 'get'
    })
}

// 新增会员
export function addMember(data: MemberRecord) {
    return axios({
        url: '/web/member',
        method: 'post',
        data,
        headers: { "Content-Type": "multipart/form-data;boundary=----WebKitFormBoundaryk4ZvuPo6pkphe7Pl" },
    })
}

// 修改会员
export function updateMember(data: MemberRecord) {
    return axios({
        url: '/web/member/update',
        method: 'post',
        data,
        headers: { "Content-Type": "multipart/form-data;boundary=----WebKitFormBoundaryk4ZvuPo6pkphe7Pl" },
    })
}

// 删除会员
export function delMember(id: string) {
    return axios({
        url: `/web/member/${id}`,
        method: 'delete'
    })
}

// 清空会员
export function delAllMember() {
    return axios({
        url: `/web/member/delAllMember`,
        method: 'delete'
    })
}

// 批量禁用会员（与项目1一致）
export function disableMember(id: string) {
    return axios({
        url: `/web/member/disable/${id}`,
        method: 'post'
    })
}

// 更新会员状态（启用/禁用开关）
export function updateMemberStatus(id: string | number, status: string) {
    return axios({
        url: `/web/member/updateStatus`,
        method: 'post',
        params: { id, status }
    })
}

/** 待审核用户：审核通过 */
export function approveMemberAudit(id: number) {
    return axios({
        url: `/web/member/audit/approve/${id}`,
        method: 'post'
    })
}

/** 待审核用户：审核拒绝（账号禁用） */
export function rejectMemberAudit(id: number) {
    return axios({
        url: `/web/member/audit/reject/${id}`,
        method: 'post'
    })
}
