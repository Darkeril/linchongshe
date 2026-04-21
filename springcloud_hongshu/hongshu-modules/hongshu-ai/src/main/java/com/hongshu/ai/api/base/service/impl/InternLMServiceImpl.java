package com.hongshu.ai.api.base.service.impl;

import com.hongshu.ai.api.base.service.ModelService;
import com.hongshu.ai.api.internlm.InternlmClient;
import com.hongshu.ai.api.internlm.constant.ModelConstant;
import com.hongshu.ai.api.internlm.entity.request.ChatCompletion;
import com.hongshu.ai.api.internlm.entity.request.ChatCompletionMessage;
import com.hongshu.ai.common.enums.ChatRoleEnum;
import com.hongshu.ai.domain.command.ChatMessageCommand;
import com.hongshu.ai.domain.dto.ChatMessageDTO;
import com.hongshu.common.core.validator.ValidatorUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 书生浦语 接口实现
 *
 * @author: Yang
 * @date: 2024/5/30
 * @version: 1.1.7
 */
@Slf4j
@Service
public class InternLMServiceImpl implements ModelService {

    private static InternlmClient internlmClient;


    @Autowired
    public InternLMServiceImpl(InternlmClient internlmClient) {
        InternLMServiceImpl.internlmClient = internlmClient;
    }

    @Override
    public ChatMessageCommand chat(List<ChatMessageDTO> chatMessages, Boolean isDraw, Long chatId, String version) {
        return null;
    }

    @Override
    @SneakyThrows
    public Boolean streamChat(HttpServletResponse response, SseEmitter sseEmitter, List<ChatMessageDTO> chatMessages, Boolean isWs, Boolean isDraw,
                              Long chatId, String conversationId, String prompt, String version, String uid) {
        if (ValidatorUtil.isNull(internlmClient.getApiKey())) {
            log.error("书生浦语未加载到密钥信息");
            return true; // 返回true表示有错误
        }
        List<ChatCompletionMessage> messages = new ArrayList<>();
        chatMessages.stream().filter(d -> !d.getRole().equals(ChatRoleEnum.SYSTEM.getValue())).forEach(v -> {
            messages.add(new ChatCompletionMessage(v.getRole(), v.getContent()));
        });
        String modelVaersion = ValidatorUtil.isNotNull(version) ? version.trim() : ModelConstant.LATEST;
        log.info("书生浦语模型版本: '{}'", modelVaersion);
        ChatCompletion chatCompletion = ChatCompletion.builder()
                .model(modelVaersion)
                .messages(messages)
                .temperature(0.7f)
                .topP(0.9f)
                .requestOutputLen(2048)
                .build();
        try {
            return internlmClient.streamChat(response, chatCompletion, chatId, conversationId, modelVaersion, uid, isWs);
        } catch (Exception e) {
            log.error("书生浦语流式聊天异常：{}", e.getMessage());
            return true; // 返回true表示有错误
        }
    }

}
