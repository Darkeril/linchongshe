package com.hongshu.web.service.sys.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hongshu.common.audit.dto.BaiduQianfanConfigDTO;
import com.hongshu.common.core.domain.R;
import com.hongshu.common.core.enums.ConfigKeyEnum;
import com.hongshu.common.core.enums.OssProviderEnum;
import com.hongshu.common.core.enums.SmsProviderEnum;
import com.hongshu.common.core.utils.StringUtils;
import com.hongshu.system.api.RemoteIdleService;
import com.hongshu.web.domain.dto.*;
import com.hongshu.web.domain.entity.*;
import com.hongshu.web.mapper.sys.*;
import com.hongshu.web.service.sys.ISysSystemConfigService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.Arrays;
import java.util.stream.Collectors;


/**
 * 系统配置关系表 服务实现类
 *
 * @author: hongshu
 */
@Slf4j
@Service
public class SysSystemConfigServiceImpl implements ISysSystemConfigService {

    private static final long SYSTEM_CONFIG_CACHE_TTL_MS = 60_000;

    private volatile SystemConfigDTO cachedSystemConfig;
    private volatile long cachedSystemConfigTimestamp;

    @Autowired
    private SysSystemConfigMapper systemConfigMapper;
    @Autowired
    private SysOssConfigMapper ossConfigMapper;
    @Autowired
    private SysSmsConfigMapper smsConfigMapper;
    @Autowired
    private SysAlipayConfigMapper alipayConfigMapper;
    @Autowired
    private SysWeChatPayConfigMapper weChatPayConfigMapper;
    @Autowired
    private SysBlacklistIpConfigMapper blacklistIpMapper;
    @Autowired(required = false)
    private RemoteIdleService remoteIdleService;


    @Override
    public ConfigDataDTO getAllConfigData() {
        ConfigDataDTO configData = new ConfigDataDTO();
        // 获取各种配置
        configData.setWebsite(this.getWebsiteConfig());
        configData.setSystem(this.getSystemConfig());
//        configData.setLocal(this.getLocalConfig());
        configData.setOss(this.getOssConfig());
        configData.setSms(this.getSmsConfig());
        configData.setAlipay(this.getAlipayConfig());
        configData.setWechatPay(this.getWeChatPayConfig());
        configData.setWechatLogin(this.getWeChatLoginConfig());
        configData.setCaptcha(this.getCaptchaConfig());
        configData.setAmap(this.getAmapConfig());
        configData.setDemoAccount(this.getDemoAccountConfig());
        configData.setDemoSite(this.getDemoSiteConfig());
        configData.setBlacklist(this.getBlacklistConfig());
        configData.setAutoAudit(this.getBaiduQianfanConfig());
        configData.setCarousel(this.getCarouselConfig());
        configData.setWorkbenchCarousel(this.getWorkbenchCarouselConfig());
        configData.setAdminLoginPromo(this.getAdminLoginPromoConfig());

        return configData;
    }

    // 更新网站配置
    @Override
    @Transactional
    public void updateWebsiteConfig(WebsiteConfigDTO websiteConfig) {
        try {
            Map<ConfigKeyEnum, String> configMap = new HashMap<>();
            configMap.put(ConfigKeyEnum.WEBSITE_RECORD_NUMBER, websiteConfig.getRecordNumber());
            configMap.put(ConfigKeyEnum.WEBSITE_LOGO, websiteConfig.getLogo());
            configMap.put(ConfigKeyEnum.WEBSITE_NAME, websiteConfig.getName());
            configMap.put(ConfigKeyEnum.WEBSITE_AUTHOR, websiteConfig.getAuthor());
            configMap.put(ConfigKeyEnum.WEBSITE_TITLE, websiteConfig.getTitle());
            configMap.put(ConfigKeyEnum.WEBSITE_DESCRIPTION, websiteConfig.getDescription());
            configMap.put(ConfigKeyEnum.WEBSITE_AI, websiteConfig.getAiUrl());
            configMap.put(ConfigKeyEnum.WEBSITE_COPYRIGHT, websiteConfig.getCopyright());
            configMap.put(ConfigKeyEnum.WEBSITE_WECHAT_QR_CODE, websiteConfig.getWechatQrCode());
            configMap.put(ConfigKeyEnum.WEBSITE_FOLLOW_US, websiteConfig.getFollowUs());
            configMap.put(ConfigKeyEnum.WEBSITE_ALIPAY_REWARD_QR_CODE, websiteConfig.getAlipayRewardQrCode());
            configMap.put(ConfigKeyEnum.WEBSITE_WECHAT_REWARD_QR_CODE, websiteConfig.getWechatRewardQrCode());

            this.updateConfigValues(configMap);
        } catch (Exception e) {
            log.error("更新网站配置失败", e);
        }
    }

    // 更新系统配置
    @Override
    @Transactional
    public void updateSystemConfig(SystemConfigDTO systemConfig) {
        Map<ConfigKeyEnum, String> configMap = new HashMap<>();
        
        // 只更新非null的配置项，避免覆盖已有配置和空指针异常
        if (systemConfig.getOssType() != null) {
            configMap.put(ConfigKeyEnum.OSS_TYPE, systemConfig.getOssType());
        }
        if (systemConfig.getSmsPrimary() != null) {
            configMap.put(ConfigKeyEnum.SMS_PRIMARY, systemConfig.getSmsPrimary());
        }
        if (systemConfig.getMqType() != null) {
            configMap.put(ConfigKeyEnum.MQ_TYPE, systemConfig.getMqType());
        }
        if (systemConfig.getQueryPrimary() != null) {
            configMap.put(ConfigKeyEnum.QUERY_PRIMARY, systemConfig.getQueryPrimary());
        }
        if (systemConfig.getAmapEnabled() != null) {
            configMap.put(ConfigKeyEnum.AMAP_ENABLED, systemConfig.getAmapEnabled());
        }
        if (systemConfig.getBlacklistEnabled() != null) {
            configMap.put(ConfigKeyEnum.BLACKLIST_ENABLED,
                    systemConfig.getBlacklistEnabled() ? "1" : "0");
        }
        if (systemConfig.getContentAuditEnabled() != null) {
            configMap.put(ConfigKeyEnum.CONTENT_AUDIT_ENABLED,
                    systemConfig.getContentAuditEnabled() ? "1" : "0");
        }
        if (systemConfig.getProductAuditEnabled() != null) {
            configMap.put(ConfigKeyEnum.PRODUCT_AUDIT_ENABLED,
                    systemConfig.getProductAuditEnabled() ? "1" : "0");
        }
        if (systemConfig.getUserAuditEnabled() != null) {
            configMap.put(ConfigKeyEnum.USER_AUDIT_ENABLED,
                    systemConfig.getUserAuditEnabled() ? "1" : "0");
        }
        if (systemConfig.getCommentAuditEnabled() != null) {
            configMap.put(ConfigKeyEnum.COMMENT_AUDIT_ENABLED,
                    systemConfig.getCommentAuditEnabled() ? "1" : "0");
        }

        this.updateConfigValues(configMap);
    }

    // 更新自动审核配置（百度千帆）
    @Override
    @Transactional
    public void updateBaiduQianfanConfig(BaiduQianfanConfigDTO baiduQianfanConfig) {
        log.info("开始更新自动审核配置，接收到的数据: auditMode={}, autoAuditThreshold={}, enabled={}", 
                baiduQianfanConfig.getAuditMode(), 
                baiduQianfanConfig.getAutoAuditThreshold(),
                baiduQianfanConfig.getEnabled());
        
        Map<ConfigKeyEnum, String> configMap = new HashMap<>();
        
        // 自动审核基础配置 - 必须更新，即使为null也要设置默认值
        String auditMode = baiduQianfanConfig.getAuditMode();
        if (auditMode != null && !auditMode.trim().isEmpty()) {
            configMap.put(ConfigKeyEnum.AUDIT_MODE, auditMode.trim());
            log.info("更新审核模式: {}", auditMode);
        } else {
            // 如果为null或空，设置默认值
            configMap.put(ConfigKeyEnum.AUDIT_MODE, "manual");
            log.info("审核模式为空，使用默认值: manual");
        }
        
        Double autoAuditThreshold = baiduQianfanConfig.getAutoAuditThreshold();
        if (autoAuditThreshold != null) {
            configMap.put(ConfigKeyEnum.AUTO_AUDIT_THRESHOLD,
                    String.valueOf(autoAuditThreshold));
            log.info("更新自动审核阈值: {}", autoAuditThreshold);
        } else {
            // 如果为null，设置默认值
            configMap.put(ConfigKeyEnum.AUTO_AUDIT_THRESHOLD, "0.8");
            log.info("自动审核阈值为空，使用默认值: 0.8");
        }
        
        // 百度千帆配置
        if (baiduQianfanConfig.getEnabled() != null) {
            configMap.put(ConfigKeyEnum.BAIDU_QIANFAN_ENABLED,
                    baiduQianfanConfig.getEnabled() ? "1" : "0");
        }
        // 百度千帆 Access Key：如果是脱敏数据（包含*），则跳过更新，避免覆盖完整数据
        if (baiduQianfanConfig.getAccessKey() != null 
                && !baiduQianfanConfig.getAccessKey().trim().isEmpty()) {
            String accessKey = baiduQianfanConfig.getAccessKey().trim();
            if (isMaskedValue(accessKey)) {
                log.warn("跳过更新 Access Key，检测到脱敏数据: {}", accessKey);
            } else {
                configMap.put(ConfigKeyEnum.BAIDU_QIANFAN_ACCESS_KEY, accessKey);
                log.info("更新 Access Key，值长度: {}", accessKey.length());
            }
        }
        // 百度千帆 Secret Key：如果是脱敏数据（包含*），则跳过更新，避免覆盖完整数据
        if (baiduQianfanConfig.getSecretKey() != null 
                && !baiduQianfanConfig.getSecretKey().trim().isEmpty()) {
            String secretKey = baiduQianfanConfig.getSecretKey().trim();
            if (isMaskedValue(secretKey)) {
                log.warn("跳过更新 Secret Key，检测到脱敏数据: {}", secretKey);
            } else {
                configMap.put(ConfigKeyEnum.BAIDU_QIANFAN_SECRET_KEY, secretKey);
                log.info("更新 Secret Key，值长度: {}", secretKey.length());
            }
        }
        if (baiduQianfanConfig.getTextEndpoint() != null && !baiduQianfanConfig.getTextEndpoint().trim().isEmpty()) {
            configMap.put(ConfigKeyEnum.BAIDU_QIANFAN_TEXT_ENDPOINT, baiduQianfanConfig.getTextEndpoint());
        }
        if (baiduQianfanConfig.getImageEndpoint() != null && !baiduQianfanConfig.getImageEndpoint().trim().isEmpty()) {
            configMap.put(ConfigKeyEnum.BAIDU_QIANFAN_IMAGE_ENDPOINT, baiduQianfanConfig.getImageEndpoint());
        }
        if (baiduQianfanConfig.getVideoEndpoint() != null && !baiduQianfanConfig.getVideoEndpoint().trim().isEmpty()) {
            configMap.put(ConfigKeyEnum.BAIDU_QIANFAN_VIDEO_ENDPOINT, baiduQianfanConfig.getVideoEndpoint());
        }

        this.updateConfigValues(configMap);
    }

