package com.hongshu.ai.api.internlm.entity.response;

import lombok.Data;

import java.util.List;

/**
 * stream流式返回
 *
 * @author: Yang
 * @date: 2024/3/25
 * @version: 1.2.0
 */
@Data
public class ChatStreamResponse {

    /**
     * 回复唯一标识
     */
    private String id;

    /**
     * 时间戳
     */
    private Long created;

    /**
     * 使用模型
     */
    private String model;

    /**
     * 返回对话内容
     */
    private List<ChatStreamChoice> choices;

}
