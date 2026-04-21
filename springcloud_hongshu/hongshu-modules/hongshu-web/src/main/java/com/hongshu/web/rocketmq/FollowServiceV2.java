package com.hongshu.web.rocketmq;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hongshu.common.core.context.AuthContextHolder;
import com.hongshu.web.domain.entity.WebFollower;
import com.hongshu.web.domain.entity.WebUser;
import com.hongshu.web.im.ChatUtils;
import com.hongshu.web.mapper.web.WebFollowerMapper;
import com.hongshu.web.mapper.web.WebUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 关注服务V2（优化版）
 * 使用Redis缓存 + RocketMQ异步更新
 *
 * @author: hongshu
 */
@Slf4j
@Service
public class FollowServiceV2 {

    @Autowired
    private FollowCacheService cacheService;

    @Autowired(required = false)
    private FollowProducerService producerService;

    @Autowired
    private WebFollowerMapper followerMapper;

    @Autowired
    private WebUserMapper userMapper;
    
    @Autowired
    private ChatUtils chatUtils;

    /**
     * 关注/取消关注（优化版）
     *
     * @param fid 被关注用户ID
     * @return true-关注成功，false-取消关注成功
     */
    public boolean follow(String fid) {
        try {
            String currentUserId = AuthContextHolder.getUserId();

            if (currentUserId.equals(fid)) {
                log.warn("不能关注自己: userId={}", currentUserId);
                return false;
            }

            // 1. 检查Redis缓存的关注状态
            Boolean isFollowing = cacheService.isFollowing(currentUserId, fid);

            if (isFollowing) {
                // 已关注，执行取消关注
                return unfollowUser(currentUserId, fid);
            } else {
                // 未关注，执行关注
                return followUser(currentUserId, fid);
            }
        } catch (Exception e) {
            log.error("关注操作失败: fid={}", fid, e);
            // 降级：直接操作数据库
            return fallbackFollow(fid);
        }
    }

    /**
     * 关注用户
     *
     * @param userId    用户ID（关注者）
     * @param fid 被关注用户ID
     * @return true-成功，false-失败
     */
    private boolean followUser(String userId, String fid) {
        try {
            // 1. 更新Redis缓存
            cacheService.cacheFollow(userId, fid);
            cacheService.incrementFollowerCount(fid);
            cacheService.incrementFollowingCount(userId);

            // 2. 清除关注列表缓存（需要重新加载）
            cacheService.clearUserFollowCache(userId);
            cacheService.clearUserFollowCache(fid);

            // 3. 异步更新数据库
            asyncUpdateFollow(userId, fid, true);

            log.info("关注成功: userId={}, fid={}", userId, fid);
            return true;
        } catch (Exception e) {
            log.error("关注用户失败: userId={}, fid={}", userId, fid, e);
            // 回滚Redis操作
            cacheService.removeFollow(userId, fid);
            cacheService.decrementFollowerCount(fid);
            cacheService.decrementFollowingCount(userId);
            throw e;
        }
    }

    /**
     * 取消关注用户
     *
     * @param userId    用户ID（关注者）
     * @param fid 被关注用户ID
     * @return true-成功，false-失败
     */
    private boolean unfollowUser(String userId, String fid) {
        try {
            // 1. 更新Redis缓存
            cacheService.removeFollow(userId, fid);
            cacheService.decrementFollowerCount(fid);
            cacheService.decrementFollowingCount(userId);

            // 2. 清除关注列表缓存
            cacheService.clearUserFollowCache(userId);
            cacheService.clearUserFollowCache(fid);

            // 3. 异步更新数据库
            asyncUpdateFollow(userId, fid, false);

            log.info("取消关注成功: userId={}, fid={}", userId, fid);
            return false;
        } catch (Exception e) {
            log.error("取消关注失败: userId={}, fid={}", userId, fid, e);
            // 回滚Redis操作
            cacheService.cacheFollow(userId, fid);
            cacheService.incrementFollowerCount(fid);
            cacheService.incrementFollowingCount(userId);
            throw e;
        }
    }

