package com.hongshu.web.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 微信登录配置DTO
 *
 * @author: hongshu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeChatLoginConfigDTO {

    /**
     * 微信登录功能开关
     */
    private Boolean enabled;

    /**
     * 小程序AppID
     */
    private String miniappAppId;

    /**
     * 小程序AppSecret
     */
    private String miniappAppSecret;

    /**
     * 网页应用AppID
     */
    private String webAppId;

    /**
     * 网页应用AppSecret
     */
    private String webAppSecret;

    /**
     * 网页应用回调地址
     */
    private String webCallbackUrl;
}

