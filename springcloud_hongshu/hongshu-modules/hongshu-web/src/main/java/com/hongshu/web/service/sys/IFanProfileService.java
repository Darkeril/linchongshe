package com.hongshu.web.service.sys;

import com.hongshu.web.domain.vo.FanProfileDistributionVo;
import com.hongshu.web.domain.vo.FanProfileOverviewVo;

/**
 * 数据中心-粉丝画像 Service
 */
public interface IFanProfileService {

    /**
     * 粉丝画像概览与趋势（总粉丝、新增、流失、按日趋势）
     *
     * @param uid  创作者用户ID，为空则尝试当前登录用户
     * @param days 近 N 天（7 或 30）
     */
    FanProfileOverviewVo getOverview(String uid, int days);

    /**
     * 粉丝画像分布（性别、年龄、地域、兴趣）
     *
     * @param uid 创作者用户ID，为空则尝试当前登录用户
     */
    FanProfileDistributionVo getDistribution(String uid);
}
