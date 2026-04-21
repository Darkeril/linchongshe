package com.hongshu.web.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 反馈DTO
 *
 * @author: hongshu
 */
@Data
@Schema(name = "反馈DTO")
public class FeedbackDTO implements Serializable {

    @Schema(description = "反馈内容")
    @NotBlank(message = "反馈内容不能为空")
    @Size(min = 5, max = 500, message = "反馈内容长度必须在5-500字符之间")
    private String content;

    @Schema(description = "反馈类型：feature(功能建议)、bug(问题反馈)、other(其他)")
    private String type;
}

