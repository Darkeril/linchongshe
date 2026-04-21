package com.hongshu.web.websocket;

import cn.hutool.json.JSONUtil;
import com.hongshu.common.core.domain.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 *订阅 Redis 系统通知频道，在本地进程内向所有已连接会话广播。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SystemWebSocketNotifyListener {

    private final WebSocketServer webSocketServer;

    /**
     * 由 {@link org.springframework.data.redis.listener.adapter.MessageListenerAdapter} 调用，参数为 String body。
     */
    public void onRedisMessage(String json) {
        if (json == null || json.isEmpty()) {
            return;
        }
        try {
            Message payload = JSONUtil.toBean(json, Message.class);
            if (payload.getMsgType() == null || payload.getMsgType() != 99) {
                return;
            }
            webSocketServer.broadcast(payload);
        } catch (Exception e) {
            log.error("Redis 系统通知解析或广播失败: {}", json, e);
        }
    }
}
