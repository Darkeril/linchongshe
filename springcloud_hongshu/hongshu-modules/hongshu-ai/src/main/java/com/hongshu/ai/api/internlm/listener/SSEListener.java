package com.hongshu.ai.api.internlm.listener;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.hongshu.ai.api.base.entity.ChatData;
import com.hongshu.ai.api.base.websocket.WebsocketServer;
import com.hongshu.ai.api.base.websocket.constant.FunctionCodeConstant;
import com.hongshu.ai.api.base.websocket.entity.WebSocketData;
import com.hongshu.ai.api.internlm.entity.response.ChatStreamResponse;
import com.hongshu.ai.common.enums.ChatContentEnum;
import com.hongshu.ai.common.enums.ChatModelEnum;
import com.hongshu.ai.common.enums.ChatRoleEnum;
import com.hongshu.ai.common.enums.ChatStatusEnum;
import com.hongshu.ai.common.exception.ErrorException;
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
 * 书生浦语 流式监听处理
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
    private Boolean error;
    private String errTxt;
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
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        }
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(HttpStatus.OK.value());
        log.info("书生浦语建立sse连接...");
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
                        try {
                            if (accumulator != null && accumulator.getChoices() != null && 
                                !accumulator.getChoices().isEmpty() && 
                                accumulator.getChoices().get(0) != null &&
                                accumulator.getChoices().get(0).hasValidMessage()) {
                                String content = accumulator.getChoices().get(0).getDelta().getContent();
                                log.info("书生浦语返回，数据：{}", content);
                                output.append(content);
                                // 向客户端发送信息
                                output();
                            } else {
                                log.debug("书生浦语流式数据无有效内容，跳过处理");
                            }
                        } catch (Exception e) {
                            log.error("书生浦语处理单条流式数据异常：{}", e.getMessage(), e);
                        }
                    })
                    .doOnError(error -> {
                        log.error("书生浦语流式数据流异常：{}", error.getMessage(), error);
                    })
                    .doOnComplete(() -> {
                        log.info("书生浦语流式数据流完成");
                    })
                    .lastElement()
                    .blockingGet();
            
            if (chatMessageAccumulator != null) {
                this.conversationId = chatMessageAccumulator.getId();
                log.info("书生浦语回数据结束了:{}", JSON.toJSONString(chatMessageAccumulator));
                ChatMessageCommand chatMessage = ChatMessageCommand.builder().chatId(chatId).messageId(conversationId).parentMessageId(parentMessageId)
                        .model(ChatModelEnum.INTERNLM.getValue()).modelVersion(version)
                        .content(output.toString()).contentType(ChatContentEnum.TEXT.getValue()).role(ChatRoleEnum.ASSISTANT.getValue()).finishReason(finishReason)
                        .status(ChatStatusEnum.SUCCESS.getValue()).appKey("").usedTokens(0L)
                        .build();
                ApplicationContextUtil.getBean(GptService.class).saveChatMessage(chatMessage);
            } else {
                log.warn("书生浦语流式响应返回空结果");
                this.error = true;
                this.errTxt = "书生浦语流式响应返回空结果";
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("书生浦语流式响应处理异常：{}", e.getMessage(), e);
            this.error = true;
            this.errTxt = "书生浦语流式响应处理失败：" + e.getMessage();
            return true;
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
            log.error("消息错误", e);
            throw new ErrorException();
        }
    }

    public Boolean getError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
    }

    public String getErrTxt() {
        return errTxt;
    }

    public void setErrTxt(String errTxt) {
        this.errTxt = errTxt;
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
                try {
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
                    log.debug("书生浦语流式数据行: {}", line);
                    Gson gson = new Gson();
                    ChatStreamResponse streamResponse = gson.fromJson(line, ChatStreamResponse.class);
                    emitter.onNext(streamResponse);
                } catch (Exception e) {
                    log.error("书生浦语解析流式数据异常，数据行: {}, 异常: {}", line, e.getMessage());
                    emitter.onError(e);
                    return;
                }
            }
            emitter.onComplete();
        }, BackpressureStrategy.BUFFER);
    }

}
