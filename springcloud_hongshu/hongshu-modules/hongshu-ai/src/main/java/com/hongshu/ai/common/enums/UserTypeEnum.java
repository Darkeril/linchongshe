package com.hongshu.ai.common.enums;

import lombok.Getter;

/**
 * 审核类型枚举类
 *
 * @author: Yang
 * @date: 2023/01/31
 * @version: 1.0.0
 */
@Getter
public enum UserTypeEnum {

    /**
     * 0 自动登录 1 微信小程序 2 公众号 3 手机号 4 账户密码
     */
    AUTO_LOGIN(0, "自动登录", "auto:"),

    WXAPP(1, "微信小程序", "wxapp:"),

    WXMP(2, "微信公众号", "wxmp:"),

    TEL(3, "手机号", "tel:"),

    USERNAME(4, "账户密码", "username:");

    /**
     * 值
     */
    private final Integer value;

    /**
     * 标签
     */
    private final String label;

    /**
     * 前缀
     */
    private final String prefix;

    UserTypeEnum(final Integer value, final String label, final String prefix) {
        this.label = label;
        this.value = value;
        this.prefix = prefix;
    }

}
