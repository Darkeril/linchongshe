package com.hongshu.web.domain.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 浏览数消息实体
 * 用于RocketMQ异步更新浏览数
 *
 * @author: hongshu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViewCountMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 类型：note-笔记，product-商品
     */
    private String type;

    /**
     * 笔记ID或商品ID
     */
    private String id;

    /**
     * 浏览数
     */
    private Long count;

    /**
     * 时间戳
     */
    private Long timestamp;
}


