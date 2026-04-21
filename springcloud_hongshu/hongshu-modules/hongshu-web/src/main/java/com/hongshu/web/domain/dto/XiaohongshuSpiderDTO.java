package com.hongshu.web.domain.dto;

import lombok.Data;

/**
 * 小红书爬虫请求DTO
 *
 * @author: hongshu
 */
@Data
public class XiaohongshuSpiderDTO {
    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 爬取类型：note（笔记）、image（图片）、video（视频）
     */
    private String type;

    /**
     * 排序方式：0-综合排序, 1-最新, 2-最多点赞, 3-最多评论, 4-最多收藏
     */
    private Integer sortType;

    /**
     * 笔记时间：0-不限, 1-一天内, 2-一周内, 3-半年内
     */
    private Integer noteTime;

    /**
     * 当前页码
     */
    private Integer page;

    /**
     * 每页数量
     */
    private Integer pageSize;

    /**
     * Cookie值（必需，用于登录后的请求）
     * 格式：key1=value1; key2=value2; ...
     */
    private String cookie;
}
