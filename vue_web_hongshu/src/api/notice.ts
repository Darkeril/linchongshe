import request from "@/utils/request";

/**
 * 获取通知公告列表
 * @param currentPage 当前页
 * @param pageSize 每页数量
 * @returns 通知公告列表
 */
export const getNoticeList = (currentPage: number, pageSize: number) => {
  return request<any>({
    url: `/web/app/notice/list/${currentPage}/${pageSize}`,
    method: "get",
  });
};

/**
 * 根据ID获取通知公告详情
 * @param noticeId 公告ID
 * @returns 通知公告详情
 */
export const getNoticeDetail = (noticeId: number) => {
  return request<any>({
    url: `/web/app/notice/${noticeId}`,
    method: "get",
  });
};





