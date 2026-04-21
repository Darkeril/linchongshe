package com.hongshu.web.service.web;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hongshu.common.core.domain.NoteSearchVo;
import com.hongshu.web.domain.dto.EsNoteDTO;
import com.hongshu.web.domain.entity.WebCategory;
import com.hongshu.web.domain.entity.WebNote;
import com.hongshu.web.domain.entity.WebUser;

import java.util.List;

/**
 * ES
 *
 * @author: hongshu
 */
public interface IWebEsNoteService extends IService<WebNote> {

    /**
     * 搜索对应的笔记
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     * @param esNoteDTO   笔记
     */
    Page<NoteSearchVo> getNoteByDTO(long currentPage, long pageSize, EsNoteDTO esNoteDTO);

    /**
     * 搜索对应的笔记
     *
     * @param esNoteDTO 笔记
     */
    List<WebCategory> getCategoryAgg(EsNoteDTO esNoteDTO);

    /**
     * 获取推荐笔记
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    Page<NoteSearchVo> getRecommendNote(long currentPage, long pageSize);

    /**
     * 获取视频笔记
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    Page<NoteSearchVo> getVideoNote(long currentPage, long pageSize);

    /**
     * 获取推荐用户
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    Page<WebUser> getRecommendUser(long currentPage, long pageSize);

    /**
     * 获取热榜笔记
     *
     * @param currentPage 当前页
     * @param pageSize    分页数
     */
    Page<NoteSearchVo> getHotNote(long currentPage, long pageSize);

    Page<NoteSearchVo> getNotesNearBy(long currentPage, long pageSize);

    /**
     * 增加笔记
     *
     * @param noteSearchVo 笔记
     */
    void addNote(NoteSearchVo noteSearchVo);

    /**
     * 按笔记 ID 从数据库读取最新数据并 upsert 到 ES（审核通过且未删除）。
     * 避免业务层用内存对象同步导致计数/字段不完整，并统一分类与用户解析逻辑。
     *
     * @param noteId 笔记主键
     */
    void upsertNoteFromDbById(String noteId);

    /**
     * 修改笔记
     *
     * @param noteSearchVo 笔记
     */
    void updateNote(NoteSearchVo noteSearchVo);

    /**
     * 批量增加笔记
     */
    void addNoteBulkData();

    /**
     * 删除es中的笔记
     *
     * @param noteId 笔记 ID
     */
    void deleteNote(String noteId);

    /**
     * 清空笔记
     */
    void delNoteBulkData();

    /**
     * 重置
     */
    void refreshNoteData();

}
