import request from "@/utils/request";
import {
  getCachedCategoryTree,
  setCachedCategoryTree,
} from "@/utils/apiCache";

/**
 * 获取树形分类数据（带内存短时缓存；整页刷新后缓存清空，会重新请求）
 */
export const getCategoryTreeData = () => {
  const hit = getCachedCategoryTree<any>();
  if (hit != null) {
    return Promise.resolve(hit);
  }
  return request<any>({
    url: "/web/category/getCategoryTreeData",
    method: "get",
  }).then((res) => {
    setCachedCategoryTree(res);
    return res;
  });
};
