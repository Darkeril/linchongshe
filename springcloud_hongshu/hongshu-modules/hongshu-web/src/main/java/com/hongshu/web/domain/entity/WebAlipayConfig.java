package com.hongshu.web.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 支付宝配置表
 *
 * @author: hongshu
 */
@Data
@TableName("web_alipay_config")
public class WebAlipayConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商户PID
     */
    private String pid;

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 商户私钥
     */
    private String merchantPrivateKey;

    /**
     * 支付宝公钥
     */
    private String alipayPublicKey;

    /**
     * 异步通知地址
     */
    private String notifyUrl;

    /**
     * 同步返回地址
     */
    private String returnUrl;

    /**
     * 签名类型
     */
    private String signType;

    /**
     * 字符集
     */
    private String charset;

    /**
     * 网关地址
     */
    private String gatewayUrl;

    /**
     * 是否启用
     */
    private Boolean isEnabled;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
