import request from "@/utils/request";

/**
 * 获取专辑数据
 * @returns 专辑数据
 */
export const getAlbumData = (userId:string) => {
  return request<any>({
    url: `/web/album/getAlbumList`, 
    method: "get",
    params: {
      userId
    },
  });
};

/**
 * 创建专辑
 * @param data 专辑数据
 */
export const createAlbum = (data: { title: string; uid: string; sort?: number }) => {
  return request<any>({
    url: `/web/album/saveAlbum`,
    method: "post",
    data,
  });
};
