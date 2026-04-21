package com.hongshu.web.service.sys;

import com.hongshu.web.domain.vo.AnalyticsDataVo;

/**
 * 数据中心-数据分析 Service
 */
public interface IAnalyticsService {

    /**
     * 获取数据分析聚合数据（观看、互动、涨粉、发布四个 Tab）
     *
     * @param uid  创作者用户ID，为空则使用当前登录用户
     * @param days 统计天数，7 或 30
     */
    AnalyticsDataVo getAnalytics(String uid, int days);
}
