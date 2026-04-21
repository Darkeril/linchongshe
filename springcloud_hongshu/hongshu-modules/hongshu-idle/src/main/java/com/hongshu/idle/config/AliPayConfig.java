package com.hongshu.idle.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.hongshu.idle.domain.dto.AlipayConfigDTO;
import com.hongshu.idle.service.sys.ISysSystemConfigService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**

 */
//@Configuration
//@Slf4j
//@Data
//public class AliPayConfig {
//
//    /**
//     * pid
//     */
//    @Value("${alipay.pid}")
//    String pid;
//
//    /**
//     * AppId
//     */
//    @Value("${alipay.appId}")
//    String appId;
//
//    /**
//     * 应用私钥
//     */
//    @Value("${alipay.merchantPrivateKey}")
//    String merchantPrivateKey;
//
//    /**
//     * 应用公钥
//     */
//    @Value("${alipay.alipayPublicKey}")
//    String alipayPublicKey;
//
//    /**
//     * 回调地址
//     */
//    @Value("${alipay.notifyUrl}")
//    String notifyUrl;
//
//    /**
//     * 返回地址
//     */
//    @Value("${alipay.returnUrl}")
//    String returnUrl;
//
//    /**
//     * 签名方式
//     */
//    @Value("${alipay.signType}")
//    String signType;
//
//    /**
//     * 字符编码格式
//     */
//    @Value("${alipay.charset}")
//    String charset;
//
//    /**
//     * 支付宝网关地址
//     */
//    @Value("${alipay.gatewayUrl}")
//    String gatewayUrl;
//
//
//    @Bean
//    public AlipayClient alipayClient() {
//        return new DefaultAlipayClient(
//                gatewayUrl,
//                appId,
//                merchantPrivateKey,
//                "json",
//                charset,
//                alipayPublicKey,
//                signType
//        );
//    }
//}

@Configuration
@Slf4j
@Data
public class AliPayConfig {

    @Autowired
    private ISysSystemConfigService systemConfigService;

    private String pid;
    private String appId;
    private String merchantPrivateKey;
    private String alipayPublicKey;
    private String notifyUrl;
    private String returnUrl;
    private String signType;
    private String charset;
    private String gatewayUrl;
    private Boolean isEnabled;
    private Boolean sandboxEnabled;  // 是否启用沙箱支付
    private Boolean qrcodeEnabled;   // 是否启用扫码支付

    // 使用volatile确保多线程可见性
    private volatile AlipayClient alipayClient;
    // 使用读写锁保证线程安全
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();


    @PostConstruct
    public void loadConfigFromDatabase() {
        try {
            // 从数据库加载支付宝配置
            this.loadAlipayConfigs();
            // 初始化时创建AlipayClient（如果启用）
            // 检查是否启用：isEnabled为true 或者 sandboxEnabled/qrcodeEnabled任一为true
            boolean anyPaymentEnabled = (isEnabled != null && isEnabled) || 
                                      (sandboxEnabled != null && sandboxEnabled) || 
                                      (qrcodeEnabled != null && qrcodeEnabled);
            if (anyPaymentEnabled && alipayClient == null) {
                createAlipayClient();
            }
        } catch (Exception e) {
            log.error("从数据库加载支付宝配置失败", e);
            // 如果数据库加载失败，使用默认配置
//            this.loadDefaultConfigs();
        }
    }

    private void loadAlipayConfigs() {
        // 使用getAlipayConfigInternal方法获取配置（不脱敏）
        AlipayConfigDTO alipayConfig = systemConfigService.getAlipayConfigInternal();

        if (alipayConfig != null) {
            lock.writeLock().lock();
            try {
            this.pid = alipayConfig.getPid();
            this.appId = alipayConfig.getAppId();
            this.merchantPrivateKey = alipayConfig.getMerchantPrivateKey();
            this.alipayPublicKey = alipayConfig.getAlipayPublicKey();
            this.notifyUrl = alipayConfig.getNotifyUrl();
            this.returnUrl = alipayConfig.getReturnUrl();
            this.signType = alipayConfig.getSignType();
            this.charset = alipayConfig.getCharset();
            this.gatewayUrl = alipayConfig.getGatewayUrl();
            this.isEnabled = alipayConfig.getIsEnabled();
            this.sandboxEnabled = alipayConfig.getSandboxEnabled();
            this.qrcodeEnabled = alipayConfig.getQrcodeEnabled();

                // 如果配置启用，重新创建AlipayClient
                // 检查是否启用：isEnabled为true 或者 sandboxEnabled/qrcodeEnabled任一为true
                boolean anyPaymentEnabled = (isEnabled != null && isEnabled) || 
                                          (sandboxEnabled != null && sandboxEnabled) || 
                                          (qrcodeEnabled != null && qrcodeEnabled);
                if (anyPaymentEnabled) {
                    createAlipayClient();
                    log.info("支付宝配置已从数据库加载完成，AlipayClient已重新创建");
                } else {
                    this.alipayClient = null;
                    log.info("支付宝配置已从数据库加载完成，但支付宝未启用");
                }
            } finally {
                lock.writeLock().unlock();
            }
        } else {
            log.warn("未找到支付宝配置，使用默认配置");
//            this.loadDefaultConfigs();
        }
    }

