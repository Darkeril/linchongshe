package com.hongshu.ai.common.enums;

import lombok.Getter;

/**
 * 状态枚举
 *
 * @author: myj
 * @date: 2020/8/6
 * @version: 1.0.0
 */
@Getter
public enum StatusEnum {

    /**
     * 禁用
     */
    DISABLED(0),

    /**
     * 启用
     */
    ENABLED(1);

    private final Integer value;

    StatusEnum(final Integer value) {
        this.value = value;
    }

}
