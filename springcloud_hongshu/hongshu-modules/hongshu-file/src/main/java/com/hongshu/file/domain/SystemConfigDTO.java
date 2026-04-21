package com.hongshu.file.domain;

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
}
