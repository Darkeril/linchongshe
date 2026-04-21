package com.hongshu.idle.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 支付全局配置DTO
 *
 * @author: hongshu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentGlobalConfigDTO {

    /**
     * 全局支付开关（控制整个系统是否允许支付）
     */
    private Boolean paymentEnabled;

    /**
     * 微信扫码支付开关
     */
    private Boolean wechatQrcodeEnabled;

    /**
     * 支付宝沙箱支付开关（开发测试用）
     */
    private Boolean alipaySandboxEnabled;

    /**
     * 支付宝扫码支付开关（生产用）
     */
    private Boolean alipayQrcodeEnabled;

    /**
     * 微信小程序支付开关
     */
    private Boolean wechatMiniprogramEnabled;

    /**
     * 支付超时时间（分钟），与 payment.timeout.minutes 一致，供前端倒计时展示
     */
    private Integer paymentTimeoutMinutes;
}