    /**
     * 检查是否关注（优化版）
     *
     * @param fid 被关注用户ID
     * @return true-已关注，false-未关注
     */
    public boolean isFollowing(String fid) {
        try {
            String currentUserId = AuthContextHolder.getUserId();

            // 1. 先从Redis获取
            Boolean cached = cacheService.isFollowing(currentUserId, fid);

            if (cached != null && cached) {
                return true;
            }

            // 2. Redis未命中，查询数据库并缓存
            boolean dbStatus = isFollowingFromDb(currentUserId, fid);

            if (dbStatus) {
                cacheService.cacheFollow(currentUserId, fid);
            }

            return dbStatus;
        } catch (Exception e) {
            log.error("检查关注状态失败: fid={}", fid, e);
            return false;
        }
    }

    /**
     * 从数据库查询关注状态
     *
     * @param userId    用户ID
     * @param fid 被关注用户ID
     * @return true-已关注，false-未关注
     */
    private boolean isFollowingFromDb(String userId, String fid) {
        Long count = followerMapper.selectCount(
                new QueryWrapper<WebFollower>()
                        .eq("uid", userId)
                        .eq("fid", fid)
        );
        return count > 0;
    }

    /**
     * 获取粉丝数（优化版）
     *
     * @param userId 用户ID
     * @return 粉丝数
     */
    public Long getFollowerCount(String userId) {
        try {
            // 先从Redis获取
            Long cachedCount = cacheService.getFollowerCount(userId);

            if (cachedCount > 0) {
                return cachedCount;
            }

            // Redis未命中，查询数据库并缓存
            Long dbCount = followerMapper.selectCount(
                    new QueryWrapper<WebFollower>().eq("fid", userId)
            );

            if (dbCount > 0) {
                cacheService.setFollowerCount(userId, dbCount);
            }

            return dbCount;
        } catch (Exception e) {
            log.error("获取粉丝数失败: userId={}", userId, e);
            return 0L;
        }
    }

    /**
     * 获取关注数（优化版）
     *
     * @param userId 用户ID
     * @return 关注数
     */
    public Long getFollowingCount(String userId) {
        try {
            Long cachedCount = cacheService.getFollowingCount(userId);

            if (cachedCount > 0) {
                return cachedCount;
            }

            Long dbCount = followerMapper.selectCount(
                    new QueryWrapper<WebFollower>().eq("uid", userId)
            );

            if (dbCount > 0) {
                cacheService.setFollowingCount(userId, dbCount);
            }

            return dbCount;
        } catch (Exception e) {
            log.error("获取关注数失败: userId={}", userId, e);
            return 0L;
        }
    }

    /**
     * 获取关注列表（优化版）
     *
     * @param userId 用户ID
     * @return 关注的用户ID列表
     */
    public List<String> getFollowingList(String userId) {
        try {
            // 先从Redis获取
            List<String> cachedList = cacheService.getFollowingList(userId);

            if (!cachedList.isEmpty()) {
                return cachedList;
            }

            // Redis未命中，查询数据库并缓存
            List<WebFollower> followers = followerMapper.selectList(
                    new QueryWrapper<WebFollower>()
                            .eq("uid", userId)
                            .select("fid")
            );

            List<String> followingList = followers.stream()
                    .map(WebFollower::getFid)
                    .collect(Collectors.toList());

            if (!followingList.isEmpty()) {
                cacheService.cacheFollowingList(userId, followingList);
            }

            return followingList;
        } catch (Exception e) {
            log.error("获取关注列表失败: userId={}", userId, e);
            return new java.util.ArrayList<>();
        }
    }

    /**
     * 异步更新关注关系到数据库
     *
     * @param userId    用户ID
     * @param fid 被关注用户ID
     * @param isFollow  true-关注，false-取消关注
     */
    private void asyncUpdateFollow(String userId, String fid, boolean isFollow) {
        try {
            if (producerService != null) {
                // RocketMQ可用，发送异步消息
                producerService.sendFollowMessage(userId, fid, isFollow);
            } else {
                // RocketMQ不可用，直接更新数据库
                syncUpdateFollow(userId, fid, isFollow);
            }
        } catch (Exception e) {
            log.error("异步更新关注关系失败，降级为同步更新: userId={}, fid={}, isFollow={}",
                    userId, fid, isFollow, e);
            syncUpdateFollow(userId, fid, isFollow);
        }
    }

