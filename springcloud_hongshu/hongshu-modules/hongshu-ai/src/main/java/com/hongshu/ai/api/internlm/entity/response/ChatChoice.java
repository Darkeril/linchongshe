package com.hongshu.ai.api.internlm.entity.response;

import com.google.gson.annotations.SerializedName;
import com.hongshu.ai.api.internlm.entity.request.ChatCompletionMessage;
import lombok.Data;

/**
 * 返回内容
 *
 * @author: Yang
 * @date: 2024/3/25
 * @version: 1.2.0
 */
@Data
public class ChatChoice {


    private int index;

    /**
     * 对话内容
     */
    private ChatCompletionMessage message;

    /**
     * 结束原因
     */
    @SerializedName("finish_reason")
    private String finishReason;

}
