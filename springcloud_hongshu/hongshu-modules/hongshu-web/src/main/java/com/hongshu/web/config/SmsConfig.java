package com.hongshu.web.config;

import com.hongshu.web.domain.dto.SmsConfigDTO;
import com.hongshu.web.service.sms.ISmsService;
import com.hongshu.web.service.sms.impl.AliyunSmsServiceImpl;
import com.hongshu.web.service.sms.impl.FallbackSmsServiceImpl;
import com.hongshu.web.service.sms.impl.TencentSmsServiceImpl;
import com.hongshu.web.service.sms.impl.YunpianSmsServiceImpl;
import com.hongshu.web.service.sys.ISysSystemConfigService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import jakarta.annotation.PostConstruct;
import java.util.Map;


//@Data
//@Configuration
//@Slf4j
//public class SmsConfig {
//
//    @NotEmpty(message = "必须指定主服务商")
//    @Value("${sms.primary}")
//    private String primary;
//
//    @NotEmpty(message = "必须指定备用服务商")
//    @Value("${sms.secondary}")
//    private String secondary;
//
//
//    // 阿里云短信配置
//    @Value("${sms.aliyun.key}")
//    private String aliyunKey;
//
//    @Value("${sms.aliyun.secret}")
//    private String aliyunSecret;
//
//    @Value("${sms.aliyun.sign-name}")
//    private String signName;
//
//    @Value("${sms.aliyun.template-code}")
//    private String templateCode;
//
//    // 腾讯云短信配置
//    @Value("${sms.tencent.secret-id}")
//    private String tencentSecretId;
//
//    @Value("${sms.tencent.secret-key}")
//    private String tencentSecretKey;
//
//    @Value("${sms.tencent.sdk-app-id}")
//    private String tencentSdkAppId;
//
//    @Value("${sms.tencent.sign-name}")
//    private String tencentSignName;
//
//    @Value("${sms.tencent.template-id}")
//    private String tencentTemplateId;
//
//    @Value("${sms.tencent.region}")
//    private String tencentRegion;
//
//    // 云片短信配置
//    @Value("${sms.yunpian.apikey}")
//    private String yunpianApiKey;
//
//    @Value("${sms.yunpian.sign-name}")
//    private String yunpianSignName;
//
//
//    @Bean(name = "aliyunSmsService")
//    public ISmsService aliyunSmsService() {
//        try {
//            return new AliyunSmsServiceImpl(
//                    aliyunKey,
//                    aliyunSecret,
//                    signName,
//                    templateCode
//            );
//        } catch (Exception e) {
//            log.error("初始化阿里云短信服务失败", e);
//            throw new RuntimeException("初始化阿里云短信服务失败", e);
//        }
//    }
//
//    @Bean(name = "tencentSmsService")
//    public ISmsService tencentSmsService() {
//        try {
//            return new TencentSmsServiceImpl(
//                    tencentSecretId,
//                    tencentSecretKey,
//                    tencentSdkAppId,
//                    tencentSignName,
//                    tencentTemplateId,
//                    tencentRegion
//            );
//        } catch (Exception e) {
//            log.error("初始化腾讯云短信服务失败", e);
//            throw new RuntimeException("初始化腾讯云短信服务失败", e);
//        }
//    }
//
//    @Bean(name = "yunpianSmsService")
//    public ISmsService yunpianSmsService() {
//        try {
//            return new YunpianSmsServiceImpl(yunpianApiKey, yunpianSignName);
//        } catch (Exception e) {
//            log.error("初始化云片短信服务失败", e);
//            throw new RuntimeException("初始化云片短信服务失败", e);
//        }
//    }
//
//    @Bean
//    @Primary    // 优先注入降级代理
//    public ISmsService fallbackSmsService(
//            @Qualifier("aliyunSmsService") ISmsService aliyunSmsService,
//            @Qualifier("tencentSmsService") ISmsService tencentSmsService,
//            @Qualifier("yunpianSmsService") ISmsService yunpianSmsService
//    ) {
//        if ("aliyun".equalsIgnoreCase(primary)) {
//            log.info("使用阿里云作为主要短信服务");
//            return new FallbackSmsServiceImpl(aliyunSmsService, yunpianSmsService);
//        } else if ("tencent".equalsIgnoreCase(primary)) {
//            log.info("使用腾讯云作为主要短信服务");
//            return new FallbackSmsServiceImpl(tencentSmsService, yunpianSmsService);
//        } else {
//            log.info("使用云片作为主要短信服务");
//            return new FallbackSmsServiceImpl(yunpianSmsService, aliyunSmsService);
//        }
//    }
//}


@Data
@Configuration
@Slf4j
public class SmsConfig {

    @Autowired
    private ISysSystemConfigService systemConfigService;


    private String primary;
    private String secondary;

    // 阿里云短信配置
    private String aliyunKey;
    private String aliyunSecret;
    private String aliyunSignName;
    private String aliyunTemplateCode;

    // 腾讯云短信配置
    private String tencentSecretId;
    private String tencentSecretKey;
    private String tencentSdkAppId;
    private String tencentSignName;
    private String tencentTemplateId;
    private String tencentRegion;