    /**
     * 同步更新关注关系到数据库
     *
     * @param userId    用户ID
     * @param fid 被关注用户ID
     * @param isFollow  true-关注，false-取消关注
     */
    private void syncUpdateFollow(String userId, String fid, boolean isFollow) {
        try {
            if (isFollow) {
                // 插入关注记录
                WebFollower follower = new WebFollower();
                follower.setUid(userId);
                follower.setFid(fid);
                follower.setCreateTime(new Date());
                follower.setUpdateTime(new Date());
                followerMapper.insert(follower);
            } else {
                // 删除关注记录
                followerMapper.delete(
                        new QueryWrapper<WebFollower>()
                                .eq("uid", userId)
                                .eq("fid", fid)
                );
            }

            // 更新用户粉丝数和关注数
            updateUserFollowCountInDb(userId, fid);
            
            // 发送WebSocket通知（仅关注时发送，取消关注不通知）
            if (isFollow) {
                try {
                    chatUtils.sendMessage(fid, 2);
                    log.debug("发送关注通知成功（同步模式）: fromUserId={}, toUserId={}", userId, fid);
                } catch (Exception e) {
                    log.error("发送关注通知失败: userId={}, fid={}", userId, fid, e);
                }
            }

            log.info("同步更新关注关系成功: userId={}, fid={}, isFollow={}",
                    userId, fid, isFollow);
        } catch (Exception e) {
            log.error("同步更新关注关系失败: userId={}, fid={}, isFollow={}",
                    userId, fid, isFollow, e);
        }
    }

    /**
     * 更新数据库中的用户粉丝数和关注数
     *
     * @param userId    用户ID
     * @param fid 被关注用户ID
     */
    private void updateUserFollowCountInDb(String userId, String fid) {
        try {
            // 更新关注者的关注数（followerCount表示关注数）
            Long followingCount = followerMapper.selectCount(
                    new QueryWrapper<WebFollower>().eq("uid", userId)
            );
            WebUser user = new WebUser();
            user.setId(userId);
            user.setFollowerCount(followingCount);
            user.setUpdateTime(new Date());
            userMapper.updateById(user);

            // 更新被关注者的粉丝数（fanCount表示粉丝数）
            Long fanCount = followerMapper.selectCount(
                    new QueryWrapper<WebFollower>().eq("fid", fid)
            );
            WebUser followedUser = new WebUser();
            followedUser.setId(fid);
            followedUser.setFanCount(fanCount);
            followedUser.setUpdateTime(new Date());
            userMapper.updateById(followedUser);
        } catch (Exception e) {
            log.error("更新用户粉丝数和关注数失败: userId={}, fid={}", userId, fid, e);
        }
    }

    /**
     * 降级处理：直接操作数据库
     *
     * @param fid 被关注用户ID
     * @return true-关注成功，false-取消关注成功
     */
    private boolean fallbackFollow(String fid) {
        try {
            String currentUserId = AuthContextHolder.getUserId();

            // 检查是否已关注
            Long count = followerMapper.selectCount(
                    new QueryWrapper<WebFollower>()
                            .eq("uid", currentUserId)
                            .eq("fid", fid)
            );

            if (count > 0) {
                // 取消关注
                followerMapper.delete(
                        new QueryWrapper<WebFollower>()
                                .eq("uid", currentUserId)
                                .eq("fid", fid)
                );
                updateUserFollowCountInDb(currentUserId, fid);
                return false;
            } else {
                // 关注
                WebFollower follower = new WebFollower();
                follower.setUid(currentUserId);
                follower.setFid(fid);
                follower.setCreateTime(new Date());
                follower.setUpdateTime(new Date());
                followerMapper.insert(follower);
                updateUserFollowCountInDb(currentUserId, fid);
                return true;
            }
        } catch (Exception e) {
            log.error("降级处理失败: fid={}", fid, e);
            return false;
        }
    }
}

