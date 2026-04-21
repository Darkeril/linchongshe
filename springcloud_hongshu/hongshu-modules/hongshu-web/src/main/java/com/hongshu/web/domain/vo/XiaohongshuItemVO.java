package com.hongshu.web.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 小红书爬取结果项VO
 *
 * @author: hongshu
 */
@Data
public class XiaohongshuItemVO {
    /**
     * 笔记ID
     */
    private String id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 封面图
     */
    private String cover;

    /**
     * 图片列表
     */
    private List<String> images;

    /**
     * 视频URL
     */
    private String video;

    /**
     * 作者
     */
    private String author;

    /**
     * 作者ID
     */
    private String authorId;

    /**
     * 笔记链接
     */
    private String url;

    /**
     * 点赞数
     */
    private Long likeCount;

    /**
     * 收藏数
     */
    private Long collectionCount;

    /**
     * 评论数
     */
    private Long commentCount;

    /**
     * 浏览量
     */
    private Long viewCount;

    /**
     * 发布时间
     */
    private String publishTime;

    /**
     * 类型：note, image, video
     */
    private String type;
}
