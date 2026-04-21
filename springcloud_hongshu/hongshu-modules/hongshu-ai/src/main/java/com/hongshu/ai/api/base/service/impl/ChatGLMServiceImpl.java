package com.hongshu.ai.api.base.service.impl;

import com.alibaba.fastjson.JSON;
import com.hongshu.ai.api.base.exception.LLMException;
import com.hongshu.ai.api.base.service.ModelService;
import com.hongshu.ai.api.chatglm.ChatGLMClient;
import com.hongshu.ai.common.enums.ChatModelEnum;
import com.hongshu.ai.common.enums.ChatRoleEnum;
import com.hongshu.ai.common.enums.ChatStatusEnum;
import com.hongshu.ai.domain.command.ChatMessageCommand;
import com.hongshu.ai.domain.dto.ChatMessageDTO;
import com.hongshu.common.core.validator.ValidatorUtil;
import lombok.extern.slf4j.Slf4j;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.ChatCompletionRequest;
import com.zhipu.oapi.service.v4.model.ChatMessage;
import com.zhipu.oapi.service.v4.model.ModelApiResponse;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 智谱清言 接口实现
 *
 * @author: Yang
 * @date: 2024/5/30
 * @version: 1.1.7
 */
@Slf4j
@Service
public class ChatGLMServiceImpl implements ModelService {

    private static ChatGLMClient chatGLMClient;


    @Autowired
    public ChatGLMServiceImpl(ChatGLMClient chatGLMClient) {
        ChatGLMServiceImpl.chatGLMClient = chatGLMClient;
    }

    /**
     * 智谱清言模型聊天
     * 支持超拟人大模型，将modelId替换为characterglm
     * query中增加参数{meta：{"user_info": "我是陆星辰","bot_info:"苏梦远，本名苏远心，是一位当红的国内女歌手及演员。", "user_name": "陆星辰", "bot_name": "苏梦远"}}
     *
     * @param chatId
     * @param chatMessages
     * @return
     */
    @Override
    public ChatMessageCommand chat(List<ChatMessageDTO> chatMessages, Boolean isDraw, Long chatId, String version) {
        if (ValidatorUtil.isNull(chatGLMClient)) {
            log.error("智谱清言无有效token，请切换其他模型进行聊天");
            return null;
        }
        List<ChatMessage> messages = new ArrayList<>();
        chatMessages.stream().filter(d -> !d.getRole().equals(ChatRoleEnum.SYSTEM.getValue())).forEach(v -> {
            messages.add(new ChatMessage(v.getRole(), v.getContent()));
        });
        String modelVaersion = ValidatorUtil.isNotNull(version) ? version : Constants.ModelChatGLM3TURBO;
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(modelVaersion)
                .messages(messages)
                .build();
        // 关闭搜索示例
        //  modelApiRequest.setRef(new HashMap<String, Object>(){{
        //    put("enable",false);
        // }});
        // 开启搜索示例
        // modelApiRequest.setRef(new HashMap<String, Object>(){{
        //    put("enable",true);
        //    put("search_query","历史");
        //  }});
        ModelApiResponse response = chatGLMClient.chat(request);
        if (!response.isSuccess()) {
            throw new LLMException(response.getMsg());
        }
        ChatMessageCommand chatMessage = ChatMessageCommand.builder().chatId(chatId).messageId(response.getData().getTaskId())
                .model(ChatModelEnum.CHATGLM.getValue()).modelVersion(modelVaersion)
                .content(response.getData().getChoices().get(0).getDelta().getContent()).role(ChatRoleEnum.ASSISTANT.getValue())
                .status(ChatStatusEnum.SUCCESS.getValue()).appKey(chatGLMClient.getApiKey()).usedTokens(Long.valueOf(response.getData().getUsage().getTotalTokens()))
                .response(JSON.toJSONString(response))
                .build();
        return chatMessage;
    }

    @Override
    @SneakyThrows
    public Boolean streamChat(HttpServletResponse response, SseEmitter sseEmitter, List<ChatMessageDTO> chatMessages, Boolean isWs, Boolean isDraw,
                              Long chatId, String conversationId, String prompt, String version, String uid) {
        if (ValidatorUtil.isNull(chatGLMClient.getApiKey())) {
            log.error("智谱清言未加载到密钥信息");
            return true; // 返回true表示有错误
        }
        List<ChatMessage> messages = new ArrayList<>();
        chatMessages.stream().filter(d -> !d.getRole().equals(ChatRoleEnum.SYSTEM.getValue())).forEach(v -> {
            messages.add(new ChatMessage(v.getRole(), v.getContent()));
        });
        String modelVaersion = ValidatorUtil.isNotNull(version) ? version : Constants.ModelChatGLM3TURBO;
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(modelVaersion)
                .stream(Boolean.TRUE)
                .messages(messages)
                .build();
        try {
            Boolean flag = chatGLMClient.streamChat(response, chatCompletionRequest, chatId, conversationId, modelVaersion, uid, isWs);
            if (isWs) {
                return false;
            }
            return flag;
        } catch (Exception e) {
            log.error("智谱清言流式聊天异常：{}", e.getMessage());
            return true; // 返回true表示有错误
        }
    }

}
