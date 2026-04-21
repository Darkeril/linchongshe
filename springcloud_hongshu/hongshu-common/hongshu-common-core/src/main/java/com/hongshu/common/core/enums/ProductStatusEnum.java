package com.hongshu.common.core.enums;

public enum ProductStatusEnum {

    ONLINE(0, "上线"),

    BUY(1, "买到"),

    SELL_OUT(2, "卖出"),

    ;


    private final Integer code;
    private final String desc;

    ProductStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

}
