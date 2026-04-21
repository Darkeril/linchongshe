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
 * 功能建议反馈
 *
 * @author: hongshu
 */
@Data
@TableName("web_feedback")
public class WebFeedback implements Serializable {

    /**
     * 主键ID（自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private String uid;

    /**
     * 反馈内容
     */
    private String content;

    /**
     * 反馈类型：feature(功能建议)、bug(问题反馈)、other(其他)
     */
    private String type;

    /**
     * 点赞数量
     */
    private Long likeCount;

    /**
     * 状态：0-待处理, 1-已处理, 2-已拒绝
     */
    private Integer status;

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

