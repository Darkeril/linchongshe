package com.hongshu.file.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OssConfigDTO {

    private String domain;

    private String accessKey;

    private String secretKey;

    private String bucketName;

    private Boolean isEnabled;

    private String endpoint;

    private String region;

    // 这些字段通过字段映射来处理，不直接存储
    private String url;              // Minio时映射到endpoint
    private String accessKeyId;      // 阿里云时映射到accessKey
    private String accessKeySecret;  // 阿里云时映射到secretKey
    private String ossKeyId;         // 腾讯云时映射到accessKey
    private String ossKeySecret;     // 腾讯云时映射到secretKey
}
