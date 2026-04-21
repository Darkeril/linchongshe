package com.hongshu.web.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 数据中心-数据分析-单个 Tab（互动/涨粉/发布）通用结构
 */
@Data
public class AnalyticsTabVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 总览卡片 */
    private List<AnalyticsOverviewItemVo> overview;
    /** 趋势：按日统计，长度等于 days */
    private List<Long> trend;
}
