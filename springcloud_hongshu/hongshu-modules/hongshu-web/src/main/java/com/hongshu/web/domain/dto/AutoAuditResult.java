//package com.hongshu.web.domain.dto;
//
//import com.hongshu.common.core.enums.AutoAuditResultEnum;
//import lombok.Builder;
//import lombok.Data;
//
//import java.util.Date;
//import java.util.List;
//
///**
// * 自动审核结果
// * 已迁移到通用审核模块：com.hongshu.common.audit.dto.AutoAuditResult
// *
// * @author hongshu
// */
//@Data
//@Builder
//public class AutoAuditResult {
//    
//    /**
//     * 审核结果
//     */
//    private AutoAuditResultEnum result;
//    
//    /**
//     * 审核分数
//     */
//    private Double score;
//    
//    /**
//     * 审核原因
//     */
//    private String reason;
//    
//    /**
//     * 违规项列表
//     */
//    private List<String> violations;
//    
//    /**
//     * 审核ID（第三方服务返回）
//     */
//    private String auditId;
//    
//    /**
//     * 审核时间
//     */
//    private Date auditTime;
//    
//    /**
//     * 审核服务商
//     */
//    private String provider;
//}
