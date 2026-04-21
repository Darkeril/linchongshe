package com.hongshu.ai.api.base.service.impl;

import com.alibaba.fastjson.JSON;
import com.hongshu.ai.api.base.entity.ChatData;
import com.hongshu.ai.api.base.exception.LLMException;
import com.hongshu.ai.api.base.service.ModelService;
import com.hongshu.ai.api.wenxin.WenXinClient;
import com.hongshu.ai.api.wenxin.entity.request.ChatCompletionMessage;
import com.hongshu.ai.api.wenxin.entity.request.ImagesBody;
import com.hongshu.ai.api.wenxin.entity.response.ChatResponse;
import com.hongshu.ai.api.wenxin.entity.response.ImageResponse;
import com.hongshu.ai.api.wenxin.enums.ModelEnum;
import com.hongshu.ai.api.wenxin.listener.SSEListener;
import com.hongshu.ai.common.enums.ChatContentEnum;
import com.hongshu.ai.common.enums.ChatModelEnum;
import com.hongshu.ai.common.enums.ChatRoleEnum;
import com.hongshu.ai.common.enums.ChatStatusEnum;
import com.hongshu.ai.domain.command.ChatMessageCommand;
import com.hongshu.ai.domain.dto.ChatMessageDTO;
import com.hongshu.ai.service.GptService;
import com.hongshu.common.core.enums.ResponseInfo;
import com.hongshu.common.core.validator.ValidatorUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 文心一言 接口实现
 *
 * @author: Yang
 * @date: 2024/5/30
 * @version: 1.1.7
 */
@Slf4j
@Service
public class WenXinServiceImpl implements ModelService {

    private static WenXinClient wenXinClient;
    private static GptService gptService;


    @Autowired
    public WenXinServiceImpl(GptService gptService, WenXinClient wenXinClient) {
        WenXinServiceImpl.gptService = gptService;
        WenXinServiceImpl.wenXinClient = wenXinClient;
    }

    @Override
    public ChatMessageCommand chat(List<ChatMessageDTO> chatMessages, Boolean isDraw, Long chatId, String version) {
        if (ValidatorUtil.isNull(wenXinClient)) {
            log.error("文心一言无有效token，请切换其他模型进行聊天");
            return null;
        }
        List<ChatCompletionMessage> messages = new ArrayList<>();
        chatMessages.stream().filter(d -> !d.getRole().equals(ChatRoleEnum.SYSTEM.getValue())).forEach(v -> {
            messages.add(ChatCompletionMessage.builder().role(v.getRole()).content(v.getContent()).build());
        });
        ModelEnum modelEnum = ModelEnum.ERNIE_Bot_turbo;
        if (ValidatorUtil.isNotNull(version)) {
            modelEnum = ModelEnum.getEnum(version);
        }
        if (ValidatorUtil.isNull(modelEnum)) {
            log.error("未知的模型");
            return null;
        }
        ResponseInfo<ChatResponse> wenxinResponse = wenXinClient.chat(messages, modelEnum);
        if (!wenxinResponse.isSuccess()) {
            throw new LLMException(wenxinResponse.getMsg());
        }
        ChatResponse response = wenxinResponse.getData();
        ChatMessageCommand chatMessage = ChatMessageCommand.builder().chatId(chatId).messageId(response.getId())
                .model(ChatModelEnum.WENXIN.getValue()).modelVersion(modelEnum.getLabel())
                .content(response.getResult()).role(ChatRoleEnum.ASSISTANT.getValue())
                .status(ChatStatusEnum.SUCCESS.getValue()).appKey(wenXinClient.getApiKey()).usedTokens(response.getUsage().getTotalTokens())
                .response(JSON.toJSONString(response))
                .build();
        return chatMessage;
    }

    @Override
    @SneakyThrows
    public Boolean streamChat(HttpServletResponse response, SseEmitter sseEmitter, List<ChatMessageDTO> chatMessages, Boolean isWs, Boolean isDraw,
                              Long chatId, String conversationId, String prompt, String version, String uid) {
        if (ValidatorUtil.isNull(wenXinClient.getApiKey())) {
            log.error("文心一言未加载到密钥信息");
            return true; // 返回true表示有错误
        }
        if (isDraw) {
            return image(sseEmitter, response, chatId, conversationId, prompt);
        }
        List<ChatCompletionMessage> messages = new ArrayList<>();
        chatMessages.stream().filter(d -> !d.getRole().equals(ChatRoleEnum.SYSTEM.getValue())).forEach(v -> {
            messages.add(ChatCompletionMessage.builder().role(v.getRole()).content(v.getContent()).build());
        });
        SSEListener sseListener = new SSEListener(response, sseEmitter, chatId, conversationId, ChatModelEnum.WENXIN.getValue(), version, uid, isWs);
        ModelEnum model = ValidatorUtil.isNotNull(version) ? ModelEnum.getEnum(version) : ModelEnum.ERNIE_Bot_turbo;
        if (ValidatorUtil.isNull(model)) {
            log.error("文心大模型不存在，请检查模型名称。");
            return true; // 返回true表示有错误
        }
        wenXinClient.streamChat(messages, sseListener, model);
        if (isWs) {
            return false;
        }
        sseListener.getCountDownLatch().await();
        return sseListener.getError();
    }

    /**
     * 文生图
     *
     * @param
     * @param prompt
     * @return
     */
    @SneakyThrows
    private Boolean image(SseEmitter sseEmitter, HttpServletResponse response, Long chatId, String conversationId, String prompt) {
        // 通知客户端返回为图片
        ChatData chatData = ChatData.builder().id(conversationId).conversationId(conversationId)
                .parentMessageId(conversationId)
                .role(ChatRoleEnum.ASSISTANT.getValue()).contentType(ChatContentEnum.IMAGE.getValue()).build();
        response.getWriter().write(JSON.toJSONString(chatData));
        response.getWriter().flush();

        // 请求生成图片 并返回base64信息
        ImagesBody body = ImagesBody.builder().prompt(prompt).build();
        ResponseInfo<ImageResponse> imageResponse = wenXinClient.image(body);
        if (!imageResponse.isSuccess()) {
            log.error("文心一言图片生成失败：{}", imageResponse.getMsg());
            return true; // 返回true表示有错误
        }
        response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(HttpStatus.OK.value());
        ImageResponse image = imageResponse.getData();
        ChatMessageCommand chatMessage = ChatMessageCommand.builder().chatId(chatId).messageId(image.getId()).parentMessageId(conversationId)
                .model(ChatModelEnum.WENXIN.getValue()).modelVersion(ModelEnum.STABLE_DIFFUSION_XL.getLabel())
                .content(JSON.toJSONString(image.getData())).contentType(ChatContentEnum.IMAGE.getValue()).role(ChatRoleEnum.ASSISTANT.getValue())
                .status(ChatStatusEnum.SUCCESS.getValue()).usedTokens(image.getUsage().getTotalTokens())
                .build();
        gptService.saveChatMessage(chatMessage);
        chatData.setContent(image.getData());
        response.getWriter().write("\n" + JSON.toJSONString(chatData));
        response.getWriter().flush();
        return false;
    }

}
