package com.hongshu.web.rocketmq;

import com.hongshu.common.redis.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 浏览数缓存服务
 * 使用Redis缓存浏览数，提升性能
 *
 * @author: hongshu
 */
@Slf4j
@Service
public class ViewCountCacheService {

    @Autowired
    private RedisService redisService;

    // Redis键前缀
    private static final String NOTE_VIEW_PREFIX = "note:view:";
    private static final String PRODUCT_VIEW_PREFIX = "product:view:";

    // 缓存过期时间（7天）
    private static final long CACHE_EXPIRE_TIME = 7 * 24 * 60 * 60L;

    /**
     * 笔记浏览数原子递增
     *
     * @param noteId 笔记ID
     * @return 递增后的浏览数
     */
    public Long incrementNoteView(String noteId) {
        String key = NOTE_VIEW_PREFIX + noteId;
        Long newCount = redisService.increment(key);

        // 设置过期时间（只在第一次创建时设置）
        if (newCount == 1) {
            redisService.expire(key, CACHE_EXPIRE_TIME);
        }

        log.debug("笔记浏览数递增: noteId={}, newCount={}", noteId, newCount);
        return newCount;
    }

    /**
     * 商品浏览数原子递增
     *
     * @param productId 商品ID
     * @return 递增后的浏览数
     */
    public Long incrementProductView(String productId) {
        String key = PRODUCT_VIEW_PREFIX + productId;
        Long newCount = redisService.increment(key);

        if (newCount == 1) {
            redisService.expire(key, CACHE_EXPIRE_TIME);
        }

        log.debug("商品浏览数递增: productId={}, newCount={}", productId, newCount);
        return newCount;
    }

    /**
     * 获取笔记浏览数
     *
     * @param noteId 笔记ID
     * @return 浏览数
     */
    public Long getNoteViewCount(String noteId) {
        String key = NOTE_VIEW_PREFIX + noteId;
        Long value = redisService.getCacheObject(key);
        return value != null ? value : 0L;
    }

    /**
     * 获取商品浏览数
     *
     * @param productId 商品ID
     * @return 浏览数
     */
    public Long getProductViewCount(String productId) {
        String key = PRODUCT_VIEW_PREFIX + productId;
        Long value = redisService.getCacheObject(key);
        return value != null ? value : 0L;
    }

    /**
     * 设置笔记浏览数
     *
     * @param noteId 笔记ID
     * @param count  浏览数
     */
    public void setNoteViewCount(String noteId, Long count) {
        String key = NOTE_VIEW_PREFIX + noteId;
        redisService.setCacheObject(key, count, CACHE_EXPIRE_TIME, java.util.concurrent.TimeUnit.SECONDS);
        log.debug("设置笔记浏览数: noteId={}, count={}", noteId, count);
    }

    /**
     * 设置商品浏览数
     *
     * @param productId 商品ID
     * @param count     浏览数
     */
    public void setProductViewCount(String productId, Long count) {
        String key = PRODUCT_VIEW_PREFIX + productId;
        redisService.setCacheObject(key, count, CACHE_EXPIRE_TIME, java.util.concurrent.TimeUnit.SECONDS);
        log.debug("设置商品浏览数: productId={}, count={}", productId, count);
    }

    /**
     * 批量获取笔记浏览数
     *
     * @param noteIds 笔记ID列表
     * @return 浏览数映射
     */
    public java.util.Map<String, Long> batchGetNoteViewCounts(java.util.List<String> noteIds) {
        java.util.Map<String, Long> result = new java.util.HashMap<>();

        for (String noteId : noteIds) {
            Long count = getNoteViewCount(noteId);
            result.put(noteId, count);
        }

        return result;
    }

    /**
     * 获取所有笔记浏览数的Redis键
     *
     * @return Redis键集合
     */
    public java.util.Set<String> getAllNoteViewKeys() {
        java.util.Collection<String> keys = redisService.keys(NOTE_VIEW_PREFIX + "*");
        return new java.util.HashSet<>(keys != null ? keys : java.util.Collections.emptySet());
    }

    /**
     * 获取所有商品浏览数的Redis键
     *
     * @return Redis键集合
     */
    public java.util.Set<String> getAllProductViewKeys() {
        java.util.Collection<String> keys = redisService.keys(PRODUCT_VIEW_PREFIX + "*");
        return new java.util.HashSet<>(keys != null ? keys : java.util.Collections.emptySet());
    }
}

