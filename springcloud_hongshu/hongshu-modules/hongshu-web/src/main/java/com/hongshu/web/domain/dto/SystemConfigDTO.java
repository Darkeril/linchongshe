package com.hongshu.web.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigDTO {

    private String ossType;

    private String smsPrimary;

    private String mqType;

    private String queryPrimary;

    private String amapEnabled;

    private Boolean blacklistEnabled;

    private Boolean contentAuditEnabled;

    private Boolean productAuditEnabled;

    private Boolean userAuditEnabled;

    private Boolean commentAuditEnabled;

    /**
     * 审核模式：manual=人工，auto=自动，hybrid=混合
     */
    private String auditMode;

    /**
     * 自动审核阈值（0-1之间的小数）
     */
    private Double autoAuditThreshold;

    /**
     * 是否启用百度千帆审核
     */
    private Boolean baiduQianfanEnabled;

    /**
     * 百度千帆AccessKey
     */
    private String baiduQianfanAccessKey;

    /**
     * 百度千帆SecretKey
     */
    private String baiduQianfanSecretKey;

    /**
     * 百度千帆文本审核接口地址
     */
    private String baiduQianfanTextEndpoint;

    /**
     * 百度千帆图片审核接口地址
     */
    private String baiduQianfanImageEndpoint;

    /**
     * 百度千帆视频审核接口地址
     */
    private String baiduQianfanVideoEndpoint;
}
