import axios, { InternalAxiosRequestConfig, AxiosResponse } from "axios";
import { useUserStore } from "@/store/userStore";
import { storage } from "./storage";
import { ElMessage } from "element-plus";
// 刷新 token 后, 将缓存的接口重新请求一次
// 是否正在刷新 token
let isRefreshing: boolean = false;

// 存储待重发的请求
let requestsQueue: ((token: string) => any)[] = [];

// 创建 axios 实例
const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API,
  // baseURL: baseURL,
  timeout: 50000,
  headers: { "Content-Type": "application/json;charset=utf-8" },
});

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const userStore = useUserStore();
    if (userStore.getToken()) {
      const token = userStore.getToken();
      // 网关期望使用 Authorization header
      config.headers.Authorization = `Bearer ${token}`;
      // 保留 accessToken 用于兼容其他接口
      config.headers.accessToken = token;
      const userInfo = userStore.getUserInfo();
      if (userInfo && userInfo.id) {
        config.headers.userId = userInfo.id;
      }
    }
    // 如果是 FormData，删除默认的 Content-Type，让浏览器自动设置（包含 boundary）
    if (config.data instanceof FormData) {
      config.headers['Content-Type'] = undefined as any;
    }
    return config;
  },
  (error: any) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const { code, msg } = response.data;
    if (code === 200) {
      return response.data;
    }
    if (code === 517) {
      return Promise.resolve(response.data);
    }
    // 响应数据为二进制流处理(Excel导出)
    // if (response.data instanceof ArrayBuffer) {
    //   return response;
    // }
    const config = response.config;

    const userStore = useUserStore();
    if (code === 501) {
      // 无感刷新Token
      if (!isRefreshing) {
        isRefreshing = true;
        storage.set("accessToken", "");
        const refreshToken = storage.get("refreshToken") as string;
        console.log("refreshToken", refreshToken);
        return userStore
          .getNewToken(refreshToken)
          .then(async (rftRes) => {
            console.log("rftRes", rftRes);
            const { accessToken, refreshToken } = rftRes.data;
            storage.set("accessToken", accessToken);
            storage.set("refreshToken", refreshToken);
            config.headers.Authorization = accessToken;
            config.headers.accessToken = accessToken;
            // 重新请求一下第一个 501 的接口
            const firstReqRes = await service.request(config);
            // token 刷新后将数组的方法重新执行
            requestsQueue.forEach((cb: any) => cb(accessToken));
            // 队列中的请求执行完毕后，清空数组
            requestsQueue = [];
            return firstReqRes;
          }).finally(() => {
            isRefreshing = false;
          });
      } else {
        // 如果正在 refreshToken

        // 如果有多个请求同时发起，第一个请求 401 了，refreshToken 又正在进行中
        // 那么把第一个以外的请求暂存起来
        return new Promise((resolve) => {
          // 用函数形式将 resolve 存入，等待 refreshToken 完成后再执行
          requestsQueue.push((token: string) => {
            config.headers.Authorization = token;
            resolve(service.request(config));
          });
        });
      }
    } else if (code == 401) {
      ElMessage.error("登录过期，请重新登录");
      // 只清除web端自己的token，避免影响其他项目（admin、uniapp）
      storage.remove("accessToken");
      storage.remove("refreshToken");
      storage.remove("userInfo");
    } else if (code === 516) {
      ElMessage.error(msg || "账号待审核，请等待管理员通过后再登录");
      storage.remove("accessToken");
      storage.remove("refreshToken");
      storage.remove("userInfo");
    } else {
      // 处理其他错误码（400、500 等）
      if (msg) {
        ElMessage.error(msg);
      }
    }
    return Promise.reject(response.data);
  },
  (error: any) => {
    // 处理 HTTP 错误状态码（400、500 等）
    if (error.response) {
      const { status, data } = error.response;
      
      // 如果后端返回了错误消息，使用后端的消息
      if (data && data.msg) {
        ElMessage.error(data.msg);
      } else {
        // 否则使用默认错误消息
        switch (status) {
          case 400:
            ElMessage.error('请求参数错误');
            break;
          case 401:
            ElMessage.error('未授权，请重新登录');
            break;
          case 403:
            ElMessage.error('拒绝访问');
            break;
          case 404:
            ElMessage.error('请求的资源不存在');
            break;
          case 500:
            ElMessage.error('服务器内部错误');
            break;
          default:
            ElMessage.error('网络错误');
        }
      }
    } else {
      ElMessage.error('网络连接失败');
    }
    return Promise.reject(error);
  }
);

// 导出 axios 实例
export default service;
