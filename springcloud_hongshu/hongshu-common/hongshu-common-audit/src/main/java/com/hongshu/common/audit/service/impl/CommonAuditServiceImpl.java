package com.hongshu.common.audit.service.impl;

import com.hongshu.common.audit.config.AuditConfigProvider;
import com.hongshu.common.audit.dto.AutoAuditResult;
import com.hongshu.common.audit.dto.BaiduQianfanConfigDTO;
import com.hongshu.common.audit.enums.AuditModeEnum;
import com.hongshu.common.audit.enums.AutoAuditResultEnum;
import com.hongshu.common.audit.service.AuditLogService;
import com.hongshu.common.audit.service.CommonAuditService;
import com.hongshu.common.audit.service.ContentAuditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 通用审核服务实现
 *
 * @author hongshu
 */
@Slf4j
@Service
public class CommonAuditServiceImpl implements CommonAuditService {

    @Autowired(required = false)
    private ContentAuditService contentAuditService;
    
    @PostConstruct
    public void init() {
        log.info("CommonAuditServiceImpl 初始化 - contentAuditService: {}", contentAuditService != null ? contentAuditService.getClass().getName() : "null");
    }

    // 配置服务接口，由具体模块实现注入
    @Autowired(required = false)
    private AuditConfigProvider configProvider;

    // 审核日志服务接口，由具体模块实现注入
    @Autowired(required = false)
    private AuditLogService auditLogService;

    @Override
    public AutoAuditResult performAuditWithMode(String content, List<String> urls, String contentType, AuditModeEnum auditMode) {
        try {
            log.info("执行审核 - 模式: {}, 内容类型: {}, 文本长度: {}, 文件数量: {}",
                    auditMode.getCode(), contentType,
                    content != null ? content.length() : 0,
                    urls != null ? urls.size() : 0);

            // 使用 if/else 避免编译器生成匿名内部类 $1，防止缺失导致的 NoClassDefFoundError
            if (auditMode == AuditModeEnum.MANUAL) {
                return handleManualMode();
            } else if (auditMode == AuditModeEnum.AUTO) {
                return handleAutoMode(content, urls, contentType);
            } else if (auditMode == AuditModeEnum.HYBRID) {
                return handleHybridMode(content, urls, contentType);
            } else {
                return handleDefaultMode();
            }

        } catch (Exception e) {
            log.error("审核执行异常 - 模式: {}, 内容类型: {}", auditMode.getCode(), contentType, e);
            return AutoAuditResult.error("审核服务异常: " + e.getMessage(), "common_audit");
        }
    }

    @Override
    public void setAuditStatus(Object entity, AutoAuditResult auditResult, AuditModeEnum auditMode) {
        try {
            if (entity == null || auditResult == null) {
                return;
            }

            // 通过反射设置审核状态
            setEntityAuditStatus(entity, auditResult, auditMode);

        } catch (Exception e) {
            log.error("设置审核状态异常", e);
        }
    }

    @Override
    public AuditModeEnum getAuditMode() {
        if (configProvider != null) {
            return configProvider.getAuditMode();
        }
        return AuditModeEnum.MANUAL; // 默认人工审核
    }

    @Override
    public BaiduQianfanConfigDTO getBaiduQianfanConfig() {
        if (configProvider != null) {
            return configProvider.getBaiduQianfanConfig();
        }
        return BaiduQianfanConfigDTO.getDefault();
    }

    @Override
    public Boolean isContentAuditEnabled() {
        if (configProvider != null) {
            return configProvider.isContentAuditEnabled();
        }
        return false; // 默认关闭
    }

    @Override
    public AutoAuditResult auditImageContent(String content, List<String> imageUrls) {
        return contentAuditService.auditContent(content, imageUrls);
    }

    @Override
    public AutoAuditResult auditVideoContent(String content, List<String> videoUrls) {
        return contentAuditService.auditVideo(content, videoUrls);
    }

    /**
     * 处理人工审核模式
     */
    private AutoAuditResult handleManualMode() {
        if (Boolean.TRUE.equals(isContentAuditEnabled())) {
            return AutoAuditResult.manual("采用人工审核模式", "common_audit");
        } else {
            return AutoAuditResult.pass("审核未开启，直接通过", "common_audit");
        }
    }

    /**
     * 处理自动审核模式
     */
    private AutoAuditResult handleAutoMode(String content, List<String> urls, String contentType) {
        log.info("handleAutoMode - contentAuditEnabled: {}, contentType: {}", isContentAuditEnabled(), contentType);
        if (!Boolean.TRUE.equals(isContentAuditEnabled())) {
            log.warn("内容审核未开启，直接通过");
            return AutoAuditResult.pass("审核未开启，直接通过", "common_audit");
        }

        // 检查审核服务是否可用
        if (contentAuditService == null) {
            log.error("contentAuditService 未注入，无法执行自动审核");
            return AutoAuditResult.manual("审核服务未配置", "common_audit");
        }
        
        // 根据内容类型选择审核方式
        if ("video".equals(contentType) || isVideoContent(urls)) {
            log.info("调用视频审核服务");
            return contentAuditService.auditVideo(content, urls);
        } else {
            log.info("调用图文审核服务");
            AutoAuditResult result = contentAuditService.auditContent(content, urls);
            log.info("图文审核结果: {}, 原因: {}", result.getResult(), result.getReason());
            return result;
        }
    }

