package com.hongshu.web.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 待实现功能
 *
 * @author: hongshu
 */
@Data
@TableName("web_planned_feature")
public class WebPlannedFeature implements Serializable {

    /**
     * 主键ID（自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 功能标题
     */
    private String title;

    /**
     * 功能描述
     */
    private String description;

    /**
     * 状态：规划中、开发中、已完成
     */
    private String status;

    /**
     * 排序（数字越小越靠前）
     */
    private Integer sort;

    /**
     * 是否显示：0-隐藏, 1-显示
     */
    private Integer isShow;

    /**
     * 创建用户
     */
    @TableField(fill = FieldFill.INSERT)
    private String creator;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 修改用户
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updater;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}