    // 云片短信配置
    private String yunpianApiKey;
    private String yunpianSignName;


    @PostConstruct
    public void loadConfigFromDatabase() {
        try {
            // 从数据库加载短信配置
            this.loadSmsConfigs();
        } catch (Exception e) {
            log.error("从数据库加载短信配置失败", e);
            // 如果数据库加载失败，使用默认配置
            this.loadDefaultConfigs();
        }
    }

    private void loadSmsConfigs() {
        // 使用getSmsConfigInternal方法获取所有短信配置（不脱敏）
        Map<String, SmsConfigDTO> smsConfigMap = systemConfigService.getSmsConfigInternal();
        // 设置主配置和备用配置
        for (Map.Entry<String, SmsConfigDTO> entry : smsConfigMap.entrySet()) {
            SmsConfigDTO config = entry.getValue();
            if (config.getIsPrimary()) {
                this.primary = entry.getKey();
            }
            if (config.getIsSecondary()) {
                this.secondary = entry.getKey();
            }
        }
        // 设置阿里云配置
        SmsConfigDTO aliyunConfig = smsConfigMap.get("aliyun");
        if (aliyunConfig != null) {
            this.aliyunKey = aliyunConfig.getAccessKey();
            this.aliyunSecret = aliyunConfig.getSecretKey();
            this.aliyunSignName = aliyunConfig.getSignName();
            this.aliyunTemplateCode = aliyunConfig.getTemplateCode();
        }
        // 设置腾讯云配置
        SmsConfigDTO tencentConfig = smsConfigMap.get("tencent");
        if (tencentConfig != null) {
            this.tencentSecretId = tencentConfig.getSecretId();
            this.tencentSecretKey = tencentConfig.getSecretKey();
            this.tencentSdkAppId = tencentConfig.getSdkAppId();
            this.tencentSignName = tencentConfig.getSignName();
            this.tencentTemplateId = tencentConfig.getTemplateId();
            this.tencentRegion = tencentConfig.getRegion();
        }
        // 设置云片配置
        SmsConfigDTO yunpianConfig = smsConfigMap.get("yunpian");
        if (yunpianConfig != null) {
            this.yunpianApiKey = yunpianConfig.getApiKey();
            this.yunpianSignName = yunpianConfig.getSignName();
        }
        log.info("短信配置已从数据库加载完成 - 主配置: {}, 备用配置: {}", primary, secondary);
    }

    private void loadDefaultConfigs() {
        // 简化默认配置，只设置必要的默认值
        this.primary = "aliyun";
        this.secondary = "yunpian";
        log.warn("使用默认配置加载短信服务，请检查数据库配置");
    }

    // 添加刷新配置的方法
    public void refreshConfig() {
        this.loadConfigFromDatabase();
    }

    @Bean(name = "aliyunSmsService")
    public ISmsService aliyunSmsService() {
        try {
            return new AliyunSmsServiceImpl(
                    aliyunKey,
                    aliyunSecret,
                    aliyunSignName,
                    aliyunTemplateCode
            );
        } catch (Exception e) {
            log.error("初始化阿里云短信服务失败", e);
            throw new RuntimeException("初始化阿里云短信服务失败", e);
        }
    }

    @Bean(name = "tencentSmsService")
    public ISmsService tencentSmsService() {
        try {
            return new TencentSmsServiceImpl(
                    tencentSecretId,
                    tencentSecretKey,
                    tencentSdkAppId,
                    tencentSignName,
                    tencentTemplateId,
                    tencentRegion
            );
        } catch (Exception e) {
            log.error("初始化腾讯云短信服务失败", e);
            throw new RuntimeException("初始化腾讯云短信服务失败", e);
        }
    }

    @Bean(name = "yunpianSmsService")
    public ISmsService yunpianSmsService() {
        try {
            return new YunpianSmsServiceImpl(yunpianApiKey, yunpianSignName);
        } catch (Exception e) {
            log.error("初始化云片短信服务失败", e);
            throw new RuntimeException("初始化云片短信服务失败", e);
        }
    }

    @Bean
    @Primary
    public ISmsService fallbackSmsService(
            @Qualifier("aliyunSmsService") ISmsService aliyunSmsService,
            @Qualifier("tencentSmsService") ISmsService tencentSmsService,
            @Qualifier("yunpianSmsService") ISmsService yunpianSmsService
    ) {
        if ("aliyun".equalsIgnoreCase(primary)) {
            log.info("使用阿里云作为主要短信服务");
            return new FallbackSmsServiceImpl(aliyunSmsService, yunpianSmsService);
        } else if ("tencent".equalsIgnoreCase(primary)) {
            log.info("使用腾讯云作为主要短信服务");
            return new FallbackSmsServiceImpl(tencentSmsService, yunpianSmsService);
        } else {
            log.info("使用云片作为主要短信服务");
            return new FallbackSmsServiceImpl(yunpianSmsService, aliyunSmsService);
        }
    }
}
