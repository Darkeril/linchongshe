package com.hongshu.common.core.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 短信服务商类型枚举
 */
public enum SmsProviderEnum {

    ALIYUN("aliyun", "阿里云"),
    TENCENT("tencent", "腾讯云"),
    YUNPIAN("yunpian", "云片");

    private final String code;
    private final String name;

    SmsProviderEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static List<String> getAllCodes() {
        return Arrays.stream(values())
                .map(SmsProviderEnum::getCode)
                .collect(Collectors.toList());
    }
}
