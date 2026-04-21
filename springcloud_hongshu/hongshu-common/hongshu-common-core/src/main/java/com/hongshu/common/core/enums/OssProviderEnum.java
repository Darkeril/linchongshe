package com.hongshu.common.core.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OSS服务商类型枚举
 */
public enum OssProviderEnum {

    LOCAL("local", "本地存储"),
    QINIUYUN("qiniuyun", "七牛云"),
    MINIO("minio", "Minio"),
    FASTDFS("fastdfs", "FastDfs"),
    ALIYUN("aliyun", "阿里云"),
    TENCENT("tencent", "腾讯云");

    private final String code;
    private final String name;

    OssProviderEnum(String code, String name) {
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
                .map(OssProviderEnum::getCode)
                .collect(Collectors.toList());
    }
}
