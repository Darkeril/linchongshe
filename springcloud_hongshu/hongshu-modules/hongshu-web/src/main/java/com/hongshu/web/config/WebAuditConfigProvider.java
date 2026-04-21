package com.hongshu.web.config;

import com.hongshu.common.audit.config.AuditConfigProvider;
import com.hongshu.common.audit.dto.BaiduQianfanConfigDTO;
import com.hongshu.common.audit.enums.AuditModeEnum;
import com.hongshu.web.service.sys.ISysSystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * web 模块审核配置提供者
 *
 * @author hongshu
 */
@Slf4j
@Component
public class WebAuditConfigProvider implements AuditConfigProvider {
    
    @Autowired
    private ISysSystemConfigService systemConfigService;
    
    @Override
    public AuditModeEnum getAuditMode() {
        try {
            // 从百度千帆配置中获取审核模式
            BaiduQianfanConfigDTO config = systemConfigService.getBaiduQianfanConfigInternal();
            String auditMode = config.getAuditMode();
            
            if (auditMode == null || auditMode.trim().isEmpty()) {
                log.info("web 模块未配置审核模式，使用默认混合审核模式");
                return AuditModeEnum.HYBRID;
            }
            
            return AuditModeEnum.fromCode(auditMode);
        } catch (Exception e) {
            log.error("获取 web 审核模式失败，使用默认混合审核模式", e);
            return AuditModeEnum.HYBRID;
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
            return systemConfigService.getBaiduQianfanConfigInternal();
        } catch (Exception e) {
            log.error("获取百度千帆配置失败，返回默认配置", e);
            return BaiduQianfanConfigDTO.getDefault();
        }
    }
}
