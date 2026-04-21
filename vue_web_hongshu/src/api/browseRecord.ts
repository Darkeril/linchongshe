import request from "@/utils/request";

/**
 * 添加浏览记录（查看笔记/闲置详情时调用，用于「浏览记录」列表）
 * @param params.nid 笔记ID（闲置详情时传商品/笔记 id）
 * @param params.uid 用户ID，不传则后端可能从 token 取
 */
export const addBrowseRecord = (params: { nid: string; uid?: string }) => {
  return request<any>({
    url: "/web/app/browseRecord/addBrowseRecord",
    method: "post",
    data: {
      nid: params.nid,
      uid: params.uid,
    },
  });
};
