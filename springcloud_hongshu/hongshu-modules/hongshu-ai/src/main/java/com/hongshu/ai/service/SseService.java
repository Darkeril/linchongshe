package com.hongshu.ai.service;

import com.hongshu.ai.common.security.UserDetail;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 描述：
 *
 * @author https:www.unfbx.com
 * @date 2023-04-08
 */
public interface SseService {

    /**
     * 创建SSE
     *
     * @param uid
     * @return
     */
    SseEmitter createSse(String uid);

    /**
     * 关闭SSE
     *
     * @param uid
     */
    void closeSse(String uid);

    /**
     * 客户端发送消息到服务端
     */
    void sseChat(UserDetail user, String conversationId, HttpServletResponse response);

}
