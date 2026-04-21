package com.hongshu.idle.service.sys;

import com.hongshu.idle.domain.dto.AlipayConfigDTO;
import com.hongshu.idle.domain.dto.PaymentGlobalConfigDTO;
import com.hongshu.idle.domain.dto.SystemConfigDTO;
import com.hongshu.idle.domain.dto.WeChatPayConfigDTO;

/**
 * 系统配置表 服务类
 *
 * @author: hongshu
 */
public interface ISysSystemConfigService {

    SystemConfigDTO getSystemConfig();

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

    void updateWeChatPayConfig(WeChatPayConfigDTO weChatPayConfig);

    void updateAlipayConfig(AlipayConfigDTO alipayConfig);

    /**
     * 获取支付全局配置
     */
    PaymentGlobalConfigDTO getPaymentGlobalConfig();

    /**
     * 更新支付全局配置
     */
    void updatePaymentGlobalConfig(PaymentGlobalConfigDTO paymentGlobalConfig);

}
