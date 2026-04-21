package com.hongshu.ai.api.base.service.impl;

import com.hongshu.ai.api.base.service.ModelService;
import com.hongshu.ai.api.moonshot.MoonshotClient;
import com.hongshu.ai.api.moonshot.constant.ModelConstant;
import com.hongshu.ai.api.moonshot.entity.request.ChatCompletion;
import com.hongshu.ai.api.moonshot.entity.request.ChatCompletionMessage;
import com.hongshu.ai.common.enums.ChatRoleEnum;
import com.hongshu.ai.domain.command.ChatMessageCommand;
import com.hongshu.ai.domain.dto.ChatMessageDTO;
import com.hongshu.common.core.exception.exceptionType.BusinessException;
import com.hongshu.common.core.validator.ValidatorUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 月之暗面 接口实现
 *
 * @author: Yang
 * @date: 2024/5/30
 * @version: 1.1.7
 */
@Service
public class MoonshotServiceImpl implements ModelService {

    private static MoonshotClient moonshotClient;


    @Autowired
    public MoonshotServiceImpl(MoonshotClient moonshotClient) {
        MoonshotServiceImpl.moonshotClient = moonshotClient;
    }

    @Override
    public ChatMessageCommand chat(List<ChatMessageDTO> chatMessages, Boolean isDraw, Long chatId, String version) {
        return null;
    }

    @Override
    @SneakyThrows
    public Boolean streamChat(HttpServletResponse response, SseEmitter sseEmitter, List<ChatMessageDTO> chatMessages, Boolean isWs, Boolean isDraw,
                              Long chatId, String conversationId, String prompt, String version, String uid) {
        if (ValidatorUtil.isNull(moonshotClient.getApiKey())) {
            throw new BusinessException("未加载到密钥信息");
        }
        List<ChatCompletionMessage> messages = new ArrayList<>();
        chatMessages.stream().filter(d -> !d.getRole().equals(ChatRoleEnum.SYSTEM.getValue())).forEach(v -> {
            messages.add(new ChatCompletionMessage(v.getRole(), v.getContent()));
        });
        String modelVaersion = ValidatorUtil.isNotNull(version) ? version : ModelConstant.API_MODEL_8K;
        ChatCompletion chatCompletion = ChatCompletion.builder()
                .model(modelVaersion)
                .messages(messages)
                .build();
        return moonshotClient.streamChat(response, chatCompletion, chatId, conversationId, modelVaersion, uid, isWs);
    }

}
