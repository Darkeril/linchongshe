import type {AxiosRequestConfig, AxiosResponse} from 'axios';
import axios from 'axios';
import {Message, Modal} from '@arco-design/web-vue';
import {useUserStore} from '@/store';
import {getToken, getAccessToken} from '@/utils/auth';
import {tansParams} from "@/utils/common";
import errorCode from "@/utils/error-code";
import cache from '@/plugins/cache'

// 是否在接口错误时自动登出；默认开启，可在 .env 设置 VITE_APP_AUTO_LOGOUT_ON_ERROR=false 关闭
const autoLogoutOnError = import.meta.env.VITE_APP_AUTO_LOGOUT_ON_ERROR !== 'false';
// 防止重复触发 logout 的标志位
let isLoggingOut = false;
const triggerLogout = () => {
    if (!autoLogoutOnError) return;
    // 如果正在登出或已经在登录页，不重复触发
    if (isLoggingOut) return;
    const currentPath = window.location.pathname;
    if (currentPath.includes('/login')) {
        // 已经在登录页，只清除 token，不重定向
        const userStore = useUserStore();
        userStore.logoutCallBack();
        return;
    }
    isLoggingOut = true;
    const userStore = useUserStore();
    userStore.logout().finally(() => {
        isLoggingOut = false;
        // 如果不在登录页，才进行重定向
        if (!window.location.pathname.includes('/login')) {
            window.location.replace('/arco-admin/login');
        }
    });
};

export interface HttpResponse<T = unknown> {
    msg: string;
    code: number;
    data?: T;
    rows?: T;
}

// 基础路径：优先 .env 配置，其次走开发代理前缀 /dev-api，避免未配置导致 404
axios.defaults.baseURL = import.meta.env.VITE_APP_BASE_API || '/dev-api'

