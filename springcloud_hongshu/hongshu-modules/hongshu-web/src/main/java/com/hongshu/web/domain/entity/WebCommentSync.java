package com.hongshu.web.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hongshu.web.domain.HongshuBaseEntity;
import lombok.Data;

/**
 * 评论同步
 *
 * @author: hongshu
 */
@Data
@TableName("web_comment_sync")
public class WebCommentSync extends HongshuBaseEntity {

    /**
     * 笔记ID
     */
    private String nid;

    /**
     * 闲置商品ID
     */
    private String productId;

    /**
     * 评论关联的笔记ID
     */
    private String noteUid;

    /**
     * 评论关联商品的用户ID
     */
    private String productUid;

    /**
     * 发布评论的用户ID
     */
    private String uid;

    /**
     * 根评论ID
     */
    private String pid;

    /**
     * 回复的评论ID
     */
    private String replyId;

    /**
     * 回复评论的用户ID
     */
    private String replyUid;

    /**
     * 评论等级
     */
    private Integer level;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论点赞数量
     */
    private Long likeCount;

    /**
     * 二级评论数量
     */
    private Long twoCommentCount;

    /**
     * 审核状态: 0-待审核, 1-通过, 2-拒绝
     */
    private Integer auditStatus;

    /**
     * 审核时间
     */
    private java.util.Date auditTime;

    /**
     * 审核原因
     */
    private String auditReason;
}
