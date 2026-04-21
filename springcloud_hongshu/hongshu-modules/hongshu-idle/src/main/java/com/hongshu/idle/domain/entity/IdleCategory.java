package com.hongshu.idle.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hongshu.idle.domain.HongshuBaseEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 分类
 *
 * @author: hongshu
 */
@Data
@TableName("idle_category")
public class IdleCategory extends HongshuBaseEntity {

    /**
     * 标题
     */
    private String title;

    /**
     * 父级ID
     */
    private String pid;

    /**
     * 分类描述
     */
    private String description;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态
     */
    private String status;

    /**
     * 分类封面
     */
    private String normalCover;

    /**
     * 热门封面
     */
    private String hotCover;

    /**
     * 子导航栏
     */
    @TableField(exist = false)
    private List<IdleCategory> children = new ArrayList<IdleCategory>();

    @TableField(exist = false)
    private String orderColumn; // 排序列名

    @TableField(exist = false)
    private String timeOrder; // 排序方向

    @TableField(exist = false)
    private String productCount; // 商品数量

    /**
     * 删除标识（0：正常 1：删除）
     */
    private Integer deleted;
}
