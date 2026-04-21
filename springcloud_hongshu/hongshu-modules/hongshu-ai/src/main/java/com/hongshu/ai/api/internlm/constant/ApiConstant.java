package com.hongshu.ai.api.internlm.constant;

/**
 * 月之暗面 接口常量
 *
 * @author: Yang
 * @date: 2024/3/26
 * @version: 1.2.0
 */
public interface ApiConstant {

    String BASE_COMPLETION_URL = "https://internlm-chat.intern-ai.org.cn/puyu/api/v1";

    /**
     * 对话
     */
    String CHAT_COMPLETION_URL = BASE_COMPLETION_URL + "/chat/completions";

    /**
     * 获取模型列表
     */
    String CHAT_LIST_MODELS_URL = BASE_COMPLETION_URL + "/models";

}
