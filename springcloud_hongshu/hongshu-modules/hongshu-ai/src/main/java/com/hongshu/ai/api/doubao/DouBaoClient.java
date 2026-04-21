package com.hongshu.ai.api.doubao;

import cn.hutool.core.util.StrUtil;
import com.hongshu.ai.api.base.key.KeyUpdater;
import com.hongshu.ai.api.doubao.listener.SSEListener;
import com.hongshu.ai.common.enums.ChatModelEnum;
import com.hongshu.common.core.exception.ValidateException;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionChunk;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionResult;
import com.volcengine.ark.runtime.service.ArkService;
import io.reactivex.Flowable;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 豆包client
 * 文档地址：https://www.volcengine.com/docs/82379/1099455
 *
 * @author: Yang
 * @date: 2025/2/16
 */
@Data
public class DouBaoClient implements KeyUpdater {

    @Getter
    @Setter
    private String apiKey;
    private ArkService service;

    public DouBaoClient() {
    }

    public DouBaoClient(String apiKey) {
        this.apiKey = apiKey;
    }

    private DouBaoClient(Builder builder) {
        if (StrUtil.isBlank(builder.apiKey)) {
            throw new ValidateException("构造错误: apiKey不能为空");
        }
        apiKey = builder.apiKey;
        service = ArkService.builder().apiKey(apiKey).build();
    }

    /**
     * 同步响应
     *
     * @param request
     */
    public ChatCompletionResult chat(ChatCompletionRequest request) {
        ChatCompletionResult invokeModelApiResp = service.createChatCompletion(request);
        try {
            return invokeModelApiResp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 流式响应
     *
     * @param request
     */
    public Boolean streamChat(HttpServletResponse response, ChatCompletionRequest request, Long chatId, String parentMessageId, String version, String uid, Boolean isWs) {
        SSEListener sseListener = new SSEListener(response, chatId, parentMessageId, version, uid, isWs);
        Flowable<ChatCompletionChunk> chunks = service.streamChatCompletion(request);
        Boolean flag = sseListener.streamChat(chunks);
        // shutdown service
//        service.shutdownExecutor();
        return flag;
    }

    @Override
    public String supportModel() {
        return ChatModelEnum.DOUBAO.getValue();
    }

    @Override
    public void updateKey(KeyModel keyModel) {
        this.setApiKey(keyModel.getAppKey());
        service = ArkService.builder().apiKey(apiKey).build();
    }

    /**
     * 构造
     *
     * @return
     */
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String apiKey;

        public Builder() {
        }

        public Builder apiKey(String val) {
            apiKey = val;
            return this;
        }

        public DouBaoClient build() {
            return new DouBaoClient(this);
        }
    }

}
