package com.hongshu.common.audit.service;

import com.hongshu.common.audit.dto.AutoAuditResult;

/**
 * 统一审核日志服务接口
 *
 * @author hongshu
 */
public interface AuditLogService {
    
    /**
     * 保存审核日志
     *
     * @param contentId   内容ID
     * @param contentType 内容类型（note/product）
     * @param auditResult 审核结果
     */
    void saveAuditLog(String contentId, String contentType, AutoAuditResult auditResult);
    
    /**
     * 查询审核日志
     *
     * @param contentId   内容ID
     * @param contentType 内容类型
     * @return 审核日志列表
     */
    Object getAuditLogs(String contentId, String contentType);
}