    // 更新本地存储配置
    @Override
    @Transactional
    public void updateLocalConfig(LocalConfigDTO localConfig) {
        Map<ConfigKeyEnum, String> configMap = new HashMap<>();
        configMap.put(ConfigKeyEnum.FILE_UPLOAD_PATH, localConfig.getFileUploadPath());
        configMap.put(ConfigKeyEnum.LOCAL_PICTURE_BASE_URL, localConfig.getLocalPictureBaseUrl());
        configMap.put(ConfigKeyEnum.LOCAL_STORAGE_ENABLED, localConfig.getIsEnabled() ? "1" : "0");

        this.updateConfigValues(configMap);
    }

    // 更新OSS配置
    @Override
    @Transactional
    public void updateOssConfig(String provider, OssConfigDTO ossConfig) {
        // 如果是本地存储，使用系统配置表
        if ("local".equals(provider)) {
            this.updateLocalConfigAsOss(ossConfig);
            return;
        }
        LambdaQueryWrapper<WebOssConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WebOssConfig::getProvider, provider);
        WebOssConfig config = ossConfigMapper.selectOne(wrapper);

        if (config == null) {
            config = new WebOssConfig();
            config.setProvider(provider);
        }

        // 根据服务商类型进行字段映射
        OssProviderEnum providerEnum = OssProviderEnum.valueOf(provider.toUpperCase());
        switch (providerEnum) {
            case MINIO:
                config.setEndpoint(ossConfig.getUrl());  // Minio的url存储在endpoint字段
                break;
            case ALIYUN:
                // 如果是脱敏数据，跳过更新，避免覆盖完整数据
                if (ossConfig.getAccessKeyId() != null && !isMaskedValue(ossConfig.getAccessKeyId())) {
                    config.setAccessKey(ossConfig.getAccessKeyId());  // 阿里云的accessKeyId存储在accessKey字段
                }
                if (ossConfig.getAccessKeySecret() != null && !isMaskedValue(ossConfig.getAccessKeySecret())) {
                    config.setSecretKey(ossConfig.getAccessKeySecret()); // 阿里云的accessKeySecret存储在secretKey字段
                }
                break;
            case TENCENT:
                // 如果是脱敏数据，跳过更新，避免覆盖完整数据
                if (ossConfig.getOssKeyId() != null && !isMaskedValue(ossConfig.getOssKeyId())) {
                    config.setAccessKey(ossConfig.getOssKeyId());  // 腾讯云的ossKeyId存储在accessKey字段
                }
                if (ossConfig.getOssKeySecret() != null && !isMaskedValue(ossConfig.getOssKeySecret())) {
                    config.setSecretKey(ossConfig.getOssKeySecret()); // 腾讯云的ossKeySecret存储在secretKey字段
                }
                break;
            case QINIUYUN:
                // 如果是脱敏数据，跳过更新，避免覆盖完整数据
                if (ossConfig.getAccessKey() != null && !isMaskedValue(ossConfig.getAccessKey())) {
                    config.setAccessKey(ossConfig.getAccessKey());
                }
                if (ossConfig.getSecretKey() != null && !isMaskedValue(ossConfig.getSecretKey())) {
                    config.setSecretKey(ossConfig.getSecretKey());
                }
                break;
            case LOCAL:
                // 本地存储已经在上面处理了，这里不需要处理
                break;
        }
        config.setDomain(ossConfig.getDomain());
        config.setBucketName(ossConfig.getBucketName());
        config.setIsEnabled(ossConfig.getIsEnabled());
        config.setEndpoint(ossConfig.getEndpoint());
        config.setRegion(ossConfig.getRegion());
        config.setIsEnabled(ossConfig.getIsEnabled());
        config.setUpdateTime(new Date());

