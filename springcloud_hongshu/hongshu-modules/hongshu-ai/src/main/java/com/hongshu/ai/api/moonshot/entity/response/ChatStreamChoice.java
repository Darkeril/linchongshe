package com.hongshu.ai.api.moonshot.entity.response;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 返回对话内容
 *
 * @author: Yang
 * @date: 2024/3/25
 * @version: 1.2.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatStreamChoice {

    private int index;

    /**
     * 返回内容
     */
    private ChatStreamChoiceDelta delta;

    /**
     * 结束原因
     */
    @SerializedName("finish_reason")
    private String finishReason;

    /**
     * 使用量
     */
    private Usage usage;

}
