package com.hongshu.common.core.enums;

public enum TerminalTypeEnum {

    WEB(0, "web"),

    APP(1, "app"),

    APPLET(2, "applet");

    private final int code;
    private final String value;

    TerminalTypeEnum(int code, String value) {
        this.code = code;
        this.value = value;
    }

    public int getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    public static TerminalTypeEnum fromValue(String value) {
        for (TerminalTypeEnum type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown terminal: " + value);
    }
}
