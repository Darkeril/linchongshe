package com.hongshu.web.domain.dto;

import com.hongshu.common.core.validator.group.AddGroup;
import com.hongshu.common.core.validator.group.UpdateGroup;
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
@Schema(name = "商品DTO")
public class ProductDTO implements Serializable {

    @Schema(description = "id")
    @NotBlank(message = "笔记ID不能为空", groups = UpdateGroup.class)
    private String id;

    @Schema(description = "笔记标题")
    @NotBlank(message = "标题不能为空", groups = AddGroup.class)
    private String title;

    @Schema(description = "笔记内容")
    @NotBlank(message = "内容不能为空", groups = AddGroup.class)
    private String content;

    @Schema(description = "笔记封面")
    private String cover;

    @Schema(description = "笔记封面高度")
    private Integer noteCoverHeight;

    @Schema(description = "笔记分类")
    @NotBlank(message = "二级分类不能为空", groups = AddGroup.class)
    private String cid;

    @Schema(description = "笔记父分类")
    @NotBlank(message = "一级分类不能为空", groups = AddGroup.class)
    private String cpid;

    @Schema(description = "笔记图片地址")
    private List<String> urls;

    @Schema(description = "商品图片数量")
    private Integer count;

    @Schema(description = "商品类型")
    private Integer productType;

    @Schema(description = "发货方式")
    private Integer postType;

    @Schema(description = "省")
    private String province;

    @Schema(description = "市")
    private String city;

    @Schema(description = "区")
    private String district;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "价格")
    private String price;

    @Schema(description = "原价")
    private String originalPrice;
}
