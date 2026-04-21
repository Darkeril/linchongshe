package com.hongshu.web.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 市集轮播图单项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarouselItemDTO {

    /**
     * 图片地址
     */
    private String url;
    /**
     * 点击跳转链接（可选）
     */
    private String link;
    /**
     * 标题/说明（可选）
     */
    private String title;
    /**
     * 排序号，越小越靠前
     */
    private Integer sortOrder;
}
