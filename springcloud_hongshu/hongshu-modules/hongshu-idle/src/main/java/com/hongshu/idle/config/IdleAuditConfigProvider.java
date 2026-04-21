package com.hongshu.idle.config;

import com.hongshu.common.audit.config.AuditConfigProvider;
import com.hongshu.common.audit.dto.BaiduQianfanConfigDTO;
import com.hongshu.common.audit.enums.AuditModeEnum;
import com.hongshu.idle.domain.dto.SystemConfigDTO;
import com.hongshu.idle.service.sys.ISysSystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * idle 模块审核配置提供者
 *
 * @author hongshu
 */
@Slf4j
@Component
public class IdleAuditConfigProvider implements AuditConfigProvider {

    @Autowired
    private ISysSystemConfigService systemConfigService;

    @Override
    public AuditModeEnum getAuditMode() {
        try {
            // 从系统配置获取审核模式
            SystemConfigDTO config = systemConfigService.getSystemConfig();
            String auditMode = config.getAuditMode();

            if (auditMode == null || auditMode.trim().isEmpty()) {
                // 如果没有配置，默认使用自动审核模式
                log.info("idle 模块未配置审核模式，使用默认自动审核模式");
                return AuditModeEnum.AUTO;
            }

            return AuditModeEnum.fromCode(auditMode);
        } catch (Exception e) {
            log.error("获取 idle 审核模式失败，使用默认自动审核模式", e);
            return AuditModeEnum.AUTO;
        }
    }

    @Override
    public Boolean isContentAuditEnabled() {
        try {
            return systemConfigService.getSystemConfig().getContentAuditEnabled();
        } catch (Exception e) {
            log.error("获取内容审核开关失败", e);
            return false;
        }
    }

    @Override
    public BaiduQianfanConfigDTO getBaiduQianfanConfig() {
        try {
            SystemConfigDTO config = systemConfigService.getSystemConfig();

            // 获取审核模式
            String auditMode = config.getAuditMode();
            if (auditMode == null || auditMode.trim().isEmpty()) {
                auditMode = "auto"; // 默认自动审核
            }

            // idle 模块使用自己的系统配置（字段名不同）
            return BaiduQianfanConfigDTO.builder()
                .enabled(config.getBaiduQianfanEnabled())
                .accessKey(config.getBaiduQianfanAccessKey())
                .secretKey(config.getBaiduQianfanSecretKey())
                .textEndpoint(config.getBaiduQianfanTextEndpoint())
                .imageEndpoint(config.getBaiduQianfanImageEndpoint())
                .videoEndpoint(config.getBaiduQianfanVideoEndpoint())
                .autoAuditThreshold(config.getAutoAuditThreshold())
                .auditMode(auditMode)
                .build();
        } catch (Exception e) {
            log.error("获取百度千帆配置失败，返回默认配置", e);
            return BaiduQianfanConfigDTO.getDefault();
        }
    }
}
