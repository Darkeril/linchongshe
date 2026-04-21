package com.hongshu.common.core.enums;

public enum PayTypeEnum {

    WX_PAY("1", "微信支付"),

    ALI_PAY("2", "支付宝支付"),

    ;


    private final String code;
    private final String desc;

    PayTypeEnum(String code, String desc) {
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
