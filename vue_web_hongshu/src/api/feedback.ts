import request from "@/utils/request";

/**
 * 提交功能建议
 */
export const submitFeedback = (data: {
  content: string;
  type?: string; // 建议类型：feature(功能建议)、bug(问题反馈)、other(其他)
}) => {
  return request<any>({
    url: "/web/feedback/submit",
    method: "post",
    data,
  });
};

/**
 * 获取功能建议列表
 */
export const getFeedbackList = (params: {
  currentPage: number;
  pageSize: number;
  type?: string;
}) => {
  return request<any>({
    url: `/web/feedback/list/${params.currentPage}/${params.pageSize}`,
    method: "get",
    params: {
      type: params.type,
    },
  });
};

/**
 * 点赞功能建议
 */
export const likeFeedback = (feedbackId: number | string) => {
  return request<any>({
    url: `/web/feedback/like/${feedbackId}`,
    method: "post",
  });
};

/**
 * 获取待实现功能列表
 */
export const getPlannedFeatures = () => {
  return request<any>({
    url: "/web/feedback/plannedFeatures",
    method: "get",
  });
};

