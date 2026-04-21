package com.hongshu.web.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 微信登录DTO
 *
 * @author: hongshu
 */
@Schema(name = "微信登录DTO")
@Data
public class WeChatLoginDTO implements Serializable {

    @Schema(description = "微信登录code")
    private String code;

    @Schema(description = "用户信息")
    private WeChatUserInfo userInfo;

    @Schema(description = "登录端标识")
    private String terminal;

    @Schema(description = "手机号授权code（通过getPhoneNumber获取）")
    private String phoneCode;

    @Data
    @Schema(name = "微信用户信息")
    public static class WeChatUserInfo implements Serializable {
        @Schema(description = "用户昵称")
        private String nickName;

        @Schema(description = "用户头像")
        private String avatarUrl;

        @Schema(description = "用户性别，0-未知，1-男，2-女")
        private Integer gender;

        @Schema(description = "国家")
        private String country;

        @Schema(description = "省份")
        private String province;

        @Schema(description = "城市")
        private String city;
    }
}


















