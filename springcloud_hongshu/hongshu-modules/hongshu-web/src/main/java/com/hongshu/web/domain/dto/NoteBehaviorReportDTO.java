package com.hongshu.web.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 笔记行为上报 DTO（曝光/观看/观看时长/分享）
 */
@Data
@Schema(description = "笔记行为上报")
public class NoteBehaviorReportDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "事件类型不能为空")
    @Schema(description = "事件类型：exposure-曝光 view-观看 watch-观看时长/完播 share-分享", requiredMode = Schema.RequiredMode.REQUIRED)
    private String eventType;

    @NotBlank(message = "笔记ID不能为空")
    @Schema(description = "笔记ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nid;

    @Schema(description = "用户ID，不传则从 token 取")
    private String uid;

    @Schema(description = "来源场景：search-搜索 recommend-推荐 follow-关注流 profile-个人页 other")
    private String scene;

    @Schema(description = "观看时长（秒），eventType=watch 时必填")
    private Integer durationSec;

    @Schema(description = "是否完播：0-否 1-是，eventType=watch 时可选")
    private Integer completed;

    /** 批量上报时使用 */
    @Schema(hidden = true)
    private List<NoteBehaviorReportDTO> list;
}
