package com.hongshu.idle.domain.dto;

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

    // 百度千帆审核配置
    private Boolean baiduQianfanEnabled;

    private String baiduQianfanAccessKey;

    private String baiduQianfanSecretKey;

    private String baiduQianfanTextEndpoint;

    private String baiduQianfanImageEndpoint;

    private String baiduQianfanVideoEndpoint;

    private String auditMode;

    private Double autoAuditThreshold;
}
