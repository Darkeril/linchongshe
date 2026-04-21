package com.hongshu.idle.service.audit.impl;

import com.hongshu.common.audit.dto.AutoAuditResult;
import com.hongshu.common.audit.service.AuditLogService;
import com.hongshu.idle.domain.entity.IdleAuditLog;
import com.hongshu.idle.mapper.IdleAuditLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * idle 模块审核日志服务实现
 *
 * @author hongshu
 */
@Slf4j
@Service
public class IdleAuditLogServiceImpl implements AuditLogService {
    
    @Autowired
    private IdleAuditLogMapper auditLogMapper;
    
    @Override
    public void saveAuditLog(String contentId, String contentType, AutoAuditResult auditResult) {
        try {
            IdleAuditLog auditLog = IdleAuditLog.builder()
                .contentId(contentId)
                .contentType(contentType)
                .auditResult(auditResult.getResult().name())
                .auditScore(auditResult.getScore() != null ? BigDecimal.valueOf(auditResult.getScore()) : null)
                .auditReason(auditResult.getReason())
                .auditProvider(auditResult.getProvider())
                .auditTime(auditResult.getAuditTime())
                .createTime(new Date())
                .build();
            
            auditLogMapper.insert(auditLog);
            log.info("idle 审核日志保存成功 - 内容ID: {}, 内容类型: {}, 审核结果: {}, 审核分数: {}",
                    contentId, contentType, auditResult.getResult(), auditResult.getScore());
        } catch (Exception e) {
            log.error("idle 审核日志保存失败 - 内容ID: {}, 内容类型: {}", contentId, contentType, e);
        }
    }
    
    @Override
    public List<Object> getAuditLogs(String contentId, String contentType) {
        try {
            // 这里可以实现查询审核日志的逻辑
            // 暂时返回 null，后续可以扩展
            return null;
        } catch (Exception e) {
            log.error("查询 idle 审核日志失败 - 内容ID: {}, 内容类型: {}", contentId, contentType, e);
            return null;
        }
    }
}
