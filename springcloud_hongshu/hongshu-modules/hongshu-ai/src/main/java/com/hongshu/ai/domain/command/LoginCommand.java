package com.hongshu.ai.domain.command;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录信息
 *
 * @author: myj
 * @date: 2024/4/1
 * @version: 1.0.0
 */
@Data
public class LoginCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 手机号
     */
    private String tel;

    /**
     * 验证码
     */
    private String code;

    /**
     * 验证码绑定uuid
     */
    private String uuid;

    /**
     * 登陆类型 1 微信小程序 2 微信公众号 3 手机号 4 账户密码登录
     */
    private Integer loginType;

}
