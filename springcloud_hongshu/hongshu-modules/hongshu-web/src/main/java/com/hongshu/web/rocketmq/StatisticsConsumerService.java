package com.hongshu.web.rocketmq;

import com.hongshu.web.domain.message.StatisticsUpdateMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * 统计消费者服务
 * 异步更新统计缓存
 *
 * @author: hongshu
 */
@Slf4j
@Service
// 添加条件注解，当配置为 false 时不加载
@ConditionalOnProperty(name = "rocketmq.consumer.listen", havingValue = "true")
@RocketMQMessageListener(
        topic = "statistics-update-topic",
        consumerGroup = "statistics-consumer-group"
)
public class StatisticsConsumerService implements RocketMQListener<StatisticsUpdateMessage> {

    @Autowired
    private StatisticsCacheService cacheService;

    @Override
    public void onMessage(StatisticsUpdateMessage message) {
        try {
            log.debug("收到统计更新消息: type={}, action={}",
                    message.getType(), message.getAction());

            // 更新Redis缓存中的计数器
            updateStatisticsCache(message);

            // 清除相关的聚合统计缓存（需要重新计算）
            clearRelatedCache(message.getType());

        } catch (Exception e) {
            log.error("处理统计更新消息失败: {}", message, e);
            // 统计消息失败不影响业务，只记录日志
        }
    }

    /**
     * 更新统计缓存
     *
     * @param message 统计消息
     */
    private void updateStatisticsCache(StatisticsUpdateMessage message) {
        String type = message.getType();
        String action = message.getAction();

        switch (type) {
            case "user":
                if ("create".equals(action)) {
                    cacheService.incrementUserCount();
                } else if ("delete".equals(action)) {
                    cacheService.decrementUserCount();
                }
                break;

            case "note":
                if ("create".equals(action)) {
                    cacheService.incrementNoteCount();
                } else if ("delete".equals(action)) {
                    cacheService.decrementNoteCount();
                }
                break;

            case "comment":
                if ("create".equals(action)) {
                    cacheService.incrementCommentCount();
                } else if ("delete".equals(action)) {
                    cacheService.decrementCommentCount();
                }
                break;

            case "product":
                if ("create".equals(action)) {
                    cacheService.incrementProductCount();
                } else if ("delete".equals(action)) {
                    cacheService.decrementProductCount();
                }
                break;

            case "visit":
                if ("create".equals(action)) {
                    cacheService.incrementVisitCount();
                }
                break;

            default:
                log.warn("未知的统计类型: {}", type);
        }

        log.debug("更新统计缓存成功: type={}, action={}", type, action);
    }

    /**
     * 清除相关的缓存
     *
     * @param type 统计类型
     */
    private void clearRelatedCache(String type) {
        cacheService.refreshBasicStatistics();
        cacheService.refreshDailyNewData();

        if ("note".equals(type) || "product".equals(type)) {
            cacheService.cacheCategoryStatistics(type, new java.util.ArrayList<>());
        }

        log.debug("清除相关缓存: type={}", type);
    }
}

