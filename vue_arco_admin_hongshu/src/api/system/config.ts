import { request } from '@/utils/request';

// 系统配置相关接口

// 获取所有配置
export function getAllConfig() {
  return request({
    url: '/web/system/config/allConfig',
    method: 'get',
  });
}

// 获取网站配置
export function getWebsiteConfig() {
  return request({
    url: '/web/system/config/websiteConfig',
    method: 'get',
  });
}

// 获取系统配置
export function getSystemConfig() {
  return request({
    url: '/web/system/config/system',
    method: 'get',
  });
}

// 获取黑名单配置
export function getBlacklistConfig() {
  return request({
    url: '/web/system/config/blacklist',
    method: 'get',
  });
}

// 编辑网站配置
export function editWebsiteConfig(data: any) {
  return request({
    url: '/web/system/config/website',
    method: 'post',
    data,
  });
}

// 编辑系统配置
export function editSystemConfig(data: any) {
  return request({
    url: '/web/system/config/system',
    method: 'post',
    data,
  });
}

// 编辑本地存储配置
export function editLocalConfig(data: any) {
  return request({
    url: '/web/system/config/local',
    method: 'post',
    data,
  });
}

// 获取支付宝配置
export function getAlipayConfig() {
  return request({
    url: '/idle/config/alipayConfig',
    method: 'get',
  });
}

// 编辑支付宝配置
export function editAlipayConfig(data: any) {
  return request({
    url: '/idle/config/alipayConfig',
    method: 'post',
    data,
  });
}

// 获取微信支付配置
export function getWeChatPayConfig() {
  return request({
    url: '/idle/config/wechatPayConfig',
    method: 'get',
  });
}

// 编辑微信支付配置
export function editWeChatPayConfig(data: any) {
  return request({
    url: '/idle/config/wechatPayConfig',
    method: 'post',
    data,
  });
}

// 获取支付全局配置
export function getPaymentGlobalConfig() {
  return request({
    url: '/idle/config/paymentGlobalConfig',
    method: 'get',
  });
}

// 编辑支付全局配置
export function editPaymentGlobalConfig(data: any) {
  return request({
    url: '/idle/config/paymentGlobalConfig',
    method: 'post',
    data,
  });
}

// 编辑微信登录配置
export function editWeChatLoginConfig(data: any) {
  return request({
    url: '/web/system/config/wechatLogin',
    method: 'post',
    data,
  });
}

// 获取微信登录配置
export function getWeChatLoginConfig() {
  return request({
    url: '/web/system/config/wechatLogin',
    method: 'get',
  });
}

// 获取 IM 微信登录配置
export function getImWeChatLoginConfig() {
  return request({
    url: '/web/system/config/imWechatLogin',
    method: 'get',
  });
}

// 编辑 IM 微信登录配置
export function editImWeChatLoginConfig(data: any) {
  return request({
    url: '/web/system/config/imWechatLogin',
    method: 'post',
    data,
  });
}

// 获取自动审核配置（百度千帆）
export function getAutoAuditConfig() {
  return request({
    url: '/web/system/config/autoAuditConfig',
    method: 'get',
  });
}

// 编辑自动审核配置（百度千帆）
export function editAutoAuditConfig(data: any) {
  return request({
    url: '/web/system/config/autoAudit',
    method: 'post',
    data,
  });
}

// 编辑短信配置
export function editSmsConfig(data: any) {
  return request({
    url: '/web/system/config/sms',
    method: 'post',
    data,
  });
}

// 编辑验证码配置
export function editCaptchaConfig(data: any) {
  return request({
    url: '/web/system/config/captcha',
    method: 'post',
    data,
  });
}

// 编辑OSS配置
export function editOssConfig(data: any) {
  return request({
    url: '/web/system/config/oss',
    method: 'post',
    data,
  });
}

// 编辑高德地图配置
export function editAmapConfig(data: any) {
  return request({
    url: '/web/system/config/amap',
    method: 'post',
    data,
  });
}

// 编辑演示账号配置
export function editDemoAccountConfig(data: any) {
  return request({
    url: '/web/system/config/demoAccount',
    method: 'post',
    data,
  });
}

// 编辑演示站配置
export function editDemoSiteConfig(data: any) {
  return request({
    url: '/web/system/config/demoSite',
    method: 'post',
    data,
  });
}

// 添加黑名单项
export function addBlacklistItem(data: any) {
  return request({
    url: '/web/system/config/blacklist/add',
    method: 'post',
    data,
  });
}

// 更新黑名单启用状态
export function updateBlacklistEnabled(enabled: boolean) {
  return request({
    url: '/web/system/config/blacklist/enabled',
    method: 'post',
    data: { enabled },
  });
}