    /**
     * 创建AlipayClient实例
     */
    private void createAlipayClient() {
        if (gatewayUrl == null || appId == null || merchantPrivateKey == null
                || alipayPublicKey == null || charset == null || signType == null) {
            log.warn("支付宝配置不完整，无法创建AlipayClient");
            this.alipayClient = null;
            return;
        }

        try {
            this.alipayClient = new DefaultAlipayClient(
                    gatewayUrl,
                    appId,
                    merchantPrivateKey,
                    "json",
                    charset,
                    alipayPublicKey,
                    signType
            );
            log.info("AlipayClient创建成功");
        } catch (Exception e) {
            log.error("创建AlipayClient失败", e);
            this.alipayClient = null;
        }
    }

//    private void loadDefaultConfigs() {
//        // 从环境变量或默认配置加载
//        Environment env = SpringContextUtils.getBean(Environment.class);
//
//        this.pid = env.getProperty("alipay.pid", "");
//        this.appId = env.getProperty("alipay.appId", "");
//        this.merchantPrivateKey = env.getProperty("alipay.merchantPrivateKey", "");
//        this.alipayPublicKey = env.getProperty("alipay.alipayPublicKey", "");
//        this.notifyUrl = env.getProperty("alipay.notifyUrl", "");
//        this.returnUrl = env.getProperty("alipay.returnUrl", "");
//        this.signType = env.getProperty("alipay.signType", "RSA2");
//        this.charset = env.getProperty("alipay.charset", "UTF-8");
//        this.gatewayUrl = env.getProperty("alipay.gatewayUrl", "https://openapi.alipaydev.com/gateway.do");
//        this.isEnabled = false;
//
//        log.warn("使用默认配置加载支付宝服务");
//    }

    /**
     * 刷新配置：从数据库重新加载配置并重新创建AlipayClient
     */
    public void refreshConfig() {
        log.info("开始刷新支付宝配置...");
        loadConfigFromDatabase();
        log.info("支付宝配置刷新完成");
    }

    /**
     * 获取AlipayClient实例（线程安全）
     * @return AlipayClient实例
     */
    public AlipayClient getAlipayClient() {
        lock.readLock().lock();
        try {
            if (alipayClient == null) {
                // 检查是否启用：isEnabled为true 或者 sandboxEnabled/qrcodeEnabled任一为true
                boolean anyPaymentEnabled = (isEnabled != null && isEnabled) || 
                                          (sandboxEnabled != null && sandboxEnabled) || 
                                          (qrcodeEnabled != null && qrcodeEnabled);
                
                if (anyPaymentEnabled) {
                    log.warn("AlipayClient未初始化，但支付宝配置已启用，尝试创建...");
                    lock.readLock().unlock();
                    lock.writeLock().lock();
                    try {
                        // 双重检查
                        if (alipayClient == null) {
                            createAlipayClient();
                        }
                    } finally {
                        lock.writeLock().unlock();
                        lock.readLock().lock();
                    }
                } else {
                    throw new IllegalStateException("支付宝未启用，AlipayClient不可用！请在管理端开启支付宝配置");
                }
            }
            return alipayClient;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 创建AlipayClient Bean
     * 注意：@Bean方法在配置类实例化时执行，此时@PostConstruct可能还没执行
     * 为了确保配置已加载，这里在Bean创建时也尝试加载配置
     *
     * 重要提示：
     * 1. 如果通过@Autowired注入AlipayClient，获取的是Bean创建时的实例
     * 2. 配置刷新后，需要通过AliPayConfig.getAlipayClient()获取最新实例
     * 3. 建议修改使用AlipayClient的代码，改为注入AliPayConfig，然后使用getAlipayClient()
     */
    @Bean
    public AlipayClient alipayClient() {
        // 在Bean创建时也尝试加载配置（如果@PostConstruct还没执行）
        try {
            if (systemConfigService != null) {
                loadConfigFromDatabase();
            }
        } catch (Exception e) {
            log.warn("Bean创建时加载配置失败，将在@PostConstruct中重试", e);
        }

        lock.readLock().lock();
        try {
            // 如果已经创建，直接返回
            if (alipayClient != null) {
                return alipayClient;
            }
            // 如果配置已加载且启用，尝试创建
            // 检查是否启用：isEnabled为true 或者 sandboxEnabled/qrcodeEnabled任一为true
            boolean anyPaymentEnabled = (isEnabled != null && isEnabled) || 
                                      (sandboxEnabled != null && sandboxEnabled) || 
                                      (qrcodeEnabled != null && qrcodeEnabled);
            if (anyPaymentEnabled && gatewayUrl != null && appId != null) {
                lock.readLock().unlock();
                lock.writeLock().lock();
                try {
                    if (alipayClient == null) {
                        createAlipayClient();
                    }
                    return alipayClient;
                } finally {
                    lock.writeLock().unlock();
                    lock.readLock().lock();
                }
            }
            // 配置未启用或未加载，创建一个默认的AlipayClient（会在使用时报错，但不会导致Bean创建失败）
            log.warn("AlipayClient Bean创建时配置未就绪或未启用，将返回占位对象");
            // 返回一个使用默认配置的AlipayClient，实际使用时应该通过getAlipayClient()获取
            return new DefaultAlipayClient(
                    "https://openapi.alipaydev.com/gateway.do",
                    "",
                    "",
                    "json",
                    "UTF-8",
                    "",
                    "RSA2"
            );
        } finally {
            lock.readLock().unlock();
        }
    }

    // 获取配置信息的方法（用于调试或验证）
    public Map<String, String> getConfigInfo() {
        Map<String, String> configInfo = new HashMap<>();
        configInfo.put("pid", pid);
        configInfo.put("appId", appId);
        configInfo.put("notifyUrl", notifyUrl);
        configInfo.put("returnUrl", returnUrl);
        configInfo.put("signType", signType);
        configInfo.put("charset", charset);
        configInfo.put("gatewayUrl", gatewayUrl);
        configInfo.put("isEnabled", String.valueOf(isEnabled));
        // 不包含敏感信息如私钥和公钥
        return configInfo;
    }
}
