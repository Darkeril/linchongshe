package com.hongshu.common.core.constant;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * token常量
 *
 * @author: hongshu
 */
@Schema(name = "token常量")
public interface TokenConstant {

    @Schema(description = "accessToken")
    String ACCESS_TOKEN = "accessToken";

    @Schema(description = "refreshToken")
    String REFRESH_TOKEN = "refreshToken";
}
