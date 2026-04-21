package com.hongshu.web.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 通过原密码修改密码
 */
@Data
public class UserPasswordOldDTO implements Serializable {

    @Schema(description = "原密码（明文）")
    private String oldPassword;

    @Schema(description = "新密码（明文）")
    private String newPassword;
}