axios.interceptors.request.use(
    (config: AxiosRequestConfig) => {
        // let each request carry token
        // this example using the JWT token
        // Authorization is a custom headers key
        // please modify it according to the actual situation
        const token = getToken();
        if (!config.headers) {
            config.headers = {};
        }
        
        // 判断是否是公开接口（不需要 token 的接口）
        const isPublicApi = config.url && (
            config.url.includes('/websiteConfig') ||
            config.url.includes('/systemConfig') ||
            config.url.includes('/localConfig') ||
            config.url.includes('/adminLoginPromo') ||
            config.url.includes('/system/config/carousel') ||
            config.url.includes('/auth/login') ||
            config.url.includes('/login') ||
            config.url.includes('/getCodeImg') ||
            config.url.includes('/code')
        );
        // 精确判断是否在登录页面：路径必须是 /login 或 /arco-admin/login（排除 /logininfor 等）
        const {pathname} = window.location;
        // 使用正则表达式精确匹配 /login，排除 /logininfor 等路径
        const isOnLoginPage = pathname === '/login' || 
                              pathname === '/arco-admin/login' || 
                              pathname.endsWith('/login') ||
                              /\/login(\?|$|#)/.test(pathname);
        
        // 是否需要设置 token（某些请求可能设置了 isToken: false）
        const {isToken} = config.headers as any;
        // 是否需要防止数据重复提交
        const {repeatSubmit: isRepeatSubmit} = config.headers;
        
        // 恢复原来的 token 设置逻辑，但增加对登录页公开接口的特殊处理
        // 原来的逻辑：默认设置 token，除非 isToken 为 true
        // 在登录页面的公开接口，不设置 token
        // 统一在一个地方设置 token，避免重复和冲突
        const shouldSetToken = token && 
                               !(isOnLoginPage && isPublicApi) && 
                               (isToken !== true && isToken !== 'true');
        if (shouldSetToken) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        
        // 调试：检查 /system/logininfor 接口的 token 设置
        if (config.url && config.url.includes('/system/logininfor')) {
            const authHeader = config.headers.Authorization;
            const authHeaderPreview = authHeader ? `${String(authHeader).substring(0, 20)}...` : 'none';
            console.log('[Interceptor Debug]', {
                url: config.url,
                hasToken: !!token,
                tokenLength: token ? token.length : 0,
                isToken,
                isOnLoginPage,
                isPublicApi,
                hasAuthHeader: !!authHeader,
                authHeader: authHeaderPreview
            });
        }
        
        const accessToken = getAccessToken();
        if (accessToken) {
            // align with legacy: custom Accesstoken header
            // @ts-ignore
            config.headers.Accesstoken = accessToken;
        }
        // Arco列表接收分页参数转换
        if (config.params && config.params.current) {
            config.params.pageNum = config.params.current
        }
        // 修复：不要修改已经是 'asc' 或 'desc' 的值
        // 后端期望接收 'asc' 或 'desc'，而不是 'ascending' 或 'descending'
        if (config.params && config.params.isAsc) {
            const isAscValue = String(config.params.isAsc);
            // 如果已经是 'asc' 或 'desc'，不添加 'ing'
            if (isAscValue !== 'asc' && isAscValue !== 'desc') {
                config.params.isAsc = `${isAscValue}ing`;
            }
        }
        // get请求映射params参数
        if (config.method === 'get' && config.params) {
            let url = `${config.url}?${tansParams(config.params)}`;
            url = url.slice(0, -1);
            config.params = {};
            config.url = url;
        }
        if (config.params)
            // 检测重复提交
            if (!isRepeatSubmit && (config.method === 'post' || config.method === 'put')) {
                const requestObj = {
                    url: config.url,
                    data: typeof config.data === 'object' ? JSON.stringify(config.data) : config.data,
                    time: new Date().getTime(),
                };
                const sessionObj = cache.session.getJSON('sessionObj');
                if (sessionObj === undefined || sessionObj === null || sessionObj === '') {
                    cache.session.setJSON('sessionObj', requestObj);
                } else {
                    const sUrl = sessionObj.url;                  // 请求地址
                    const sData = sessionObj.data;                // 请求数据
                    const sTime = sessionObj.time;                // 请求时间
                    const interval = 1000;                         // 间隔时间(ms)，小于此时间视为重复提交
                    if (sData === requestObj.data && requestObj.time - sTime < interval && sUrl === requestObj.url) {
                        const message = '数据正在处理，请勿重复提交';
                        console.warn(`[${sUrl}]: ${message}`);
                        return Promise.reject(new Error(message));
                    }
                    cache.session.setJSON('sessionObj', requestObj);
                }
            }
        return config;
    },
    (error) => {
        // do something
        return Promise.reject(error);
    }
);
// add response interceptors
axios.interceptors.response.use(
    (res: AxiosResponse<HttpResponse>) => {
        // 未设置状态码则默认成功状态
        const code = res.data.code || 200;
        // 获取错误信息
        // @ts-ignore
        const msg = errorCode[code] || res.data.msg || errorCode.default
        // 二进制数据则直接返回
        if (res.request.responseType === 'blob' || res.request.responseType === 'arraybuffer') {
            return res.data
        }
        // success code
        if (code === 200) {
            return res.data;
        }

        // 检查是否是验证码相关错误（用于登录接口，不显示拦截器的错误提示）
        const errorMsg = (msg || '').toLowerCase();
        const isCaptchaError = errorMsg.includes('验证码') && 
                              (errorMsg.includes('已时效') || 
                               errorMsg.includes('过期') ||
                               errorMsg.includes('失效') ||
                               errorMsg.includes('expired') ||
                               errorMsg.includes('invalid'));
        const isLoginUrl = res.config.url && (
            res.config.url.includes('/auth/login') || 
            res.config.url.includes('/login')
        );
        // 检查是否是公开接口（在登录页面不应该显示 token 过期错误）
        const isPublicApi = res.config.url && (
            res.config.url.includes('/websiteConfig') ||
            res.config.url.includes('/systemConfig') ||
            res.config.url.includes('/localConfig') ||
            res.config.url.includes('/adminLoginPromo') ||
            res.config.url.includes('/system/config/carousel') ||
            res.config.url.includes('/getCodeImg') ||
            res.config.url.includes('/code')
        );
        // 精确判断是否在登录页面：路径必须是 /login 或 /arco-admin/login（排除 /logininfor 等）
        const {pathname} = window.location;
        // 使用正则表达式精确匹配 /login，排除 /logininfor 等路径
        const isOnLoginPage = pathname === '/login' || 
                              pathname === '/arco-admin/login' || 
                              pathname.endsWith('/login') ||
                              /\/login(\?|$|#)/.test(pathname);
        // 在登录页面的公开接口，不显示 token 相关错误
        const shouldSkipMessage = (isCaptchaError && isLoginUrl) || (isOnLoginPage && isPublicApi);

        // handle specific codes similar to legacy project
        // 401 需要退出；501 仅业务/权限异常，不退出
        if (code === 401) {
            if (!shouldSkipMessage) {
                Message.error({ content: msg, duration: 5 * 1000 });
            }
            const error: any = new Error(msg || 'Unauthorized');
            error.response = res; // 保留原始响应，让组件知道错误已经被处理
            // 在登录页面的公开接口，不触发 logout，避免循环重定向
            if (!(isOnLoginPage && isPublicApi)) {
                triggerLogout();
            }
            return Promise.reject(error);
        }
        if (code === 501) {
            if (!shouldSkipMessage) {
                Message.error({ content: msg, duration: 5 * 1000 });
            }
            const error: any = new Error(msg || 'Token expired');
            error.response = res; // 保留原始响应，让组件知道错误已经被处理
            // 在登录页面的公开接口，不触发 logout，避免循环重定向
            if (!(isOnLoginPage && isPublicApi)) {
                triggerLogout();
            }
            return Promise.reject(error);
        }

        if (code === 500) {
            // 检查是否是 JWT 相关错误
            const isJwtError = errorMsg.includes('jwt') || 
                              errorMsg.includes('token') ||
                              errorMsg.includes('signature') ||
                              errorMsg.includes('expired') ||
                              errorMsg.includes('过期') ||
                              errorMsg.includes('validity') ||
                              errorMsg.includes('cannot be asserted') ||
                              errorMsg.includes('should not be trusted');
            
            if (isJwtError) {
                // JWT 相关错误，显示友好的提示并退出登录
                if (!shouldSkipMessage) {
                    Message.error({ 
                        content: '登录已过期，请重新登录', 
                        duration: 5 * 1000 
                    });
                }
                const error: any = new Error('登录已过期，请重新登录');
                error.response = res;
                // 在登录页面的公开接口，不触发 logout，避免循环重定向
                if (!(isOnLoginPage && isPublicApi)) {
                    triggerLogout();
                }
                return Promise.reject(error);
            }
            
            // 其他 500 错误正常显示
            if (!shouldSkipMessage) {
                Message.error({ content: msg, duration: 5 * 1000 });
            }
            const error: any = new Error(msg);
            error.response = res; // 保留原始响应，让组件知道错误已经被处理
            return Promise.reject(error);
        }
        if (code === 601) {
            if (!shouldSkipMessage) {
                Message.warning({ content: msg, duration: 5 * 1000 });
            }
            const error: any = new Error(msg);
            error.response = res; // 保留原始响应，让组件知道错误已经被处理
            return Promise.reject(error);
        }

        // fallback
        if (!shouldSkipMessage) {
            Message.error({
                content: msg || 'Error',
                duration: 5 * 1000,
            });
        }
        
        // 508: Illegal token; 512: Other clients logged in; 514: Token expired;
        if (
            [508, 512, 514, 401, 501].includes(code) &&
            res.config.url !== '/api/user/info'
        ) {
            triggerLogout();
        }
        const error: any = new Error(msg || 'Error');
        error.response = res; // 保留原始响应，让组件知道错误已经被处理
        return Promise.reject(error);
    },
    (error) => {
        // 如果错误有 response.data，说明是业务错误，已经在成功回调中处理过了
        // 这里只处理网络错误等没有 response.data 的情况
        if (!error?.response?.data) {
            // 检查错误信息中是否包含 JWT 相关关键词
            const errorMsg = (error.msg || error.message || '').toLowerCase();
            const isJwtError = errorMsg.includes('jwt') || 
                              errorMsg.includes('token') ||
                              errorMsg.includes('signature') ||
                              errorMsg.includes('expired') ||
                              errorMsg.includes('过期') ||
                              errorMsg.includes('validity') ||
                              errorMsg.includes('cannot be asserted') ||
                              errorMsg.includes('should not be trusted');
            
            if (isJwtError) {
                // JWT 相关错误，显示友好的提示并退出登录
                Message.error({
                    content: '登录已过期，请重新登录',
                    duration: 5 * 1000,
                });
                triggerLogout();
            } else {
                Message.error({
                    content: error.msg || error.message || 'Request Error',
                    duration: 5 * 1000,
                });
            }
        } else {
            // 如果有 response.data，检查是否是 JWT 相关错误
            const responseData = error.response.data;
            const errorMsg = (responseData.msg || responseData.message || '').toLowerCase();
            const isJwtError = errorMsg.includes('jwt') || 
                              errorMsg.includes('token') ||
                              errorMsg.includes('signature') ||
                              errorMsg.includes('expired') ||
                              errorMsg.includes('过期') ||
                              errorMsg.includes('validity') ||
                              errorMsg.includes('cannot be asserted') ||
                              errorMsg.includes('should not be trusted');
            
            if (isJwtError && error.response.status !== 401 && error.response.status !== 501) {
                // JWT 相关错误（非 401/501，这些已经在上面处理了），显示友好的提示并退出登录
                Message.error({
                    content: '登录已过期，请重新登录',
                    duration: 5 * 1000,
                });
                triggerLogout();
            }
        }
        return Promise.reject(error);
    }
);

export default axios;
