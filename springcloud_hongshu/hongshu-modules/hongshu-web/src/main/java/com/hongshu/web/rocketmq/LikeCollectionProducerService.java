package com.hongshu.web.rocketmq;

import com.alibaba.fastjson2.JSON;
import com.hongshu.web.domain.dto.LikeCollectionMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * 点赞/收藏消息生产者
 *
 * @author: hongshu
 *
 * 注意：需要先启动 RocketMQ 才能启用此生产者
 * 未启动 RocketMQ 时，此 Bean 不会创建
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "rocketmq.name-server")  // 配置了 RocketMQ 才创建
@ConditionalOnBean(RocketMQTemplate.class)             // 同时需要模板 Bean 存在
public class LikeCollectionProducerService {

    @Autowired(required = false)
    private RocketMQTemplate rocketMQTemplate;

    // 初始化时打印日志
    public LikeCollectionProducerService() {
        log.info("🚀 LikeCollectionProducerService 已创建（RocketMQ Producer 已启用）");
    }

    // Topic名称
    private static final String TOPIC = "LIKE_COLLECTION_TOPIC";

    /**
     * 发送点赞/收藏消息
     *
     * @throws Exception 发送失败时抛出异常，由调用方处理降级
     */
    public void sendLikeCollectionMessage(LikeCollectionMessage message) throws Exception {
        if (rocketMQTemplate == null) {
            log.warn("RocketMQTemplate 未注入，跳过发送: {}", message);
            return;
        }
        // 构建消息
        String messageBody = JSON.toJSONString(message);

        // 发送异步消息（使用简化的API）
        rocketMQTemplate.convertAndSend(TOPIC, messageBody);

        log.info("✅ 发送点赞/收藏消息成功: action={}, uid={}, itemId={}",
                message.getAction(), message.getUid(), message.getLikeOrCollectionId());
    }

    /**
     * 发送点赞消息
     *
     * @throws Exception 发送失败时抛出异常
     */
    public void sendLikeMessage(String uid, String itemId, String publishUid, Integer type, boolean isLike) throws Exception {
        LikeCollectionMessage message = new LikeCollectionMessage()
                .setAction(isLike ? "LIKE" : "UNLIKE")
                .setUid(uid)
                .setLikeOrCollectionId(itemId)
                .setPublishUid(publishUid)
                .setType(type)
                .setTimestamp(System.currentTimeMillis());
        sendLikeCollectionMessage(message);
    }

    /**
     * 发送收藏消息
     *
     * @throws Exception 发送失败时抛出异常
     */
    public void sendCollectMessage(String uid, String itemId, String publishUid, Integer type, boolean isCollect) throws Exception {
        LikeCollectionMessage message = new LikeCollectionMessage()
                .setAction(isCollect ? "COLLECT" : "UNCOLLECT")
                .setUid(uid)
                .setLikeOrCollectionId(itemId)
                .setPublishUid(publishUid)
                .setType(type)
                .setTimestamp(System.currentTimeMillis());
        sendLikeCollectionMessage(message);
    }
}

