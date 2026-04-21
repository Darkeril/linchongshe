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
@TableName("web_oss_config")
public class WebOssConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 服务商：qiniuyun/minio/tencent/aliyun
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
     * 存储桶名称
     */
    private String bucketName;

    /**
     * 域名
     */
    private String domain;

    /**
     * 端点地址
     */
    private String endpoint;

    /**
     * 区域
     */
    private String region;

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
