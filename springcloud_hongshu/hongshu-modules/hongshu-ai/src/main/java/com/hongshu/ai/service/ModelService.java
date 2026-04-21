package com.hongshu.ai.service;

import com.hongshu.ai.domain.command.ChatMessageCommand;
import com.hongshu.ai.domain.dto.ChatMessageDTO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 大模型统一接口
 * 参考项目9的架构设计，提供统一的模型服务接口
 *
 * @author: myj
 * @date: 2024/4/1
 * @version: 1.0.0
 */
public interface ModelService {

    /**
     * 同步聊天响应
     *
     * @param chatMessages 聊天消息列表
     * @param isDraw 是否绘画
     * @param chatId 聊天ID
     * @param version 模型版本
     * @return 聊天消息命令
     */
    ChatMessageCommand chat(List<ChatMessageDTO> chatMessages, Boolean isDraw, Long chatId, String version);

    /**
     * 流式聊天响应
     *
     * @param response HTTP响应
     * @param sseEmitter SSE发射器
     * @param chatMessages 聊天消息列表
     * @param isWs 是否WebSocket
     * @param isDraw 是否绘画
     * @param chatId 聊天ID
     * @param conversationId 会话ID
     * @param prompt 提示词
     * @param version 模型版本
     * @param uid 用户ID
     * @return true 响应异常 false 响应正常
     */
    Boolean streamChat(HttpServletResponse response, SseEmitter sseEmitter, List<ChatMessageDTO> chatMessages, 
                      Boolean isWs, Boolean isDraw, Long chatId, String conversationId, String prompt, 
                      String version, String uid);

}