import axios from 'axios';

export interface NavbarRecord {
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
export function listNavbar(query: any) {
    return axios({
        url: '/web/navbar/list',
        method: 'get',
        params: query
    })
}

// 查询导航栏列表（排除节点）
export function listNavbarExcludeChild(id: number) {
    return axios({
        url: `/web/navbar/treeSelect/${id}`,
        method: 'get'
    })
}

// 查询导航栏详细
export function getNavbar(id: number) {
    return axios({
        url: `/web/navbar/${id}`,
        method: 'get'
    })
}

// 新增导航栏
export function addNavbar(data: NavbarRecord) {
    return axios({
        url: '/web/navbar',
        method: 'post',
        data,
    })
}

// 修改导航栏
export function updateNavbar(data: NavbarRecord) {
    return axios({
        url: '/web/navbar/update',
        method: 'put',
        data,
    })
}

// 删除导航栏
export function delNavbar(id: number) {
    return axios({
        url: `/web/navbar/${id}`,
        method: 'delete'
    })
}

// 更新导航栏状态
export function updateNavbarStatus(id: number | string, status: string) {
    return axios({
        url: '/web/navbar/updateStatus',
        method: 'post',
        params: { id, status }
    })
}