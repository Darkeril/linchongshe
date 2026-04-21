package com.hongshu.web.mapper.web;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hongshu.web.domain.entity.WebProductNoteRelation;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: hongshu
 */
@Mapper
public interface WebProductNoteRelationMapper extends BaseMapper<WebProductNoteRelation> {

    /**
     * 批量插入笔记商品关联
     */
    @Insert("<script>" +
            "INSERT INTO web_product_note_relation(id, nid, pid, create_time, update_time) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.id}, #{item.nid}, #{item.pid}, #{item.createTime}, #{item.updateTime})" +
            "</foreach>" +
            "</script>")
    int insertBatchSql(@Param("list") List<WebProductNoteRelation> list);
}
