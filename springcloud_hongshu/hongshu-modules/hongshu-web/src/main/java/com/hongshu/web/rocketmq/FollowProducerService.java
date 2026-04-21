package com.hongshu.web.rocketmq;

import com.hongshu.web.domain.message.FollowMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * 关注生产者服务
 * 发送关注操作消息到RocketMQ
 *
 * @author: hongshu
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "rocketmq.name-server")
public class FollowProducerService {

    @Autowired(required = false)
    private RocketMQTemplate rocketMQTemplate;

    // 主题名称
    private static final String TOPIC = "follow-topic";

    /**
     * 发送关注操作消息
     *
     * @param userId    用户ID（关注者）
     * @param fid 被关注用户ID
     * @param isFollow  true-关注，false-取消关注
     */
    public void sendFollowMessage(String userId, String fid, Boolean isFollow) throws Exception {
        if (rocketMQTemplate == null) {
            log.warn("RocketMQ未配置，无法发送关注消息");
            throw new Exception("RocketMQ未配置");
        }

        FollowMessage message = FollowMessage.builder()
                .userId(userId)
                .fid(fid)
                .isFollow(isFollow)
                .timestamp(System.currentTimeMillis())
                .build();

        try {
            rocketMQTemplate.convertAndSend(TOPIC, message);
            log.debug("发送关注消息成功: userId={}, fid={}, isFollow={}",
                    userId, fid, isFollow);
        } catch (Exception e) {
            log.error("发送关注消息失败: userId={}, fid={}, isFollow={}",
                    userId, fid, isFollow, e);
            throw e;
        }
    }
}

