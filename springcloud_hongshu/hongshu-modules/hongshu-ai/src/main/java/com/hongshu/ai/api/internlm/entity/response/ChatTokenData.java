package com.hongshu.ai.api.internlm.entity.response;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * 使用token
 *
 * @author: Yang
 * @date: 2024/3/25
 * @version: 1.2.0
 */
@Data
public class ChatTokenData {

    /**
     * 使用token
     */
    @SerializedName("total_tokens")
    public int totalTokens;


}
