package com.hongshu.web.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 点赞/收藏
 *
 * @author: hongshu
 */
@Data
@Schema(name = "点赞收藏DTO")
public class LikeOrCollectionDTO implements Serializable {

    @Schema(description = "点赞或收藏的id")
    private String likeOrCollectionId;

    @Schema(description = "收藏收藏需要通知的用户id")
    private String publishUid;

    @Schema(description = "收藏收藏类型:1 点赞图片 2点赞评论  3收藏图片 4收藏专辑")
    private Integer type;
}
