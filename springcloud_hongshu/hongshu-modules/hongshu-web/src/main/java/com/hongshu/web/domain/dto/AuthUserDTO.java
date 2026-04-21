package com.hongshu.web.domain.dto;

import com.hongshu.common.core.annotation.NotBlank;
import com.hongshu.common.core.validator.group.DefaultGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 权限用户
 *
 * @author: hongshu
 */
@Schema(name = "权限用户dto")
@Data
public class AuthUserDTO implements Serializable {

    @Schema(description = "用户id")
    private String id;
    @Schema(description = "用户名")
    private String username;

    @Pattern(regexp = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{8,20}$", message = "请输入8-20位由字母和数字组成的密码", groups = DefaultGroup.class)
    @Schema(description = "登录密码")
    private String password;

    @Schema(description = "校验密码")
    private String checkPassword;

    @Pattern(regexp = "^1[0-9]{10}$", message = "手机号格式有误", groups = DefaultGroup.class)
    @NotBlank("手机号不能为空")
    @Schema(description = "手机号")
    private String phone;

    //@Pattern(regexp = "^([a-zA-Z0-9._-])+@([a-zA-Z0-9_-])+((.[a-zA-Z0-9_-]{2,3}){1,2})$", message = "邮箱输入有误", groups = DefaultGroup.class)
    @Schema(description = "email")
    private String email;

    @Schema(description = "登录验证码")
    private String code;

    @Schema(description = "登录端标识")
    private String terminal;

    private String captchaVerification;
}
