package com.hongshu.common.core.enums;

public enum ProductTypeEnum {

    ONLINE(0, "在售"),

    BUY(1, "买到"),

    SELL_OUT(2, "售出"),

    PEND_LIST(3, "待上架"),

    LIST(4, "已上架"),

    REMOVE_LIST(5, "已下架"),

    ;


    private final Integer code;
    private final String desc;

    ProductTypeEnum(Integer code, String desc) {
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
