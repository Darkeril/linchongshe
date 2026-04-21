package com.hongshu.common.core.constant;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @author: hongshu
 */
@Schema(name = "权限管理常量")
public interface AuthConstant {

    @Schema(description = "accessToken过期时间")
    long ACCESS_TOKEN_EXPIRATION_TIME = 1000 * 60 * 60 * 24;
    @Schema(description = "refreshToken过期时间")
    long REFRESH_TOKEN_EXPIRATION_TIME = ACCESS_TOKEN_EXPIRATION_TIME * 2;
    @Schema(description = "refreshToken保留时间")
    String REFRESH_TOKEN_START_TIME = "refreshTokenStartTime:";
    @Schema(description = "用户key")
    String USER_KEY = "userKey:";
    @Schema(description = "用户信息")
    String USER_INFO = "userInfo";
    @Schema(description = "验证码key")
    String CODE = "code:";
    @Schema(description = "默认头像")
    String DEFAULT_AVATAR = "https://image.mayongjian.cn/profile.jpeg";
    @Schema(description = "默认群聊头像")
    String DEFAULT_GROUP_AVATAR = "https://image.mayongjian.cn/chat-group.jpg";
    @Schema(description = "管理端头像")
    String ADMIN_AVATAR = "https://image.mayongjian.cn/admin-profile.jpg";
    @Schema(description = "默认背景")
    String DEFAULT_COVER = "https://image.mayongjian.cn/%E6%89%8B%E6%9C%BA%E5%A3%81%E7%BA%B8_1_00_kk_%E6%9D%A5%E8%87%AA%E5%B0%8F%E7%BA%A2%E4%B9%A6%E7%BD%91%E9%A1%B5%E7%89%88.jpg";
    @Schema(description = "默认密码")
    String DEFAULT_PASSWORD = "123456";
    @Schema(description = "登录失败")
    String LOGIN_FAIL = "登录失败";
    /** 登录接口返回：首次注册且待审核（不发放 token） */
    String REGISTER_PENDING_AUDIT_FLAG = "registerPendingAudit";
}
