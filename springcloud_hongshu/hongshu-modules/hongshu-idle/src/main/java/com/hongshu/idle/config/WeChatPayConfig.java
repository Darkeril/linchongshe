package com.hongshu.idle.config;

import com.hongshu.idle.domain.dto.WeChatPayConfigDTO;
import com.hongshu.idle.service.sys.ISysSystemConfigService;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 微信支付配置类
 *
 * @author: hongshu
 */
@Configuration
@Slf4j
@Data
public class WeChatPayConfig {

    @Autowired
    private ISysSystemConfigService systemConfigService;

    private String mchId;
    private String pcAppId;
    private String appAppId;
    private String appId;
    private String apiKey;
    private String apiV2Key;
    private String apiV3Key;
    private String certSerialNo;
    private String privateKeyPath;
    private String wechatPayCertificate;
    private String notifyUrl;
    private String domain;
    private Boolean isEnabled;

    // 微信支付HTTP客户端
    private volatile OkHttpClient wechatPayHttpClient;
    // 使用读写锁保证线程安全
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @PostConstruct
    public void loadConfigFromDatabase() {
        try {
            // 从数据库加载微信支付配置
            this.loadWeChatPayConfigs();
            // 初始化微信支付客户端（如果启用）
            if (isEnabled != null && isEnabled && wechatPayHttpClient == null) {
                createWeChatPayHttpClient();
            }
        } catch (Exception e) {
            log.warn("从数据库加载微信支付配置失败（表可能不存在）: {}", e.getMessage());
            // 表不存在时不抛出异常，允许服务正常启动
        }
    }

    private void loadWeChatPayConfigs() {
        // 从数据库获取微信支付配置
        WeChatPayConfigDTO weChatPayConfig = systemConfigService.getWeChatPayConfig();

        if (weChatPayConfig != null) {
            lock.writeLock().lock();
            try {
                this.mchId = weChatPayConfig.getMchId();
                this.pcAppId = weChatPayConfig.getPcAppId();
                this.appAppId = weChatPayConfig.getAppAppId();
                this.appId = weChatPayConfig.getAppId();
                this.apiKey = weChatPayConfig.getApiKey();
                this.apiV2Key = weChatPayConfig.getApiV2Key();
                this.apiV3Key = weChatPayConfig.getApiV3Key();
                this.certSerialNo = weChatPayConfig.getCertSerialNo();
                this.privateKeyPath = weChatPayConfig.getPrivateKeyPath();
                this.wechatPayCertificate = weChatPayConfig.getWechatPayCertificate();
                this.notifyUrl = weChatPayConfig.getNotifyUrl();
                this.domain = weChatPayConfig.getDomain();
                this.isEnabled = weChatPayConfig.getIsEnabled();

                // 如果配置启用，重新创建微信支付客户端
                if (isEnabled != null && isEnabled) {
                    createWeChatPayHttpClient();
                    log.info("微信支付配置已从数据库加载完成，WeChatPayHttpClient已重新创建");
                } else {
                    this.wechatPayHttpClient = null;
                    log.info("微信支付配置已从数据库加载完成，但微信支付未启用");
                }
            } finally {
                lock.writeLock().unlock();
            }
        } else {
            log.warn("未找到微信支付配置");
        }
    }

    /**
     * 创建微信支付HTTP客户端
     * 注意：这里创建的是基础的OkHttpClient，实际的签名和验证需要在业务代码中实现
     * 如果需要使用微信支付SDK，请根据实际的SDK包名和API进行调整
     */
    private void createWeChatPayHttpClient() {
        if (mchId == null || apiV3Key == null) {
            log.warn("微信支付配置不完整，无法创建WeChatPayHttpClient");
            this.wechatPayHttpClient = null;
            return;
        }

        try {
            // 创建基础的OkHttpClient
            // 注意：微信支付APIv3需要签名和验证，这里只创建基础客户端
            // 实际的签名需要在调用API时使用商户私钥和证书序列号进行签名
            this.wechatPayHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();
            log.info("WeChatPayHttpClient创建成功（基础版，需在业务代码中实现签名逻辑）");
            log.warn("微信支付SDK集成需要完善：需要在调用API时实现签名和验证逻辑");
        } catch (Exception e) {
            log.error("创建WeChatPayHttpClient失败", e);
            this.wechatPayHttpClient = null;
        }
    }

    /**
     * 刷新配置：从数据库重新加载配置并重新创建WeChatPayHttpClient
     */
    public void refreshConfig() {
        log.info("开始刷新微信支付配置...");
        loadConfigFromDatabase();
        log.info("微信支付配置刷新完成");
    }

    /**
     * 获取WeChatPayHttpClient实例（线程安全）
     *
     * @return WeChatPayHttpClient实例
     */
    public OkHttpClient getWeChatPayHttpClient() {
        lock.readLock().lock();
        try {
            if (wechatPayHttpClient == null) {
                if (isEnabled != null && isEnabled) {
                    log.warn("WeChatPayHttpClient未初始化，但配置已启用，尝试创建...");
                    lock.readLock().unlock();
                    lock.writeLock().lock();
                    try {
                        // 双重检查
                        if (wechatPayHttpClient == null) {
                            createWeChatPayHttpClient();
                        }
                    } finally {
                        lock.writeLock().unlock();
                        lock.readLock().lock();
                    }
                } else {
                    throw new IllegalStateException("微信支付未启用，WeChatPayHttpClient不可用！");
                }
            }
            return wechatPayHttpClient;
        } finally {
            lock.readLock().unlock();
        }
    }

    // 获取配置信息的方法（用于调试或验证）
    public Map<String, String> getConfigInfo() {
        Map<String, String> configInfo = new HashMap<>();
        configInfo.put("mchId", mchId);
        configInfo.put("appId", appId);
        configInfo.put("notifyUrl", notifyUrl);
        configInfo.put("certSerialNo", certSerialNo);
        configInfo.put("domain", domain);
        configInfo.put("isEnabled", String.valueOf(isEnabled));
        // 不包含敏感信息如密钥
        return configInfo;
    }
}

