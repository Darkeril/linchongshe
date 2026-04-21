import request from "@/utils/request";
import { EsProductDTO } from "@/type/product"

/**
 * 获取树形分类数据
 * @returns 分类数据
 */
export const getShopCategoryTreeData = () => {
  return request<any>({
    url: "/idle/category/getCategoryTreeData",
    method: "get",
  });
};

export const getProductCategoryAgg = (data: EsProductDTO) => {
  return request<any>({
    url: `/idle/category/getCategoryAgg`, // mock接口
    method: "post",
    data: data
  });
};
