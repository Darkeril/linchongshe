package com.hongshu.web.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 短信配置表
 *
 * @author: hongshu
 */
@Data
@TableName("web_sms_config")
public class WebSmsConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 服务商：aliyun/tencent/yunpian
     */
    private String provider;

    /**
     * 访问密钥
     */
    private String accessKey;

    /**
     * 密钥
     */
    private String secretKey;

    /**
     * 签名
     */
    private String signName;

    /**
     * 模板代码
     */
    private String templateCode;

    /**
     * SDK应用ID（腾讯云用）
     */
    private String sdkAppId;

    /**
     * 区域（腾讯云用）
     */
    private String region;

    /**
     * 是否主配置
     */
    private Boolean isPrimary;

    /**
     * 是否备用配置
     */
    private Boolean isSecondary;

    /**
     * 状态：1启用 0禁用
     */
    private Boolean status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
