package com.hongshu.web.domain.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 关注消息实体
 * 用于RocketMQ异步更新关注关系
 *
 * @author: hongshu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID（关注者）
     */
    private String userId;

    /**
     * 被关注用户ID
     */
    private String fid;

    /**
     * 操作类型：true-关注，false-取消关注
     */
    private Boolean isFollow;

    /**
     * 时间戳
     */
    private Long timestamp;
}

