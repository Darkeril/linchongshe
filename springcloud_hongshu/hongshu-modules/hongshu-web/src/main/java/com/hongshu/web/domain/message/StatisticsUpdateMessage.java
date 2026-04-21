package com.hongshu.web.domain.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统计更新消息实体
 * 用于RocketMQ异步更新统计数据
 *
 * @author: hongshu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsUpdateMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 统计类型：user-用户，note-笔记，comment-评论，product-商品，visit-访问
     */
    private String type;

    /**
     * 操作类型：create-新增，delete-删除
     */
    private String action;

    /**
     * 关联ID（可选）
     */
    private String id;

    /**
     * 时间戳
     */
    private Long timestamp;
}


