package com.hongshu.idle.service.idle;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hongshu.idle.domain.dto.CollectionDTO;
import com.hongshu.idle.domain.entity.IdleCollection;
import com.hongshu.idle.domain.vo.CollectionVO;
import com.hongshu.idle.domain.vo.IdleProductVO;

/**
 * 点赞/收藏
 *
 * @author: hongshu
 */
public interface IIdleCollectionService extends IService<IdleCollection> {

    /**
     * 收藏
     *
     * @param collectionDTO 收藏
     */
    void collectionByDTO(CollectionDTO collectionDTO);

    /**
     * 是否收藏
     *
     * @param collectionDTO 收藏
     */
    boolean isCollection(CollectionDTO collectionDTO);

    /**
     * 获取当前用户最新的收藏信息
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    Page<IdleProductVO> getNoticeCollection(long currentPage, long pageSize);
}
