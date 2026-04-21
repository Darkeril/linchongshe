package com.hongshu.idle.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlipayConfigDTO {

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

    /**
     * 支付宝沙箱支付开关（开发测试用）
     */
    private Boolean sandboxEnabled;

    /**
     * 支付宝扫码支付开关（生产用）
     */
    private Boolean qrcodeEnabled;
}
