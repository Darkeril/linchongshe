package com.hongshu.common.core.enums;

public enum PostTypeEnum {

    MAIL("1", "邮寄"),

    SELF_PICKUP("2", "自提"),

    ;


    private final String code;
    private final String desc;

    PostTypeEnum(String code, String desc) {
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
