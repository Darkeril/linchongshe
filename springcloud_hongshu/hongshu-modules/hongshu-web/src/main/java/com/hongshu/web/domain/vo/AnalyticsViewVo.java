package com.hongshu.web.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 数据中心-数据分析-观看数据
 */
@Data
public class AnalyticsViewVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 总览卡片：曝光数、观看数、封面点击率、平均观看时长、观看总时长、视频完播率等 */
    private List<AnalyticsOverviewItemVo> overview;
    /** 趋势：每日观看数，长度等于 days */
    private List<Long> trend;
    /** 观看来源饼图：{ name, value } */
    private List<AnalyticsNameValueVo> source;
    /** 观看时段：24 小时分布，长度为 24 */
    private List<Integer> timeSlot;
}
