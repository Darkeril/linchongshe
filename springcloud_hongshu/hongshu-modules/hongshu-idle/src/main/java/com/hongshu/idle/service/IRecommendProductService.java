package com.hongshu.idle.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hongshu.idle.domain.entity.IdleCollection;
import com.hongshu.idle.domain.vo.IdleProductVO;

import java.util.List;

/**
 * ES
 *
 * @author: hongshu
 */
public interface IRecommendProductService extends IService<IdleCollection> {

    /**
     * 推荐商品
     *
     * @param userId 用户ID
     */
    List<IdleProductVO> getRecommendProduct(long userId);

    Page<IdleProductVO> getRecommendProductFromMysql(String userId, long currentPage, long pageSize);
}
