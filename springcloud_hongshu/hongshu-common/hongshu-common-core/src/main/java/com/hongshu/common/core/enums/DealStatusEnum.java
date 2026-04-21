package com.hongshu.common.core.enums;

public enum DealStatusEnum {

    TEMPORARY("0", "临时"),

    PAYING("1", "付款中"),

    PAY_COMPLETE("2", "交易完成"),

    PAY_FAILED("3", "交易失败"),

    PEND_SHIP("4", "待发货"),

    PEND_CONFIRM("5", "待确认"),

    RETURN_GOODS("6", "退货中"),

    PEND_EVALUATE("7", "待评价"),

    EVALUATE_COMPLETE("8", "评价完成"),

    ;


    private final String code;
    private final String desc;

    DealStatusEnum(String code, String desc) {
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
