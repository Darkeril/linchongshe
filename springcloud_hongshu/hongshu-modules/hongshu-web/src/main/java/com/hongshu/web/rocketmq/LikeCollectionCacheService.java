package com.hongshu.web.rocketmq;

import com.hongshu.common.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 点赞/收藏缓存服务
 *
 * @author: hongshu
 */
@Service
public class LikeCollectionCacheService {

    @Autowired
    private RedisService redisService;

    // 缓存key前缀
    private static final String LIKE_KEY_PREFIX = "like:";
    private static final String COLLECT_KEY_PREFIX = "collect:";
    private static final String LIKE_COUNT_PREFIX = "note:likeCount:";
    private static final String COLLECT_COUNT_PREFIX = "note:collectCount:";

    // 缓存过期时间：7天
    private static final long CACHE_EXPIRE_DAYS = 7;

    /**
     * 缓存点赞状态
     */
    public void cacheLikeStatus(String uid, String itemId, boolean isLike) {
        String key = LIKE_KEY_PREFIX + uid + ":" + itemId;
        redisService.setCacheObject(key, isLike, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
    }

    /**
     * 获取点赞状态
     */
    public Boolean getLikeStatus(String uid, String itemId) {
        String key = LIKE_KEY_PREFIX + uid + ":" + itemId;
        return redisService.getCacheObject(key);
    }

    /**
     * 删除点赞状态缓存
     */
    public void deleteLikeStatus(String uid, String itemId) {
        String key = LIKE_KEY_PREFIX + uid + ":" + itemId;
        redisService.deleteObject(key);
    }

    /**
     * 缓存收藏状态
     */
    public void cacheCollectStatus(String uid, String itemId, boolean isCollect) {
        String key = COLLECT_KEY_PREFIX + uid + ":" + itemId;
        redisService.setCacheObject(key, isCollect, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
    }

    /**
     * 获取收藏状态
     */
    public Boolean getCollectStatus(String uid, String itemId) {
        String key = COLLECT_KEY_PREFIX + uid + ":" + itemId;
        return redisService.getCacheObject(key);
    }

    /**
     * 删除收藏状态缓存
     */
    public void deleteCollectStatus(String uid, String itemId) {
        String key = COLLECT_KEY_PREFIX + uid + ":" + itemId;
        redisService.deleteObject(key);
    }

    /**
     * 增加点赞数（Redis原子操作）
     */
    public Long incrLikeCount(String itemId) {
        String key = LIKE_COUNT_PREFIX + itemId;
        Long count = redisService.increment(key);
        // 设置过期时间
        redisService.expire(key, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
        return count;
    }

    /**
     * 减少点赞数（Redis原子操作）
     */
    public Long decrLikeCount(String itemId) {
        String key = LIKE_COUNT_PREFIX + itemId;
        Long count = redisService.decrement(key);
        // 防止负数
        if (count < 0) {
            redisService.setCacheObject(key, 0L);
            return 0L;
        }
        return count;
    }

    /**
     * 获取点赞数
     */
    public Long getLikeCount(String itemId) {
        String key = LIKE_COUNT_PREFIX + itemId;
        Long count = redisService.getCacheObject(key);
        return count != null ? count : 0L;
    }

    /**
     * 设置点赞数
     */
    public void setLikeCount(String itemId, Long count) {
        String key = LIKE_COUNT_PREFIX + itemId;
        redisService.setCacheObject(key, count, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
    }

    /**
     * 增加收藏数
     */
    public Long incrCollectCount(String itemId) {
        String key = COLLECT_COUNT_PREFIX + itemId;
        Long count = redisService.increment(key);
        redisService.expire(key, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
        return count;
    }

    /**
     * 减少收藏数
     */
    public Long decrCollectCount(String itemId) {
        String key = COLLECT_COUNT_PREFIX + itemId;
        Long count = redisService.decrement(key);
        if (count < 0) {
            redisService.setCacheObject(key, 0L);
            return 0L;
        }
        return count;
    }

    /**
     * 获取收藏数
     */
    public Long getCollectCount(String itemId) {
        String key = COLLECT_COUNT_PREFIX + itemId;
        Long count = redisService.getCacheObject(key);
        return count != null ? count : 0L;
    }

    /**
     * 设置收藏数
     */
    public void setCollectCount(String itemId, Long count) {
        String key = COLLECT_COUNT_PREFIX + itemId;
        redisService.setCacheObject(key, count, CACHE_EXPIRE_DAYS, TimeUnit.DAYS);
    }
}

