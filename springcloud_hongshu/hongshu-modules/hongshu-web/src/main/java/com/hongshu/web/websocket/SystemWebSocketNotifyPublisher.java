package com.hongshu.web.websocket;

import cn.hutool.json.JSONUtil;
import com.hongshu.common.core.domain.Message;
import com.hongshu.web.config.WebSocketSystemNotifyRedisConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 将系统通知发布到 Redis，由各节点订阅后向本地 WebSocket 会话广播。
 */
@Component
@RequiredArgsConstructor
public class SystemWebSocketNotifyPublisher {

    private final StringRedisTemplate stringRedisTemplate;

    public void publish(Message message) {
        stringRedisTemplate.convertAndSend(WebSocketSystemNotifyRedisConfig.SYSTEM_NOTIFY_CHANNEL, JSONUtil.toJsonStr(message));
    }
}
