package com.hongshu.common.audit.enums;

/**
 * 审核模式枚举
 *
 * @author hongshu
 */
public enum AuditModeEnum {

    /**
     * 纯人工审核模式
     */
    MANUAL("manual", "纯人工审核"),

    /**
     * 纯自动审核模式
     */
    AUTO("auto", "纯自动审核"),

    /**
     * 混合审核模式（自动审核失败时转人工审核）
     */
    HYBRID("hybrid", "混合审核");

    private final String code;
    private final String description;

    AuditModeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据代码获取枚举
     */
    public static AuditModeEnum fromCode(String code) {
        for (AuditModeEnum mode : values()) {
            if (mode.getCode().equals(code)) {
                return mode;
            }
        }
        return MANUAL; // 默认人工审核
    }
}
