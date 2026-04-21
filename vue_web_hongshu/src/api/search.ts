import request from "@/utils/request";
import { NoteDTO } from "@/type/note"
import { EsRecordDTO } from "@/type/note"
import {
  getCachedRecommendFirstPage,
  setCachedRecommendFirstPage,
  getCachedVideoFirstPage,
  setCachedVideoFirstPage,
} from "@/utils/apiCache";

/**
 * 推荐笔记分页；仅第一页带短时缓存（按用户指纹区分），翻页始终直连接口
 */
export const getRecommendNote = (currentPage: number, pageSize: number) => {
  if (currentPage === 1) {
    const hit = getCachedRecommendFirstPage<any>(pageSize);
    if (hit != null) {
      return Promise.resolve(hit);
    }
  }
  return request<any>({
    url: `/web/es/note/getRecommendNote/${currentPage}/${pageSize}`,
    method: "get",
  }).then((res) => {
    if (currentPage === 1) {
      setCachedRecommendFirstPage(pageSize, res);
    }
    return res;
  });
};

/**
 * 
 * @param currentPage 
 * @param pageSize 
 * @param data 
 * @returns 
 */
export const getNoteByDTO = (currentPage: number, pageSize: number, data: NoteDTO) => {
  return request<any>({
    url: `/web/es/note/getNoteByDTO/${currentPage}/${pageSize}`, // mock接口
    method: "post",
    data: data
  });
};

export const getCategoryAgg = (data: NoteDTO) => {
  return request<any>({
    url: `/web/es/note/getCategoryAgg`, // mock接口
    method: "post",
    data: data
  });
};

/**
 * 视频笔记分页；仅第一页带短时缓存（与推荐一致：按用户指纹、TTL 见 apiCache）
 */
export const getVideoNote = (currentPage: number, pageSize: number) => {
  if (currentPage === 1) {
    const hit = getCachedVideoFirstPage<any>(pageSize);
    if (hit != null) {
      return Promise.resolve(hit);
    }
  }
  return request<any>({
    url: `/web/es/note/getVideoNote/${currentPage}/${pageSize}`,
    method: "get",
  }).then((res) => {
    if (currentPage === 1) {
      setCachedVideoFirstPage(pageSize, res);
    }
    return res;
  });
};

/**
 * 
 * @param keyword 
 * @returns 
 */
export const getRecordByKeyWord = (keyword: string) => {
  return request<any>({
    url: `/web/es/record/getRecordByKeyWord`, // mock接口
    method: "get",
    params: {
      keyword
    }
  });
};

/**
 * 
 * @returns 
 */
export const getHotRecord = () => {
  return request<any>({
    url: `web/es/record/getHotRecord`, // mock接口
    method: "get",
  });
};

/**
 * 
 * @param keyword 
 * @returns 
 */
export const addRecord = (data: EsRecordDTO) => {
  return request<any>({
    url: `/web/es/record/addRecord`, // mock接口
    method: "post",
    data: data
  });
};
