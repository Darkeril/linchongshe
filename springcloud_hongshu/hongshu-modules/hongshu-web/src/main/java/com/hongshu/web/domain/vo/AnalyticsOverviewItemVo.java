package com.hongshu.web.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 数据中心-数据分析-概览单项（总览卡片）
 */
@Data
public class AnalyticsOverviewItemVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 指标名称，如 "曝光数"、"观看数" */
    private String label;
    /** 指标值，可为数字或带单位字符串，如 1124、"21.9%"、"22秒" */
    private Object value;
    /** 环比百分比，如 -3 表示环比 -3% */
    private Integer ring;
    /** 环比文案，如 "环比 -3%" */
    private String ringText;
}
