package com.hongshu.web.service.audit;

import com.hongshu.web.domain.dto.AuditLogQueryDTO;
import com.hongshu.web.domain.entity.WebAuditLog;

import java.util.List;

/**
 * 审核日志查询服务接口
 *
 * @author hongshu
 */
public interface IWebAuditLogQueryService {
    
    /**
     * 查询审核日志列表
     *
     * @param queryDTO 查询条件
     * @return 审核日志列表
     */
    List<WebAuditLog> queryAuditLogs(AuditLogQueryDTO queryDTO);
    
    /**
     * 根据ID查询审核日志详情
     *
     * @param id 日志ID
     * @return 审核日志
     */
    WebAuditLog getAuditLogById(String id);
}
