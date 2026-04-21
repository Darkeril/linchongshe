package com.hongshu.web.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 第三方账号绑定状态
 */
@Data
public class ThirdBindStatusVO implements Serializable {

    @Schema(description = "是否绑定微信")
    private Boolean wechatBind;

    @Schema(description = "是否绑定QQ")
    private Boolean qqBind;

    @Schema(description = "是否绑定Facebook")
    private Boolean facebookBind;
}

