package com.hongshu.web.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hongshu.web.domain.HongshuBaseEntity;
import lombok.Data;

/**
 * 专辑
 *
 * @author: hongshu
 */
@Data
@TableName("web_album")
public class WebAlbum extends HongshuBaseEntity {

    /**
     * 标题
     */
    private String title;

    /**
     * 用户ID
     */
    private String uid;

    /**
     * 所属用户名（管理端列表等场景由服务层填充，非表字段）
     */
    @TableField(exist = false)
    private String ownerUsername;

    /**
     * 专辑封面图
     */
    private String albumCover;

    /**
     * 专辑类型（0：默认）
     */
    private Integer type;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 专辑中笔记数量
     */
    private Long imgCount;

    /**
     * 收藏数量
     */
    private Long collectionCount;

}