    /**
     * 处理混合审核模式
     */
    private AutoAuditResult handleHybridMode(String content, List<String> urls, String contentType) {
        log.info("handleHybridMode - contentAuditEnabled: {}", isContentAuditEnabled());
        if (!Boolean.TRUE.equals(isContentAuditEnabled())) {
            log.warn("内容审核未开启，直接通过");
            return AutoAuditResult.pass("审核未开启，直接通过", "common_audit");
        }

        // 先尝试自动审核
        AutoAuditResult autoResult = handleAutoMode(content, urls, contentType);
        log.info("自动审核结果: {}, 原因: {}", autoResult.getResult(), autoResult.getReason());

        // 如果自动审核失败或需要人工审核，转为人工审核
        if (autoResult.getResult() == AutoAuditResultEnum.ERROR ||
                autoResult.getResult() == AutoAuditResultEnum.MANUAL) {
            String reason = autoResult.getReason();
            // 如果是系统错误（如配置错误），保留原始错误信息
            if (autoResult.getResult() == AutoAuditResultEnum.ERROR) {
                log.warn("自动审核失败，转为人工审核 - 原因: {}", reason);
                return AutoAuditResult.manual(reason != null && !reason.isEmpty() 
                    ? reason 
                    : "自动审核失败，转人工审核", "common_audit");
            } else {
                log.warn("自动审核需要人工审核，转为人工审核 - 原因: {}", reason);
                return AutoAuditResult.manual(reason != null && !reason.isEmpty() 
                    ? reason 
                    : "自动审核失败，转人工审核", "common_audit");
            }
        }

        return autoResult;
    }

    /**
     * 处理默认模式（未配置时）
     */
    private AutoAuditResult handleDefaultMode() {
        return AutoAuditResult.manual("审核模式未配置，默认人工审核", "common_audit");
    }

    /**
     * 判断是否为视频内容
     */
    private boolean isVideoContent(List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return false;
        }

        for (String url : urls) {
            if (url != null && (url.contains(".mp4") || url.contains(".avi") ||
                    url.contains(".mov") || url.contains(".wmv"))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 通过反射设置实体审核状态
     */
    private void setEntityAuditStatus(Object entity, AutoAuditResult auditResult, AuditModeEnum auditMode) {
        try {
            Class<?> entityClass = entity.getClass();

            // 设置审核状态
            setFieldValue(entity, entityClass, "auditStatus", getAuditStatusCode(auditResult, auditMode));

            // 设置商品/笔记类型状态
            setFieldValue(entity, entityClass, "type", getTypeStatusCode(auditResult, auditMode));

        } catch (Exception e) {
            log.error("设置实体审核状态异常", e);
        }
    }

    /**
     * 设置字段值
     */
    private void setFieldValue(Object entity, Class<?> entityClass, String fieldName, String value) {
        try {
            // 尝试调用setter方法
            String setterName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            Method setter = entityClass.getMethod(setterName, String.class);
            setter.invoke(entity, value);

        } catch (Exception e) {
            // 如果setter方法不存在，忽略
            log.debug("设置字段 {} 失败: {}", fieldName, e.getMessage());
        }
    }

    /**
     * 获取审核状态代码（返回数据库中的状态码）
     */
    private String getAuditStatusCode(AutoAuditResult auditResult, AuditModeEnum auditMode) {
        if (auditResult.getResult() == AutoAuditResultEnum.PASS) {
            return "1";  // 通过
        } else if (auditResult.getResult() == AutoAuditResultEnum.REJECT) {
            return "2";  // 拒绝
        }
        return "0";  // 待审核
    }

    /**
     * 获取类型状态代码
     */
    private String getTypeStatusCode(AutoAuditResult auditResult, AuditModeEnum auditMode) {
        if (auditResult.getResult() == AutoAuditResultEnum.PASS) {
            return "ONLINE";
        }
        return "PEND_LIST";
    }

    @Override
    public void saveAuditLog(String contentId, String contentType, AutoAuditResult auditResult) {
        try {
            if (auditLogService != null) {
                auditLogService.saveAuditLog(contentId, contentType, auditResult);
            } else {
                log.debug("审核日志服务未配置，跳过日志保存");
            }
        } catch (Exception e) {
            log.error("保存审核日志异常 - 内容ID: {}, 内容类型: {}", contentId, contentType, e);
        }
    }

}
