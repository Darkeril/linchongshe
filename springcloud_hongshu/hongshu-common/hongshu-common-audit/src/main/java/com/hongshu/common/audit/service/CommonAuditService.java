package com.hongshu.common.audit.service;

import com.hongshu.common.audit.dto.AutoAuditResult;
import com.hongshu.common.audit.dto.BaiduQianfanConfigDTO;
import com.hongshu.common.audit.enums.AuditModeEnum;

import java.util.List;

/**
 * 通用审核服务接口
 *
 * @author hongshu
 */
public interface CommonAuditService {
    
    /**
     * 根据审核模式执行内容审核
     *
     * @param content     文本内容
     * @param urls        文件URL列表（图片或视频）
     * @param contentType 内容类型（如 note、product、user）
     * @param auditMode   审核模式
     * @return 审核结果
     */
    AutoAuditResult performAuditWithMode(String content, List<String> urls, String contentType, AuditModeEnum auditMode);
    
    /**
     * 根据审核结果设置实体状态（通用方法）
     *
     * @param entity      实体对象
     * @param auditResult 审核结果
     * @param auditMode   审核模式
     */
    void setAuditStatus(Object entity, AutoAuditResult auditResult, AuditModeEnum auditMode);
    
    /**
     * 获取当前审核模式
     *
     * @return 审核模式
     */
    AuditModeEnum getAuditMode();
    
    /**
     * 获取百度千帆配置
     *
     * @return 百度千帆配置
     */
    BaiduQianfanConfigDTO getBaiduQianfanConfig();
    
    /**
     * 检查内容审核是否开启
     *
     * @return 是否开启
     */
    Boolean isContentAuditEnabled();
    
    /**
     * 执行图文内容审核
     *
     * @param content   文本内容
     * @param imageUrls 图片URL列表
     * @return 审核结果
     */
    AutoAuditResult auditImageContent(String content, List<String> imageUrls);
    
    /**
     * 执行视频内容审核
     *
     * @param content   文本内容
     * @param videoUrls 视频URL列表
     * @return 审核结果
     */
    AutoAuditResult auditVideoContent(String content, List<String> videoUrls);
    
    /**
     * 保存审核日志
     *
     * @param contentId   内容ID
     * @param contentType 内容类型
     * @param auditResult 审核结果
     */
    void saveAuditLog(String contentId, String contentType, AutoAuditResult auditResult);
}
