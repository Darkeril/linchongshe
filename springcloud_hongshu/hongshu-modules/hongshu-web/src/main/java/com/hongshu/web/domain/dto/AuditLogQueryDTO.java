package com.hongshu.web.domain.dto;

import lombok.Data;

import java.util.Date;

/**
 * 审核日志查询条件
 *
 * @author hongshu
 */
@Data
public class AuditLogQueryDTO {
    
    /**
     * 内容ID
     */
    private String contentId;
    
    /**
     * 内容类型（note/product）
     */
    private String contentType;
    
    /**
     * 审核结果（PASS/MANUAL/REJECT）
     */
    private String auditResult;
    
    /**
     * 审核提供商
     */
    private String auditProvider;
    
    /**
     * 开始时间
     */
    private Date startTime;
    
    /**
     * 结束时间
     */
    private Date endTime;
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页数量
     */
    private Integer pageSize = 10;
}
