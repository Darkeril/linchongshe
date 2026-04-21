package com.hongshu.web.mapper.web;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hongshu.common.core.domain.NoteSearchVo;
import com.hongshu.web.domain.entity.WebNote;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author: hongshu
 */
@Mapper
public interface WebNoteMapper extends BaseMapper<WebNote> {

    List<Map<String, Object>> sumInteractionGroupByDay(
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime,
            @Param("auditStatus") String auditStatus);

    Map<String, Object> sumInteractionInRange(
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime,
            @Param("auditStatus") String auditStatus);

    List<Map<String, Object>> topCategoryPublishInRange(
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime,
            @Param("auditStatus") String auditStatus,
            @Param("limit") int limit);

    Page<NoteSearchVo> selectNotesWithUserInfo(
            @Param("page") Page<Object> page,
            @Param("cityName") String cityName,
            @Param("auditStatus") String auditStatus
    );

    @Select("SELECT COUNT(*) FROM web_note WHERE deleted = 0 AND audit_status = #{auditStatus}")
    long countByAuditStatus(@Param("auditStatus") String auditStatus);

    @Select("SELECT COUNT(*) FROM web_note WHERE deleted = 0 AND DATE(create_time) = CURDATE()")
    long countCreatedToday();
}
