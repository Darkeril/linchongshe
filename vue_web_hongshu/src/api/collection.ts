import request from "@/utils/request";
import type { CollectionDTO } from "@/type/collection";

/**
 * 点赞或收藏
 * @param data 点赞收藏实体
 * @returns success
 */
export const collectionByDTO = (data: CollectionDTO) => {
  return request<any>({
    url: `/idle/collection/collectionByDTO`, // mock接口
    method: "post",
    data: data,
  });
};

/**
 * 是否点赞或收藏
 * @param data 点赞收藏实体
 * @returns 
 */
export const isCollection = (data: CollectionDTO) => {
  return request<any>({
    url: `/idle/collection/isCollection`, // mock接口
    method: "post",
    data: data,
  });
};

/**
 * 得到当前用户最新的点赞和收藏信息
 * @param currentPage 当前页
 * @param pageSize 分页数
 * @returns page
 */
export const getNoticeCollection = (currentPage: number, pageSize: number) => {
  return request<any>({
    url: `/idle/collection/getNoticeCollection/${currentPage}/${pageSize}`, // mock接口
    method: "get",
  });
};


