package com.hongshu.ai.api.locallm.langchain.enums;

import lombok.Getter;

/**
 * Langchain 对话模型类型
 *
 * @author: Yang
 * @date: 2023/01/31
 * @version: 1.0.0
 */
@Getter
public enum ModelEnum {

    /**
     * 对话模型类型
     */
    LLM("llm模型对话", "/chat/chat"),

    KNOWLEDGE("知识库对话", "/chat/knowledge_base_chat"),

    ;

    /**
     * 值
     */
    private final String value;

    /**
     * 标签
     */
    private final String url;

    ModelEnum(final String value, final String url) {
        this.value = value;
        this.url = url;
    }

}
