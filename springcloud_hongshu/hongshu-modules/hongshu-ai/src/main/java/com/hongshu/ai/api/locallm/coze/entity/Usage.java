package com.hongshu.ai.api.locallm.coze.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 使用token数
 *
 * @author: Yang
 * @date: 2024/9/19
 * @version: 1.1.8
 */
@Data
public class Usage {

    @JsonProperty("token_count")
    private long tokenCount;

    @JsonProperty("output_tokens")
    private long outputTokens;

    @JsonProperty("input_tokens")
    private long inputTokens;

}
