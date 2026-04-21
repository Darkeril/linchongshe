package com.hongshu.web.rocketmq;

import com.hongshu.common.redis.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 关注缓存服务
 * 使用Redis缓存关注关系和粉丝数，提升性能
 *
 * @author: hongshu
 */
@Slf4j
@Service
public class FollowCacheService {

    @Autowired
    private RedisService redisService;

    // Redis键前缀
    private static final String FOLLOW_KEY_PREFIX = "follow:";           // 关注关系：follow:userId:fid
    private static final String FOLLOWER_COUNT_PREFIX = "follower:count:"; // 粉丝数：follower:count:userId
    private static final String FOLLOWING_COUNT_PREFIX = "following:count:"; // 关注数：following:count:userId
    private static final String FOLLOWING_LIST_PREFIX = "following:list:"; // 关注列表：following:list:userId
    private static final String FOLLOWER_LIST_PREFIX = "follower:list:";  // 粉丝列表：follower:list:userId

    // 缓存过期时间（1小时）
    private static final long CACHE_EXPIRE_TIME = 60 * 60L;

    /**
     * 缓存关注关系
     *
     * @param userId    用户ID
     * @param fid 被关注用户ID
     */
    public void cacheFollow(String userId, String fid) {
        String key = FOLLOW_KEY_PREFIX + userId + ":" + fid;
        redisService.setCacheObject(key, true, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
        log.debug("缓存关注关系: userId={}, fid={}", userId, fid);
    }

    /**
     * 移除关注关系缓存
     *
     * @param userId    用户ID
     * @param fid 被关注用户ID
     */
    public void removeFollow(String userId, String fid) {
        String key = FOLLOW_KEY_PREFIX + userId + ":" + fid;
        redisService.deleteObject(key);
        log.debug("移除关注关系: userId={}, fid={}", userId, fid);
    }

    /**
     * 检查是否关注
     *
     * @param userId    用户ID
     * @param fid 被关注用户ID
     * @return true-已关注，false-未关注
     */
    public Boolean isFollowing(String userId, String fid) {
        String key = FOLLOW_KEY_PREFIX + userId + ":" + fid;
        return redisService.hasKey(key);
    }

    /**
     * 获取粉丝数
     *
     * @param userId 用户ID
     * @return 粉丝数
     */
    public Long getFollowerCount(String userId) {
        String key = FOLLOWER_COUNT_PREFIX + userId;
        Long count = redisService.getCacheObject(key);
        return count != null ? count : 0L;
    }

    /**
     * 设置粉丝数
     *
     * @param userId 用户ID
     * @param count  粉丝数
     */
    public void setFollowerCount(String userId, Long count) {
        String key = FOLLOWER_COUNT_PREFIX + userId;
        redisService.setCacheObject(key, count, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
        log.debug("设置粉丝数: userId={}, count={}", userId, count);
    }

    /**
     * 原子递增粉丝数
     *
     * @param userId 用户ID
     * @return 递增后的粉丝数
     */
    public Long incrementFollowerCount(String userId) {
        String key = FOLLOWER_COUNT_PREFIX + userId;
        Long newCount = redisService.increment(key);

        // 设置过期时间（只在第一次创建时设置）
        if (newCount == 1) {
            redisService.expire(key, CACHE_EXPIRE_TIME);
        }

        log.debug("粉丝数递增: userId={}, newCount={}", userId, newCount);
        return newCount;
    }

    /**
     * 原子递减粉丝数
     *
     * @param userId 用户ID
     * @return 递减后的粉丝数
     */
    public Long decrementFollowerCount(String userId) {
        String key = FOLLOWER_COUNT_PREFIX + userId;
        Long newCount = redisService.decrement(key);

        // 确保不会小于0
        if (newCount < 0) {
            redisService.setCacheObject(key, 0L, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
            newCount = 0L;
        }

        log.debug("粉丝数递减: userId={}, newCount={}", userId, newCount);
        return newCount;
    }

    /**
     * 获取关注数
     *
     * @param userId 用户ID
     * @return 关注数
     */
    public Long getFollowingCount(String userId) {
        String key = FOLLOWING_COUNT_PREFIX + userId;
        Long count = redisService.getCacheObject(key);
        return count != null ? count : 0L;
    }

    /**
     * 设置关注数
     *
     * @param userId 用户ID
     * @param count  关注数
     */
    public void setFollowingCount(String userId, Long count) {
        String key = FOLLOWING_COUNT_PREFIX + userId;
        redisService.setCacheObject(key, count, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
        log.debug("设置关注数: userId={}, count={}", userId, count);
    }

    /**
     * 原子递增关注数
     *
     * @param userId 用户ID
     * @return 递增后的关注数
     */
    public Long incrementFollowingCount(String userId) {
        String key = FOLLOWING_COUNT_PREFIX + userId;
        Long newCount = redisService.increment(key);

        if (newCount == 1) {
            redisService.expire(key, CACHE_EXPIRE_TIME);
        }

        log.debug("关注数递增: userId={}, newCount={}", userId, newCount);
        return newCount;
    }

    /**
     * 原子递减关注数
     *
     * @param userId 用户ID
     * @return 递减后的关注数
     */
    public Long decrementFollowingCount(String userId) {
        String key = FOLLOWING_COUNT_PREFIX + userId;
        Long newCount = redisService.decrement(key);

        if (newCount < 0) {
            redisService.setCacheObject(key, 0L, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
            newCount = 0L;
        }

        log.debug("关注数递减: userId={}, newCount={}", userId, newCount);
        return newCount;
    }

    /**
     * 缓存关注列表
     *
     * @param userId        用户ID
     * @param followingList 关注的用户ID列表
     */
    public void cacheFollowingList(String userId, List<String> followingList) {
        String key = FOLLOWING_LIST_PREFIX + userId;
        redisService.setCacheObject(key, followingList, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
        log.debug("缓存关注列表: userId={}, size={}", userId, followingList.size());
    }

    /**
     * 获取关注列表
     *
     * @param userId 用户ID
     * @return 关注的用户ID列表
     */
    public List<String> getFollowingList(String userId) {
        String key = FOLLOWING_LIST_PREFIX + userId;
        List<String> list = redisService.getCacheObject(key);
        return list != null ? list : new ArrayList<>();
    }

    /**
     * 缓存粉丝列表
     *
     * @param userId       用户ID
     * @param followerList 粉丝用户ID列表
     */
    public void cacheFollowerList(String userId, List<String> followerList) {
        String key = FOLLOWER_LIST_PREFIX + userId;
        redisService.setCacheObject(key, followerList, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
        log.debug("缓存粉丝列表: userId={}, size={}", userId, followerList.size());
    }

    /**
     * 获取粉丝列表
     *
     * @param userId 用户ID
     * @return 粉丝用户ID列表
     */
    public List<String> getFollowerList(String userId) {
        String key = FOLLOWER_LIST_PREFIX + userId;
        List<String> list = redisService.getCacheObject(key);
        return list != null ? list : new ArrayList<>();
    }

    /**
     * 清除用户的所有关注缓存
     *
     * @param userId 用户ID
     */
    public void clearUserFollowCache(String userId) {
        // 清除关注数和粉丝数
        redisService.deleteObject(FOLLOWER_COUNT_PREFIX + userId);
        redisService.deleteObject(FOLLOWING_COUNT_PREFIX + userId);

        // 清除关注列表和粉丝列表
        redisService.deleteObject(FOLLOWING_LIST_PREFIX + userId);
        redisService.deleteObject(FOLLOWER_LIST_PREFIX + userId);

        log.debug("清除用户关注缓存: userId={}", userId);
    }

    /**
     * 获取所有关注关系的Redis键
     *
     * @return Redis键集合
     */
    public Set<String> getAllFollowKeys() {
        java.util.Collection<String> keys = redisService.keys(FOLLOW_KEY_PREFIX + "*");
        return new HashSet<>(keys != null ? keys : java.util.Collections.emptySet());
    }

    /**
     * 获取所有粉丝数的Redis键
     *
     * @return Redis键集合
     */
    public Set<String> getAllFollowerCountKeys() {
        java.util.Collection<String> keys = redisService.keys(FOLLOWER_COUNT_PREFIX + "*");
        return new HashSet<>(keys != null ? keys : java.util.Collections.emptySet());
    }

    /**
     * 获取所有关注数的Redis键
     *
     * @return Redis键集合
     */
    public Set<String> getAllFollowingCountKeys() {
        java.util.Collection<String> keys = redisService.keys(FOLLOWING_COUNT_PREFIX + "*");
        return new HashSet<>(keys != null ? keys : java.util.Collections.emptySet());
    }
}

