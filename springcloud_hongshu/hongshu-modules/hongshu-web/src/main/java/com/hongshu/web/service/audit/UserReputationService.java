package com.hongshu.web.service.audit;

import com.hongshu.web.domain.entity.WebUser;
import com.hongshu.web.mapper.web.WebUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 用户信誉评分服务
 *
 * @author hongshu
 */
@Slf4j
@Service
public class UserReputationService {

    @Autowired
    private WebUserMapper userMapper;

    /**
     * 获取用户信誉分数 (0-100)
     * 
     * @param userId 用户ID
     * @return 信誉分数
     */
    public int getUserReputation(String userId) {
        try {
            WebUser user = userMapper.selectById(userId);
            if (user == null) {
                return 0;
            }

            int score = 50; // 基础分数

            // 1. 账号年龄加分（最多+20分）
            if (user.getCreateTime() != null) {
                long days = (new Date().getTime() - user.getCreateTime().getTime()) / (1000 * 60 * 60 * 24);
                if (days > 365) {
                    score += 20;
                } else if (days > 180) {
                    score += 15;
                } else if (days > 90) {
                    score += 10;
                } else if (days > 30) {
                    score += 5;
                }
            }

            // 2. 认证用户加分（+15分）
            if ("1".equals(user.getStatus())) {
                score += 15;
            }

            // 3. VIP用户加分（+15分）
            // 假设有VIP字段，根据实际情况调整
            // if (user.getVipLevel() != null && user.getVipLevel() > 0) {
            //     score += 15;
            // }

            // 4. 粉丝数加分（最多+10分）
            if (user.getFanCount() != null) {
                if (user.getFanCount() > 10000) {
                    score += 10;
                } else if (user.getFanCount() > 1000) {
                    score += 7;
                } else if (user.getFanCount() > 100) {
                    score += 5;
                }
            }

            // 5. 发布内容数加分（最多+10分）
            if (user.getNoteCount() != null) {
                if (user.getNoteCount() > 100) {
                    score += 10;
                } else if (user.getNoteCount() > 50) {
                    score += 7;
                } else if (user.getNoteCount() > 10) {
                    score += 5;
                }
            }

            // 确保分数在0-100之间
            score = Math.max(0, Math.min(100, score));

            log.debug("用户{}的信誉分数: {}", userId, score);
            return score;

        } catch (Exception e) {
            log.error("获取用户信誉分数失败: userId={}", userId, e);
            return 50; // 异常时返回中等分数
        }
    }

    /**
     * 判断是否为高信誉用户
     * 
     * @param userId 用户ID
     * @return true-高信誉用户
     */
    public boolean isHighReputationUser(String userId) {
        return getUserReputation(userId) >= 80;
    }

    /**
     * 判断是否为低信誉用户
     * 
     * @param userId 用户ID
     * @return true-低信誉用户
     */
    public boolean isLowReputationUser(String userId) {
        return getUserReputation(userId) < 50;
    }

    /**
     * 获取用户信誉等级
     * 
     * @param userId 用户ID
     * @return 等级：HIGH, MEDIUM, LOW
     */
    public String getUserReputationLevel(String userId) {
        int score = getUserReputation(userId);
        if (score >= 80) {
            return "HIGH";
        } else if (score >= 50) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
}
