package com.hongshu.web.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理端登录页左侧圆形视频/图片展示区单项（固定 7 个槽位，由前端与默认布局对齐）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoginPromoCircleDTO {

    /**
     * 是否展示该圆框
     */
    private Boolean enabled;

    /**
     * 视频地址（mp4 等），为空则登录页使用该槽位内置默认资源或仅图片
     */
    private String videoUrl;

    /**
     * 图片地址（无视频或作封面时可填）
     */
    private String imageUrl;

    private Integer width;
    private Integer height;
    private Integer marginTop;
    private Integer marginLeft;
    private String background;
    private String transform;
}
