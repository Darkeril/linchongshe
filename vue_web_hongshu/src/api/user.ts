import request from "@/utils/request";
import type { UserLogin } from "@/type/user";

interface ProductQueryParams {
  currentPage: number;
  pageSize: number;
  uid: string;
  type: number;
  status?: string; // 新增状态参数
}

/**
 * 验证码类型
 */
export const getCaptchaType = () => {
  return request<any>({
    url: "/web/captcha/type", // mock接口
    method: "get",
  });
};

/**
 * 获取登录二维码
 */
export const getQrcodeUrl = () => {
  return request<any>({
    url: "/web/auth/qrcode", // mock接口
    method: "get",
  });
};

/**
 * 检查二维码状态
 */
export const checkQrCodeStatus = (loginToken: string) => {
  return request<any>({
    url: "/web/auth/qrcode/status",
    method: "get",
    params: {
      loginToken,
    },
  });
};

/**
 * 二维码登录
 */
export const qrCodeLogin = (userId: string) => {
  return request<{
    code: number;
    message: string;
    data: {
      accessToken: string;
      refreshToken: string;
      userInfo: any;
      success: boolean;
      message: string;
    };
  }>({
    url: "/web/auth/qrcode/login",
    method: "post",
    params: {
      userId,
    },
  });
};

/**
 * 
 * @param deptId 
 * @param file 
 * @returns 
 */
export function importFile(deptId: number, file: File) {
  const formData = new FormData();
  formData.append("file", file);
  return request({
    url: "/api/v1/users/_import",
    method: "post",
    params: { deptId: deptId },
    data: formData,
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
}

/**
 * 
 * @param accessToken 
 * @returns 
 */
export const getUserInfoByToken = (accessToken: string) => {
  return request<any>({
    url: "/web/auth/getUserInfoByToken", // mock接口
    method: "get",
    params: {
      accessToken,
    },
  });
};

/**
 * 
 * @param refreshToken 
 * @returns 
 */
export const refreshToken = (refreshToken: string) => {
  return request<any>({
    url: `/web/auth/refreshToken`, // mock接口
    method: "get",
    params: {
      refreshToken,
    },
  });
};

// 发送验证码
export function sendVerifyCode(phone: string, captchaVerification: string) {
  return request({
    url: '/web/auth/sendCode',
    method: 'post',
    params: {
      phone,
      captchaVerification
    },
  })
}

/**
 * 
 * @param data 
 * @returns 
 */
export const login = (data: any) => {
  return request<any>({
    url: "/web/auth/login", // mock接口
    method: "post",
    data
  });
};

/**
 * 
 * @param data 
 * @returns 
 */
export const demoLogin = (data: any) => {
  return request<any>({
    url: "/web/auth/demoLogin", // mock接口
    method: "post",
    data
  });
};

/**
 * 验证码登录
 * 
 * @param data 
 * @returns 
 */
export const loginByCode = (data: UserLogin) => {
  return request<any>({
    url: "/web/auth/loginByCode", // mock接口
    method: "post",
    data
  });
};

/**
 * 
 * @param currentPage 
 * @param pageSize 
 * @param userId 
 * @param type 
 * @returns 
 */
export const getTrendByUser = (currentPage: number, pageSize: number, userId: string, type: number, status: string) => {
  return request<any>({
    url: `/web/user/getTrendByUser/${currentPage}/${pageSize}`, // mock接口
    method: "get",
    params: {
      userId,
      type,
      status
    },
  });
};

export const getProductByUser = (params: ProductQueryParams) => {
  return request<any>({
    url: `/web/user/getProductByUser/${params.currentPage}/${params.pageSize}`,
    method: "get",
    params: {
      userId: params.uid,
      type: params.type,
      status: params.status // 添加状态参数
    },
  });
};

/**
 * 
 * @param userId 
 * @returns 
 */
export const getUserById = (userId: string) => {
  return request<any>({
    url: `/web/user/getUserById`, // mock接口
    method: "get",
    params: {
      userId
    },
  });
};

/**
 * 
 * @param userId 
 * @returns 
 */
export const loginOut = (userId: string) => {
  return request<any>({
    url: `/web/auth/loginOut`, // mock接口
    method: "get",
    params: {
      userId
    },
  });
};

export const updateUserPassword = (data: any) => {
  return request<any>({
    url: "/web/auth/updatePassword", // mock接口
    method: "post",
    data,
  });
};

export const updateUser = async (formData: FormData) => {
  return await request({
    url: '/web/user/updateUser',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export const getUserByKeyword = (currentPage: number, pageSize: number, keyword: string) => {
  return request<any>({
    url: `/web/user/getUserByKeyword/${currentPage}/${pageSize}`, // mock接口
    method: "get",
    params: {
      keyword
    },
  });
};


export const getAddressList = (uid: string) => {
  return request<any>({
    url: `/idle/address/addressList`, // mock接口
    method: "get",
    params: {
      uid,
    },
  });
};

export const saveAddress = (data: any) => {
  return request<any>({
    url: "/idle/address/saveAddress", // mock接口
    method: "post",
    data,
  });
};

export const deleteAddress = (data: any) => {
  return request<any>({
    url: "/idle/address/deleteAddress", // mock接口
    method: "post",
    data,
  });
};

export const getCollectCount = (userId: string) => {
  return request<any>({
    url: `/web/user/collectCount`,
    method: 'get',
    params: {
      userId
    }
  });
}

export function getUserProducts(params: {
  page: number
  pageSize: number
  keyword?: string
  userId: string
}) {
  return request({
    url: '/web/user/getUserProducts',
    method: 'get',
    params: {
      pageNum: params.page,
      pageSize: params.pageSize,
      title: params.keyword,
      uid: params.userId
    }
  })
}

/**
 * 生成微信网页授权URL
 * @param redirectUri 回调地址（可选）
 * @returns 
 */
export const generateWeChatAuthUrl = (redirectUri?: string) => {
  return request<{
    code: number;
    message: string;
    data: string; // 授权URL
  }>({
    url: "/web/auth/wechat/authUrl",
    method: "get",
    params: redirectUri ? { redirectUri } : {},
  });
};

/**
 * 通过临时token获取微信登录信息
 * @param tempToken 临时token
 * @returns 
 */
export const getWeChatLoginInfo = (tempToken: string) => {
  return request<{
    code: number;
    message: string;
    data: {
      accessToken: string;
      refreshToken: string;
      userInfo: any;
    };
  }>({
    url: "/web/auth/wechat/getLoginInfo",
    method: "get",
    params: {
      tempToken,
    },
  });
};