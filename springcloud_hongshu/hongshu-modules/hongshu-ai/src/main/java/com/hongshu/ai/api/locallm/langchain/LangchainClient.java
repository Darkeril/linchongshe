package com.hongshu.ai.api.locallm.langchain;

import com.hongshu.ai.api.locallm.LocalLMClient;
import com.hongshu.ai.api.locallm.langchain.constant.ApiConstant;
import com.hongshu.ai.api.locallm.langchain.entity.ChatCompletion;
import com.hongshu.ai.api.locallm.langchain.entity.ChatCompletionMessage;
import com.hongshu.ai.api.locallm.langchain.enums.ModelEnum;
import com.hongshu.ai.api.locallm.langchain.listenter.SSEListener;
import com.hongshu.ai.domain.dto.ChatMessageDTO;
import com.hongshu.ai.domain.dto.ModelDTO;
import com.hongshu.common.core.validator.ValidatorUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * LangChain-Chatchat client
 *
 * @author: Yang
 * @date: 2023/12/4
 * @version: 1.0.0
 */
@Slf4j
@Service
public class LangchainClient {
    private static LocalLMClient localLMClient;

    @Autowired
    public LangchainClient(LocalLMClient localLMClient) {
        LangchainClient.localLMClient = localLMClient;
    }

    /**
     * 构建请求
     *
     * @param
     * @param chatMessages
     * @param prompt
     * @param version
     * @param modelDTO
     */
    @SneakyThrows
    public Boolean buildChatCompletion(HttpServletResponse response, SseEmitter sseEmitter, Long chatId, String conversationId, Boolean isWs, String uid,
                                       List<ChatMessageDTO> chatMessages, String prompt, String version, ModelDTO modelDTO) {

        List<ChatCompletionMessage> history = new ArrayList<>();
        chatMessages.stream().forEach(v -> {
            ChatCompletionMessage message = ChatCompletionMessage.builder().content(v.getContent()).role(v.getRole()).build();
            history.add(message);
        });
        String model = ValidatorUtil.isNotNull(version) ? version : ApiConstant.DEFAULT_MODEL;
        ChatCompletion chat = ChatCompletion.builder()
                .query(prompt)
                .knowledgeBaseName(ApiConstant.DEFAULT_KNOWLEDGE)
                .history(history)
                .modelName(model)
                .build();
        String domain = ValidatorUtil.isNotNull(modelDTO.getModelUrl()) ? modelDTO.getModelUrl() : ApiConstant.BASE_DOMAIN;
        ModelEnum modelUrl = ValidatorUtil.isNotNull(modelDTO.getKnowledge()) ? ModelEnum.KNOWLEDGE : ModelEnum.LLM;
        SSEListener sseListener = new SSEListener(response, sseEmitter, chatId, conversationId, version, modelDTO.getKnowledge(), prompt, uid, isWs);
        Response callResponse = localLMClient.streamChat(chat, domain, modelUrl.getUrl());
        if (callResponse == null || callResponse.code() != 200) {
            log.error("Langchain流式响应失败: " + callResponse.body().string());
            return true;
        }
        return sseListener.streamChat(callResponse);
    }

}

