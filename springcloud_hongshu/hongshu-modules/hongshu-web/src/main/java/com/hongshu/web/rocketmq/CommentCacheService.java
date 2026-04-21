package com.hongshu.web.rocketmq;

import com.hongshu.common.redis.service.RedisService;
import com.hongshu.web.domain.vo.CommentVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 评论缓存服务
 * 使用Redis缓存评论数据，提升性能
 *
 * @author: hongshu
 */
@Slf4j
@Service
public class CommentCacheService {

    @Autowired
    private RedisService redisService;

    // Redis键前缀
    private static final String NOTE_COMMENT_COUNT_PREFIX = "comment:count:note:";
    private static final String PRODUCT_COMMENT_COUNT_PREFIX = "comment:count:product:";
    private static final String COMMENT_LIST_PREFIX = "comment:list:";
    private static final String COMMENT_DETAIL_PREFIX = "comment:detail:";
    private static final String COMMENT_CHILDREN_PREFIX = "comment:children:";

    // 缓存过期时间
    private static final long COUNT_EXPIRE_TIME = 3600L; // 1小时
    private static final long LIST_EXPIRE_TIME = 300L; // 5分钟
    private static final long DETAIL_EXPIRE_TIME = 1800L; // 30分钟

    /**
     * 递增笔记评论数
     *
     * @param noteId 笔记ID
     * @return 递增后的评论数
     */
    public Long incrementNoteCommentCount(String noteId) {
        String key = NOTE_COMMENT_COUNT_PREFIX + noteId;
        Long newCount = redisService.increment(key);

        if (newCount == 1) {
            redisService.expire(key, COUNT_EXPIRE_TIME);
        }

        log.debug("笔记评论数递增: noteId={}, newCount={}", noteId, newCount);
        return newCount;
    }

    /**
     * 递减笔记评论数
     *
     * @param noteId 笔记ID
     * @return 递减后的评论数
     */
    public Long decrementNoteCommentCount(String noteId) {
        String key = NOTE_COMMENT_COUNT_PREFIX + noteId;
        Long newCount = redisService.decrement(key);

        if (newCount < 0) {
            redisService.setCacheObject(key, 0L, COUNT_EXPIRE_TIME, TimeUnit.SECONDS);
            newCount = 0L;
        }

        log.debug("笔记评论数递减: noteId={}, newCount={}", noteId, newCount);
        return newCount;
    }

    /**
     * 获取笔记评论数
     *
     * @param noteId 笔记ID
     * @return 评论数
     */
    public Long getNoteCommentCount(String noteId) {
        String key = NOTE_COMMENT_COUNT_PREFIX + noteId;
        Long count = redisService.getCacheObject(key);
        return count != null ? count : 0L;
    }

    /**
     * 设置笔记评论数
     *
     * @param noteId 笔记ID
     * @param count  评论数
     */
    public void setNoteCommentCount(String noteId, Long count) {
        String key = NOTE_COMMENT_COUNT_PREFIX + noteId;
        redisService.setCacheObject(key, count, COUNT_EXPIRE_TIME, TimeUnit.SECONDS);
        log.debug("设置笔记评论数: noteId={}, count={}", noteId, count);
    }

    /**
     * 递增商品评论数
     *
     * @param productId 商品ID
     * @return 递增后的评论数
     */
    public Long incrementProductCommentCount(String productId) {
        String key = PRODUCT_COMMENT_COUNT_PREFIX + productId;
        Long newCount = redisService.increment(key);

        if (newCount == 1) {
            redisService.expire(key, COUNT_EXPIRE_TIME);
        }

        log.debug("商品评论数递增: productId={}, newCount={}", productId, newCount);
        return newCount;
    }

    /**
     * 递减商品评论数
     *
     * @param productId 商品ID
     * @return 递减后的评论数
     */
    public Long decrementProductCommentCount(String productId) {
        String key = PRODUCT_COMMENT_COUNT_PREFIX + productId;
        Long newCount = redisService.decrement(key);

        if (newCount < 0) {
            redisService.setCacheObject(key, 0L, COUNT_EXPIRE_TIME, TimeUnit.SECONDS);
            newCount = 0L;
        }

        log.debug("商品评论数递减: productId={}, newCount={}", productId, newCount);
        return newCount;
    }

