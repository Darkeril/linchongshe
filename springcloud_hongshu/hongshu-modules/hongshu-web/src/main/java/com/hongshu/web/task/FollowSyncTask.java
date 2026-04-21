package com.hongshu.web.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hongshu.web.domain.entity.WebFollower;
import com.hongshu.web.domain.entity.WebUser;
import com.hongshu.web.mapper.web.WebFollowerMapper;
import com.hongshu.web.mapper.web.WebUserMapper;
import com.hongshu.web.rocketmq.FollowCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 关注数据定时同步任务
 * 定期同步Redis中的关注数据到数据库
 *
 * @author: hongshu
 */
@Slf4j
@Component
public class FollowSyncTask {

    @Autowired
    private FollowCacheService cacheService;

    @Autowired
    private WebFollowerMapper followerMapper;

    @Autowired
    private WebUserMapper userMapper;

    /**
     * 定时同步粉丝数和关注数
     * 每10分钟执行一次
     */
    @Scheduled(fixedDelay = 600000) // 10分钟 = 600000毫秒
    public void syncFollowCounts() {
        try {
            log.info("开始同步粉丝数和关注数到数据库...");

            // 同步粉丝数
            int followerCountSuccess = syncFollowerCounts();

            // 同步关注数
            int followingCountSuccess = syncFollowingCounts();

            log.info("粉丝数和关注数同步完成: 粉丝数同步={}, 关注数同步={}",
                    followerCountSuccess, followingCountSuccess);
        } catch (Exception e) {
            log.error("同步粉丝数和关注数异常", e);
        }
    }

    /**
     * 同步粉丝数
     *
     * @return 同步成功的数量
     */
    private int syncFollowerCounts() {
        try {
            Set<String> keys = cacheService.getAllFollowerCountKeys();

            if (keys.isEmpty()) {
                log.debug("没有需要同步的粉丝数");
                return 0;
            }

            int successCount = 0;

            for (String key : keys) {
                try {
                    // 从Redis键中提取用户ID
                    String userId = key.replace("follower:count:", "");

                    // 获取Redis中的粉丝数
                    Long cachedCount = cacheService.getFollowerCount(userId);

                    // 查询数据库中的实际粉丝数
                    Long dbCount = followerMapper.selectCount(
                            new QueryWrapper<WebFollower>().eq("fid", userId)
                    );

                    // 如果不一致，更新数据库
                    if (!cachedCount.equals(dbCount)) {
                        WebUser user = new WebUser();
                        user.setId(userId);
                        user.setFollowerCount(cachedCount);

                        int rows = userMapper.updateById(user);

                        if (rows > 0) {
                            successCount++;
                            log.debug("同步粉丝数成功: userId={}, cachedCount={}, dbCount={}",
                                    userId, cachedCount, dbCount);
                        }
                    }
                } catch (Exception e) {
                    log.error("同步单个用户粉丝数失败: key={}", key, e);
                }
            }

            return successCount;
        } catch (Exception e) {
            log.error("同步粉丝数异常", e);
            return 0;
        }
    }

    /**
     * 同步关注数
     *
     * @return 同步成功的数量
     */
    private int syncFollowingCounts() {
        try {
            Set<String> keys = cacheService.getAllFollowingCountKeys();

            if (keys.isEmpty()) {
                log.debug("没有需要同步的关注数");
                return 0;
            }

            int successCount = 0;

            for (String key : keys) {
                try {
                    String userId = key.replace("following:count:", "");

                    Long cachedCount = cacheService.getFollowingCount(userId);

                    Long dbCount = followerMapper.selectCount(
                            new QueryWrapper<WebFollower>().eq("uid", userId)
                    );

                    if (!cachedCount.equals(dbCount)) {
                        WebUser user = new WebUser();
                        user.setId(userId);
                        user.setFollowerCount(cachedCount);

                        int rows = userMapper.updateById(user);

                        if (rows > 0) {
                            successCount++;
                            log.debug("同步关注数成功: userId={}, cachedCount={}, dbCount={}",
                                    userId, cachedCount, dbCount);
                        }
                    }
                } catch (Exception e) {
                    log.error("同步单个用户关注数失败: key={}", key, e);
                }
            }

            return successCount;
        } catch (Exception e) {
            log.error("同步关注数异常", e);
            return 0;
        }
    }

    /**
     * 定时检查并修复数据一致性
     * 每天凌晨3点执行
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void checkDataConsistency() {
        try {
            log.info("开始检查关注数据一致性...");

            // 查询所有用户
            List<WebUser> users = userMapper.selectList(null);

            int fixedCount = 0;

            for (WebUser user : users) {
                try {
                    String userId = user.getId();

                    // 查询数据库中的实际粉丝数和关注数
                    Long actualFollowerCount = followerMapper.selectCount(
                            new QueryWrapper<WebFollower>().eq("fid", userId)
                    );
                    Long actualFollowingCount = followerMapper.selectCount(
                            new QueryWrapper<WebFollower>().eq("uid", userId)
                    );

                    // 检查是否一致
                    boolean needUpdate = false;
                    WebUser updateUser = new WebUser();
                    updateUser.setId(userId);

                    if (user.getFollowerCount() != null && !user.getFollowerCount().equals(actualFollowerCount)) {
                        updateUser.setFollowerCount(actualFollowerCount);
                        needUpdate = true;
                        log.warn("发现粉丝数不一致: userId={}, userFollowerCount={}, actualFollowerCount={}",
                                userId, user.getFollowerCount(), actualFollowerCount);
                    }

                    if (user.getFollowerCount() != null && !user.getFollowerCount().equals(actualFollowingCount)) {
                        updateUser.setFollowerCount(actualFollowingCount);
                        needUpdate = true;
                        log.warn("发现关注数不一致: userId={}, userFollowingCount={}, actualFollowingCount={}",
                                userId, user.getFollowerCount(), actualFollowingCount);
                    }

                    if (needUpdate) {
                        userMapper.updateById(updateUser);

                        // 更新Redis缓存
                        cacheService.setFollowerCount(userId, actualFollowerCount);
                        cacheService.setFollowingCount(userId, actualFollowingCount);

                        fixedCount++;
                    }
                } catch (Exception e) {
                    log.error("检查用户数据一致性失败: userId={}", user.getId(), e);
                }
            }

            log.info("关注数据一致性检查完成: 修复数量={}", fixedCount);
        } catch (Exception e) {
            log.error("检查数据一致性异常", e);
        }
    }

    /**
     * 定时清理过期的关注缓存
     * 每天凌晨4点执行
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void cleanExpiredFollowCache() {
        try {
            log.info("开始清理过期的关注缓存...");

            Set<String> followKeys = cacheService.getAllFollowKeys();
            Set<String> followerCountKeys = cacheService.getAllFollowerCountKeys();
            Set<String> followingCountKeys = cacheService.getAllFollowingCountKeys();

            log.info("当前缓存统计: 关注关系={}, 粉丝数={}, 关注数={}",
                    followKeys.size(), followerCountKeys.size(), followingCountKeys.size());

            // Redis的过期键会自动删除，这里只需要记录日志
        } catch (Exception e) {
            log.error("清理过期关注缓存异常", e);
        }
    }
}


