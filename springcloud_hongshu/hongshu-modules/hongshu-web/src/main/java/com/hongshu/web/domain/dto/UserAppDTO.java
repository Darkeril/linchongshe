package com.hongshu.web.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;


/**
 * 用户头像
 *
 * @author: hongshu
 */
@Data
public class UserAppDTO implements Serializable {

    @Schema(description = "用户id")
    private String id;

    @Schema(description = "头像Url")
    private String avatarUrl;

    private String username;

    private String description;

    @Schema(description = "标签")
    private String tags;

    @Schema(description = "背景图")
    private String userCover;

    @Schema(description = "性别")
    private String gender;

    @Schema(description = "生日")
    private String birthday;

    @Schema(description = "省")
    private String province;

    @Schema(description = "市")
    private String city;

    @Schema(description = "区")
    private String district;

    @Schema(description = "详细地址")
    private String address;

}
