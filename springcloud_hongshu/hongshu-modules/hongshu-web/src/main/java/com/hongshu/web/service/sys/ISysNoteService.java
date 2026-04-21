package com.hongshu.web.service.sys;

import com.hongshu.common.core.domain.NoteSearchVo;
import com.hongshu.web.domain.Query;
import com.hongshu.web.domain.entity.WebNote;
import com.hongshu.web.domain.vo.NoteDataCenterDetailVo;
import com.hongshu.web.domain.vo.NoteDataCenterVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 笔记信息 服务层
 *
 */
public interface ISysNoteService {

    /**
     * 查询笔记信息集合
     */
    List<WebNote> getAllNoteList();

    /**
     * 查询笔记信息集合
     *
     * @param query 笔记信息
     */
    List<WebNote> selectNoteList(Query query);

    /**
     * 获取未审核笔记列表
     */
    List<WebNote> selectUnAuditNoteList(Query query);

    /**
     * 通过笔记ID查询笔记信息
     *
     * @param id 笔记ID
     */
    WebNote selectNoteById(Long id);

    /**
     * 新增保存笔记信息
     *
     * @param note 笔记信息
     */
    int insertNote(WebNote note, MultipartFile file);

    /**
     * 修改保存笔记信息
     *
     * @param note 笔记信息
     */
    int updateNote(WebNote note, MultipartFile file);

    /**
     * 批量删除笔记信息
     *
     * @param ids 需要删除的笔记ID
     */
    int deleteNoteByIds(Long[] ids);

    Object getNoteCount(int status);

    Map<String, Object> getNoteContributeCount();

    List<Map<String, Object>> getNoteCountByType();

    List<Map<String, Object>> getNoteCountByCategory();

    /**
     * 审核管理
     *
     * @param noteId    笔记ID
     * @param auditType 审核状态
     */
    boolean auditNote(String noteId, String auditType);

    List<NoteSearchVo> getHotNote(String noteType);

    /**
     * 数据中心-热门内容-笔记数据列表（分页）
     *
     * @param query 查询条件：noteType(笔记题材 1=图文 2=视频 3=live图)、startTime、endTime、pageNum、pageSize
     */
    List<NoteDataCenterVo> selectNoteDataCenterList(Query query);

    /**
     * 数据中心-笔记数据详情（单条笔记的曝光、观看、来源、时段、趋势等）
     *
     * @param notesId 笔记ID
     * @param days    统计天数，7 或 30，默认 30
     * @return 详情 VO，笔记不存在时返回 null
     */
    NoteDataCenterDetailVo getNoteDataCenterDetail(String notesId, Integer days);
}
