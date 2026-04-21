package com.hongshu.idle.service.sys.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hongshu.common.core.enums.ConfigKeyEnum;
import com.hongshu.idle.domain.dto.AlipayConfigDTO;
import com.hongshu.idle.domain.dto.PaymentGlobalConfigDTO;
import com.hongshu.idle.domain.dto.SystemConfigDTO;
import com.hongshu.idle.domain.dto.WeChatPayConfigDTO;
import com.hongshu.idle.domain.entity.WebAlipayConfig;
import com.hongshu.idle.domain.entity.WebSystemConfig;
import com.hongshu.idle.domain.entity.WebWeChatPayConfig;
import com.hongshu.idle.mapper.sys.SysAlipayConfigMapper;
import com.hongshu.idle.mapper.sys.SysSystemConfigMapper;
import com.hongshu.idle.mapper.sys.SysWeChatPayConfigMapper;
import com.hongshu.idle.service.sys.ISysSystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


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
    private SysAlipayConfigMapper alipayConfigMapper;
    @Autowired
    private SysWeChatPayConfigMapper weChatPayConfigMapper;

    @Value("${payment.timeout.minutes:15}")
    private int paymentTimeoutMinutes;

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
            String key = config.getConfigKey();
            if (key == null) continue;
            if (ConfigKeyEnum.OSS_TYPE.getKey().equals(key)) {
                systemConfig.setOssType(config.getConfigValue());
            } else if (ConfigKeyEnum.SMS_PRIMARY.getKey().equals(key)) {
                systemConfig.setSmsPrimary(config.getConfigValue());
            } else if (ConfigKeyEnum.MQ_TYPE.getKey().equals(key)) {
                systemConfig.setMqType(config.getConfigValue());
            } else if (ConfigKeyEnum.QUERY_PRIMARY.getKey().equals(key)) {
                systemConfig.setQueryPrimary(config.getConfigValue());
            } else if (ConfigKeyEnum.AMAP_ENABLED.getKey().equals(key)) {
                systemConfig.setAmapEnabled(config.getConfigValue());
            } else if (ConfigKeyEnum.BLACKLIST_ENABLED.getKey().equals(key)) {
                systemConfig.setBlacklistEnabled("1".equals(config.getConfigValue()));
            } else if (ConfigKeyEnum.CONTENT_AUDIT_ENABLED.getKey().equals(key)) {
                systemConfig.setContentAuditEnabled("1".equals(config.getConfigValue()));
            } else if (ConfigKeyEnum.PRODUCT_AUDIT_ENABLED.getKey().equals(key)) {
                systemConfig.setProductAuditEnabled("1".equals(config.getConfigValue()));
            } else if (ConfigKeyEnum.USER_AUDIT_ENABLED.getKey().equals(key)) {
                systemConfig.setUserAuditEnabled("1".equals(config.getConfigValue()));
            } else if (ConfigKeyEnum.COMMENT_AUDIT_ENABLED.getKey().equals(key)) {
                systemConfig.setCommentAuditEnabled("1".equals(config.getConfigValue()));
            } else if (ConfigKeyEnum.BAIDU_QIANFAN_ENABLED.getKey().equals(key)) {
                systemConfig.setBaiduQianfanEnabled("1".equals(config.getConfigValue()));
            } else if (ConfigKeyEnum.BAIDU_QIANFAN_ACCESS_KEY.getKey().equals(key)) {
                systemConfig.setBaiduQianfanAccessKey(config.getConfigValue());
            } else if (ConfigKeyEnum.BAIDU_QIANFAN_SECRET_KEY.getKey().equals(key)) {
                systemConfig.setBaiduQianfanSecretKey(config.getConfigValue());
            } else if (ConfigKeyEnum.BAIDU_QIANFAN_TEXT_ENDPOINT.getKey().equals(key)) {
                systemConfig.setBaiduQianfanTextEndpoint(config.getConfigValue());
            } else if (ConfigKeyEnum.BAIDU_QIANFAN_IMAGE_ENDPOINT.getKey().equals(key)) {
                systemConfig.setBaiduQianfanImageEndpoint(config.getConfigValue());
            } else if (ConfigKeyEnum.BAIDU_QIANFAN_VIDEO_ENDPOINT.getKey().equals(key)) {
                systemConfig.setBaiduQianfanVideoEndpoint(config.getConfigValue());
            } else if (ConfigKeyEnum.AUDIT_MODE.getKey().equals(key)) {
                systemConfig.setAuditMode(config.getConfigValue());
            } else if (ConfigKeyEnum.AUTO_AUDIT_THRESHOLD.getKey().equals(key)) {
                try {
                    systemConfig.setAutoAuditThreshold(Double.parseDouble(config.getConfigValue()));
                } catch (Exception ignore) {
                    systemConfig.setAutoAuditThreshold(0.8);
                }
            }
        }
        
        // 设置默认值
        if (systemConfig.getBaiduQianfanEnabled() == null) {
            systemConfig.setBaiduQianfanEnabled(false);
        }
        if (systemConfig.getAuditMode() == null) {
            systemConfig.setAuditMode("manual");
        }
        if (systemConfig.getAutoAuditThreshold() == null) {
            systemConfig.setAutoAuditThreshold(0.8);
        }
        
        cachedSystemConfig = systemConfig;
        cachedSystemConfigTimestamp = System.currentTimeMillis();
        return systemConfig;
    }

    // 获取支付宝配置
    public AlipayConfigDTO getAlipayConfig() {
        LambdaQueryWrapper<WebAlipayConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.last("LIMIT 1");
        WebAlipayConfig config = alipayConfigMapper.selectOne(wrapper);

        if (config != null) {
            return AlipayConfigDTO.builder()
                    .pid(config.getPid())
                    .appId(config.getAppId())
                    .merchantPrivateKey(config.getMerchantPrivateKey())
                    .alipayPublicKey(config.getAlipayPublicKey())
                    .notifyUrl(config.getNotifyUrl())
                    .returnUrl(config.getReturnUrl())
                    .signType(config.getSignType())
                    .charset(config.getCharset())
                    .gatewayUrl(config.getGatewayUrl())
                    .isEnabled(config.getIsEnabled())
                    .sandboxEnabled(config.getSandboxEnabled())
                    .qrcodeEnabled(config.getQrcodeEnabled())
                    .build();
        }
        return new AlipayConfigDTO();
    }

    // 获取支付宝配置（内部使用，不脱敏）- idle模块本身就没有脱敏，直接调用原方法
    @Override
    public AlipayConfigDTO getAlipayConfigInternal() {
        return getAlipayConfig();
    }

    // 获取微信支付配置
    public WeChatPayConfigDTO getWeChatPayConfig() {
        LambdaQueryWrapper<WebWeChatPayConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.last("LIMIT 1");
        WebWeChatPayConfig config = weChatPayConfigMapper.selectOne(wrapper);

        if (config != null) {
            return WeChatPayConfigDTO.builder()
                    .mchId(config.getMchId())
                    .pcAppId(config.getPcAppId())
                    .appAppId(config.getAppAppId())
                    .appId(config.getAppId())
                    .apiKey(config.getApiKey())
                    .apiV2Key(config.getApiV2Key())
                    .apiV3Key(config.getApiV3Key())
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

    // 获取微信支付配置（内部使用，不脱敏）- idle模块本身就没有脱敏，直接调用原方法
    @Override
    public WeChatPayConfigDTO getWeChatPayConfigInternal() {
        return getWeChatPayConfig();
    }

    // 更新微信支付配置
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
        config.setApiKey(weChatPayConfig.getApiKey());
        config.setApiV2Key(weChatPayConfig.getApiV2Key());
        config.setApiV3Key(weChatPayConfig.getApiV3Key());
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

        // 刷新配置
        if (weChatPayConfig.getIsEnabled() != null && weChatPayConfig.getIsEnabled()) {
            log.info("微信支付配置已更新，建议重启服务以使配置生效");
        }
    }

    // 更新支付宝配置
    @Transactional
    @Override
    public void updateAlipayConfig(AlipayConfigDTO alipayConfig) {
        LambdaQueryWrapper<WebAlipayConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.last("LIMIT 1");
        WebAlipayConfig config = alipayConfigMapper.selectOne(wrapper);

        if (config == null) {
            config = new WebAlipayConfig();
        }

        config.setPid(alipayConfig.getPid());
        config.setAppId(alipayConfig.getAppId());
        config.setMerchantPrivateKey(alipayConfig.getMerchantPrivateKey());
        config.setAlipayPublicKey(alipayConfig.getAlipayPublicKey());
        config.setNotifyUrl(alipayConfig.getNotifyUrl());
        config.setReturnUrl(alipayConfig.getReturnUrl());
        config.setSignType(alipayConfig.getSignType());
        config.setCharset(alipayConfig.getCharset());
        config.setGatewayUrl(alipayConfig.getGatewayUrl());
        config.setIsEnabled(alipayConfig.getIsEnabled());
        config.setSandboxEnabled(alipayConfig.getSandboxEnabled());
        config.setQrcodeEnabled(alipayConfig.getQrcodeEnabled());
        config.setUpdateTime(new Date());

        if (config.getId() == null) {
            config.setCreateTime(new Date());
            alipayConfigMapper.insert(config);
        } else {
            alipayConfigMapper.updateById(config);
        }

        // 刷新配置
        if (alipayConfig.getIsEnabled() != null && alipayConfig.getIsEnabled()) {
            log.info("支付宝配置已更新，建议重启服务以使配置生效");
        }
    }

    // 获取支付全局配置
    @Override
    public PaymentGlobalConfigDTO getPaymentGlobalConfig() {
        PaymentGlobalConfigDTO config = new PaymentGlobalConfigDTO();
        
        // 从系统配置表中获取全局支付开关
        LambdaQueryWrapper<WebSystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WebSystemConfig::getConfigKey, "payment_enabled");
        WebSystemConfig paymentEnabledConfig = systemConfigMapper.selectOne(wrapper);
        config.setPaymentEnabled(paymentEnabledConfig != null && "1".equals(paymentEnabledConfig.getConfigValue()));
        
        // 从支付宝配置中获取支付方式开关
        AlipayConfigDTO alipayConfig = getAlipayConfig();
        config.setAlipaySandboxEnabled(alipayConfig.getSandboxEnabled() != null ? alipayConfig.getSandboxEnabled() : false);
        config.setAlipayQrcodeEnabled(alipayConfig.getQrcodeEnabled() != null ? alipayConfig.getQrcodeEnabled() : false);
        
        // 从微信支付配置中获取支付方式开关
        WeChatPayConfigDTO wechatConfig = getWeChatPayConfig();
        config.setWechatQrcodeEnabled(wechatConfig.getIsEnabled() != null ? wechatConfig.getIsEnabled() : false);
        
        // 从系统配置表中获取微信小程序支付开关
        LambdaQueryWrapper<WebSystemConfig> miniprogramWrapper = new LambdaQueryWrapper<>();
        miniprogramWrapper.eq(WebSystemConfig::getConfigKey, "wechat_miniprogram_enabled");
        WebSystemConfig miniprogramConfig = systemConfigMapper.selectOne(miniprogramWrapper);
        config.setWechatMiniprogramEnabled(miniprogramConfig != null && "1".equals(miniprogramConfig.getConfigValue()));
        config.setPaymentTimeoutMinutes(paymentTimeoutMinutes);

        return config;
    }

    // 更新支付全局配置
    @Transactional
    @Override
    public void updatePaymentGlobalConfig(PaymentGlobalConfigDTO paymentGlobalConfig) {
        // 更新全局支付开关
        LambdaQueryWrapper<WebSystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WebSystemConfig::getConfigKey, "payment_enabled");
        WebSystemConfig paymentEnabledConfig = systemConfigMapper.selectOne(wrapper);
        
        if (paymentEnabledConfig == null) {
            paymentEnabledConfig = new WebSystemConfig();
            paymentEnabledConfig.setConfigKey("payment_enabled");
            paymentEnabledConfig.setConfigGroup("payment");
            paymentEnabledConfig.setRemark("全局支付开关");
            paymentEnabledConfig.setIsSensitive(false);
            paymentEnabledConfig.setCreateTime(new Date());
        }
        
        paymentEnabledConfig.setConfigValue(paymentGlobalConfig.getPaymentEnabled() ? "1" : "0");
        paymentEnabledConfig.setUpdateTime(new Date());
        
        if (paymentEnabledConfig.getId() == null) {
            systemConfigMapper.insert(paymentEnabledConfig);
        } else {
            systemConfigMapper.updateById(paymentEnabledConfig);
        }
        
        // 更新支付宝支付方式开关
        LambdaQueryWrapper<WebAlipayConfig> alipayWrapper = new LambdaQueryWrapper<>();
        alipayWrapper.last("LIMIT 1");
        WebAlipayConfig alipayConfig = alipayConfigMapper.selectOne(alipayWrapper);
        
        if (alipayConfig != null) {
            alipayConfig.setSandboxEnabled(paymentGlobalConfig.getAlipaySandboxEnabled());
            alipayConfig.setQrcodeEnabled(paymentGlobalConfig.getAlipayQrcodeEnabled());
            alipayConfig.setUpdateTime(new Date());
            alipayConfigMapper.updateById(alipayConfig);
        }
        
        // 更新微信支付方式开关
        LambdaQueryWrapper<WebWeChatPayConfig> wechatWrapper = new LambdaQueryWrapper<>();
        wechatWrapper.last("LIMIT 1");
        WebWeChatPayConfig wechatConfig = weChatPayConfigMapper.selectOne(wechatWrapper);
        
        if (wechatConfig != null) {
            wechatConfig.setIsEnabled(paymentGlobalConfig.getWechatQrcodeEnabled());
            wechatConfig.setUpdateTime(new Date());
            weChatPayConfigMapper.updateById(wechatConfig);
        }
        
        // 更新微信小程序支付开关
        LambdaQueryWrapper<WebSystemConfig> miniprogramWrapper = new LambdaQueryWrapper<>();
        miniprogramWrapper.eq(WebSystemConfig::getConfigKey, "wechat_miniprogram_enabled");
        WebSystemConfig miniprogramConfig = systemConfigMapper.selectOne(miniprogramWrapper);
        
        if (miniprogramConfig == null) {
            miniprogramConfig = new WebSystemConfig();
            miniprogramConfig.setConfigKey("wechat_miniprogram_enabled");
            miniprogramConfig.setConfigGroup("payment");
            miniprogramConfig.setRemark("微信小程序支付开关");
            miniprogramConfig.setIsSensitive(false);
            miniprogramConfig.setCreateTime(new Date());
        }
        
        miniprogramConfig.setConfigValue(paymentGlobalConfig.getWechatMiniprogramEnabled() != null && paymentGlobalConfig.getWechatMiniprogramEnabled() ? "1" : "0");
        miniprogramConfig.setUpdateTime(new Date());
        
        if (miniprogramConfig.getId() == null) {
            systemConfigMapper.insert(miniprogramConfig);
        } else {
            systemConfigMapper.updateById(miniprogramConfig);
        }
        
        log.info("支付全局配置已更新");
    }

    // 通用的配置查询方法
    private List<WebSystemConfig> getConfigsByKeys(List<String> configKeys) {
        LambdaQueryWrapper<WebSystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(WebSystemConfig::getConfigKey, configKeys);
        return systemConfigMapper.selectList(wrapper);
    }
}
