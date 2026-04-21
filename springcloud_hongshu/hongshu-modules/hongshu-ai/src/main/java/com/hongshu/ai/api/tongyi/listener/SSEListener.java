package com.hongshu.ai.api.tongyi.listener;

import com.alibaba.dashscope.aigc.generation.GenerationOutput;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.ResultCallback;
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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Semaphore;

/**
 * 通义千问监听sse流式响应处理
 *
 * @author: Yang
 * @date: 2023/12/4
 */
@Slf4j
public class SSEListener extends ResultCallback<GenerationResult> {
    private Semaphore semaphore;
    private HttpServletResponse response;
    private Long chatId;
    private String parentMessageId;
    private String conversationId;
    private String finishReason;
    private String version;
    private Boolean error;
    private String errTxt;
    private String uid;
    private Boolean isWs = false;


    public SSEListener(HttpServletResponse response, Semaphore semaphore, Long chatId, String parentMessageId, String uid, String version, Boolean isWs) {
        this.response = response;
        this.semaphore = semaphore;
        this.chatId = chatId;
        this.parentMessageId = parentMessageId;
        if (!isWs) {
            this.response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
        }
        this.response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        this.response.setStatus(HttpStatus.OK.value());
        this.uid = uid;
        this.version = version;
        this.isWs = isWs;
        this.error = false;
    }

    @SneakyThrows
    @Override
    public void onEvent(GenerationResult result) {
        log.error("通义千问SSE返回，数据：{}", JSON.toJSONString(result));
        GenerationOutput.Choice choice = result.getOutput().getChoices().get(0);
        String text = choice.getMessage().getContent();
        if (ValidatorUtil.isNotNull(choice.getFinishReason())) {
            finishReason = choice.getFinishReason();
            log.info("通义千问返回数据结束了");
            ChatMessageCommand chatMessage = ChatMessageCommand.builder().chatId(chatId).messageId(result.getRequestId()).parentMessageId(parentMessageId)
                    .model(ChatModelEnum.TONGYI.getValue()).modelVersion(version)
                    .content(choice.getMessage().getContent()).contentType(ChatContentEnum.TEXT.getValue()).role(ChatRoleEnum.ASSISTANT.getValue()).finishReason(finishReason)
                    .status(ChatStatusEnum.SUCCESS.getValue()).appKey("").usedTokens(Long.valueOf(result.getUsage().getOutputTokens() + result.getUsage().getInputTokens()))
                    .build();
            ApplicationContextUtil.getBean(GptService.class).saveChatMessage(chatMessage);
            return;
        }
        ChatData chatData = ChatData.builder().id(result.getRequestId()).conversationId(result.getRequestId())
                .parentMessageId(parentMessageId)
                .role(ChatRoleEnum.ASSISTANT.getValue()).content(text).build();
        if (isWs) {
            WebSocketData wsData = WebSocketData.builder().functionCode(FunctionCodeConstant.MESSAGE).message(chatData).build();
            WebsocketServer.sendMessageByUserId(uid, JSON.toJSONString(wsData));
        } else {
            response.getWriter().write(ValidatorUtil.isNull(text) ? JSON.toJSONString(chatData) : "\n" + JSON.toJSONString(chatData));
            response.getWriter().flush();
        }
    }

    @Override
    public void onComplete() {
        log.info("通义千问输出完成");
        semaphore.release();
    }

    @SneakyThrows
    @Override
    public void onError(Exception e) {
        // 检查是否是正常的响应结束，而不是真正的错误
        if (e.getMessage() != null && e.getMessage().contains("statusCode\":200")) {
            log.info("通义千问响应正常结束，不是错误");
            semaphore.release();
            return;
        }
        
        log.error("通义千问连接异常，异常：{}", e.getMessage());
        ChatData chatData = ChatData.builder().id(conversationId).conversationId(conversationId)
                .parentMessageId(parentMessageId).role(ChatRoleEnum.ASSISTANT.getValue()).content("通义千问接口请求失败，无法响应！")
                .contentType(ChatContentEnum.TEXT.getValue()).finish(true).build();
        this.error = true;
        this.errTxt = "通义千问接口连接异常" ;
        this.response.getWriter().write(JSON.toJSONString(chatData));
        this.response.getWriter().flush();
        semaphore.release();
    }

    public Boolean getError() {
        return error;
    }

    public String getErrTxt() {
        return errTxt;
    }

}
