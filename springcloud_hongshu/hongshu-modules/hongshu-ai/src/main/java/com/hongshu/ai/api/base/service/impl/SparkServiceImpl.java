package com.hongshu.ai.api.base.service.impl;

import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson.JSON;
import com.hongshu.ai.api.base.exception.LLMException;
import com.hongshu.ai.api.base.service.ModelService;
import com.hongshu.ai.api.spark.SparkClient;
import com.hongshu.ai.api.spark.entity.request.ChatCompletionMessage;
import com.hongshu.ai.api.spark.entity.request.ChatRequest;
import com.hongshu.ai.api.spark.entity.response.ChatSyncResponse;
import com.hongshu.ai.api.spark.entity.response.Usage;
import com.hongshu.ai.api.spark.enums.ModelEnum;
import com.hongshu.ai.api.spark.listener.SSEListener;
import com.hongshu.ai.common.enums.ChatModelEnum;
import com.hongshu.ai.common.enums.ChatRoleEnum;
import com.hongshu.ai.common.enums.ChatStatusEnum;
import com.hongshu.ai.domain.command.ChatMessageCommand;
import com.hongshu.ai.domain.dto.ChatMessageDTO;
import com.hongshu.common.core.exception.exceptionType.BusinessException;
import com.hongshu.common.core.validator.ValidatorUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 讯飞星火 接口实现
 *
 * @author: Yang
 * @date: 2024/5/30
 * @version: 1.1.7
 */
@Slf4j
@Service
public class SparkServiceImpl implements ModelService {

    private static SparkClient sparkClient;


    public SparkServiceImpl(SparkClient SparkClient) {
        SparkServiceImpl.sparkClient = SparkClient;
    }

    @Override
    @SneakyThrows
    public ChatMessageCommand chat(List<ChatMessageDTO> chatMessages, Boolean isDraw, Long chatId, String version) {
        if (ValidatorUtil.isNull(sparkClient)) {
            throw new BusinessException("讯飞星火无有效token，请切换其他模型进行聊天");
        }
        List<ChatCompletionMessage> messages = new ArrayList<>();
        chatMessages.stream().forEach(v -> {
            ChatCompletionMessage currentMessage = new ChatCompletionMessage(v.getRole(), v.getContent());
            messages.add(currentMessage);
        });
        // 构造请求
        ChatRequest chatRequest = ChatRequest.builder()
                .messages(messages)
                // 模型回答的tokens的最大长度,非必传,取值为[1,4096],默认为2048
                .maxTokens(2048)
                // 核采样阈值。用于决定结果随机性,取值越高随机性越强即相同的问题得到的不同答案的可能性越高 非必传,取值为[0,1],默认为0.5
                .temperature(0.5)
                // 指定请求版本，默认使用最新2.0版本
                .apiVersion(ValidatorUtil.isNotNull(version) ? ModelEnum.getEnum(version) : ModelEnum.Lite)
                .chatId(chatId.toString())
                .build();
        ChatSyncResponse response = sparkClient.chat(chatRequest);
        if (!response.isSuccess()) {
            throw new LLMException(response.getErrTxt());
        }
        Usage textUsage = response.getTextUsage();
        ChatMessageCommand chatMessage = ChatMessageCommand.builder().chatId(chatId).messageId(UUID.fastUUID().toString())
                .model(ChatModelEnum.SPARK.getValue()).modelVersion(ModelEnum.Lite.getVersion())
                .content(response.getContent()).role(ChatRoleEnum.ASSISTANT.getValue())
                .status(ChatStatusEnum.SUCCESS.getValue()).appKey(sparkClient.apiKey).usedTokens(Long.valueOf(textUsage.getTotalTokens()))
                .response(JSON.toJSONString(response))
                .build();
        return chatMessage;
    }

    @Override
    @SneakyThrows
    public Boolean streamChat(HttpServletResponse response, SseEmitter sseEmitter, List<ChatMessageDTO> chatMessages, Boolean isWs, Boolean isDraw,
                              Long chatId, String conversationId, String prompt, String version, String uid) {
        if (ValidatorUtil.isNull(sparkClient.appid)) {
            throw new BusinessException("未加载到密钥信息");
        }
        List<ChatCompletionMessage> messages = new ArrayList<>();
        chatMessages.stream().forEach(v -> {
            ChatCompletionMessage currentMessage = new ChatCompletionMessage(v.getRole(), v.getContent());
            messages.add(currentMessage);
        });
        ChatRequest chatRequest = ChatRequest.builder()
                .messages(messages)
                // 模型回答的tokens的最大长度,非必传,取值为[1,4096],默认为2048
                .maxTokens(2048)
                // 核采样阈值。用于决定结果随机性,取值越高随机性越强即相同的问题得到的不同答案的可能性越高 非必传,取值为[0,1],默认为0.5
                .temperature(0.5)
                // 指定请求版本，默认使用最新2.0版本
                .apiVersion(ValidatorUtil.isNotNull(version) ? ModelEnum.getEnum(version) : ModelEnum.Lite)
                .chatId(chatId.toString())
                .build();
        chatRequest.getHeader().setAppId(sparkClient.appid);
//        chatRequest.getHeader().setUid(uid);
        SSEListener sseListener = new SSEListener(chatRequest, response, chatId, conversationId, uid, version, isWs);
        try {
            sparkClient.streamChat(chatRequest, sseListener);
        } catch (Exception e) {
            log.error("讯飞星火流式聊天异常：{}", e.getMessage());
            // 如果发生异常，设置错误状态
            sseListener.setError(true);
            sseListener.setErrTxt("讯飞星火接口请求失败：" + e.getMessage());
            return true; // 返回true表示有错误
        }
        if (isWs) {
            return false;
        }
        sseListener.getCountDownLatch().await();
        return sseListener.getError();
    }

}
