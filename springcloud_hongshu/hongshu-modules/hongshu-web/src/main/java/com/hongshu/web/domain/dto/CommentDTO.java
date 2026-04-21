package com.hongshu.web.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 评论
 *
 * @author: hongshu
 */
@Data
@Schema(name = "评论DTO")
public class CommentDTO implements Serializable {

    @Schema(description = "笔记id")
    private String nid;

    @Schema(description = "商品ID")
    private String productId;

    @Schema(description = "笔记发布的用户id")
    private String noteUid;

    @Schema(description = "商品发布的用户id")
    private String productUid;

    @Schema(description = "评论的父id")
    private String pid;

    @Schema(description = "当前评论回复的评论id")
    private String replyId;

    @Schema(description = "回复的评论的用户id")
    private String replyUid;

    @Schema(description = "评论等级")
    private Integer level;

    @Schema(description = "评论内容")
    private String content;
}
