package com.hongshu.ai.api.internlm.entity.response;

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
     * 检查是否有有效的消息内容
     */
    public boolean hasValidMessage() {
        return delta != null && delta.getContent() != null && !delta.getContent().trim().isEmpty();
    }

}
