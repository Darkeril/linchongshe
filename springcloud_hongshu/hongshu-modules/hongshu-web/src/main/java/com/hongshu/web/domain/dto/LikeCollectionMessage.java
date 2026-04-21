package com.hongshu.web.domain.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 点赞/收藏消息
 *
 * @author: hongshu
 */
@Data
@Accessors(chain = true)
public class LikeCollectionMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 操作类型: LIKE, UNLIKE, COLLECT, UNCOLLECT
     */
    private String action;

    /**
     * 用户ID
     */
    private String uid;

    /**
     * 点赞/收藏对象ID
     */
    private String likeOrCollectionId;

    /**
     * 发布者ID
     */
    private String publishUid;

    /**
     * 类型: 1=点赞笔记, 2=点赞评论, 3=收藏笔记, 4=收藏专辑
     */
    private Integer type;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 操作前的点赞数（用于回滚）
     */
    private Long previousCount;
}
