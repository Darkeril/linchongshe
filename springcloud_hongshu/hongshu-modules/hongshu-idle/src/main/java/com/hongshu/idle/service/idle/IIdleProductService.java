package com.hongshu.idle.service.idle;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hongshu.idle.domain.dto.EsProductDTO;
import com.hongshu.idle.domain.dto.ProductAppDTO;
import com.hongshu.idle.domain.entity.IdleProduct;
import com.hongshu.idle.domain.vo.IdleProductVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 产品
 *

 */
public interface IIdleProductService extends IService<IdleProduct> {

    /**
     * 获取商品
     *
     * @param productId 商品ID
     */
    IdleProductVO getProductById(String productId);

//    /**
//     * 获取推荐商品
//     *
//     * @param currentPage 当前页
//     * @param pageSize    分页数
//     */
//    Page<IdleProductVO> getRecommendProduct(long currentPage, long pageSize);

    /**
     * 搜索对应的商品
     *
     * @param currentPage  当前页
     * @param pageSize     分页数
     * @param esProductDTO 商品
     */
    Page<IdleProductVO> getProduct(long currentPage, long pageSize, EsProductDTO esProductDTO);

    /**
     * 查找商品
     *
     * @param keyword 关键词
     */
    Page<IdleProductVO> getProductByKeyword(long currentPage, long pageSize, String keyword);

    /**
     * 新增闲置商品
     *
     * @param terminal  来源标识
     * @param requestId 唯一标识
     * @param noteData  商品对象
     * @param coverFile 封面图
     * @param files     图片文件
     */
    void saveProductByDTO(String terminal, String requestId, String noteData, MultipartFile coverFile, MultipartFile[] files);

    void saveProduct(String terminal, String requestId, ProductAppDTO productDTO);

    void updateProductByDTO(String productData, MultipartFile coverFile, MultipartFile[] files);

    /**
     * 置顶商品
     *
     * @param productId 商品ID
     */
    boolean pinnedProduct(String productId);

    Object getIdleCount(String userId);

}
