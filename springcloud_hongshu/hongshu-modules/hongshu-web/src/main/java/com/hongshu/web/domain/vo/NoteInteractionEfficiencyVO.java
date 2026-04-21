package com.hongshu.web.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 工作台-笔记互动效率（按笔记创建时间落在统计窗口内聚合）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteInteractionEfficiencyVO {

    @Builder.Default
    private Summary summary = new Summary();

    /** 趋势横轴标签，如 MM-dd */
    @Builder.Default
    private List<String> trendLabels = new ArrayList<>();

    /** 每日互动率 0~100，与 trendLabels 对齐 */
    @Builder.Default
    private List<Double> trendRates = new ArrayList<>();

    @Builder.Default
    private List<TopTypeItem> topTypes = new ArrayList<>();

    @Data
    public static class Summary {
        private long likeCount;
        private long commentCount;
        /** 窗口内新发笔记的收藏数合计（字段 collection_count 快照） */
        private long collectionCount;
        /** 互动率百分比：(点赞+评论) / max(浏览,1) * 100 */
        private double interactionRatePct;
        /** 环比，上一同长周期；null 表示无可比或上期为空 */
        private Double likeMomPct;
        private Double commentMomPct;
        private Double collectionMomPct;
        private Double rateMomPct;
    }

    @Data
    public static class TopTypeItem {
        private String name;
        private long count;
        /** 占窗口内新发笔记数的比例 */
        private double percent;
    }
}
