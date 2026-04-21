import axios from 'axios';

export interface NoteRecord {
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

// 查询导航栏列表
export function tagList(query: any) {
    return axios({
        url: '/web/tag/list',
        method: 'get',
        params: query
    })
}

// 查询导航栏详细
export function getTag(id: number) {
    return axios({
        url: `/web/tag/${id}`,
        method: 'get'
    })
}

// 新增标签
export function addTag(data: NoteRecord) {
    return axios({
        url: '/web/tag',
        method: 'post',
        data,
    })
}

// 修改标签
export function updateTag(data: NoteRecord) {
    return axios({
        url: '/web/tag',
        method: 'put',
        data,
    })
}

// 删除标签（支持单个ID或逗号分隔的ID字符串）
export function delTag(id: number | string) {
    return axios({
        url: `/web/tag/${id}`,
        method: 'delete'
    })
}
