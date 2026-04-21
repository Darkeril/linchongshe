package com.hongshu.ai.api.moonshot.listener;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.hongshu.ai.api.base.entity.ChatData;
import com.hongshu.ai.api.base.websocket.WebsocketServer;
import com.hongshu.ai.api.base.websocket.constant.FunctionCodeConstant;
import com.hongshu.ai.api.base.websocket.entity.WebSocketData;
import com.hongshu.ai.api.moonshot.entity.response.ChatStreamResponse;
import com.hongshu.ai.api.moonshot.entity.response.Usage;
import com.hongshu.ai.common.enums.ChatContentEnum;
import com.hongshu.ai.common.enums.ChatModelEnum;
import com.hongshu.ai.common.enums.ChatRoleEnum;
import com.hongshu.ai.common.enums.ChatStatusEnum;
import com.hongshu.ai.domain.command.ChatMessageCommand;
import com.hongshu.ai.service.GptService;
import com.hongshu.common.core.utils.ApplicationContextUtil;
import com.hongshu.common.core.validator.ValidatorUtil;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * 月之暗面 流式监听处理
 *
 * @author: Yang
 * @date: 2024/3/25
 * @version: 1.2.0
 */
@Slf4j
@NoArgsConstructor(force = true)
public class SSEListener {
    private HttpServletResponse response;
    private StringBuffer output = new StringBuffer();
    private Long chatId;
    private String parentMessageId;
    private String conversationId;
    private String finishReason = "stop" ;
    private String version;
    private String uid;
    private Boolean isWs = false;

    /**
     * 流式响应
     *
     * @param
     * @param
     */
    public SSEListener(HttpServletResponse response, Long chatId, String parentMessageId, String version, String uid, Boolean isWs) {
        this.response = response;
        this.chatId = chatId;
        this.parentMessageId = parentMessageId;
        this.version = version;
        this.uid = uid;
        this.isWs = isWs;
        if (response == null) {
            log.error("客户端非sse推送");
            return;
        }
        if (!isWs) {
            response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
        }
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(HttpStatus.OK.value());
        log.info("月之暗面建立sse连接...");
    }

    /**
     * 流失回答
     *
     * @param
     */
    public Boolean streamChat(Response response) {
        try {
            ChatStreamResponse chatMessageAccumulator = mapStreamToAccumulator(response)
                    .doOnNext(accumulator -> {
                        if (accumulator.getChoices() != null && accumulator.getChoices().get(0).getDelta().getContent() != null) {
                            log.info("月之暗面返回，数据：{}", accumulator.getChoices().get(0).getDelta().getContent());
                            output.append(accumulator.getChoices().get(0).getDelta().getContent()).toString();
                            // 向客户端发送信息
                            output();
                        }
                    }).doOnComplete(System.out::println).lastElement().blockingGet();
            this.conversationId = chatMessageAccumulator.getId();
            Usage usage = chatMessageAccumulator.getChoices().get(0).getUsage();
            log.info("月之暗面回数据结束了:{}", JSON.toJSONString(chatMessageAccumulator));
            ChatMessageCommand chatMessage = ChatMessageCommand.builder().chatId(chatId).messageId(conversationId).parentMessageId(parentMessageId)
                    .model(ChatModelEnum.MOONSHOT.getValue()).modelVersion(version)
                    .content(output.toString()).contentType(ChatContentEnum.TEXT.getValue()).role(ChatRoleEnum.ASSISTANT.getValue()).finishReason(finishReason)
                    .status(ChatStatusEnum.SUCCESS.getValue()).appKey("").usedTokens(Long.valueOf(usage.getTotalTokens()))
                    .build();
            ApplicationContextUtil.getBean(GptService.class).saveChatMessage(chatMessage);
            return false;
        } catch (Exception e) {
            log.error("月之暗面流式响应处理异常", e);
            return true; // 返回true表示有错误
        }
    }

    private void output() {
        try {
            String text = output.toString();
            ChatData chatData = ChatData.builder().id(conversationId).conversationId(conversationId)
                    .parentMessageId(parentMessageId)
                    .role(ChatRoleEnum.ASSISTANT.getValue()).content(text).build();
            if (isWs) {
                WebSocketData wsData = WebSocketData.builder().functionCode(FunctionCodeConstant.MESSAGE).message(chatData).build();
                WebsocketServer.sendMessageByUserId(uid, JSON.toJSONString(wsData));
            } else {
                response.getWriter().write(ValidatorUtil.isNull(text) ? JSON.toJSONString(chatData) : "\n" + JSON.toJSONString(chatData));
                response.getWriter().flush();
            }
        } catch (IOException e) {
            log.error("月之暗面消息输出错误", e);
            // 不抛出异常，只是记录错误
        }
    }

    public static Flowable<ChatStreamResponse> mapStreamToAccumulator(Response response) {
        return Flowable.create(emitter -> {
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                emitter.onError(new RuntimeException("Response body is null"));
                return;
            }
            String line;
            while ((line = responseBody.source().readUtf8Line()) != null) {
                if (line.startsWith("data:")) {
                    line = line.substring(5);
                    line = line.trim();
                }
                if (Objects.equals(line, "[DONE]")) {
                    emitter.onComplete();
                    return;
                }
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                Gson gson = new Gson();
                ChatStreamResponse streamResponse = gson.fromJson(line, ChatStreamResponse.class);
                emitter.onNext(streamResponse);
            }
            emitter.onComplete();
        }, BackpressureStrategy.BUFFER);
    }


}
