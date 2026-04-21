package com.hongshu.web.domain.dto;

import com.hongshu.common.core.validator.group.AddGroup;
import com.hongshu.common.core.validator.group.UpdateGroup;
import com.hongshu.web.domain.entity.IdleProduct;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

/**
 * 笔记
 *
 * @author: hongshu
 */
@Data
@Schema(name = "笔记DTO")
public class NoteAppDTO implements Serializable {

    @Schema(description = "id")
    @NotBlank(message = "标题不能为空", groups = UpdateGroup.class)
    private String id;

    @Schema(description = "笔记标题")
    @NotBlank(message = "标题不能为空", groups = AddGroup.class)
    private String title;

    @Schema(description = "笔记内容")
    @NotBlank(message = "内容不能为空", groups = AddGroup.class)
    private String content;

    @Schema(description = "笔记封面")
    private String noteCover;

    @Schema(description = "笔记封面高度")
    private Integer noteCoverHeight;

    @Schema(description = "笔记父分类")
    @NotBlank(message = "一级分类不能为空", groups = AddGroup.class)
    private String cpid;

    @Schema(description = "笔记分类")
    @NotBlank(message = "二级分类不能为空", groups = AddGroup.class)
    private String cid;

    @Schema(description = "笔记图片地址")
    private List<String> urls;

    @Schema(description = "笔记标签")
    private List<String> tags;

    @Schema(description = "笔记图片数量")
    private Integer count;

    @Schema(description = "笔记类型")
    private String noteType;

    @Schema(description = "省")
    private String province;

    @Schema(description = "市")
    private String city;

    @Schema(description = "区")
    private String district;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "专辑ID")
    private String albumId;

    private List<IdleProduct> relatedProducts; // 关联商品列表

}
