package com.hongshu.web.rocketmq;

import com.hongshu.web.domain.message.StatisticsUpdateMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * 统计生产者服务
 * 发送统计更新消息到RocketMQ
 *
 * @author: hongshu
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "rocketmq.name-server")
public class StatisticsProducerService {

    @Autowired(required = false)
    private RocketMQTemplate rocketMQTemplate;

    // 主题名称
    private static final String TOPIC = "statistics-update-topic";

    /**
     * 发送统计更新消息
     *
     * @param type   统计类型：user, note, comment, product, visit
     * @param action 操作类型：create, delete
     */
    public void sendStatisticsUpdateMessage(String type, String action) {
        sendStatisticsUpdateMessage(type, action, null);
    }

    /**
     * 发送统计更新消息
     *
     * @param type   统计类型
     * @param action 操作类型
     * @param id     关联ID
     */
    public void sendStatisticsUpdateMessage(String type, String action, String id) {
        if (rocketMQTemplate == null) {
            log.debug("RocketMQ未配置，跳过统计消息发送");
            return;
        }

        StatisticsUpdateMessage message = StatisticsUpdateMessage.builder()
                .type(type)
                .action(action)
                .id(id)
                .timestamp(System.currentTimeMillis())
                .build();

        try {
            rocketMQTemplate.convertAndSend(TOPIC, message);
            log.debug("发送统计更新消息: type={}, action={}", type, action);
        } catch (Exception e) {
            log.error("发送统计更新消息失败: type={}, action={}", type, action, e);
            // 统计消息失败不影响业务，只记录日志
        }
    }
}

