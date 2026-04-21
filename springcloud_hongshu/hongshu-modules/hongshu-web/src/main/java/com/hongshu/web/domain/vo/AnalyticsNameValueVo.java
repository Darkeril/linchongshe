package com.hongshu.web.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 数据分析-名称数值对（饼图、来源等）
 */
@Data
public class AnalyticsNameValueVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private Long value;
}
