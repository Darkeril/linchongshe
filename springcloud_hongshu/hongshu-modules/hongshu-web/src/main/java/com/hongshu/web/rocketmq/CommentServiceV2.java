package com.hongshu.web.rocketmq;

import com.hongshu.common.core.utils.StringUtils;
import com.hongshu.web.domain.entity.WebComment;
import com.hongshu.web.domain.entity.WebCommentSync;
import com.hongshu.web.domain.entity.WebNote;
import com.hongshu.web.mapper.idle.IdleProductMapper;
import com.hongshu.web.mapper.web.WebCommentMapper;
import com.hongshu.web.mapper.web.WebCommentSyncMapper;
import com.hongshu.web.mapper.web.WebNoteMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 评论服务V2（优化版）
 * 使用Redis缓存 + RocketMQ异步更新
 *
 * @author: hongshu
 */
@Slf4j
@Service
public class CommentServiceV2 {

    @Autowired
    private CommentCacheService cacheService;

    @Autowired(required = false)
    private CommentProducerService producerService;

    @Autowired
    private WebNoteMapper noteMapper;

    @Autowired
    private IdleProductMapper productMapper;

    @Autowired
    private WebCommentMapper commentMapper;

    @Autowired
    private WebCommentSyncMapper commentSyncMapper;

    /**
     * 记录评论创建（优化版）
     *
     * @param commentId 评论ID
     * @param noteId    笔记ID
     * @param productId 商品ID
     * @param parentId  父评论ID
     */
    public void recordCommentCreate(String commentId, String noteId, String productId, String parentId) {
        try {
            // 1. 更新Redis缓存
            if (StringUtils.isNotBlank(noteId)) {
                cacheService.incrementNoteCommentCount(noteId);
                cacheService.clearNoteCommentCache(noteId);
            }

            if (StringUtils.isNotBlank(productId)) {
                cacheService.incrementProductCommentCount(productId);
                cacheService.clearProductCommentCache(productId);
            }

            // 2. 异步更新数据库
            asyncUpdateComment(commentId, noteId, productId, parentId, "create");

            log.info("记录评论创建成功: commentId={}", commentId);
        } catch (Exception e) {
            log.error("记录评论创建失败: commentId={}", commentId, e);
            // 降级：直接更新数据库
            syncUpdateCommentCount(noteId, productId, parentId, 1);
        }
    }

    /**
     * 记录评论删除（优化版）
     *
     * @param commentId 评论ID
     * @param noteId    笔记ID
     * @param productId 商品ID
     * @param parentId  父评论ID
     */
    public void recordCommentDelete(String commentId, String noteId, String productId, String parentId) {
        try {
            // 1. 更新Redis缓存
            if (StringUtils.isNotBlank(noteId)) {
                cacheService.decrementNoteCommentCount(noteId);
                cacheService.clearNoteCommentCache(noteId);
            }

            if (StringUtils.isNotBlank(productId)) {
                cacheService.decrementProductCommentCount(productId);
                cacheService.clearProductCommentCache(productId);
            }

            // 2. 异步更新数据库
            asyncUpdateComment(commentId, noteId, productId, parentId, "delete");

            log.info("记录评论删除成功: commentId={}", commentId);
        } catch (Exception e) {
            log.error("记录评论删除失败: commentId={}", commentId, e);
            // 降级：直接更新数据库
            syncUpdateCommentCount(noteId, productId, parentId, -1);
        }
    }

    /**
     * 获取笔记评论数（优化版）
     *
     * @param noteId 笔记ID
     * @return 评论数
     */
    public Long getNoteCommentCount(String noteId) {
        try {
            // 先从Redis获取
            Long cachedCount = cacheService.getNoteCommentCount(noteId);

            if (cachedCount > 0) {
                return cachedCount;
            }

            // Redis未命中，查询数据库并缓存
            WebNote note = noteMapper.selectById(noteId);
            if (note != null && note.getCommentCount() != null) {
                cacheService.setNoteCommentCount(noteId, note.getCommentCount());
                return note.getCommentCount();
            }

            return 0L;
        } catch (Exception e) {
            log.error("获取笔记评论数失败: noteId={}", noteId, e);
            return 0L;
        }
    }

    /**
     * 异步更新评论数据到数据库
     *
     * @param commentId 评论ID
     * @param noteId    笔记ID
     * @param productId 商品ID
     * @param parentId  父评论ID
     * @param action    操作类型
     */
    private void asyncUpdateComment(String commentId, String noteId, String productId, String parentId, String action) {
        try {
            if (producerService != null) {
                // RocketMQ可用，发送异步消息
                if ("create".equals(action)) {
                    producerService.sendCreateCommentMessage(commentId, noteId, productId, parentId);
                } else {
                    producerService.sendDeleteCommentMessage(commentId, noteId, productId, parentId);
                }
            } else {
                // RocketMQ不可用，直接更新数据库
                int delta = "create".equals(action) ? 1 : -1;
                syncUpdateCommentCount(noteId, productId, parentId, delta);
            }
        } catch (Exception e) {
            log.error("异步更新评论失败，降级为同步更新: commentId={}, action={}", commentId, action, e);
            int delta = "create".equals(action) ? 1 : -1;
            syncUpdateCommentCount(noteId, productId, parentId, delta);
        }
    }

    /**
     * 同步更新评论数到数据库
     *
     * @param noteId    笔记ID
     * @param productId 商品ID
     * @param parentId  父评论ID
     * @param delta     变化量
     */
    private void syncUpdateCommentCount(String noteId, String productId, String parentId, int delta) {
        try {
            // 更新笔记评论数
            if (StringUtils.isNotBlank(noteId)) {
                WebNote note = noteMapper.selectById(noteId);
                if (note != null) {
                    Long newCount = Math.max(0, note.getCommentCount() + delta);
                    note.setCommentCount(newCount);
//                    note.setUpdateTime(new Date());
                    noteMapper.updateById(note);
                }
            }

            // 更新商品评论数
            if (StringUtils.isNotBlank(productId)) {
                // TODO: 实现商品评论数更新
            }

            // 更新父评论的子评论数
            if (StringUtils.isNotBlank(parentId) && !"0".equals(parentId)) {
                updateParentCommentChildrenCountInDb(parentId, delta);
            }

            log.info("同步更新评论数成功: noteId={}, delta={}", noteId, delta);
        } catch (Exception e) {
            log.error("同步更新评论数失败: noteId={}, delta={}", noteId, delta, e);
        }
    }

    /**
     * 更新父评论的子评论数到数据库
     *
     * @param parentId 父评论ID
     * @param delta    变化量
     */
    private void updateParentCommentChildrenCountInDb(String parentId, int delta) {
        // 先查 WebComment
        WebComment parentComment = commentMapper.selectById(parentId);
        if (parentComment != null) {
            Long newCount = Math.max(0, parentComment.getTwoCommentCount() + delta);
            parentComment.setTwoCommentCount(newCount);
            parentComment.setUpdateTime(new Date());
            commentMapper.updateById(parentComment);
            return;
        }

        // 再查 WebCommentSync
        WebCommentSync commentSync = commentSyncMapper.selectById(parentId);
        if (commentSync != null) {
            Long newCount = Math.max(0, commentSync.getTwoCommentCount() + delta);
            commentSync.setTwoCommentCount(newCount);
            commentSync.setUpdateTime(new Date());
            commentSyncMapper.updateById(commentSync);
        }
    }
}

