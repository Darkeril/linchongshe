//package com.hongshu.common.core.config;
//
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Configuration;
//
///**
//
// */
//@Slf4j
//@Configuration
//@Data
//public class OssConfig {
//
//    // 配置type 0:本地 1:七牛云 2:Minio 3:腾讯云 4：阿里云
//    @Value("${oss.type}")
//    Integer type;
//
//    // 七牛云配置
//    @Value("${oss.qiniuyun.domain}")
//    String domain;
//    @Value("${oss.qiniuyun.accessKey}")
//    String accessKey;
//    @Value("${oss.qiniuyun.secretKey}")
//    String secretKey;
//    @Value("${oss.qiniuyun.bucketName}")
//    String bucketName;
//
//    // Minio配置
//    @Value("${oss.minio.url}")
//    String minioDomain;
//    @Value("${oss.minio.accessKey}")
//    String minioAccessKey;
//    @Value("${oss.minio.secretKey}")
//    String minioSecretKey;
//    @Value("${oss.minio.bucketName}")
//    String minioBucketName;
//
//    // 阿里云配置
//    @Value("${oss.aliyun.bucketName}")
//    String aliyunBucketName;
//    @Value("${oss.aliyun.endpoint}")
//    String aliyunEndpoint;
//    @Value("${oss.aliyun.accessKeyId}")
//    String aliyunAccessKeyId;
//    @Value("${oss.aliyun.accessKeySecret}")
//    String aliyunAccessKeySecret;
//
//    // 腾讯云配置
//    @Value("${oss.tencent.bucketName}")
//    String tencentBucketName;
//    @Value("${oss.tencent.endpoint}")
//    String tencentEndpoint;
//    @Value("${oss.tencent.ossKeyId}")
//    String tencentOssKeyId;
//    @Value("${oss.tencent.ossKeySecret}")
//    String tencentOssKeySecret;
//}
