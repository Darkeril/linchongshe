package com.hongshu.common.core.constant;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @author: hongshu
 */
@Schema(name = "用户常量")
public interface UserConstant {

    @Schema(description = "用户id")
    String USER_ID = "userId";
}
