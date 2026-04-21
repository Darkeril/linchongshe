package com.hongshu.ai.api.base.service.impl;

import com.hongshu.ai.api.base.service.ModelService;
import com.hongshu.ai.api.locallm.base.enums.ModelTypeEnum;
import com.hongshu.ai.api.locallm.coze.CozeClient;
import com.hongshu.ai.api.locallm.dify.DifyClient;
import com.hongshu.ai.api.locallm.gitee.GiteeClient;
import com.hongshu.ai.api.locallm.langchain.LangchainClient;
import com.hongshu.ai.api.locallm.ollama.OllamaClient;
import com.hongshu.ai.common.enums.ChatModelEnum;
import com.hongshu.ai.domain.command.ChatMessageCommand;
import com.hongshu.ai.domain.dto.ChatMessageDTO;
import com.hongshu.ai.domain.dto.ModelDTO;
import com.hongshu.ai.service.GptService;
import com.hongshu.common.core.exception.exceptionType.BusinessException;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 本地模型 接口实现
 *
 * @author: Yang
 * @date: 2024/5/30
 * @version: 1.1.7
 */
@Service
public class LocalLMServiceImpl implements ModelService {

    private static LangchainClient langchainClient;
    private static OllamaClient ollamaClient;
    private static CozeClient cozeClient;
    private static GptService gptService;
    private static GiteeClient giteeClient;
    private static DifyClient difyClient;


    @Autowired
    public LocalLMServiceImpl(GptService gptService, LangchainClient langchainClient, OllamaClient ollamaClient, CozeClient cozeClient,
                              GiteeClient giteeClient, DifyClient difyClient) {
        LocalLMServiceImpl.langchainClient = langchainClient;
        LocalLMServiceImpl.ollamaClient = ollamaClient;
        LocalLMServiceImpl.cozeClient = cozeClient;
        LocalLMServiceImpl.gptService = gptService;
        LocalLMServiceImpl.giteeClient = giteeClient;
        LocalLMServiceImpl.difyClient = difyClient;
    }

    @Override
    public ChatMessageCommand chat(List<ChatMessageDTO> chatMessages, Boolean isDraw, Long chatId, String version) {
        return null;
    }

    @Override
    @SneakyThrows
    public Boolean streamChat(HttpServletResponse response, SseEmitter sseEmitter, List<ChatMessageDTO> chatMessages, Boolean isWs, Boolean isDraw,
                              Long chatId, String conversationId, String prompt, String version, String uid) {
        ModelDTO modelDTO = gptService.getModel(ChatModelEnum.LOCALLM.getValue());
        ModelTypeEnum modelType = ModelTypeEnum.getEnum(modelDTO.getLocalModelType());
        switch (modelType) {
            case LANGCHAIN:
                return langchainClient.buildChatCompletion(response, sseEmitter, chatId, conversationId, isWs, uid, chatMessages, prompt, version, modelDTO);
            case OLLAMA:
                return ollamaClient.buildChatCompletion(response, sseEmitter, chatId, conversationId, isWs, uid, chatMessages, prompt, version, modelDTO);
            case COZE:
                return cozeClient.buildChatCompletion(response, sseEmitter, chatId, conversationId, isWs, uid, chatMessages, prompt, version, modelDTO);
            case GITEE_AI:
                return giteeClient.buildChatCompletion(response, sseEmitter, chatId, conversationId, isWs, uid, chatMessages, prompt, version, modelDTO);
            case Dify:
                return difyClient.buildChatCompletion(response, sseEmitter, chatId, conversationId, isWs, uid, chatMessages, prompt, version, modelDTO);
            default:
                throw new BusinessException("未知的模型类型，功能未接入");
        }
    }


}
