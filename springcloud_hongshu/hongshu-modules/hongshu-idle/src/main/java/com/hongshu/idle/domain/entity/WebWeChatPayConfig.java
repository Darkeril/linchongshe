package com.hongshu.idle.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 微信支付配置表
 *
 * @author: hongshu
 */
@Data
@TableName("web_wechat_pay_config")
public class WebWeChatPayConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 微信支付商户号
     */
    private String mchId;

    /**
     * PC端应用ID
     */
    private String pcAppId;

    /**
     * APP应用ID
     */
    private String appAppId;

    /**
     * 应用ID（小程序APPID）
     */
    private String appId;

    /**
     * API密钥（商户密钥）- 兼容旧版
     */
    private String apiKey;

    /**
     * APIv2密钥
     */
    private String apiV2Key;

    /**
     * APIv3密钥
     */
    private String apiV3Key;

    /**
     * 商户证书序列号
     */
    private String certSerialNo;

    /**
     * 商户私钥证书路径
     */
    private String privateKeyPath;

    /**
     * 微信支付证书路径
     */
    private String wechatPayCertificate;

    /**
     * 异步通知地址
     */
    private String notifyUrl;

    /**
     * 微信服务器地址
     */
    private String domain;

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

