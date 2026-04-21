package com.hongshu.web.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 数据中心-粉丝画像-概览与趋势 VO
 */
@Data
public class FanProfileOverviewVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 总粉丝数 */
    private Long totalFans;
    /** 近 N 天新增粉丝数 */
    private Long newFans;
    /** 近 N 天流失粉丝数（当前无取关流水时返回 null） */
    private Long lostFans;

    /** 趋势：日期列表，如 ["01-01","01-02",...] */
    private List<String> dates;
    /** 趋势：每日总粉丝数（截至当日） */
    private List<Long> total;
    /** 趋势：每日新增粉丝数 */
    private List<Long> newFansTrend;
    /** 趋势：每日流失粉丝数（当前无取关流水时为空） */
    private List<Long> lostFansTrend;
}
