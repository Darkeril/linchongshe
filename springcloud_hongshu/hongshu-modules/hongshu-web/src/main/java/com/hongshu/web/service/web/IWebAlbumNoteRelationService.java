package com.hongshu.web.service.web;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hongshu.common.core.domain.NoteSearchVo;
import com.hongshu.web.domain.entity.WebAlbumNoteRelation;

/**
 * 专辑-笔记
 *
 * @author: hongshu
 */
public interface IWebAlbumNoteRelationService extends IService<WebAlbumNoteRelation> {

    /**
     * 得到当前专辑下的所有笔记
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     * @param albumId     专辑ID
     * @param userId      用户ID
     */
    Page<NoteSearchVo> getNoteByAlbumId(long currentPage, long pageSize, String albumId, String userId);

    /**
     * 管理端：分页查询专辑内笔记（不过滤发布状态，含已删除关联外的笔记）
     */
    Page<NoteSearchVo> pageNotesByAlbumIdForAdmin(String albumId, long currentPage, long pageSize);
}
