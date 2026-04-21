package com.hongshu.web.service.audit.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hongshu.web.domain.dto.AuditLogQueryDTO;
import com.hongshu.web.domain.entity.WebAuditLog;
import com.hongshu.web.mapper.web.WebAuditLogMapper;
import com.hongshu.web.service.audit.IWebAuditLogQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 审核日志查询服务实现
 *
 * @author hongshu
 */
@Slf4j
@Service
public class WebAuditLogQueryServiceImpl implements IWebAuditLogQueryService {

    @Autowired
    private WebAuditLogMapper auditLogMapper;


    @Override
    public List<WebAuditLog> queryAuditLogs(AuditLogQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<WebAuditLog> wrapper = new LambdaQueryWrapper<>();

        // 内容ID
        if (StringUtils.hasText(queryDTO.getContentId())) {
            wrapper.eq(WebAuditLog::getContentId, queryDTO.getContentId());
        }

        // 内容类型
        if (StringUtils.hasText(queryDTO.getContentType())) {
            wrapper.eq(WebAuditLog::getContentType, queryDTO.getContentType());
        }

        // 审核结果
        if (StringUtils.hasText(queryDTO.getAuditResult())) {
            wrapper.eq(WebAuditLog::getAuditResult, queryDTO.getAuditResult());
        }

        // 审核提供商
        if (StringUtils.hasText(queryDTO.getAuditProvider())) {
            wrapper.eq(WebAuditLog::getAuditProvider, queryDTO.getAuditProvider());
        }

        // 时间范围
        if (queryDTO.getStartTime() != null) {
            wrapper.ge(WebAuditLog::getAuditTime, queryDTO.getStartTime());
        }
        if (queryDTO.getEndTime() != null) {
            wrapper.le(WebAuditLog::getAuditTime, queryDTO.getEndTime());
        }

        // 按审核时间倒序
        wrapper.orderByDesc(WebAuditLog::getAuditTime);

        // 查询列表
        return auditLogMapper.selectList(wrapper);
    }

    @Override
    public WebAuditLog getAuditLogById(String id) {
        try {
            return auditLogMapper.selectById(id);
        } catch (Exception e) {
            log.error("查询审核日志详情失败 - ID: {}", id, e);
            return null;
        }
    }
}
