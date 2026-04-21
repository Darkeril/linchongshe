package com.hongshu.common.audit.enums;

/**
 * 自动审核结果枚举
 *
 * @author hongshu
 */
public enum AutoAuditResultEnum {
    
    /**
     * 审核通过
     */
    PASS("PASS", "审核通过"),
    
    /**
     * 审核拒绝
     */
    REJECT("REJECT", "审核拒绝"),
    
    /**
     * 需要人工审核
     */
    MANUAL("MANUAL", "需要人工审核"),
    
    /**
     * 审核异常
     */
    ERROR("ERROR", "审核异常");
    
    private final String code;
    private final String description;
    
    AutoAuditResultEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
}
