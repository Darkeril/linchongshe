package com.hongshu.web.rocketmq;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hongshu.web.domain.entity.WebFollower;
import com.hongshu.web.domain.entity.WebUser;
import com.hongshu.web.domain.message.FollowMessage;
import com.hongshu.web.im.ChatUtils;
import com.hongshu.web.mapper.web.WebFollowerMapper;
import com.hongshu.web.mapper.web.WebUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 关注消费者服务
 * 异步更新数据库中的关注关系
 *
 * @author: hongshu
 */
@Slf4j
@Service
// 添加条件注解，当配置为 false 时不加载
@ConditionalOnProperty(name = "rocketmq.consumer.listen", havingValue = "true")
@RocketMQMessageListener(
        topic = "follow-topic",
        consumerGroup = "follow-consumer-group"
)
public class FollowConsumerService implements RocketMQListener<FollowMessage> {

    @Autowired
    private WebFollowerMapper followerMapper;

    @Autowired
    private WebUserMapper userMapper;

    @Autowired
    private ChatUtils chatUtils;

    @Override
    public void onMessage(FollowMessage message) {
        try {
            log.info("收到关注操作消息: userId={}, fid={}, isFollow={}",
                    message.getUserId(), message.getFid(), message.getIsFollow());

            if (message.getIsFollow()) {
                // 关注操作
                handleFollow(message);
            } else {
                // 取消关注操作
                handleUnfollow(message);
            }
        } catch (Exception e) {
            log.error("处理关注消息失败: {}", message, e);
            throw e; // 抛出异常触发重试
        }
    }

    /**
     * 处理关注操作
     *
     * @param message 关注消息
     */
    private void handleFollow(FollowMessage message) {
        try {
            String userId = message.getUserId();
            String fid = message.getFid();

            // 检查是否已存在关注关系
            Long count = followerMapper.selectCount(
                    new QueryWrapper<WebFollower>()
                            .eq("uid", userId)
                            .eq("fid", fid)
            );

            if (count > 0) {
                log.warn("关注关系已存在: userId={}, fid={}", userId, fid);
                return;
            }

            // 插入关注记录
            WebFollower follower = new WebFollower();
            follower.setUid(userId);
            follower.setFid(fid);
            follower.setCreateTime(new Date());
            follower.setUpdateTime(new Date());

            int rows = followerMapper.insert(follower);

            if (rows > 0) {
                log.info("关注成功: userId={}, fid={}", userId, fid);

                // 更新用户粉丝数和关注数
                updateUserFollowCount(userId, fid, true);

                // 发送WebSocket通知（type=2 表示关注）
                try {
                    chatUtils.sendMessage(fid, 2);
                    log.debug("发送关注通知成功: fromUserId={}, toUserId={}", userId, fid);
                } catch (Exception e) {
                    log.error("发送关注通知失败: userId={}, fid={}", userId, fid, e);
                }
            } else {
                log.warn("关注失败: userId={}, fid={}", userId, fid);
            }
        } catch (Exception e) {
            log.error("处理关注操作异常: userId={}, fid={}",
                    message.getUserId(), message.getFid(), e);
            throw e;
        }
    }

    /**
     * 处理取消关注操作
     *
     * @param message 关注消息
     */
    private void handleUnfollow(FollowMessage message) {
        try {
            String userId = message.getUserId();
            String fid = message.getFid();

            // 删除关注记录
            int rows = followerMapper.delete(
                    new QueryWrapper<WebFollower>()
                            .eq("uid", userId)
                            .eq("fid", fid)
            );

            if (rows > 0) {
                log.info("取消关注成功: userId={}, fid={}", userId, fid);

                // 更新用户粉丝数和关注数
                updateUserFollowCount(userId, fid, false);
            } else {
                log.warn("取消关注失败，关注关系不存在: userId={}, fid={}", userId, fid);
            }
        } catch (Exception e) {
            log.error("处理取消关注操作异常: userId={}, fid={}",
                    message.getUserId(), message.getFid(), e);
            throw e;
        }
    }

    /**
     * 更新用户粉丝数和关注数
     *
     * @param userId    用户ID（关注者）
     * @param fid 被关注用户ID
     * @param isFollow  true-关注，false-取消关注
     */
    private void updateUserFollowCount(String userId, String fid, boolean isFollow) {
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

            log.debug("更新关注数统计成功: userId={}, followingCount={}, fid={}, fanCount={}",
                    userId, followingCount, fid, fanCount);
        } catch (Exception e) {
            log.error("更新关注数统计失败: userId={}, fid={}", userId, fid, e);
        }
    }
}

