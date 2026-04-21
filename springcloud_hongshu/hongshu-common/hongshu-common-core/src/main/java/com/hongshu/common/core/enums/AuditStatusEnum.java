package com.hongshu.common.core.enums;

public enum AuditStatusEnum {

    REVIEW("0", "未审核"),

    PASS("1", "通过"),

    REJECT("2", "未通过");


    private final String code;
    private final String desc;

    AuditStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