        if (config.getId() == null) {
            ossConfigMapper.insert(config);
        } else {
            ossConfigMapper.updateById(config);
        }
    }

    // 新增方法：将OSS格式的本地存储配置更新到系统配置表
    private void updateLocalConfigAsOss(OssConfigDTO ossConfig) {
        Map<ConfigKeyEnum, String> configMap = new HashMap<>();
        configMap.put(ConfigKeyEnum.FILE_UPLOAD_PATH, ossConfig.getUrl()); // url存储文件路径
        configMap.put(ConfigKeyEnum.LOCAL_PICTURE_BASE_URL, ossConfig.getDomain()); // domain存储域名
        configMap.put(ConfigKeyEnum.LOCAL_STORAGE_ENABLED, ossConfig.getIsEnabled() ? "1" : "0");

        this.updateConfigValues(configMap);
    }

    // 更新短信配置
    @Override
    @Transactional
    public void updateSmsConfig(String provider, SmsConfigDTO smsConfig) {
        LambdaQueryWrapper<WebSmsConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WebSmsConfig::getProvider, provider);
        WebSmsConfig config = smsConfigMapper.selectOne(wrapper);

        if (config == null) {
            config = new WebSmsConfig();
            config.setProvider(provider);
        }

        // 根据服务商类型进行字段映射
        SmsProviderEnum providerEnum = SmsProviderEnum.valueOf(provider.toUpperCase());
        switch (providerEnum) {
            case TENCENT:
                // 如果是脱敏数据，跳过更新，避免覆盖完整数据
                if (smsConfig.getSecretId() != null && !isMaskedValue(smsConfig.getSecretId())) {
                    config.setAccessKey(smsConfig.getSecretId());  // 腾讯云的secretId存储在accessKey字段
                }
                config.setTemplateCode(smsConfig.getTemplateId()); // 腾讯云的templateId存储在templateCode字段
                break;
            case YUNPIAN:
                // 如果是脱敏数据，跳过更新，避免覆盖完整数据
                if (smsConfig.getApiKey() != null && !isMaskedValue(smsConfig.getApiKey())) {
                    config.setAccessKey(smsConfig.getApiKey());  // 云片的apiKey存储在accessKey字段
                }
                break;
            case ALIYUN:
                // 如果是脱敏数据，跳过更新，避免覆盖完整数据
                if (smsConfig.getAccessKey() != null && !isMaskedValue(smsConfig.getAccessKey())) {
                    config.setAccessKey(smsConfig.getAccessKey());
                }
                config.setTemplateCode(smsConfig.getTemplateCode());
                break;
        }
        // 如果是脱敏数据，跳过更新，避免覆盖完整数据
        if (smsConfig.getSecretKey() != null && !isMaskedValue(smsConfig.getSecretKey())) {
            config.setSecretKey(smsConfig.getSecretKey());
        }
        config.setSignName(smsConfig.getSignName());
        config.setIsPrimary(smsConfig.getIsPrimary());
        config.setIsSecondary(smsConfig.getIsSecondary());
        config.setStatus(smsConfig.getStatus());
        config.setSdkAppId(smsConfig.getSdkAppId());
        config.setRegion(smsConfig.getRegion());
        config.setUpdateTime(new Date());

        if (config.getId() == null) {
            smsConfigMapper.insert(config);
        } else {
            smsConfigMapper.updateById(config);
        }
    }

    // 更新支付宝配置
    @Override
    @Transactional
    public void updateAlipayConfig(AlipayConfigDTO alipayConfig) {
        LambdaQueryWrapper<WebAlipayConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.last("LIMIT 1");
        WebAlipayConfig config = alipayConfigMapper.selectOne(wrapper);

        if (config == null) {
            config = new WebAlipayConfig();
        }

        config.setPid(alipayConfig.getPid());
        config.setAppId(alipayConfig.getAppId());
        // 如果是脱敏数据，跳过更新，避免覆盖完整数据
        if (alipayConfig.getMerchantPrivateKey() != null && !isMaskedValue(alipayConfig.getMerchantPrivateKey())) {
            config.setMerchantPrivateKey(alipayConfig.getMerchantPrivateKey());
        }
        if (alipayConfig.getAlipayPublicKey() != null && !isMaskedValue(alipayConfig.getAlipayPublicKey())) {
            config.setAlipayPublicKey(alipayConfig.getAlipayPublicKey());
        }
        config.setNotifyUrl(alipayConfig.getNotifyUrl());
        config.setReturnUrl(alipayConfig.getReturnUrl());
        config.setSignType(alipayConfig.getSignType());
        config.setCharset(alipayConfig.getCharset());
        config.setGatewayUrl(alipayConfig.getGatewayUrl());
        config.setIsEnabled(alipayConfig.getIsEnabled());
        config.setUpdateTime(new Date());

        if (config.getId() == null) {
            alipayConfigMapper.insert(config);
        } else {
            alipayConfigMapper.updateById(config);
        }

        // 通过Feign调用hongshu-idle服务刷新配置
        refreshAlipayConfig();
    }

    // 更新微信支付配置
    @Override
    @Transactional
    public void updateWeChatPayConfig(WeChatPayConfigDTO weChatPayConfig) {
        LambdaQueryWrapper<WebWeChatPayConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.last("LIMIT 1");
        WebWeChatPayConfig config = weChatPayConfigMapper.selectOne(wrapper);

        if (config == null) {
            config = new WebWeChatPayConfig();
        }

        config.setMchId(weChatPayConfig.getMchId());
        config.setPcAppId(weChatPayConfig.getPcAppId());
        config.setAppAppId(weChatPayConfig.getAppAppId());
        config.setAppId(weChatPayConfig.getAppId());
        // 如果是脱敏数据，跳过更新，避免覆盖完整数据
        if (weChatPayConfig.getApiKey() != null && !isMaskedValue(weChatPayConfig.getApiKey())) {
            config.setApiKey(weChatPayConfig.getApiKey());
        }
        if (weChatPayConfig.getApiV2Key() != null && !isMaskedValue(weChatPayConfig.getApiV2Key())) {
            config.setApiV2Key(weChatPayConfig.getApiV2Key());
        }
        if (weChatPayConfig.getApiV3Key() != null && !isMaskedValue(weChatPayConfig.getApiV3Key())) {
            config.setApiV3Key(weChatPayConfig.getApiV3Key());
        }
        config.setCertSerialNo(weChatPayConfig.getCertSerialNo());
        config.setPrivateKeyPath(weChatPayConfig.getPrivateKeyPath());
        config.setWechatPayCertificate(weChatPayConfig.getWechatPayCertificate());
        config.setNotifyUrl(weChatPayConfig.getNotifyUrl());
        config.setDomain(weChatPayConfig.getDomain());
        config.setIsEnabled(weChatPayConfig.getIsEnabled());
        config.setUpdateTime(new Date());

        if (config.getId() == null) {
            weChatPayConfigMapper.insert(config);
        } else {
            weChatPayConfigMapper.updateById(config);
        }

        // 通过Feign调用hongshu-idle服务刷新配置
        refreshWeChatPayConfig();
    }

    // 更新微信登录配置
    @Override
    @Transactional
    public void updateWeChatLoginConfig(WeChatLoginConfigDTO weChatLoginConfig) {
        Map<ConfigKeyEnum, String> configMap = new HashMap<>();
        configMap.put(ConfigKeyEnum.WECHAT_LOGIN_ENABLED,
                weChatLoginConfig.getEnabled() != null && weChatLoginConfig.getEnabled() ? "1" : "0");
        configMap.put(ConfigKeyEnum.WECHAT_MINIAPP_APPID, weChatLoginConfig.getMiniappAppId());
        // 如果是脱敏数据，跳过更新，避免覆盖完整数据
        if (weChatLoginConfig.getMiniappAppSecret() != null 
                && !isMaskedValue(weChatLoginConfig.getMiniappAppSecret())) {
            configMap.put(ConfigKeyEnum.WECHAT_MINIAPP_APPSECRET, weChatLoginConfig.getMiniappAppSecret());
        }
        configMap.put(ConfigKeyEnum.WECHAT_WEB_APPID, weChatLoginConfig.getWebAppId());
        // 如果是脱敏数据，跳过更新，避免覆盖完整数据
        if (weChatLoginConfig.getWebAppSecret() != null 
                && !isMaskedValue(weChatLoginConfig.getWebAppSecret())) {
            configMap.put(ConfigKeyEnum.WECHAT_WEB_APPSECRET, weChatLoginConfig.getWebAppSecret());
        }
        configMap.put(ConfigKeyEnum.WECHAT_WEB_CALLBACK_URL, weChatLoginConfig.getWebCallbackUrl());

        this.updateConfigValues(configMap);
    }

    // 获取微信登录配置
    @Override
    public WeChatLoginConfigDTO getWeChatLoginConfig() {
        List<String> configKeys = Arrays.asList(
                ConfigKeyEnum.WECHAT_LOGIN_ENABLED.getKey(),
                ConfigKeyEnum.WECHAT_MINIAPP_APPID.getKey(),
                ConfigKeyEnum.WECHAT_MINIAPP_APPSECRET.getKey(),
                ConfigKeyEnum.WECHAT_WEB_APPID.getKey(),
                ConfigKeyEnum.WECHAT_WEB_APPSECRET.getKey(),
                ConfigKeyEnum.WECHAT_WEB_CALLBACK_URL.getKey()
        );
        List<WebSystemConfig> configs = this.getConfigsByKeys(configKeys);

        WeChatLoginConfigDTO weChatLoginConfig = new WeChatLoginConfigDTO();
        for (WebSystemConfig config : configs) {
            ConfigKeyEnum configKey = ConfigKeyEnum.fromKey(config.getConfigKey());
            if (configKey != null) {
                switch (configKey) {
                    case WECHAT_LOGIN_ENABLED:
                        weChatLoginConfig.setEnabled("1".equals(config.getConfigValue()));
                        break;
                    case WECHAT_MINIAPP_APPID:
                        weChatLoginConfig.setMiniappAppId(config.getConfigValue());
                        break;
                    case WECHAT_MINIAPP_APPSECRET:
                        weChatLoginConfig.setMiniappAppSecret(config.getConfigValue());
                        break;
                    case WECHAT_WEB_APPID:
                        weChatLoginConfig.setWebAppId(config.getConfigValue());
                        break;
                    case WECHAT_WEB_APPSECRET:
                        weChatLoginConfig.setWebAppSecret(config.getConfigValue());
                        break;
                    case WECHAT_WEB_CALLBACK_URL:
                        weChatLoginConfig.setWebCallbackUrl(config.getConfigValue());
                        break;
                }
            }
        }
        // 设置默认值
        if (weChatLoginConfig.getEnabled() == null) {
            weChatLoginConfig.setEnabled(false);
        }
        return weChatLoginConfig;
    }

    @Override
    public WeChatLoginConfigDTO getImWeChatLoginConfig() {
        List<String> configKeys = Arrays.asList(
                ConfigKeyEnum.WECHAT_IM_LOGIN_ENABLED.getKey(),
                ConfigKeyEnum.WECHAT_IM_MINIAPP_APPID.getKey(),
                ConfigKeyEnum.WECHAT_IM_MINIAPP_APPSECRET.getKey()
        );
        List<WebSystemConfig> configs = this.getConfigsByKeys(configKeys);

        WeChatLoginConfigDTO dto = new WeChatLoginConfigDTO();
        for (WebSystemConfig config : configs) {
            ConfigKeyEnum configKey = ConfigKeyEnum.fromKey(config.getConfigKey());
            if (configKey != null) {
                switch (configKey) {
                    case WECHAT_IM_LOGIN_ENABLED:
                        dto.setEnabled("1".equals(config.getConfigValue()));
                        break;
                    case WECHAT_IM_MINIAPP_APPID:
                        dto.setMiniappAppId(config.getConfigValue());
                        break;
                    case WECHAT_IM_MINIAPP_APPSECRET:
                        dto.setMiniappAppSecret(config.getConfigValue());
                        break;
                }
            }
        }
        if (dto.getEnabled() == null) {
            dto.setEnabled(false);
        }
        return dto;
    }

    @Override
    @Transactional
    public void updateImWeChatLoginConfig(WeChatLoginConfigDTO config) {
        Map<ConfigKeyEnum, String> configMap = new HashMap<>();
        configMap.put(ConfigKeyEnum.WECHAT_IM_LOGIN_ENABLED,
                config.getEnabled() != null && config.getEnabled() ? "1" : "0");
        configMap.put(ConfigKeyEnum.WECHAT_IM_MINIAPP_APPID, config.getMiniappAppId());
        if (config.getMiniappAppSecret() != null && !isMaskedValue(config.getMiniappAppSecret())) {
            configMap.put(ConfigKeyEnum.WECHAT_IM_MINIAPP_APPSECRET, config.getMiniappAppSecret());
        }
        this.updateConfigValues(configMap);
    }

    // 更新验证码配置
    @Override
    @Transactional
    public void updateCaptchaConfig(CaptchaConfigDTO captchaConfig) {
        Map<ConfigKeyEnum, String> configMap = new HashMap<>();
        configMap.put(ConfigKeyEnum.CAPTCHA_CACHE_TYPE, captchaConfig.getCacheType());
        configMap.put(ConfigKeyEnum.CAPTCHA_TYPE, captchaConfig.getType());
        configMap.put(ConfigKeyEnum.CAPTCHA_WATER_MARK, captchaConfig.getWaterMark());
        configMap.put(ConfigKeyEnum.CAPTCHA_SLIP_OFFSET, String.valueOf(captchaConfig.getSlipOffset()));
        configMap.put(ConfigKeyEnum.CAPTCHA_AES_STATUS, captchaConfig.getAesStatus() ? "true" : "false");
        configMap.put(ConfigKeyEnum.CAPTCHA_INTERFERENCE_OPTIONS, String.valueOf(captchaConfig.getInterferenceOptions()));
        configMap.put(ConfigKeyEnum.CAPTCHA_JIGSAW, captchaConfig.getJigsaw());
        configMap.put(ConfigKeyEnum.CAPTCHA_PIC_CLICK, captchaConfig.getPicClick());
        configMap.put(ConfigKeyEnum.CAPTCHA_ENABLED, captchaConfig.getIsEnabled() ? "1" : "0");

        this.updateConfigValues(configMap);
    }

    // 更新高德地图配置
    @Override
    @Transactional
    public void updateAmapConfig(AmapConfigDTO amapConfig) {
        Map<ConfigKeyEnum, String> configMap = new HashMap<>();
        configMap.put(ConfigKeyEnum.AMAP_KEY, amapConfig.getKey());
        configMap.put(ConfigKeyEnum.AMAP_SECURITY_KEY, amapConfig.getSecurityKey());
        configMap.put(ConfigKeyEnum.AMAP_ENABLED, amapConfig.getIsEnabled() ? "1" : "0");

        this.updateConfigValues(configMap);
    }

    // 更新演示账号配置
    @Override
    @Transactional
    public void updateDemoAccount(DemoAccountConfigDTO demoAccountConfig) {
        Map<ConfigKeyEnum, String> configMap = new HashMap<>();
        configMap.put(ConfigKeyEnum.DEMO_ACCOUNT_ENABLED, demoAccountConfig.getEnabled() ? "1" : "0");
        configMap.put(ConfigKeyEnum.DEMO_ACCOUNT_USERNAME, demoAccountConfig.getUsername());
        configMap.put(ConfigKeyEnum.DEMO_ACCOUNT_PASSWORD, demoAccountConfig.getPassword());
        configMap.put(ConfigKeyEnum.DEMO_ACCOUNT_DESCRIPTION, demoAccountConfig.getDescription());
        configMap.put(ConfigKeyEnum.DEMO_ACCOUNT_PERMISSIONS,
                demoAccountConfig.getPermissions() != null ?
                        String.join(",", demoAccountConfig.getPermissions()) : "");
        configMap.put(ConfigKeyEnum.DEMO_ACCOUNT_EXPIRE_TIME, demoAccountConfig.getExpireTime());

        this.updateConfigValues(configMap);
    }

    // 更新演示站配置
    @Override
    @Transactional
    public void updateDemoSite(DemoSiteConfigDTO demoSiteConfig) {
        Map<ConfigKeyEnum, String> configMap = new HashMap<>();
        configMap.put(ConfigKeyEnum.DEMO_SITE_ENABLED, demoSiteConfig.getEnabled() ? "1" : "0");
        configMap.put(ConfigKeyEnum.DEMO_SITE_MOBILE_URL, demoSiteConfig.getMobileUrl());
        configMap.put(ConfigKeyEnum.DEMO_SITE_WEB_URL, demoSiteConfig.getWebUrl());
        configMap.put(ConfigKeyEnum.DEMO_SITE_ADMIN_URL, demoSiteConfig.getAdminUrl());
        configMap.put(ConfigKeyEnum.DEMO_SITE_GITEE_URL, demoSiteConfig.getGiteeUrl());
        configMap.put(ConfigKeyEnum.DEMO_SITE_GITHUB_URL, demoSiteConfig.getGithubUrl());
        configMap.put(ConfigKeyEnum.DEMO_SITE_DOC_URL, demoSiteConfig.getDocUrl());
        configMap.put(ConfigKeyEnum.DEMO_SITE_ARCO_ADMIN_URL, demoSiteConfig.getArcoAdminUrl());
        configMap.put(ConfigKeyEnum.DEMO_SITE_DESCRIPTION, demoSiteConfig.getDescription());
        configMap.put(ConfigKeyEnum.DEMO_SITE_ENABLE_STATS, demoSiteConfig.getEnableStats() ? "1" : "0");

        this.updateConfigValues(configMap);
    }

    // 更新黑名单配置
    @Override
    @Transactional
    public void updateBlacklistEnabled(Boolean enabled) {
        updateConfigValue(ConfigKeyEnum.BLACKLIST_ENABLED.getKey(), enabled ? "1" : "0");
    }

    @Override
    @Transactional
    public void addBlacklistItem(BlacklistItemDTO blacklistItemDTO) {
        WebIpBlacklist existingItem = blacklistIpMapper
                .selectOne(new LambdaQueryWrapper<WebIpBlacklist>()
                        .eq(WebIpBlacklist::getIpAddress, blacklistItemDTO.getIpAddress()));

        if (existingItem != null) {
            if ("1".equals(existingItem.getStatus())) {
                throw new RuntimeException("该IP地址已处于封禁状态");
            }
            // 已解封的IP重新封禁
            existingItem.setReason(blacklistItemDTO.getReason());
            existingItem.setStatus("1");
            existingItem.setBanTime(new Date());
            existingItem.setUpdateTime(new Date());
            existingItem.setExpireTime(blacklistItemDTO.getExpireTime());
            blacklistIpMapper.updateById(existingItem);
            return;
        }

        WebIpBlacklist blacklistItem = new WebIpBlacklist();
        blacklistItem.setIpAddress(blacklistItemDTO.getIpAddress());
        blacklistItem.setReason(blacklistItemDTO.getReason());
        blacklistItem.setStatus("1");
        blacklistItem.setBanTime(new Date());
        blacklistItem.setCreateTime(new Date());
        blacklistItem.setUpdateTime(new Date());
        if (blacklistItemDTO.getExpireTime() != null) {
            blacklistItem.setExpireTime(blacklistItemDTO.getExpireTime());
        }
        blacklistIpMapper.insert(blacklistItem);
    }

    @Override
    @Transactional
    public void unbanBlacklistItem(Long id) {
        // 查询黑名单项是否存在
        WebIpBlacklist blacklistItem = blacklistIpMapper.selectById(id);
        if (blacklistItem == null) {
            throw new RuntimeException("黑名单项不存在");
        }
        // 检查是否已经是解封状态
        if ("0".equals(blacklistItem.getStatus())) {
            throw new RuntimeException("该IP已经是解封状态");
        }
        // 更新状态为解封
        blacklistItem.setStatus("0"); // 0-已解封
        blacklistItem.setExpireTime(LocalDateTime.now());
        blacklistItem.setUpdateTime(new Date());
        // 更新数据库
        int result = blacklistIpMapper.updateById(blacklistItem);
        if (result <= 0) {
            throw new RuntimeException("解封失败，请重试");
        }
    }

    @Override
    @Transactional
    public void deleteBlacklistItem(Long id) {
        // 查询黑名单项是否存在
        WebIpBlacklist blacklistItem = blacklistIpMapper.selectById(id);
        if (blacklistItem == null) {
            throw new RuntimeException("黑名单项不存在");
        }
        // 检查是否正在封禁状态
        if ("1".equals(blacklistItem.getStatus())) {
            throw new RuntimeException("该IP正在封禁状态，请先解封后再删除");
        }
        // 删除黑名单项
        int result = blacklistIpMapper.deleteById(id);
        if (result <= 0) {
            throw new RuntimeException("删除失败，请重试");
        }
    }

    // 通用的配置更新方法
    private void updateConfigValues(Map<ConfigKeyEnum, String> configMap) {
        for (Map.Entry<ConfigKeyEnum, String> entry : configMap.entrySet()) {
            if (entry.getValue() != null) {
                this.updateConfigValue(entry.getKey().getKey(), entry.getValue());
            }
        }
        cachedSystemConfig = null;
    }

    // 通用的配置更新方法
    private void updateConfigValue(String configKey, String configValue) {
        LambdaQueryWrapper<WebSystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WebSystemConfig::getConfigKey, configKey);
        WebSystemConfig config = systemConfigMapper.selectOne(wrapper);

        if (config != null) {
            String oldValue = config.getConfigValue();
            config.setConfigValue(configValue);
            config.setUpdateTime(new Date());
            int result = systemConfigMapper.updateById(config);
            log.info("更新配置项: key={}, oldValue={}, newValue={}, result={}", 
                    configKey, oldValue, configValue, result);
        } else {
            // 如果配置不存在，创建新的配置
            WebSystemConfig newConfig = new WebSystemConfig();
            newConfig.setConfigKey(configKey);
            newConfig.setConfigValue(configValue);
            newConfig.setConfigGroup("website");
            newConfig.setCreateTime(new Date());
            newConfig.setUpdateTime(new Date());
            int result = systemConfigMapper.insert(newConfig);
            log.info("创建新配置项: key={}, value={}, result={}", configKey, configValue, result);
        }
    }


    // 获取网站配置
    public WebsiteConfigDTO getWebsiteConfig() {
        List<String> configKeys = ConfigKeyEnum.getWebsiteConfigKeys();
        List<WebSystemConfig> configs = this.getConfigsByKeys(configKeys);

        WebsiteConfigDTO websiteConfig = new WebsiteConfigDTO();
        for (WebSystemConfig config : configs) {
            ConfigKeyEnum configKey = ConfigKeyEnum.fromKey(config.getConfigKey());
            if (configKey != null) {
                switch (configKey) {
                    case WEBSITE_RECORD_NUMBER:
                        websiteConfig.setRecordNumber(config.getConfigValue());
                        break;
                    case WEBSITE_LOGO:
                        websiteConfig.setLogo(config.getConfigValue());
                        break;
                    case WEBSITE_NAME:
                        websiteConfig.setName(config.getConfigValue());
                        break;
                    case WEBSITE_AUTHOR:
                        websiteConfig.setAuthor(config.getConfigValue());
                        break;
                    case WEBSITE_TITLE:
                        websiteConfig.setTitle(config.getConfigValue());
                        break;
                    case WEBSITE_DESCRIPTION:
                        websiteConfig.setDescription(config.getConfigValue());
                        break;
                    case WEBSITE_COPYRIGHT:
                        websiteConfig.setCopyright(config.getConfigValue());
                        break;
                    case WEBSITE_AI:
                        websiteConfig.setAiUrl(config.getConfigValue());
                        break;
                    case WEBSITE_WECHAT_QR_CODE:
                        websiteConfig.setWechatQrCode(config.getConfigValue());
                        break;
                    case WEBSITE_FOLLOW_US:
                        websiteConfig.setFollowUs(config.getConfigValue());
                        break;
                    case WEBSITE_ALIPAY_REWARD_QR_CODE:
                        websiteConfig.setAlipayRewardQrCode(config.getConfigValue());
                        break;
                    case WEBSITE_WECHAT_REWARD_QR_CODE:
                        websiteConfig.setWechatRewardQrCode(config.getConfigValue());
                        break;
                }
            }
        }
        return websiteConfig;
    }

    // 获取系统配置（带本地缓存，60秒 TTL）
    public SystemConfigDTO getSystemConfig() {
        SystemConfigDTO cached = cachedSystemConfig;
        if (cached != null && (System.currentTimeMillis() - cachedSystemConfigTimestamp) < SYSTEM_CONFIG_CACHE_TTL_MS) {
            return cached;
        }

        List<String> configKeys = ConfigKeyEnum.getSystemConfigKeys();
        List<WebSystemConfig> configs = this.getConfigsByKeys(configKeys);

        SystemConfigDTO systemConfig = new SystemConfigDTO();
        for (WebSystemConfig config : configs) {
            ConfigKeyEnum configKey = ConfigKeyEnum.fromKey(config.getConfigKey());
            if (configKey != null) {
                switch (configKey) {
                    case OSS_TYPE:
                        systemConfig.setOssType(config.getConfigValue());
                        break;
                    case SMS_PRIMARY:
                        systemConfig.setSmsPrimary(config.getConfigValue());
                        break;
                    case MQ_TYPE:
                        systemConfig.setMqType(config.getConfigValue());
                        break;
                    case QUERY_PRIMARY:
                        systemConfig.setQueryPrimary(config.getConfigValue());
                        break;
                    case AMAP_ENABLED:
                        systemConfig.setAmapEnabled(config.getConfigValue());
                        break;
                    case BLACKLIST_ENABLED:
                        systemConfig.setBlacklistEnabled("1".equals(config.getConfigValue()));
                        break;
                    case CONTENT_AUDIT_ENABLED:
                        systemConfig.setContentAuditEnabled("1".equals(config.getConfigValue()));
                        break;
                    case PRODUCT_AUDIT_ENABLED:
                        systemConfig.setProductAuditEnabled("1".equals(config.getConfigValue()));
                        break;
                    case USER_AUDIT_ENABLED:
                        systemConfig.setUserAuditEnabled("1".equals(config.getConfigValue()));
                        break;
                    case COMMENT_AUDIT_ENABLED:
                        systemConfig.setCommentAuditEnabled("1".equals(config.getConfigValue()));
                        break;
                    case BAIDU_QIANFAN_ENABLED:
                        systemConfig.setBaiduQianfanEnabled("1".equals(config.getConfigValue()));
                        break;
                    case BAIDU_QIANFAN_ACCESS_KEY:
                        systemConfig.setBaiduQianfanAccessKey(config.getConfigValue());
                        break;
                    case BAIDU_QIANFAN_SECRET_KEY:
                        systemConfig.setBaiduQianfanSecretKey(config.getConfigValue());
                        break;
                    case BAIDU_QIANFAN_TEXT_ENDPOINT:
                        systemConfig.setBaiduQianfanTextEndpoint(config.getConfigValue());
                        break;
                    case BAIDU_QIANFAN_IMAGE_ENDPOINT:
                        systemConfig.setBaiduQianfanImageEndpoint(config.getConfigValue());
                        break;
                    case BAIDU_QIANFAN_VIDEO_ENDPOINT:
                        systemConfig.setBaiduQianfanVideoEndpoint(config.getConfigValue());
                        break;
                }
            }
        }
        cachedSystemConfig = systemConfig;
        cachedSystemConfigTimestamp = System.currentTimeMillis();
        return systemConfig;
    }

    // 获取本地存储配置
    public LocalConfigDTO getLocalConfig() {
        List<String> configKeys = ConfigKeyEnum.getLocalConfigKeys();
        List<WebSystemConfig> configs = this.getConfigsByKeys(configKeys);

        LocalConfigDTO localConfig = new LocalConfigDTO();
        for (WebSystemConfig config : configs) {
            ConfigKeyEnum configKey = ConfigKeyEnum.fromKey(config.getConfigKey());
            if (configKey != null) {
                switch (configKey) {
                    case FILE_UPLOAD_PATH:
                        localConfig.setFileUploadPath(config.getConfigValue());
                        break;
                    case LOCAL_PICTURE_BASE_URL:
                        localConfig.setLocalPictureBaseUrl(config.getConfigValue());
                        break;
                    case LOCAL_STORAGE_ENABLED:
                        localConfig.setIsEnabled("1".equals(config.getConfigValue()));
                        break;
                }
            }
        }
        // 设置默认值
        if (localConfig.getLocalPictureBaseUrl() == null) {
            localConfig.setLocalPictureBaseUrl("http://localhost:8080");
        }
        if (localConfig.getIsEnabled() == null) {
            localConfig.setIsEnabled(false);
        }
        return localConfig;
    }

    // 获取OSS配置（对外接口，脱敏）
    public Map<String, OssConfigDTO> getOssConfig() {
        return this.buildOssConfigMap(true);
    }

    // 获取OSS配置（内部使用，不脱敏）
    @Override
    public Map<String, OssConfigDTO> getOssConfigInternal() {
        return this.buildOssConfigMap(false);
    }

    // 构建OSS配置Map的通用方法
    private Map<String, OssConfigDTO> buildOssConfigMap(boolean maskSensitive) {
        List<String> providers = OssProviderEnum.getAllCodes();

        LambdaQueryWrapper<WebOssConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(WebOssConfig::getProvider, providers);
        List<WebOssConfig> configs = ossConfigMapper.selectList(wrapper);

        Map<String, OssConfigDTO> ossConfigMap = new HashMap<>();
        // 处理本地存储配置（从系统配置表获取）
        OssConfigDTO localConfig = this.getLocalConfigAsOss();
        if (localConfig != null) {
            ossConfigMap.put("local", localConfig);
        }
        for (WebOssConfig config : configs) {
            String accessKey = maskSensitive ? maskSensitiveValue(config.getAccessKey()) : config.getAccessKey();
            String secretKey = maskSensitive ? maskSensitiveValue(config.getSecretKey()) : config.getSecretKey();
            
            OssConfigDTO dto = OssConfigDTO.builder()
                    .domain(config.getDomain())
                    .accessKey(accessKey)
                    .secretKey(secretKey)
                    .bucketName(config.getBucketName())
                    .isEnabled(config.getIsEnabled())
                    .endpoint(config.getEndpoint())
                    .region(config.getRegion())
                    .build();

            // 根据服务商类型进行字段映射
            OssProviderEnum provider = OssProviderEnum.valueOf(config.getProvider().toUpperCase());
            switch (provider) {
                case MINIO:
                    dto.setUrl(config.getEndpoint());  // Minio的url从endpoint字段获取
                    break;
                case ALIYUN:
                    dto.setAccessKeyId(accessKey);
                    dto.setAccessKeySecret(secretKey);
                    break;
                case TENCENT:
                    dto.setOssKeyId(accessKey);
                    dto.setOssKeySecret(secretKey);
                    break;
                case QINIUYUN:
                    // 七牛云不需要特殊处理
                    break;
                case LOCAL:
                    // 本地存储已经在上面处理了，这里跳过
                    continue;
            }
            ossConfigMap.put(config.getProvider(), dto);
        }
        return ossConfigMap;
    }

    // 新增方法：将本地存储配置转换为OSS配置格式
    private OssConfigDTO getLocalConfigAsOss() {
        List<String> configKeys = ConfigKeyEnum.getLocalConfigKeys();
        List<WebSystemConfig> configs = this.getConfigsByKeys(configKeys);

        OssConfigDTO localConfig = new OssConfigDTO();
        for (WebSystemConfig config : configs) {
            ConfigKeyEnum configKey = ConfigKeyEnum.fromKey(config.getConfigKey());
            if (configKey != null) {
                switch (configKey) {
                    case FILE_UPLOAD_PATH:
                        localConfig.setUrl(config.getConfigValue()); // 使用url字段存储路径
                        break;
                    case LOCAL_PICTURE_BASE_URL:
                        localConfig.setDomain(config.getConfigValue()); // 使用domain字段存储域名
                        break;
                    case LOCAL_STORAGE_ENABLED:
                        localConfig.setIsEnabled("1".equals(config.getConfigValue()));
                        break;
                }
            }
        }
        // 设置默认值
        if (localConfig.getDomain() == null) {
            localConfig.setDomain("http://localhost:8080");
        }
        if (localConfig.getIsEnabled() == null) {
            localConfig.setIsEnabled(false);
        }
        return localConfig;
    }

    // 获取OSS配置
    public OssConfigDTO getOssConfig(String provider) {
        // 根据provider查询对应的OSS配置
        WebOssConfig config = ossConfigMapper
                .selectOne(new LambdaQueryWrapper<WebOssConfig>()
                        .eq(WebOssConfig::getProvider, provider));
        if (config == null) {
            log.warn("未找到OSS配置: " + provider);
            return null;
        }
        // 构建DTO
        OssConfigDTO dto = OssConfigDTO.builder()
                .domain(config.getDomain())
                .accessKey(config.getAccessKey())
                .secretKey(config.getSecretKey())
                .bucketName(config.getBucketName())
                .isEnabled(config.getIsEnabled())
                .endpoint(config.getEndpoint())
                .region(config.getRegion())
                .build();
        // 根据服务商类型进行字段映射
        try {
            OssProviderEnum providerEnum = OssProviderEnum.valueOf(config.getProvider().toUpperCase());
            switch (providerEnum) {
                case MINIO:
                    dto.setUrl(config.getEndpoint());  // Minio的url从endpoint字段获取
                    break;
                case ALIYUN:
                    dto.setAccessKeyId(config.getAccessKey());  // 阿里云的accessKeyId从accessKey字段获取
                    dto.setAccessKeySecret(config.getSecretKey()); // 阿里云的accessKeySecret从secretKey字段获取
                    break;
                case TENCENT:
                    dto.setOssKeyId(config.getAccessKey());  // 腾讯云的ossKeyId从accessKey字段获取
                    dto.setOssKeySecret(config.getSecretKey()); // 腾讯云的ossKeySecret从secretKey字段获取
                    break;
                case QINIUYUN:
                    // 七牛云不需要特殊处理
                    break;
            }
        } catch (IllegalArgumentException e) {
            log.error("不支持的OSS服务商类型: " + config.getProvider());
        }
        return dto;
    }

    // 获取短信配置（对外接口，脱敏）
    public Map<String, SmsConfigDTO> getSmsConfig() {
        return buildSmsConfigMap(true);
    }

    // 获取短信配置（内部使用，不脱敏）
    public Map<String, SmsConfigDTO> getSmsConfigInternal() {
        return buildSmsConfigMap(false);
    }

    // 构建短信配置Map的通用方法
    private Map<String, SmsConfigDTO> buildSmsConfigMap(boolean maskSensitive) {
        List<String> providers = SmsProviderEnum.getAllCodes();

        LambdaQueryWrapper<WebSmsConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(WebSmsConfig::getProvider, providers);
        List<WebSmsConfig> configs = smsConfigMapper.selectList(wrapper);

        Map<String, SmsConfigDTO> smsConfigMap = new HashMap<>();
        for (WebSmsConfig config : configs) {
            String accessKey = maskSensitive ? maskSensitiveValue(config.getAccessKey()) : config.getAccessKey();
            String secretKey = maskSensitive ? maskSensitiveValue(config.getSecretKey()) : config.getSecretKey();
            
            SmsConfigDTO dto = SmsConfigDTO.builder()
                    .accessKey(accessKey)
                    .secretKey(secretKey)
                    .signName(config.getSignName())
                    .templateCode(config.getTemplateCode())
                    .isPrimary(config.getIsPrimary())
                    .isSecondary(config.getIsSecondary())
                    .status(config.getStatus())
                    .sdkAppId(config.getSdkAppId())
                    .region(config.getRegion())
                    .build();

            // 根据服务商类型进行字段映射
            SmsProviderEnum provider = SmsProviderEnum.valueOf(config.getProvider().toUpperCase());
            switch (provider) {
                case TENCENT:
                    dto.setSecretId(accessKey);
                    dto.setTemplateId(config.getTemplateCode());
                    break;
                case YUNPIAN:
                    dto.setApiKey(accessKey);
                    break;
                case ALIYUN:
                    // 阿里云不需要特殊处理
                    break;
            }
            smsConfigMap.put(config.getProvider(), dto);
        }
        return smsConfigMap;
    }

    // 获取支付宝配置（对外接口，脱敏）
    public AlipayConfigDTO getAlipayConfig() {
        return buildAlipayConfig(true);
    }

    // 获取支付宝配置（内部使用，不脱敏）
    public AlipayConfigDTO getAlipayConfigInternal() {
        return buildAlipayConfig(false);
    }

    // 构建支付宝配置的通用方法
    private AlipayConfigDTO buildAlipayConfig(boolean maskSensitive) {
        LambdaQueryWrapper<WebAlipayConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.last("LIMIT 1");
        WebAlipayConfig config = alipayConfigMapper.selectOne(wrapper);

        if (config != null) {
            String merchantPrivateKey = maskSensitive ? maskSensitiveValue(config.getMerchantPrivateKey()) : config.getMerchantPrivateKey();
            String alipayPublicKey = maskSensitive ? maskSensitiveValue(config.getAlipayPublicKey()) : config.getAlipayPublicKey();
            
            return AlipayConfigDTO.builder()
                    .pid(config.getPid())
                    .appId(config.getAppId())
                    .merchantPrivateKey(merchantPrivateKey)
                    .alipayPublicKey(alipayPublicKey)
                    .notifyUrl(config.getNotifyUrl())
                    .returnUrl(config.getReturnUrl())
                    .signType(config.getSignType())
                    .charset(config.getCharset())
                    .gatewayUrl(config.getGatewayUrl())
                    .isEnabled(config.getIsEnabled())
                    .build();
        }
        return new AlipayConfigDTO();
    }

    // 获取微信支付配置（对外接口，脱敏）
    @Override
    public WeChatPayConfigDTO getWeChatPayConfig() {
        return buildWeChatPayConfig(true);
    }

    // 获取微信支付配置（内部使用，不脱敏）
    public WeChatPayConfigDTO getWeChatPayConfigInternal() {
        return buildWeChatPayConfig(false);
    }

    // 构建微信支付配置的通用方法
    private WeChatPayConfigDTO buildWeChatPayConfig(boolean maskSensitive) {
        LambdaQueryWrapper<WebWeChatPayConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.last("LIMIT 1");
        WebWeChatPayConfig config = weChatPayConfigMapper.selectOne(wrapper);

        if (config != null) {
            String apiKey = maskSensitive ? maskSensitiveValue(config.getApiKey()) : config.getApiKey();
            String apiV2Key = maskSensitive ? maskSensitiveValue(config.getApiV2Key()) : config.getApiV2Key();
            String apiV3Key = maskSensitive ? maskSensitiveValue(config.getApiV3Key()) : config.getApiV3Key();
            
            return WeChatPayConfigDTO.builder()
                    .mchId(config.getMchId())
                    .pcAppId(config.getPcAppId())
                    .appAppId(config.getAppAppId())
                    .appId(config.getAppId())
                    .apiKey(apiKey)
                    .apiV2Key(apiV2Key)
                    .apiV3Key(apiV3Key)
                    .certSerialNo(config.getCertSerialNo())
                    .privateKeyPath(config.getPrivateKeyPath())
                    .wechatPayCertificate(config.getWechatPayCertificate())
                    .notifyUrl(config.getNotifyUrl())
                    .domain(config.getDomain())
                    .isEnabled(config.getIsEnabled())
                    .build();
        }
        return new WeChatPayConfigDTO();
    }

    // 获取验证码配置
    public CaptchaConfigDTO getCaptchaConfig() {
        List<String> configKeys = ConfigKeyEnum.getCaptchaConfigKeys();
        List<WebSystemConfig> configs = this.getConfigsByKeys(configKeys);

        CaptchaConfigDTO captchaConfig = new CaptchaConfigDTO();
        for (WebSystemConfig config : configs) {
            ConfigKeyEnum configKey = ConfigKeyEnum.fromKey(config.getConfigKey());
            if (configKey != null) {
                switch (configKey) {
                    case CAPTCHA_CACHE_TYPE:
                        captchaConfig.setCacheType(config.getConfigValue());
                        break;
                    case CAPTCHA_TYPE:
                        captchaConfig.setType(config.getConfigValue());
                        break;
                    case CAPTCHA_WATER_MARK:
                        captchaConfig.setWaterMark(config.getConfigValue());
                        break;
                    case CAPTCHA_SLIP_OFFSET:
                        captchaConfig.setSlipOffset(Integer.valueOf(config.getConfigValue()));
                        break;
                    case CAPTCHA_AES_STATUS:
                        captchaConfig.setAesStatus("true".equals(config.getConfigValue()));
                        break;
                    case CAPTCHA_INTERFERENCE_OPTIONS:
                        captchaConfig.setInterferenceOptions(Integer.valueOf(config.getConfigValue()));
                        break;
                    case CAPTCHA_JIGSAW:
                        captchaConfig.setJigsaw(config.getConfigValue());
                        break;
                    case CAPTCHA_PIC_CLICK:
                        captchaConfig.setPicClick(config.getConfigValue());
                        break;
                    case CAPTCHA_ENABLED:
                        captchaConfig.setIsEnabled("1".equals(config.getConfigValue()));
                        break;
                }
            }
        }
        return captchaConfig;
    }

    // 获取高德地图配置（内部使用，不脱敏）
    public AmapConfigDTO getAmapConfig() {
        List<String> configKeys = ConfigKeyEnum.getAmapConfigKeys();
        List<WebSystemConfig> configs = this.getConfigsByKeysInternal(configKeys);

        AmapConfigDTO amapConfig = new AmapConfigDTO();
        for (WebSystemConfig config : configs) {
            ConfigKeyEnum configKey = ConfigKeyEnum.fromKey(config.getConfigKey());
            if (configKey != null) {
                switch (configKey) {
                    case AMAP_KEY:
                        amapConfig.setKey(config.getConfigValue());
                        break;
                    case AMAP_SECURITY_KEY:
                        amapConfig.setSecurityKey(config.getConfigValue());
                        break;
                    case AMAP_ENABLED:
                        amapConfig.setIsEnabled("1".equals(config.getConfigValue()));
                        break;
                }
            }
        }
        return amapConfig;
    }

    // 获取演示账号配置
    public DemoAccountConfigDTO getDemoAccountConfig() {
        List<String> configKeys = ConfigKeyEnum.getDemoAccountConfigKeys();
        List<WebSystemConfig> configs = this.getConfigsByKeys(configKeys);

        DemoAccountConfigDTO demoAccountConfig = new DemoAccountConfigDTO();
        for (WebSystemConfig config : configs) {
            ConfigKeyEnum configKey = ConfigKeyEnum.fromKey(config.getConfigKey());
            if (configKey != null) {
                switch (configKey) {
                    case DEMO_ACCOUNT_ENABLED:
                        demoAccountConfig.setEnabled("1".equals(config.getConfigValue()));
                        break;
                    case DEMO_ACCOUNT_USERNAME:
                        demoAccountConfig.setUsername(config.getConfigValue());
                        break;
                    case DEMO_ACCOUNT_PASSWORD:
                        demoAccountConfig.setPassword(config.getConfigValue());
                        break;
                    case DEMO_ACCOUNT_DESCRIPTION:
                        demoAccountConfig.setDescription(config.getConfigValue());
                        break;
                    case DEMO_ACCOUNT_PERMISSIONS:
                        if (StringUtils.isNotBlank(config.getConfigValue())) {
                            demoAccountConfig.setPermissions(Arrays.asList(config.getConfigValue().split(",")));
                        } else {
                            demoAccountConfig.setPermissions(new ArrayList<>());
                        }
                        break;
                    case DEMO_ACCOUNT_EXPIRE_TIME:
                        demoAccountConfig.setExpireTime(config.getConfigValue());
                        break;
                }
            }
        }
        // 设置默认值
        if (demoAccountConfig.getEnabled() == null) {
            demoAccountConfig.setEnabled(false);
        }
        if (demoAccountConfig.getPermissions() == null) {
            demoAccountConfig.setPermissions(new ArrayList<>());
        }
        return demoAccountConfig;
    }

    // 获取演示站点配置
    public DemoSiteConfigDTO getDemoSiteConfig() {
        List<String> configKeys = ConfigKeyEnum.getDemoSiteConfigKeys();
        List<WebSystemConfig> configs = this.getConfigsByKeys(configKeys);

        DemoSiteConfigDTO demoSiteConfig = new DemoSiteConfigDTO();
        for (WebSystemConfig config : configs) {
            ConfigKeyEnum configKey = ConfigKeyEnum.fromKey(config.getConfigKey());
            if (configKey != null) {
                switch (configKey) {
                    case DEMO_SITE_ENABLED:
                        demoSiteConfig.setEnabled("1".equals(config.getConfigValue()));
                        break;
                    case DEMO_SITE_MOBILE_URL:
                        demoSiteConfig.setMobileUrl(config.getConfigValue());
                        break;
                    case DEMO_SITE_WEB_URL:
                        demoSiteConfig.setWebUrl(config.getConfigValue());
                        break;
                    case DEMO_SITE_ADMIN_URL:
                        demoSiteConfig.setAdminUrl(config.getConfigValue());
                        break;
                    case DEMO_SITE_GITEE_URL:
                        demoSiteConfig.setGiteeUrl(config.getConfigValue());
                        break;
                    case DEMO_SITE_GITHUB_URL:
                        demoSiteConfig.setGithubUrl(config.getConfigValue());
                        break;
                    case DEMO_SITE_DOC_URL:
                        demoSiteConfig.setDocUrl(config.getConfigValue());
                        break;
                    case DEMO_SITE_ARCO_ADMIN_URL:
                        demoSiteConfig.setArcoAdminUrl(config.getConfigValue());
                        break;
                    case DEMO_SITE_DESCRIPTION:
                        demoSiteConfig.setDescription(config.getConfigValue());
                        break;
                    case DEMO_SITE_ENABLE_STATS:
                        demoSiteConfig.setEnableStats("1".equals(config.getConfigValue()));
                        break;
                }
            }
        }
        // 设置默认值
        if (demoSiteConfig.getEnabled() == null) {
            demoSiteConfig.setEnabled(false);
        }
        if (demoSiteConfig.getEnableStats() == null) {
            demoSiteConfig.setEnableStats(false);
        }
        return demoSiteConfig;
    }

    // 获取黑名单配置
    @Override
    public BlacklistConfigDTO getBlacklistConfig() {
        // 获取黑名单开关状态
        LambdaQueryWrapper<WebSystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WebSystemConfig::getConfigKey, ConfigKeyEnum.BLACKLIST_ENABLED.getKey());
        WebSystemConfig blacklistEnabled = systemConfigMapper.selectOne(wrapper);

        BlacklistConfigDTO blacklistConfig = new BlacklistConfigDTO();
        blacklistConfig.setEnabled("1".equals(blacklistEnabled != null ? blacklistEnabled.getConfigValue() : "0"));

        // 如果黑名单启用，获取黑名单列表
        if (blacklistConfig.getEnabled()) {
            List<WebIpBlacklist> blacklistItems = blacklistIpMapper.selectList(null);
            List<BlacklistItemDTO> itemDTOs = blacklistItems.stream()
                    .map(item -> BlacklistItemDTO.builder()
                            .id(item.getId())
                            .ipAddress(item.getIpAddress())
                            .reason(item.getReason())
                            .status(item.getStatus())
                            .banTime(item.getBanTime())
                            .expireTime(item.getExpireTime())
                            .build())
                    .collect(Collectors.toList());
            blacklistConfig.setList(itemDTOs);
        } else {
            blacklistConfig.setList(new ArrayList<>());
        }
        return blacklistConfig;
    }

    @Override
    public List<CarouselItemDTO> getCarouselConfig() {
        List<String> keys = Arrays.asList(ConfigKeyEnum.CAROUSEL_MALL.getKey());
        List<WebSystemConfig> configs = getConfigsByKeysWithoutMask(keys);
        if (configs == null || configs.isEmpty()) {
            return new ArrayList<>();
        }
        String json = configs.get(0).getConfigValue();
        if (StringUtils.isBlank(json)) {
            return new ArrayList<>();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, new TypeReference<List<CarouselItemDTO>>() {});
        } catch (Exception e) {
            log.warn("解析市集轮播图配置失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional
    public void updateCarouselConfig(List<CarouselItemDTO> list) {
        if (list == null) {
            list = new ArrayList<>();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(list);
            updateConfigValue(ConfigKeyEnum.CAROUSEL_MALL.getKey(), json);
        } catch (Exception e) {
            log.error("保存市集轮播图配置失败", e);
            throw new RuntimeException("保存市集轮播图配置失败", e);
        }
    }

    @Override
    public List<CarouselItemDTO> getWorkbenchCarouselConfig() {
        List<String> keys = Arrays.asList(ConfigKeyEnum.CAROUSEL_WORKBENCH.getKey());
        List<WebSystemConfig> configs = getConfigsByKeysWithoutMask(keys);
        if (configs == null || configs.isEmpty()) {
            return new ArrayList<>();
        }
        String json = configs.get(0).getConfigValue();
        if (StringUtils.isBlank(json)) {
            return new ArrayList<>();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, new TypeReference<List<CarouselItemDTO>>() {});
        } catch (Exception e) {
            log.warn("解析工作台轮播图配置失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional
    public void updateWorkbenchCarouselConfig(List<CarouselItemDTO> list) {
        if (list == null) {
            list = new ArrayList<>();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(list);
            updateConfigValue(ConfigKeyEnum.CAROUSEL_WORKBENCH.getKey(), json);
        } catch (Exception e) {
            log.error("保存工作台轮播图配置失败", e);
            throw new RuntimeException("保存工作台轮播图配置失败", e);
        }
    }

    @Override
    public List<AdminLoginPromoCircleDTO> getAdminLoginPromoConfig() {
        List<String> keys = Collections.singletonList(ConfigKeyEnum.ADMIN_LOGIN_PROMO.getKey());
        List<WebSystemConfig> configs = getConfigsByKeysWithoutMask(keys);
        if (configs == null || configs.isEmpty()) {
            return new ArrayList<>();
        }
        String json = configs.get(0).getConfigValue();
        if (StringUtils.isBlank(json)) {
            return new ArrayList<>();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, new TypeReference<List<AdminLoginPromoCircleDTO>>() {});
        } catch (Exception e) {
            log.warn("解析管理端登录页展示区配置失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional
    public void updateAdminLoginPromoConfig(List<AdminLoginPromoCircleDTO> list) {
        if (list == null) {
            list = new ArrayList<>();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(list);
            updateConfigValue(ConfigKeyEnum.ADMIN_LOGIN_PROMO.getKey(), json);
        } catch (Exception e) {
            log.error("保存管理端登录页展示区配置失败", e);
            throw new RuntimeException("保存管理端登录页展示区配置失败", e);
        }
    }

    // 通用的配置查询方法
    private List<WebSystemConfig> getConfigsByKeys(List<String> configKeys) {
        LambdaQueryWrapper<WebSystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(WebSystemConfig::getConfigKey, configKeys);
        List<WebSystemConfig> configs = systemConfigMapper.selectList(wrapper);
        // 对敏感信息进行脱敏处理
        return maskSensitiveConfigs(configs);
    }

    /**
     * 按 key 查询配置原始值（不脱敏）。JSON 型配置若走脱敏会破坏结构导致反序列化失败，登录页等会退回默认数据。
     */
    private List<WebSystemConfig> getConfigsByKeysWithoutMask(List<String> configKeys) {
        if (configKeys == null || configKeys.isEmpty()) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<WebSystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(WebSystemConfig::getConfigKey, configKeys);
        return systemConfigMapper.selectList(wrapper);
    }

    /**
     * 根据配置键获取配置列表（内部使用，不脱敏）
     */
    private List<WebSystemConfig> getConfigsByKeysInternal(List<String> configKeys) {
        LambdaQueryWrapper<WebSystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(WebSystemConfig::getConfigKey, configKeys);
        return systemConfigMapper.selectList(wrapper);
    }

    /**
     * 对敏感配置信息进行脱敏处理
     * @param configs 配置列表
     * @return 脱敏后的配置列表
     */
    private List<WebSystemConfig> maskSensitiveConfigs(List<WebSystemConfig> configs) {
        if (configs == null || configs.isEmpty()) {
            return configs;
        }
        
        for (WebSystemConfig config : configs) {
            // 如果是敏感字段，进行脱敏处理
            if (config.getIsSensitive() != null && config.getIsSensitive() && StringUtils.isNotEmpty(config.getConfigValue())) {
                config.setConfigValue(maskSensitiveValue(config.getConfigValue()));
            }
        }
        return configs;
    }

    /**
     * 脱敏处理：只显示前3位和后3位，中间用*代替
     * @param value 原始值
     * @return 脱敏后的值
     */
    private String maskSensitiveValue(String value) {
        if (StringUtils.isEmpty(value)) {
            return value;
        }
        
        int length = value.length();
        // 如果长度小于等于6，全部用*代替
        if (length <= 6) {
            return "******";
        }
        
        // 显示前3位和后3位，中间用*代替
        String prefix = value.substring(0, 3);
        String suffix = value.substring(length - 3);
        int maskLength = Math.min(length - 6, 10); // 最多显示10个*
        
        // Java 8 兼容的字符串重复实现
        StringBuilder mask = new StringBuilder();
        for (int i = 0; i < maskLength; i++) {
            mask.append("*");
        }
        
        return prefix + mask.toString() + suffix;
    }
    
    /**
     * 判断值是否为脱敏数据
     * 脱敏格式：前3位 + 中间用*代替 + 后3位，或者全部是*（如******）
     * 注意：由于maskSensitiveValue最多显示10个*，所以中间*的数量可能在1-10个之间
     * @param value 待检查的值
     * @return 如果是脱敏数据返回true，否则返回false
     */
    private boolean isMaskedValue(String value) {
        if (StringUtils.isEmpty(value)) {
            return false;
        }
        
        // 如果全部是*，肯定是脱敏数据
        if (value.matches("^\\*+$")) {
            return true;
        }
        
        // 检查是否符合脱敏格式：前3位 + 中间* + 后3位
        // 例如：eVa**********rJ 或 eVa**********J11
        // 模式：前3个字符 + 至少1个*（最多10个） + 后3个字符
        // 最小长度：3 + 1 + 3 = 7
        // 最大长度（当原始值长度>16时）：3 + 10 + 3 = 16
        if (value.length() >= 7) {
            String prefix = value.substring(0, 3);
            String suffix = value.substring(value.length() - 3);
            String middle = value.substring(3, value.length() - 3);
            
            // 如果中间部分全部是*（至少1个），且前后部分都不包含*，则认为是脱敏数据
            // 注意：middle可能包含1-10个*
            if (middle.matches("^\\*+$") && middle.length() >= 1 
                    && !prefix.contains("*") && !suffix.contains("*")) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 通过Feign调用hongshu-idle服务刷新支付宝配置
     */
    private void refreshAlipayConfig() {
        if (remoteIdleService == null) {
            log.warn("RemoteIdleService未注入，无法刷新支付宝配置，请确保hongshu-api-system模块已正确配置");
            return;
        }
        try {
            R<?> result = remoteIdleService.refreshAlipayConfig();
            if (result.getCode() == 200) {
                log.info("支付宝配置刷新成功");
            } else {
                log.warn("支付宝配置刷新失败: {}", result.getMsg());
            }
        } catch (Exception e) {
            log.error("调用hongshu-idle服务刷新支付宝配置失败", e);
        }
    }

    /**
     * 通过Feign调用hongshu-idle服务刷新微信支付配置
     */
    private void refreshWeChatPayConfig() {
        if (remoteIdleService == null) {
            log.warn("RemoteIdleService未注入，无法刷新微信支付配置，请确保hongshu-api-system模块已正确配置");
            return;
        }
        try {
            R<?> result = remoteIdleService.refreshWeChatPayConfig();
            if (result.getCode() == 200) {
                log.info("微信支付配置刷新成功");
            } else {
                log.warn("微信支付配置刷新失败: {}", result.getMsg());
            }
        } catch (Exception e) {
            log.error("调用hongshu-idle服务刷新微信支付配置失败", e);
        }
    }

    /**
     * 通过Feign调用hongshu-idle服务刷新所有支付配置
     */
    private void refreshPayConfig() {
        if (remoteIdleService == null) {
            log.warn("RemoteIdleService未注入，无法刷新支付配置，请确保hongshu-api-system模块已正确配置");
            return;
        }
        try {
            R<?> result = remoteIdleService.refreshPayConfig();
            if (result.getCode() == 200) {
                log.info("支付配置刷新成功");
            } else {
                log.warn("支付配置刷新失败: {}", result.getMsg());
            }
        } catch (Exception e) {
            log.error("调用hongshu-idle服务刷新支付配置失败", e);
        }
    }

    // 获取百度千帆配置（对外接口，脱敏）
    @Override
    public BaiduQianfanConfigDTO getBaiduQianfanConfig() {
        List<String> configKeys = ConfigKeyEnum.getBaiduQianfanConfigKeys();
        List<WebSystemConfig> configs = this.getConfigsByKeys(configKeys);

        BaiduQianfanConfigDTO baiduConfig = new BaiduQianfanConfigDTO();
        for (WebSystemConfig config : configs) {
            ConfigKeyEnum configKey = ConfigKeyEnum.fromKey(config.getConfigKey());
            if (configKey != null) {
                switch (configKey) {
                    case BAIDU_QIANFAN_ENABLED:
                        String enabledValue = config.getConfigValue();
                        boolean enabled = "1".equals(enabledValue) || "true".equalsIgnoreCase(enabledValue);
                        log.info("百度千帆开关配置 - 原始值: '{}', 解析结果: {}", enabledValue, enabled);
                        baiduConfig.setEnabled(enabled);
                        break;
                    case BAIDU_QIANFAN_ACCESS_KEY:
                        baiduConfig.setAccessKey(config.getConfigValue());
                        break;
                    case BAIDU_QIANFAN_SECRET_KEY:
                        baiduConfig.setSecretKey(config.getConfigValue());
                        break;
                    case BAIDU_QIANFAN_TEXT_ENDPOINT:
                        baiduConfig.setTextEndpoint(config.getConfigValue());
                        break;
                    case BAIDU_QIANFAN_IMAGE_ENDPOINT:
                        baiduConfig.setImageEndpoint(config.getConfigValue());
                        break;
                    case BAIDU_QIANFAN_VIDEO_ENDPOINT:
                        baiduConfig.setVideoEndpoint(config.getConfigValue());
                        break;
                    case AUDIT_MODE:
                        baiduConfig.setAuditMode(config.getConfigValue());
                        break;
                    case AUTO_AUDIT_THRESHOLD:
                        try {
                            baiduConfig.setAutoAuditThreshold(Double.parseDouble(config.getConfigValue()));
                        } catch (NumberFormatException e) {
                            baiduConfig.setAutoAuditThreshold(0.8); // 默认阈值
                        }
                        break;
                }
            }
        }

        // 设置默认值
        if (baiduConfig.getEnabled() == null) {
            baiduConfig.setEnabled(false);
        }
        if (baiduConfig.getAuditMode() == null) {
            baiduConfig.setAuditMode("manual");
        }
        if (baiduConfig.getAutoAuditThreshold() == null) {
            baiduConfig.setAutoAuditThreshold(0.8);
        }

        return baiduConfig;
    }

    // 获取百度千帆配置（内部使用，不脱敏）
    @Override
    public BaiduQianfanConfigDTO getBaiduQianfanConfigInternal() {
        List<String> configKeys = ConfigKeyEnum.getBaiduQianfanConfigKeys();
        List<WebSystemConfig> configs = this.getConfigsByKeysInternal(configKeys);

        BaiduQianfanConfigDTO baiduConfig = new BaiduQianfanConfigDTO();
        for (WebSystemConfig config : configs) {
            ConfigKeyEnum configKey = ConfigKeyEnum.fromKey(config.getConfigKey());
            if (configKey != null) {
                switch (configKey) {
                    case BAIDU_QIANFAN_ENABLED:
                        String enabledValue = config.getConfigValue();
                        boolean enabled = "1".equals(enabledValue) || "true".equalsIgnoreCase(enabledValue);
                        baiduConfig.setEnabled(enabled);
                        break;
                    case BAIDU_QIANFAN_ACCESS_KEY:
                        baiduConfig.setAccessKey(config.getConfigValue());
                        break;
                    case BAIDU_QIANFAN_SECRET_KEY:
                        baiduConfig.setSecretKey(config.getConfigValue());
                        break;
                    case BAIDU_QIANFAN_TEXT_ENDPOINT:
                        baiduConfig.setTextEndpoint(config.getConfigValue());
                        break;
                    case BAIDU_QIANFAN_IMAGE_ENDPOINT:
                        baiduConfig.setImageEndpoint(config.getConfigValue());
                        break;
                    case BAIDU_QIANFAN_VIDEO_ENDPOINT:
                        baiduConfig.setVideoEndpoint(config.getConfigValue());
                        break;
                    case AUDIT_MODE:
                        baiduConfig.setAuditMode(config.getConfigValue());
                        break;
                    case AUTO_AUDIT_THRESHOLD:
                        try {
                            baiduConfig.setAutoAuditThreshold(Double.parseDouble(config.getConfigValue()));
                        } catch (NumberFormatException e) {
                            baiduConfig.setAutoAuditThreshold(0.8); // 默认阈值
                        }
                        break;
                }
            }
        }

        // 设置默认值
        if (baiduConfig.getEnabled() == null) {
            baiduConfig.setEnabled(false);
        }
        if (baiduConfig.getAuditMode() == null) {
            baiduConfig.setAuditMode("manual");
        }
        if (baiduConfig.getAutoAuditThreshold() == null) {
            baiduConfig.setAutoAuditThreshold(0.8);
        }

        return baiduConfig;
    }
}
