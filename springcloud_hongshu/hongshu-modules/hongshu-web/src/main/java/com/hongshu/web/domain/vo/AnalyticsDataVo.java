package com.hongshu.web.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 数据中心-数据分析-聚合返回（四个 Tab 数据）
 */
@Data
public class AnalyticsDataVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 观看数据 */
    private AnalyticsViewVo view;
    /** 互动数据 */
    private AnalyticsTabVo interact;
    /** 涨粉数据 */
    private AnalyticsTabVo fans;
    /** 发布数据 */
    private AnalyticsTabVo publish;
}
