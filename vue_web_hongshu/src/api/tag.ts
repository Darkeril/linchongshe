import request from "@/utils/request";

/**
 * 获取热门标签
 * @param currentPage 
 * @param pageSize 
 * @returns 
 */
export const getHotTagList = (currentPage: number, pageSize: number) => {
  return request<any>({
    url: `/web/tag/getHotTagList/${currentPage}/${pageSize}`,
    method: "get",
  });
};

/**
 * 
 * @param currentPage 
 * @param pageSize 
 * @param keyword 
 * @returns 
 */
export const getTagByKeyword = (currentPage: number, pageSize: number,keyword:string) => {
    return request<any>({
      url: `/web/tag/getTagByKeyword/${currentPage}/${pageSize}`, // mock接口
      method: "get",
      params:{
        keyword
      }
    });
  };