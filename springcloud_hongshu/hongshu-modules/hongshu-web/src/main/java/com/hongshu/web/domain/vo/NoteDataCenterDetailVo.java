package com.hongshu.web.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 数据中心-笔记数据详情页 返回 VO
 */
@Data
public class NoteDataCenterDetailVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 笔记基本信息 */
    private NoteInfo noteInfo;
    /** 核心数据概览：曝光、观看、观看时长、互动数及环比 */
    private List<NoteDetailOverviewItem> overview;
    /** 曝光趋势（按日） */
    private List<NoteDetailTrendPoint> exposureTrend;
    /** 观看趋势（按日） */
    private List<NoteDetailTrendPoint> viewTrend;
    /** 观看来源：推荐页、关注页、搜索页、个人主页、其他 */
    private List<NoteDetailNameValue> viewSource;
    /** 24 小时观看分布（0-23 点，值为观看数） */
    private List<Long> viewTimeSlot;
    /** 人均观看时长（秒） */
    private Integer avgDurationSec;
    /** 完播率 0-100 */
    private Double completionRate;
    /** 2秒退出率 0-100 */
    private Double twoSecExitRate;
    /** 分享数 */
    private Long shares;
    /** 点赞数（来自笔记表） */
    private Long likes;
    /** 评论数 */
    private Long comments;
    /** 收藏数 */
    private Long favorites;
    /** 观众画像：性别/年龄/城市/兴趣分布（name=分类，value=占比或人数） */
    private AudienceProfile audienceProfile;

    @Data
    public static class AudienceProfile implements Serializable {
        private static final long serialVersionUID = 1L;
        /** 性别分布，如 男性/女性，value 为占比 0-100 或人数 */
        private List<NoteDetailNameValue> gender;
        /** 年龄分布，如 18-24/25-34，value 为占比或人数 */
        private List<NoteDetailNameValue> age;
        /** 城市分布，name 为城市名，value 为占比或人数 */
        private List<NoteDetailNameValue> region;
        /** 兴趣分布，name 为兴趣标签，value 为占比或人数 */
        private List<NoteDetailNameValue> interest;
    }

    @Data
    public static class NoteInfo implements Serializable {
        private String id;
        private String title;
        private String cover;
        private String author;
        private String publishTime;
        /** 标签，逗号分隔或 JSON 数组字符串 */
        private String tags;
    }

    @Data
    public static class NoteDetailOverviewItem implements Serializable {
        private String label;
        private Object value;
        /** 环比百分比，如 3.5、-1.2 */
        private Integer ring;
        private String ringText;
    }

    @Data
    public static class NoteDetailTrendPoint implements Serializable {
        private String date;
        private Long cnt;
    }

    @Data
    public static class NoteDetailNameValue implements Serializable {
        private String name;
        private Long value;
    }
}
