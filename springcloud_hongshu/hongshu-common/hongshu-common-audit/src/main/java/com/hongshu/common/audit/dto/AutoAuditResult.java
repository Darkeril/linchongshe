package com.hongshu.common.audit.dto;

import com.hongshu.common.audit.enums.AutoAuditResultEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 自动审核结果DTO
 *
 * @author hongshu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoAuditResult {

    /**
     * 审核结果
     */
    private AutoAuditResultEnum result;

    /**
     * 审核分数（0-1之间，越高越可能违规）
     */
    private Double score;

    /**
     * 审核原因/详情
     */
    private String reason;

    /**
     * 违规项列表
     */
    private List<String> violations;

    /**
     * 审核ID（第三方服务返回）
     */
    private String auditId;

    /**
     * 审核服务商
     */
    private String provider;

    /**
     * 审核时间
     */
    private Date auditTime;

    /**
     * 创建通过结果
     */
    public static AutoAuditResult pass(String reason, String provider) {
        return AutoAuditResult.builder()
                .result(AutoAuditResultEnum.PASS)
                .reason(reason)
                .provider(provider)
                .auditTime(new Date())
                .build();
    }

    /**
     * 创建拒绝结果
     */
    public static AutoAuditResult reject(String reason, String provider) {
        return AutoAuditResult.builder()
                .result(AutoAuditResultEnum.REJECT)
                .reason(reason)
                .provider(provider)
                .auditTime(new Date())
                .build();
    }

    /**
     * 创建人工审核结果
     */
    public static AutoAuditResult manual(String reason, String provider) {
        return AutoAuditResult.builder()
                .result(AutoAuditResultEnum.MANUAL)
                .reason(reason)
                .provider(provider)
                .auditTime(new Date())
                .build();
    }

    /**
     * 创建异常结果
     */
    public static AutoAuditResult error(String reason, String provider) {
        return AutoAuditResult.builder()
                .result(AutoAuditResultEnum.ERROR)
                .reason(reason)
                .provider(provider)
                .auditTime(new Date())
                .build();
    }
}
