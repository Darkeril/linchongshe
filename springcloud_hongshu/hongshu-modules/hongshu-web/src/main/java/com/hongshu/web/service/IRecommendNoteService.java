package com.hongshu.web.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hongshu.common.core.domain.NoteSearchVo;
import com.hongshu.web.domain.entity.WebLikeOrCollection;
import com.hongshu.web.domain.entity.WebUser;

import java.util.List;

/**
 * ES
 *
 * @author: hongshu
 */
public interface IRecommendNoteService extends IService<WebLikeOrCollection> {

    /**
     * 推荐笔记
     *
     * @param userId 用户ID
     */
    List<NoteSearchVo> getRecommendNote(long userId);

    /**
     * 推荐用户
     *
     * @param userId 用户ID
     */
    List<WebUser> getRecommendUser(long userId);

    /**
     * 从MySQL获取推荐笔记
     *
     * @param userId      用户ID
     * @param currentPage 当前页
     * @param pageSize    页大小
     * @return 分页结果
     */
    Page<NoteSearchVo> getRecommendNoteFromMysql(String userId, long currentPage, long pageSize);
}
