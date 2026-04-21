package com.hongshu.web.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 数据中心-热门内容-笔记数据 VO
 */
@Data
public class NoteDataCenterVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 笔记ID */
    private String id;
    /** 标题 */
    private String title;
    /** 封面图 */
    private String cover;
    /** 发布时间 yyyy-MM-dd HH:mm */
    private String publishTime;
    /** 是否未通过审核 */
    private Boolean notApproved;
    /** 曝光数 */
    private Long exposure;
    /** 观看数 */
    private Long views;
    /** 封面点击率，如 11.10% */
    private String coverCtr;
    /** 点赞数 */
    private Long likes;
    /** 评论数 */
    private Long comments;
    /** 收藏数 */
    private Long favorites;
    /** 涨粉数 */
    private Long follows;
    /** 分享数 */
    private Long shares;
    /** 人均观看时长，如 2s、17s */
    private String avgDuration;
    /** 弹幕数 */
    private Long danmaku;
}
