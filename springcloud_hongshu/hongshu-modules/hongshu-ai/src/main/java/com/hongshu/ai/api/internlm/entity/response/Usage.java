package com.hongshu.ai.api.internlm.entity.response;

import lombok.Data;

/**
 * 使用量
 *
 * @author: Yang
 * @date: 2024/3/25
 * @version: 1.2.0
 */
@Data
public class Usage {

    /**
     * 提示词token
     */
    private int promptTokens;

    /**
     * 输出token
     */
    private int completionTokens;

    /**
     * 总token
     */
    private int totalTokens;

}
