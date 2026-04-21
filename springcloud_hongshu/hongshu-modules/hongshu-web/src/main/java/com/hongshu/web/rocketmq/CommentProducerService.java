package com.hongshu.web.rocketmq;

import com.hongshu.web.domain.message.CommentMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * 评论生产者服务
 * 发送评论操作消息到RocketMQ
 *
 * @author: hongshu
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "rocketmq.name-server")
public class CommentProducerService {

    @Autowired(required = false)
    private RocketMQTemplate rocketMQTemplate;

    // 主题名称
    private static final String TOPIC = "comment-topic";

    /**
     * 发送评论操作消息
     *
     * @param message 评论消息
     */
    public void sendCommentMessage(CommentMessage message) throws Exception {
        if (rocketMQTemplate == null) {
            log.warn("RocketMQ未配置，无法发送评论消息");
            throw new Exception("RocketMQ未配置");
        }

        try {
            rocketMQTemplate.convertAndSend(TOPIC, message);
            log.debug("发送评论消息成功: action={}, noteId={}, productId={}",
                    message.getAction(), message.getNoteId(), message.getProductId());
        } catch (Exception e) {
            log.error("发送评论消息失败: {}", message, e);
            throw e;
        }
    }

    /**
     * 发送创建评论消息
     *
     * @param commentId 评论ID
     * @param noteId    笔记ID
     * @param productId 商品ID
     * @param parentId  父评论ID
     */
    public void sendCreateCommentMessage(String commentId, String noteId, String productId, String parentId) throws Exception {
        CommentMessage message = CommentMessage.builder()
                .commentId(commentId)
                .noteId(noteId)
                .productId(productId)
                .parentId(parentId)
                .action("create")
                .timestamp(System.currentTimeMillis())
                .build();

        sendCommentMessage(message);
    }

    /**
     * 发送删除评论消息
     *
     * @param commentId 评论ID
     * @param noteId    笔记ID
     * @param productId 商品ID
     * @param parentId  父评论ID
     */
    public void sendDeleteCommentMessage(String commentId, String noteId, String productId, String parentId) throws Exception {
        CommentMessage message = CommentMessage.builder()
                .commentId(commentId)
                .noteId(noteId)
                .productId(productId)
                .parentId(parentId)
                .action("delete")
                .timestamp(System.currentTimeMillis())
                .build();

        sendCommentMessage(message);
    }
}

