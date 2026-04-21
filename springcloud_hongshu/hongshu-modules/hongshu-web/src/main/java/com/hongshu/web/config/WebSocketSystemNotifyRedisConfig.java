package com.hongshu.web.config;

import com.hongshu.web.websocket.SystemWebSocketNotifyListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * 系统通知 WebSocket：经 Redis Pub/Sub 广播到所有 hongshu-web 节点，避免网关多实例下 * HTTP 推送落到「无用户连接」的实例导致 C 端收不到。
 */
@Configuration
public class WebSocketSystemNotifyRedisConfig {

    public static final String SYSTEM_NOTIFY_CHANNEL = "hongshu:websocket:system_notify";

    @Bean
    public RedisMessageListenerContainer webSocketSystemNotifyRedisContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter webSocketSystemNotifyListenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(webSocketSystemNotifyListenerAdapter, new ChannelTopic(SYSTEM_NOTIFY_CHANNEL));
        return container;
    }

    @Bean
    public MessageListenerAdapter webSocketSystemNotifyListenerAdapter(SystemWebSocketNotifyListener listener) {
        return new MessageListenerAdapter(listener, "onRedisMessage");
    }
}
