package com.hongshu.web.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 通义千问内容生成请求DTO
 *
 * @author hongshu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TongYiContentRequestDTO {

    /**
     * 图片URL列表，支持远程图片
     */
    private List<String> imageUrls;

    /**
     * 视频URL，支持远程视频
     */
    private String videoUrl;

    /**
     * 商品类别，例如"美妆"、"数码"
     */
    private String category;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品描述
     */
    private String productDescription;

    /**
     * 文案风格，例如"小红书笔记"，默认为"小红书笔记"
     */
    private String style;

    /**
     * 内容类型：note-笔记，idle-闲置商品
     */
    private String contentType;

    /**
     * 是否只生成标题
     */
    private Boolean titleOnly;
}

