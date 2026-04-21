import request from "@/utils/request";

export const getAllConfig = () => {
  return request<any>({
    url: `/web/system/config/allConfig`,
    method: "get",
  });
};

export const getSystemConfig = () => {
  return request<any>({
    url: `/web/system/config/systemConfig`,
    method: "get",
  });
};

export const getWebsiteConfig = () => {
  return request<any>({
    url: `/web/system/config/websiteConfig`,
    method: "get",
  });
};

export const getCaptchaConfig = () => {
  return request<any>({
    url: `/web/system/config/captchaConfig`,
    method: "get",
  });
};

export const getDemoAccountConfig = () => {
  return request<any>({
    url: `/web/system/config/demoAccountConfig`,
    method: "get",
  });
};

export const getWeChatLoginConfig = () => {
  return request<any>({
    url: `/web/system/config/wechatLogin`,
    method: "get",
  });
};

export const getDemoSiteConfig = () => {
  return request<any>({
    url: `/web/system/config/demoSiteConfig`,
    method: "get",
  });
};
