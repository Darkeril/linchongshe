package com.hongshu.common.audit.service;

import com.hongshu.common.audit.dto.AutoAuditResult;

import java.util.List;

/**
 * 内容审核服务接口
 *
 * @author hongshu
 */
public interface ContentAuditService {
    
    /**
     * 审核文本和图片内容
     *
     * @param textContent 文本内容
     * @param imageUrls   图片URL列表
     * @return 审核结果
     */
    AutoAuditResult auditContent(String textContent, List<String> imageUrls);
    
    /**
     * 审核文本和视频内容
     *
     * @param textContent 文本内容
     * @param videoUrls   视频URL列表
     * @return 审核结果
     */
    AutoAuditResult auditVideo(String textContent, List<String> videoUrls);
    
    /**
     * 仅审核文本内容
     *
     * @param textContent 文本内容
     * @return 审核结果
     */
    AutoAuditResult auditText(String textContent);
    
    /**
     * 仅审核图片内容
     *
     * @param imageUrls 图片URL列表
     * @return 审核结果
     */
    AutoAuditResult auditImages(List<String> imageUrls);
}
