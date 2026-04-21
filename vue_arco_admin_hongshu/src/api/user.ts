import axios from 'axios';
import {RouteRecordNormalized} from "vue-router";
import {UserAction} from "@/store/modules/user/types";

export interface LoginData {
    username: string;
    password: string;
    code: string;
    uuid: string;
}

export interface LoginRes {
    code: number;
    msg: string | null;
    data: {
        access_token: string;
        expires_in: number;
    };
}


export function login(data: LoginData) {
    return axios({
        method: 'post',
        url: '/auth/login',
        data
    });
}

export function logout() {
    return axios({
        method: 'delete',
        url: '/auth/logout',
    });
}

export function getUserInfo() {
    return axios.get<UserAction>('/system/user/getInfo');
}

export function getMenuList() {
    // 对齐项目1：/system/menu/getRouters
    return axios.get<RouteRecordNormalized[]>('/system/menu/getRouters');
}

//
// export function getMenuList() {
//   return axios({
//     url: '/getRouters',
//     method: 'get'
//   });
// }
