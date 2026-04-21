package com.hongshu.web.domain.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 评论消息实体
 * 用于RocketMQ异步更新评论数据
 *
 * @author: hongshu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评论ID
     */
    private String commentId;

    /**
     * 笔记ID
     */
    private String noteId;

    /**
     * 商品ID
     */
    private String productId;

    /**
     * 父评论ID
     */
    private String parentId;

    /**
     * 操作类型：create-创建，delete-删除
     */
    private String action;

    /**
     * 时间戳
     */
    private Long timestamp;
}


