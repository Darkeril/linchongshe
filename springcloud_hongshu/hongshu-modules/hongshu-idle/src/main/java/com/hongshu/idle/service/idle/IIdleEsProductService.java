package com.hongshu.idle.service.idle;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hongshu.common.core.enums.Result;
import com.hongshu.common.core.validator.myVaildator.noLogin.NoLoginIntercept;
import com.hongshu.idle.domain.dto.EsProductDTO;
import com.hongshu.idle.domain.entity.IdleProduct;
import com.hongshu.idle.domain.vo.IdleProductVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * ES
 *
 * @author: hongshu
 */
public interface IIdleEsProductService extends IService<IdleProduct> {

    /**
     * 搜索对应的笔记
     *
     * @param currentPage  当前页
     * @param pageSize     分页数
     * @param esProductDTO 笔记
     */
    Page<IdleProductVO> getProductByDTO(long currentPage, long pageSize, EsProductDTO esProductDTO);

    /**
     * 获取推荐笔记
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    Page<IdleProductVO> getRecommendProduct(long currentPage, long pageSize);

    /**
     * 获取视频商品
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    Page<IdleProductVO> getVideoProduct(long currentPage, long pageSize);

    /**
     * 获取热榜笔记
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    Page<IdleProductVO> getHotProduct(long currentPage, long pageSize);

    /**
     * 增加笔记
     *
     * @param idleProductVO 笔记
     */
    void addProduct(IdleProductVO idleProductVO);

    /**
     * 修改笔记
     *
     * @param idleProductVO 笔记
     */
    void updateProduct(IdleProductVO idleProductVO);

    /**
     * 删除es中的笔记
     *
     * @param productId 笔记ID
     */
    void deleteProduct(String productId);

    /**
     * 按数据库当前状态同步 ES：可检索则 upsert，否则删除索引文档。
     *
     * @param productId 商品主键
     */
    void applyProductEsIndexState(String productId);

}
