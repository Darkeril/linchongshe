package com.hongshu.common.audit.config;

import com.hongshu.common.audit.dto.BaiduQianfanConfigDTO;
import com.hongshu.common.audit.enums.AuditModeEnum;

/**
 * 审核配置提供者接口
 * 由具体模块实现，为通用审核服务提供配置
 *
 * @author hongshu
 */
public interface AuditConfigProvider {
    
    /**
     * 获取审核模式
     *
     * @return 审核模式
     */
    AuditModeEnum getAuditMode();
    
    /**
     * 获取内容审核开关
     *
     * @return 是否开启内容审核
     */
    Boolean isContentAuditEnabled();
    
    /**
     * 获取百度千帆配置
     *
     * @return 百度千帆配置
     */
    BaiduQianfanConfigDTO getBaiduQianfanConfig();
}
