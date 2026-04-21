package com.hongshu.ai.api.internlm.entity.response;

import lombok.Data;

/**
 * 返回内容
 *
 * @author: Yang
 * @date: 2024/3/25
 * @version: 1.2.0
 */
@Data
public class ChatStreamChoiceDelta {

    /**
     * 角色
     */
    private String role;

    /**
     * 内容
     */
    private String content;

}