    /**
     * 缓存评论列表
     *
     * @param key         缓存键（包含笔记ID/商品ID和分页信息）
     * @param commentList 评论列表
     */
    public void cacheCommentList(String key, List<CommentVo> commentList) {
        String fullKey = COMMENT_LIST_PREFIX + key;
        redisService.setCacheObject(fullKey, commentList, LIST_EXPIRE_TIME, TimeUnit.SECONDS);
        log.debug("缓存评论列表: key={}, size={}", key, commentList.size());
    }

    /**
     * 获取评论列表
     *
     * @param key 缓存键
     * @return 评论列表
     */
    public List<CommentVo> getCommentList(String key) {
        String fullKey = COMMENT_LIST_PREFIX + key;
        List<CommentVo> list = redisService.getCacheObject(fullKey);
        return list != null ? list : new ArrayList<>();
    }

    /**
     * 缓存评论详情
     *
     * @param commentId 评论ID
     * @param comment   评论详情
     */
    public void cacheCommentDetail(String commentId, CommentVo comment) {
        String key = COMMENT_DETAIL_PREFIX + commentId;
        redisService.setCacheObject(key, comment, DETAIL_EXPIRE_TIME, TimeUnit.SECONDS);
        log.debug("缓存评论详情: commentId={}", commentId);
    }

    /**
     * 获取评论详情
     *
     * @param commentId 评论ID
     * @return 评论详情
     */
    public CommentVo getCommentDetail(String commentId) {
        String key = COMMENT_DETAIL_PREFIX + commentId;
        return redisService.getCacheObject(key);
    }

    /**
     * 缓存子评论列表
     *
     * @param parentId 父评论ID
     * @param children 子评论列表
     */
    public void cacheCommentChildren(String parentId, List<CommentVo> children) {
        String key = COMMENT_CHILDREN_PREFIX + parentId;
        redisService.setCacheObject(key, children, LIST_EXPIRE_TIME, TimeUnit.SECONDS);
        log.debug("缓存子评论列表: parentId={}, size={}", parentId, children.size());
    }

    /**
     * 获取子评论列表
     *
     * @param parentId 父评论ID
     * @return 子评论列表
     */
    public List<CommentVo> getCommentChildren(String parentId) {
        String key = COMMENT_CHILDREN_PREFIX + parentId;
        List<CommentVo> list = redisService.getCacheObject(key);
        return list != null ? list : new ArrayList<>();
    }

    /**
     * 清除笔记的评论缓存
     *
     * @param noteId 笔记ID
     */
    public void clearNoteCommentCache(String noteId) {
        // 清除评论列表缓存（需要手动删除所有相关键）
        String pattern = COMMENT_LIST_PREFIX + "note:" + noteId + ":*";
        java.util.Collection<String> keys = redisService.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            for (String key : keys) {
                redisService.deleteObject(key);
            }
        }
        log.debug("清除笔记评论缓存: noteId={}", noteId);
    }

    /**
     * 清除商品的评论缓存
     *
     * @param productId 商品ID
     */
    public void clearProductCommentCache(String productId) {
        String pattern = COMMENT_LIST_PREFIX + "product:" + productId + ":*";
        java.util.Collection<String> keys = redisService.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            for (String key : keys) {
                redisService.deleteObject(key);
            }
        }
        log.debug("清除商品评论缓存: productId={}", productId);
    }

    /**
     * 清除评论详情缓存
     *
     * @param commentId 评论ID
     */
    public void clearCommentDetail(String commentId) {
        String key = COMMENT_DETAIL_PREFIX + commentId;
        redisService.deleteObject(key);

        // 清除子评论缓存
        String childrenKey = COMMENT_CHILDREN_PREFIX + commentId;
        redisService.deleteObject(childrenKey);

        log.debug("清除评论详情缓存: commentId={}", commentId);
    }
}

