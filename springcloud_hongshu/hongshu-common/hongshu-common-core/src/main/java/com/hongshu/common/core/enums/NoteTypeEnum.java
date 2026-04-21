package com.hongshu.common.core.enums;

public enum NoteTypeEnum {

    IMAGE_TEXT("1", "图文"),

    VIDEO("2", "视频"),

    LIVE_IMAGE("3", "Live图");


    private final String code;
    private final String desc;

    NoteTypeEnum(String code, String desc) {
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
