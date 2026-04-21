package com.hongshu.web.service.sys;

import com.hongshu.common.audit.dto.BaiduQianfanConfigDTO;
import com.hongshu.web.domain.dto.*;

import java.util.List;
import java.util.Map;

/**
 * 系统配置表 服务类
 *
 * @author: hongshu
 */
public interface ISysSystemConfigService {

    ConfigDataDTO getAllConfigData();

    WebsiteConfigDTO getWebsiteConfig();

    SystemConfigDTO getSystemConfig();

    LocalConfigDTO getLocalConfig();

    Map<String, OssConfigDTO> getOssConfig();

    /**
     * 获取OSS配置（内部使用，不脱敏）
     * 用于文件服务等需要真实密钥的场景
     */
    Map<String, OssConfigDTO> getOssConfigInternal();

    OssConfigDTO getOssConfig(String provider);

    Map<String, SmsConfigDTO> getSmsConfig();

    /**
     * 获取短信配置（内部使用，不脱敏）
     */
    Map<String, SmsConfigDTO> getSmsConfigInternal();

    AlipayConfigDTO getAlipayConfig();

    /**
     * 获取支付宝配置（内部使用，不脱敏）
     */
    AlipayConfigDTO getAlipayConfigInternal();

    WeChatPayConfigDTO getWeChatPayConfig();

    /**
     * 获取微信支付配置（内部使用，不脱敏）
     */
    WeChatPayConfigDTO getWeChatPayConfigInternal();

    WeChatLoginConfigDTO getWeChatLoginConfig();

    WeChatLoginConfigDTO getImWeChatLoginConfig();

    void updateImWeChatLoginConfig(WeChatLoginConfigDTO config);

    CaptchaConfigDTO getCaptchaConfig();

    AmapConfigDTO getAmapConfig();

    DemoAccountConfigDTO getDemoAccountConfig();

    DemoSiteConfigDTO getDemoSiteConfig();

    BlacklistConfigDTO getBlacklistConfig();

    BaiduQianfanConfigDTO getBaiduQianfanConfig();

    /**
     * 获取百度千帆配置（内部使用，不脱敏）
     * 用于审核服务等需要真实密钥的场景
     */
    BaiduQianfanConfigDTO getBaiduQianfanConfigInternal();

    void updateBaiduQianfanConfig(BaiduQianfanConfigDTO baiduQianfanConfig);

    void updateWebsiteConfig(WebsiteConfigDTO websiteConfig);

    void updateSystemConfig(SystemConfigDTO systemConfig);

    void updateLocalConfig(LocalConfigDTO localConfig);

    void updateOssConfig(String provider, OssConfigDTO ossConfig);

    void updateSmsConfig(String provider, SmsConfigDTO smsConfig);

    void updateAlipayConfig(AlipayConfigDTO alipayConfig);

    void updateWeChatPayConfig(WeChatPayConfigDTO weChatPayConfig);

    void updateWeChatLoginConfig(WeChatLoginConfigDTO weChatLoginConfig);

    void updateCaptchaConfig(CaptchaConfigDTO captchaConfig);

    void updateAmapConfig(AmapConfigDTO amapConfig);

    void updateDemoAccount(DemoAccountConfigDTO demoAccountConfig);

    void updateDemoSite(DemoSiteConfigDTO demoSiteConfig);

    void updateBlacklistEnabled(Boolean enabled);

    void addBlacklistItem(BlacklistItemDTO blacklistItemDTO);

    void unbanBlacklistItem(Long id);

    void deleteBlacklistItem(Long id);

    /** 获取市集轮播图配置 */
    List<CarouselItemDTO> getCarouselConfig();

    /** 更新市集轮播图配置 */
    void updateCarouselConfig(List<CarouselItemDTO> list);

    /** 管理端工作台右侧轮播图 */
    List<CarouselItemDTO> getWorkbenchCarouselConfig();

    void updateWorkbenchCarouselConfig(List<CarouselItemDTO> list);

    /** 管理端登录页圆形展示区 */
    List<AdminLoginPromoCircleDTO> getAdminLoginPromoConfig();

    void updateAdminLoginPromoConfig(List<AdminLoginPromoCircleDTO> list);
}
