package com.hongshu.common.core.enums;

public enum MessageTypeEnum {

    LIKE(0),
    COMMENT(1),
    FOLLOW(2),
    CHAT(3),
    PRODUCT_CHAT(4);

    private final int value;

    MessageTypeEnum(int value) {
        this.value = value;
    }

    public static MessageTypeEnum valueOf(int value) {
        for (MessageTypeEnum type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid message type: " + value);
    }
}
