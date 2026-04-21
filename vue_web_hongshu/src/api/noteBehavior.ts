import request from "@/utils/request";

/** 行为事件类型：exposure-曝光 view-观看 watch-观看时长/完播 share-分享 */
export type NoteBehaviorEventType = "exposure" | "view" | "watch" | "share";

/** 来源场景：search-搜索 recommend-推荐 follow-关注流 profile-个人页 other */
export type NoteBehaviorScene = "search" | "recommend" | "follow" | "profile" | "other";

export interface NoteBehaviorReportItem {
  eventType: NoteBehaviorEventType;
  nid: string;
  uid?: string;
  scene?: NoteBehaviorScene | string;
  durationSec?: number;
  completed?: 0 | 1;
}

/**
 * 单条笔记行为上报（曝光/观看/观看时长完播）
 */
export const reportNoteBehavior = (data: NoteBehaviorReportItem) => {
  return request<any>({
    url: "/web/app/noteBehavior/report",
    method: "post",
    data,
  });
};

/**
 * 批量笔记行为上报
 */
export const reportNoteBehaviorBatch = (list: NoteBehaviorReportItem[]) => {
  if (!list?.length) return Promise.resolve();
  return request<any>({
    url: "/web/app/noteBehavior/reportBatch",
    method: "post",
    data: list,
  });
};
