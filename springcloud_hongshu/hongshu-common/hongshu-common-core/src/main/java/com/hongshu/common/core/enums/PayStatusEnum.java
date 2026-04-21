package com.hongshu.common.core.enums;

public enum PayStatusEnum {

    UN_PAY("0", "未支付"),

    PEND_PAY("1", "待支付"),

    PAYING("2", "支付中"),

    PAY("3", "已支付"),

    PAY_COMPLETE("4", "交易完成"),

    ;


    private final String code;
    private final String desc;

    PayStatusEnum(String code, String desc) {
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
