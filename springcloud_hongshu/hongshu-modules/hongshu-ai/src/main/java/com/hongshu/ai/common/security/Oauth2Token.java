package com.hongshu.ai.common.security;

import lombok.Builder;
import lombok.Data;

/**
 * 返回token信息
 *
 * @author: myj
 * @date: 2024/4/1
 * @version: 1.0.0
 */
@Data
@Builder
public class Oauth2Token {

    /**
     * 访问令牌
     */
    private String token;

    /**
     * 刷新令牌
     */
    private String refreshToken;

    /**
     * 有效时间(秒)
     */
    private Long expiresIn;

}
