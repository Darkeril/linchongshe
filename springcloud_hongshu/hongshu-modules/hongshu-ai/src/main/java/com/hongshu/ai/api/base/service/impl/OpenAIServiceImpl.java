package com.hongshu.ai.api.base.service.impl;

import com.alibaba.fastjson.JSON;
import com.hongshu.ai.api.base.exception.LLMException;
import com.hongshu.ai.api.base.service.ModelService;
import com.hongshu.ai.api.openai.OpenAiClient;
import com.hongshu.ai.api.openai.entity.chat.ChatChoice;
import com.hongshu.ai.api.openai.entity.chat.ChatCompletion;
import com.hongshu.ai.api.openai.entity.chat.ChatCompletionResponse;
import com.hongshu.ai.api.openai.entity.chat.Message;
import com.hongshu.ai.api.openai.listener.SSEListener;
import com.hongshu.ai.common.enums.ChatModelEnum;
import com.hongshu.ai.common.enums.ChatStatusEnum;
import com.hongshu.ai.domain.command.ChatMessageCommand;
import com.hongshu.ai.domain.dto.ChatMessageDTO;
import com.hongshu.common.core.validator.ValidatorUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * openai 接口实现
 *
 * @author: Yang
 * @date: 2024/5/30
 * @version: 1.1.7
 */
@Slf4j
@Service
public class OpenAIServiceImpl implements ModelService {

    private static OpenAiClient openAiClient;


    public OpenAIServiceImpl(OpenAiClient openAiClient) {
        OpenAIServiceImpl.openAiClient = openAiClient;
    }

    @Override
    public ChatMessageCommand chat(List<ChatMessageDTO> chatMessages, Boolean isDraw, Long chatId, String version) {
        if (ValidatorUtil.isNull(openAiClient)) {
            log.error("ChatGpt无有效token，请切换其他模型进行聊天");
            return null;
        }
        List<Message> openAiMessages = new ArrayList<>();
        chatMessages.stream().forEach(v -> {
            Message currentMessage = Message.builder().content(v.getContent()).role(v.getRole()).build();
            openAiMessages.add(currentMessage);
        });
        ChatCompletion chatCompletion = ChatCompletion.builder()
                .messages(openAiMessages)
                .model(ValidatorUtil.isNotNull(version) ? version : ChatCompletion.Model.GPT_3_5_TURBO.getName())
                .build();
        ChatCompletionResponse response;
        try {
            response = openAiClient.chatCompletion(chatCompletion);
        } catch (Exception e) {
            throw new LLMException("OpenAi接口请求异常，请稍后再试");
        }
        ChatChoice choice = response.getChoices().get(0);
        ChatMessageCommand chatMessage = ChatMessageCommand.builder().chatId(chatId).messageId(response.getId())
                .model(ChatModelEnum.OPENAI.getValue()).modelVersion(response.getModel())
                .content(choice.getMessage().getContent()).role(choice.getMessage().getRole()).finishReason(choice.getFinishReason())
                .status(ChatStatusEnum.SUCCESS.getValue()).appKey(openAiClient.getApiKey().get(0)).usedTokens(response.getUsage().getTotalTokens())
                .response(JSON.toJSONString(response))
                .build();
        return chatMessage;
    }

    @Override
    @SneakyThrows
    public Boolean streamChat(HttpServletResponse response, SseEmitter sseEmitter, List<ChatMessageDTO> chatMessages, Boolean isWs, Boolean isDraw,
                              Long chatId, String conversationId, String prompt, String version, String uid) {
        if (ValidatorUtil.isNullIncludeArray(openAiClient.getApiKey())) {
            log.error("ChatGPT未加载到密钥信息");
            return true; // 返回true表示有错误
        }
        List<Message> messages = new ArrayList<>();
        chatMessages.stream().forEach(v -> {
            Message currentMessage = Message.builder().content(v.getContent()).role(v.getRole()).build();
            messages.add(currentMessage);
        });
        SSEListener sseListener = new SSEListener(response, sseEmitter, chatId, conversationId, ChatModelEnum.OPENAI.getValue(), version, uid, isWs);
        ChatCompletion completion = ChatCompletion
                .builder()
                .messages(messages)
                .model(ValidatorUtil.isNotNull(version) ? version : ChatCompletion.Model.GPT_3_5_TURBO_0613.getName())
                .build();
        try {
            openAiClient.streamChatCompletion(completion, sseListener);
            if (isWs) {
                return false;
            }
            sseListener.getCountDownLatch().await();
            return sseListener.getError();
        } catch (Exception e) {
            log.error("ChatGPT流式聊天异常：{}", e.getMessage());
            return true; // 返回true表示有错误
        }
    }

}
