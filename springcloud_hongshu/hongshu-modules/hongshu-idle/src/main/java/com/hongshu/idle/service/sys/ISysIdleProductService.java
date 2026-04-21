package com.hongshu.idle.service.sys;

import com.hongshu.idle.domain.Query;
import com.hongshu.idle.domain.entity.IdleProduct;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 闲宝信息 服务层
 *

 */
public interface ISysIdleProductService {

    /**
     * 查询笔记信息集合
     */
    List<IdleProduct> getAllProductList();

    /**
     * 查询笔记信息集合
     *
     * @param query 笔记信息
     */
    List<IdleProduct> selectProductList(Query query);

    /**
     * 获取未审核笔记列表
     */
    List<IdleProduct> selectUnAuditProductList(Query query);

    /**
     * 通过笔记ID查询笔记信息
     *
     * @param id 笔记ID
     */
    IdleProduct selectProductById(Long id);

    /**
     * 新增保存笔记信息
     *
     * @param product 笔记信息
     */
    int insertProduct(IdleProduct product, MultipartFile file);

    /**
     * 修改保存笔记信息
     *
     * @param product 笔记信息
     * @param file 封面文件
     * @param files 商品图片/视频文件数组
     */
    int updateProduct(IdleProduct product, MultipartFile file, MultipartFile[] files);

    /**
     * 批量删除笔记信息
     *
     * @param ids 需要删除的笔记ID
     */
    int deleteProductByIds(Long[] ids);

    /**
     * 审核管理
     *
     * @param productId 笔记ID
     * @param auditType 审核状态
     */
    void auditProduct(String productId, String auditType);

    /**
     * 批量增加商品
     */
    void addProductBulkData();

    /**
     * 清空商品
     */
    void delProductBulkData();

    /**
     * 重置
     */
    void refreshProductData();

    List<Map<String, Object>> getProductCountByCategory();

    Object getProductCount();

    List<Map<String, Object>> getProductCountByType();

    Map<String, Object> getProductContributeCount();
}
