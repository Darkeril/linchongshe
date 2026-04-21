import request from "@/utils/request";
import { EsProductDTO } from "@/type/product"

/**
 * 
 * @param currentPage 
 * @param pageSize 
 * @returns 
 */
export const getRecommendProduct = (currentPage: number, pageSize: number) => {
  return request<any>({
    url: `/idle/es/product/recommendProduct/${currentPage}/${pageSize}`, // mock接口
    // url: `/idle/api/recommend/product/lightweight/products/${currentPage}/${pageSize}`, // mock接口
    method: "get",
  });
};

/**
 * 获取视频商品
 * @param currentPage 当前页
 * @param pageSize 每页数量
 * @returns
 */
export const getVideoProduct = (currentPage: number, pageSize: number) => {
  return request<any>({
    url: `/idle/es/product/getVideoProduct/${currentPage}/${pageSize}`,
    method: "get",
  });
};

/**
 * 
 * @param currentPage 
 * @param pageSize 
 * @param data 
 * @returns 
 */
export const getProduct = (currentPage: number, pageSize: number, data: EsProductDTO) => {
  return request<any>({
    url: `/idle/product/getProduct/${currentPage}/${pageSize}`, // mock接口
    method: "post",
    data: data
  });
};

export const getProductByKeyword = (currentPage: number, pageSize: number, keyword: string) => {
  return request<any>({
    url: `/idle/product/getProductByKeyword/${currentPage}/${pageSize}`, // mock接口
    method: "get",
    params: {
      keyword
    },
  });
};

/**
 * 根据产品id获取商品
 * @param productId 商品id
 * @returns
 */
export const getProductById = (productId: string) => {
  return request<any>({
    url: "/idle/product/getProductById", // mock接口
    method: "get",
    params: {
      productId,
    },
  });
};

/**
 * 置顶商品
 * @param productId 
 * @returns 
 */
export const pinnedProduct = (productId: string) => {
  return request<any>({
    url: "/idle/product/pinnedProduct", // mock接口
    method: "get",
    params: {
      productId,
    },
  });
};

export const updateOrderStatus = (paymentOrderId: string) => {
  return request<any>({
    url: "/idle/payment/pay/updateOrderStatus", // mock接口
    method: "post",
    params: {
      paymentOrderId,
    },
  });
};

export const updateProductStatus = (productId: string) => {
  return request<any>({
    url: "/idle/product/order/updateProductStatus", // mock接口
    method: "post",
    params: {
      productId,
    },
  });
};

/**
 * 删除商品
 * @param data 
 * @returns 
 */
export const deleteProductByIds = (data: any) => {
  return request<any>({
    url: "/idle/product/deleteProductByIds", // mock接口
    method: "post",
    data: data,
  });
};

export const createProductOrder = (data: any) => {
  return request<any>({
    url: "/idle/product/order/createOrder", // mock接口
    method: "post",
    data: data,
  });
};

export const createPaymentOrder = (productOrderId: string, payType: string = '2') => {
  return request<any>({
    url: "/idle/payment/order/createPayment", // mock接口
    method: "get",
    params: {
      productOrderId,
      payType, // 1-微信支付，2-支付宝支付
    },
  });
};

export const getPaymentOrder = (paymentOrderId: string) => {
  return request<any>({
    url: "/idle/payment/order/getPaymentOrder", // mock接口
    method: "get",
    params: {
      paymentOrderId,
    },
  });
};

export const createPayOrder = (paymentOrderId: string) => {
  return request<any>({
    url: "/idle/payment/pay/createPaymentPay", // mock接口
    method: "get",
    params: {
      paymentOrderId,
    },
  });
};

export const getProductOrderDetail = (orderNumber: string) => {
  return request<any>({
    url: "/idle/product/order/detail",
    method: "get",
    params: {
      orderNumber,
    },
  });
};

export const payOrder = (paymentPayId: string, paymentMethod?: string) => {
  return request<any>({
    url: "/idle/payment/pay/finish",
    method: "post",
    params: {
      paymentPayId,
      ...(paymentMethod && { paymentMethod }),
    },
  });
};

export const cancelPay = (paymentPayId: string) => {
  return request<any>({
    url: "/idle/payment/pay/cancel", // mock接口
    method: "post",
    params: {
      paymentPayId,
    },
  });
};

/**
 * 保存商品（使用文件URL）
 * @param requestId 请求ID
 * @param productDTO 商品实体（包含文件URL）
 * @returns 商品id
 */
export const saveProduct = (requestId: string, productDTO: any) => {
  return request<any>({
    url: "/idle/product/saveProduct",
    method: "post",
    params: { requestId },
    data: productDTO,
  });
};

/**
 * 保存商品（使用文件，已废弃，保留用于兼容）
 * @param data 商品实体
 * @returns 商品id
 */
export const saveIdleByDTO = (data: any) => {
  return request<any>({
    url: "/idle/product/saveProductByDTO", // mock接口
    method: "post",
    data: data,
    headers: { "Content-Type": "multipart/form-data;boundary=----WebKitFormBoundaryk4ZvuPo6pkphe7Pl" },
  });
};

/**
 * 更新笔记
 * @param data 笔记实体
 * @returns 笔记id
 */
export const updateIdleByDTO = (data: any) => {
  return request<any>({
    url: "/idle/product/updateProductByDTO", // mock接口
    method: "post",
    data: data,
    headers: { "Content-Type": "multipart/form-data;boundary=----WebKitFormBoundaryk4ZvuPo6pkphe7Pl" },
  });
};

export const getProductOrderInfo = (paymentOrderId: string) => {
  return request<any>({
    url: "/idle/payment/order/getPaymentOrder", // mock接口
    method: "get",
    params: {
      paymentOrderId,
    },
  });
};

/** 订单评价（与后端 PutMapping("evaluate") 对齐；接口未启用时会失败） */
export const evaluateOrder = (data: any) => {
  return request<any>({
    url: "/idle/product/order/evaluate",
    method: "put",
    data: data,
  });
};

export const getIdleCount = (userId: string) => {
  return request<any>({
    url: `/idle/product/idleCount`,
    method: 'get',
    params: {
      userId
    }
  });
}

export const createPost = (data: any) => {
  return request<any>({
    url: "/idle/product/order/createPost", // mock接口
    method: "post",
    data: data,
  });
};

export const createPostSelf = (data: any) => {
  return request<any>({
    url: "/idle/product/order/createPostSelf", // mock接口
    method: "post",
    data: data,
  });
};

/**
 * 更新商品
 * @param data 商品实体
 * @returns 商品id
 */
export const updateProductByDTO = (data: any) => {
  return request<any>({
    url: "/idle/product/updateProductByDTO", // mock接口
    method: "post",
    data: data,
    headers: { "Content-Type": "multipart/form-data;boundary=----WebKitFormBoundaryk4ZvuPo6pkphe7Pl" },
  });
};

/**
 * 获取支付全局配置（支付开关和支付方式）
 * @returns 支付配置信息
 */
export const getPaymentGlobalConfig = () => {
  return request<any>({
    url: "/idle/config/paymentGlobalConfig",
    method: "get",
  });
};