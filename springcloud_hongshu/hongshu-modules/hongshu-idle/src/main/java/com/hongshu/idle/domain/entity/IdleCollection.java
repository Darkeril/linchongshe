package com.hongshu.idle.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hongshu.idle.domain.HongshuBaseEntity;
import lombok.Data;

/**
 * 收藏
 *
 * @author: hongshu
 */
@TableName("idle_collection")
@Data
public class IdleCollection extends HongshuBaseEntity {

    /**
     * 点赞用户ID
     */
    private String uid;

    /**
     * 收藏商品的ID
     */
    private String collectionId;

    /**
     * 点赞和收藏通知的用户ID
     */
    private String publishUid;

    /**
     * 点赞和收藏类型（1：点赞图片 2：点赞评论 3：收藏图片 4：收藏专辑）
     */
    private Integer type;

    /**
     * 时间戳
     */
    private long timestamp;
}
