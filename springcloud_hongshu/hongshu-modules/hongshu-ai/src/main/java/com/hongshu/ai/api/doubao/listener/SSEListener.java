package com.hongshu.ai.api.doubao.listener;

import com.alibaba.fastjson.JSON;
import com.hongshu.ai.api.base.entity.ChatData;
import com.hongshu.ai.api.base.websocket.WebsocketServer;
import com.hongshu.ai.api.base.websocket.constant.FunctionCodeConstant;
import com.hongshu.ai.api.base.websocket.entity.WebSocketData;
import com.hongshu.ai.common.enums.ChatContentEnum;
import com.hongshu.ai.common.enums.ChatModelEnum;
import com.hongshu.ai.common.enums.ChatRoleEnum;
import com.hongshu.ai.common.enums.ChatStatusEnum;
import com.hongshu.ai.domain.command.ChatMessageCommand;
import com.hongshu.ai.service.GptService;
import com.hongshu.common.core.utils.ApplicationContextUtil;
import com.hongshu.common.core.validator.ValidatorUtil;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionChunk;
import io.reactivex.Flowable;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 豆包流式接口处理
 *
 * @author: Yang
 * @date: 2023/12/4
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
            response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
        }
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(HttpStatus.OK.value());
        log.info("豆包建立sse连接...");
    }

    /**
     * 流式响应
     *
     * @param
     */
    public Boolean streamChat(Flowable<ChatCompletionChunk> chunks) {
        try {
            ChatCompletionChunk chatCompletionChunk = chunks.doOnError(Throwable::printStackTrace)
                    .doOnNext(
                            choice -> {
                                if (choice.getChoices().size() > 0) {
                                    log.info("豆包返回，数据：{}", choice.getChoices().get(0).getMessage().getContent());
                                    output.append(choice.getChoices().get(0).getMessage().getContent()).toString();
                                    // 向客户端发送信息
                                    output();
                                }
                            }
                    ).doOnComplete(System.out::println)
                    .lastElement()
                    .blockingGet();
            
            this.conversationId = chatCompletionChunk.getId();
            log.info("豆包返回数据结束了:{}", JSON.toJSONString(chatCompletionChunk));
            ChatMessageCommand chatMessage = ChatMessageCommand.builder().chatId(chatId).messageId(conversationId).parentMessageId(parentMessageId)
                    .model(ChatModelEnum.DOUBAO.getValue()).modelVersion(version)
                    .content(output.toString()).contentType(ChatContentEnum.TEXT.getValue()).role(ChatRoleEnum.ASSISTANT.getValue()).finishReason(finishReason)
                    .status(ChatStatusEnum.SUCCESS.getValue()).appKey("").usedTokens(Long.valueOf(output.toString().length()))
                    .build();
            ApplicationContextUtil.getBean(GptService.class).saveChatMessage(chatMessage);
            return false;
        } catch (Exception e) {
            log.error("豆包流式响应处理异常", e);
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
            log.error("输出信息异常", e);
        }
    }

}
