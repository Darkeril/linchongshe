package com.hongshu.ai.api.wenxin.entity.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文心一言token信息
 *
 * @author: Yang
 * @date: 2024/5/30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    String refresh_token;
    Long expires_in;
    String session_key;
    String access_token;
    String scope;
    String session_secret;

}