// 解封黑名单项
export function unbanBlacklistItem(id: string | number) {
  return request({
    url: `/web/system/config/blacklist/${id}/unban`,
    method: 'post',
  });
}

// 删除黑名单项
export function deleteBlacklistItem(id: string | number) {
  return request({
    url: `/web/system/config/blacklist/${id}`,
    method: 'delete',
  });
}

// 获取自动封禁配置
export function getAutobanConfig() {
  return request({
    url: '/web/system/config/blacklist/autoban',
    method: 'get',
  });
}

// 更新自动封禁配置
export function updateAutobanConfig(data: any) {
  return request({
    url: '/web/system/config/blacklist/autoban',
    method: 'post',
    data,
  });
}

// 批量上传文件
export function uploadBatchFiles(files: File[]) {
  const formData = new FormData();
  files.forEach((file) => {
    formData.append('uploadFiles', file);
  });

  return request({
    url: '/web/oss/uploadBatch',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
}

// 清理Redis缓存
export function cleanRedisByKey(data: any) {
  return request({
    url: '/web/system/config/cleanRedisByKey',
    method: 'post',
    data,
  });
}

// 获取爬虫配置
export function getSpiderConfig() {
  return request({
    url: '/web/system/config/spider',
    method: 'get',
  });
}

// 获取市集轮播图配置
export function getCarouselConfig() {
  return request({
    url: '/web/system/config/carousel',
    method: 'get',
  });
}

// 更新市集轮播图配置
export function editCarouselConfig(data: any[]) {
  return request({
    url: '/web/system/config/carousel',
    method: 'post',
    data,
  });
}

/** 管理端工作台右侧轮播图（需登录，与市集轮播数据结构一致） */
export function getWorkbenchCarouselConfig() {
  return request({
    url: '/web/system/config/carousel/workbench',
    method: 'get',
  });
}

export function editWorkbenchCarouselConfig(data: any[]) {
  return request({
    url: '/web/system/config/carousel/workbench',
    method: 'post',
    data,
  });
}

// 管理端登录页圆形展示区（免登录 GET，与 websiteConfig 一致供登录页使用）
export function getAdminLoginPromoConfig() {
  return request({
    url: '/web/system/config/adminLoginPromo',
    method: 'get',
  });
}

export function editAdminLoginPromoConfig(data: any[]) {
  return request({
    url: '/web/system/config/adminLoginPromo',
    method: 'post',
    data,
  });
}

// 编辑爬虫配置
export function editSpiderConfig(data: any) {
  return request({
    url: '/web/system/config/spider',
    method: 'post',
    data,
  });
}

// 获取单个配置
export function getConfig(id: number) {
  return request({
    url: `/web/system/config/${id}`,
    method: 'get',
  });
}

// 新增配置
export function addConfig(data: any) {
  return request({
    url: '/web/system/config',
    method: 'post',
    data,
  });
}

// 更新配置
export function updateConfig(data: any) {
  return request({
    url: '/web/system/config',
    method: 'put',
    data,
  });
}

// 系统通知相关接口
// 获取系统通知列表
export function getSystemNotificationList() {
  return request({
    url: '/web/system/notification/list',
    method: 'get',
  });
}

// 添加系统通知（显式序列化，避免部分环境下对 body 的二次处理导致非法 JSON）
export function addSystemNotification(data: any) {
  return request({
    url: '/web/system/notification',
    method: 'post',
    data: JSON.stringify(data ?? {}),
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    },
  });
}

// 更新系统通知
export function updateSystemNotification(data: any) {
  return request({
    url: '/web/system/notification',
    method: 'put',
    data: JSON.stringify(data ?? {}),
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    },
  });
}

// 删除系统通知
export function deleteSystemNotification(id: string | number) {
  return request({
    url: `/web/system/notification/${id}`,
    method: 'delete',
  });
}

// 推送系统通知
export function pushSystemNotification(id: string | number) {
  return request({
    url: `/web/system/notification/${id}/push`,
    method: 'post',
  });
}

// 待实现功能管理相关接口
// 获取待实现功能列表
export function getPlannedFeatureList() {
  return request({
    url: '/web/feedback/plannedFeatures/list',
    method: 'get',
  });
}

// 添加待实现功能
export function addPlannedFeature(data: any) {
  return request({
    url: '/web/feedback/plannedFeatures',
    method: 'post',
    data,
  });
}

// 更新待实现功能
export function updatePlannedFeature(data: any) {
  return request({
    url: '/web/feedback/plannedFeatures',
    method: 'put',
    data,
  });
}

// 删除待实现功能
export function deletePlannedFeature(id: number) {
  return request({
    url: `/web/feedback/plannedFeatures/${id}`,
    method: 'delete',
  });
}
