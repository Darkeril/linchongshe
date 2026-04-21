package com.hongshu.ai.api.base.websocket.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * websocket 转换对象
 *
 * @author: Yang
 * @date: 2023/5/5
 * @version: 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketData {

    /**
     * 心跳码
     */
    private String functionCode;

    /**
     * 消息
     */
    private Object message;


}
