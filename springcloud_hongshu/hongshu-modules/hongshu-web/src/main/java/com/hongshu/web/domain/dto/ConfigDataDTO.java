package com.hongshu.web.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigDataDTO {

    /**
     * 网站配置
     */
    private WebsiteConfigDTO website;

    /**
     * 系统配置
     */
    private SystemConfigDTO system;

//    /**
//     * 本地存储配置
//     */
//    private LocalConfigDTO local;

    /**
     * OSS配置
     */
    private Map<String, OssConfigDTO> oss;

    /**
     * 短信配置
     */
    private Map<String, SmsConfigDTO> sms;

    /**
     * 支付宝配置
     */
    private AlipayConfigDTO alipay;

    /**
     * 微信支付配置
     */
    private WeChatPayConfigDTO wechatPay;

    /**
     * 微信登录配置
     */
    private WeChatLoginConfigDTO wechatLogin;

    /**
     * 验证码配置
     */
    private CaptchaConfigDTO captcha;

    /**
     * 高德地图配置
     */
    private AmapConfigDTO amap;

    /**
     * 演示账号配置
     */
    private DemoAccountConfigDTO demoAccount;

    /**
     * 演示站配置
     */
    private DemoSiteConfigDTO demoSite;

    /**
     * 黑名单配置
     */
    private BlacklistConfigDTO blacklist;

    /**
     * 自动审核配置（百度千帆）
     */
    private com.hongshu.common.audit.dto.BaiduQianfanConfigDTO autoAudit;

    /**
     * 市集轮播图配置
     */
    private List<CarouselItemDTO> carousel;

    /**
     * 管理端工作台右侧轮播图配置
     */
    private List<CarouselItemDTO> workbenchCarousel;

    /**
     * 管理端登录页圆形展示区配置
     */
    private List<AdminLoginPromoCircleDTO> adminLoginPromo;
}

