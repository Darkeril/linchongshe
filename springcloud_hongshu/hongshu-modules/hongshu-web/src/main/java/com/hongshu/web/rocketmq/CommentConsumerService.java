package com.hongshu.web.rocketmq;

import com.hongshu.common.core.utils.StringUtils;
import com.hongshu.web.domain.entity.WebComment;
import com.hongshu.web.domain.entity.WebCommentSync;
import com.hongshu.web.domain.entity.WebNote;
import com.hongshu.web.domain.message.CommentMessage;
import com.hongshu.web.mapper.idle.IdleProductMapper;
import com.hongshu.web.mapper.web.WebCommentMapper;
import com.hongshu.web.mapper.web.WebCommentSyncMapper;
import com.hongshu.web.mapper.web.WebNoteMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 评论消费者服务
 * 异步更新数据库中的评论数据
 *
 * @author: hongshu
 */
@Slf4j
@Service
// 添加条件注解，当配置为 false 时不加载
@ConditionalOnProperty(name = "rocketmq.consumer.listen", havingValue = "true")
@RocketMQMessageListener(
        topic = "comment-topic",
        consumerGroup = "comment-consumer-group"
)
public class CommentConsumerService implements RocketMQListener<CommentMessage> {

    @Autowired
    private WebNoteMapper noteMapper;

    @Autowired
    private IdleProductMapper productMapper;

    @Autowired
    private WebCommentMapper commentMapper;

    @Autowired
    private WebCommentSyncMapper commentSyncMapper;

    @Override
    public void onMessage(CommentMessage message) {
        try {
            log.info("收到评论操作消息: action={}, commentId={}, noteId={}, productId={}",
                    message.getAction(), message.getCommentId(), message.getNoteId(), message.getProductId());

            if ("create".equals(message.getAction())) {
                handleCreateComment(message);
            } else if ("delete".equals(message.getAction())) {
                handleDeleteComment(message);
            }
        } catch (Exception e) {
            log.error("处理评论消息失败: {}", message, e);
            throw e; // 抛出异常触发重试
        }
    }

    /**
     * 处理创建评论
     *
     * @param message 评论消息
     */
    private void handleCreateComment(CommentMessage message) {
        try {
            // 更新笔记评论数
            if (StringUtils.isNotBlank(message.getNoteId())) {
                updateNoteCommentCount(message.getNoteId(), 1);
            }

            // 更新商品评论数
            if (StringUtils.isNotBlank(message.getProductId())) {
                updateProductCommentCount(message.getProductId(), 1);
            }

            // 更新父评论的子评论数
            if (StringUtils.isNotBlank(message.getParentId()) && !"0".equals(message.getParentId())) {
                updateParentCommentChildrenCount(message.getParentId(), 1);
            }

            log.info("创建评论成功: commentId={}", message.getCommentId());
        } catch (Exception e) {
            log.error("处理创建评论失败: {}", message, e);
            throw e;
        }
    }

    /**
     * 处理删除评论
     *
     * @param message 评论消息
     */
    private void handleDeleteComment(CommentMessage message) {
        try {
            // 更新笔记评论数
            if (StringUtils.isNotBlank(message.getNoteId())) {
                updateNoteCommentCount(message.getNoteId(), -1);
            }

            // 更新商品评论数
            if (StringUtils.isNotBlank(message.getProductId())) {
                updateProductCommentCount(message.getProductId(), -1);
            }

            // 更新父评论的子评论数
            if (StringUtils.isNotBlank(message.getParentId()) && !"0".equals(message.getParentId())) {
                updateParentCommentChildrenCount(message.getParentId(), -1);
            }

            log.info("删除评论成功: commentId={}", message.getCommentId());
        } catch (Exception e) {
            log.error("处理删除评论失败: {}", message, e);
            throw e;
        }
    }

    /**
     * 更新笔记评论数
     *
     * @param noteId 笔记ID
     * @param delta  变化量（+1 或 -1）
     */
    private void updateNoteCommentCount(String noteId, int delta) {
        try {
            WebNote note = noteMapper.selectById(noteId);
            if (note != null) {
                Long newCount = Math.max(0, note.getCommentCount() + delta);
                note.setCommentCount(newCount);
//                note.setUpdateTime(new Date());
                noteMapper.updateById(note);
                log.debug("更新笔记评论数: noteId={}, newCount={}", noteId, newCount);
            }
        } catch (Exception e) {
            log.error("更新笔记评论数失败: noteId={}", noteId, e);
        }
    }

    /**
     * 更新商品评论数
     *
     * @param productId 商品ID
     * @param delta     变化量
     */
    private void updateProductCommentCount(String productId, int delta) {
        try {
            // TODO: 实现商品评论数更新
            log.debug("更新商品评论数: productId={}, delta={}", productId, delta);
        } catch (Exception e) {
            log.error("更新商品评论数失败: productId={}", productId, e);
        }
    }

    /**
     * 更新父评论的子评论数
     *
     * @param parentId 父评论ID
     * @param delta    变化量
     */
    private void updateParentCommentChildrenCount(String parentId, int delta) {
        try {
            // 先查 WebComment
            WebComment parentComment = commentMapper.selectById(parentId);
            if (parentComment != null) {
                Long newCount = Math.max(0, parentComment.getTwoCommentCount() + delta);
                parentComment.setTwoCommentCount(newCount);
                parentComment.setUpdateTime(new Date());
                commentMapper.updateById(parentComment);
                log.debug("更新父评论子评论数: parentId={}, newCount={}", parentId, newCount);
                return;
            }

            // 再查 WebCommentSync
            WebCommentSync commentSync = commentSyncMapper.selectById(parentId);
            if (commentSync != null) {
                Long newCount = Math.max(0, commentSync.getTwoCommentCount() + delta);
                commentSync.setTwoCommentCount(newCount);
                commentSync.setUpdateTime(new Date());
                commentSyncMapper.updateById(commentSync);
                log.debug("更新父评论子评论数(Sync): parentId={}, newCount={}", parentId, newCount);
            }
        } catch (Exception e) {
            log.error("更新父评论子评论数失败: parentId={}", parentId, e);
        }
    }
}

