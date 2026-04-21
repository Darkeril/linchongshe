package com.hongshu.web.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsConfigDTO {

    private String accessKey;

    private String secretKey;

    private String signName;

    private String templateCode;

    private Boolean isPrimary;

    private Boolean isSecondary;

    private Boolean status;

    private String sdkAppId;

    private String region;

    // 这些字段通过字段映射来处理，不直接存储
    private String secretId;      // 腾讯云时映射到accessKey
    private String templateId;    // 腾讯云时映射到templateCode
    private String apiKey;        // 云片时映射到accessKey
}
