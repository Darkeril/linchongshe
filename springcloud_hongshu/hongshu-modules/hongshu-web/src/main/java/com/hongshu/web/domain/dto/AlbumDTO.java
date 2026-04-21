package com.hongshu.web.domain.dto;

import com.hongshu.common.core.validator.group.AddGroup;
import com.hongshu.common.core.validator.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;


/**
 * 专辑
 *
 * @author: hongshu
 */
@Data
@Schema(name = "专辑")
public class AlbumDTO implements Serializable {
    @Schema(description = "专辑id")
    @NotBlank(message = "专辑id不能为空", groups = UpdateGroup.class)
    private String id;

    @Schema(description = "专辑名称")
    @NotBlank(message = "专辑名称不能为空", groups = AddGroup.class)
    private String title;

    @Schema(description = "专辑发布的用户id")
    @NotNull(message = "用户id不能为空", groups = AddGroup.class)
    private String uid;

    @Schema(description = "专辑封面")
    private String albumCover;

    @Schema(description = "专辑排序")
    private Integer sort;
}
