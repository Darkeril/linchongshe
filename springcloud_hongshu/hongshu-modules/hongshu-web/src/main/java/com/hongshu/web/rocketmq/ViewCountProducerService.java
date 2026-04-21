package com.hongshu.web.rocketmq;

import com.hongshu.web.domain.message.ViewCountMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * 浏览数生产者服务
 * 发送浏览数更新消息到RocketMQ
 *
 * @author: hongshu
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "rocketmq.name-server")
public class ViewCountProducerService {

    @Autowired(required = false)
    private RocketMQTemplate rocketMQTemplate;

    @Autowired(required = false)
    private Environment environment;

    // 主题名称
    private static final String TOPIC = "view-count-topic";

    private boolean isMqAvailable() {
        if (rocketMQTemplate == null) return false;
        try {
            String namesrv = environment != null ? environment.getProperty("rocketmq.name-server", "") : "";
            if (namesrv == null || namesrv.trim().isEmpty()) return false;
            // 一些实现里可通过 producer 读取 namesrv 地址
            String configured = rocketMQTemplate.getProducer() != null ? rocketMQTemplate.getProducer().getNamesrvAddr() : null;
            if (configured == null || configured.trim().isEmpty()) return false;
            return true;
        } catch (Exception ignore) {
            return false;
        }
    }

    /**
     * 发送浏览数更新消息
     *
     * @param type  类型：note-笔记，product-商品
     * @param id    笔记ID或商品ID
     * @param count 浏览数
     */
    public void sendViewCountMessage(String type, String id, Long count) throws Exception {
        if (!isMqAvailable()) {
            log.warn("RocketMQ未配置或未启动，跳过发送浏览数消息");
            throw new Exception("RocketMQ未配置");
        }

        ViewCountMessage message = ViewCountMessage.builder()
                .type(type)
                .id(id)
                .count(count)
                .timestamp(System.currentTimeMillis())
                .build();

        try {
            rocketMQTemplate.convertAndSend(TOPIC, message);
            log.debug("发送浏览数消息成功: type={}, id={}, count={}", type, id, count);
        } catch (Exception e) {
            // 不打印过长堆栈，交由上层同步降级
            log.warn("发送浏览数消息失败，将由上层降级同步: type={}, id={}, count={}, cause={}", type, id, count, e.getMessage());
            throw e;
        }
    }

    /**
     * 发送笔记浏览数消息
     *
     * @param noteId 笔记ID
     * @param count  浏览数
     */
    public void sendNoteViewMessage(String noteId, Long count) throws Exception {
        sendViewCountMessage("note", noteId, count);
    }

    /**
     * 发送商品浏览数消息
     *
     * @param productId 商品ID
     * @param count     浏览数
     */
    public void sendProductViewMessage(String productId, Long count) throws Exception {
        sendViewCountMessage("product", productId, count);
    }
}

