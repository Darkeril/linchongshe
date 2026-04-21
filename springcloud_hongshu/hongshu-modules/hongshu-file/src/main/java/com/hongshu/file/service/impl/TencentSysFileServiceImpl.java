package com.hongshu.file.service.impl;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hongshu.common.core.domain.R;
import com.hongshu.common.core.enums.OssProviderEnum;
import com.hongshu.file.domain.OssConfigDTO;
import com.hongshu.file.service.ISysFileService;
import com.hongshu.file.utils.FileUploadUtils;
import com.hongshu.system.api.RemoteWebService;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

import com.hongshu.common.core.utils.DateUtils;
import com.hongshu.common.core.utils.uuid.Seq;
import org.apache.commons.io.FilenameUtils;

@Service
@Slf4j
public class TencentSysFileServiceImpl implements ISysFileService {

    @Autowired
    private RemoteWebService remoteWebService;


    @Override
    public String uploadFile(MultipartFile file) {
        R<?> r = remoteWebService.getOssConfigInternal();
        if (r == null || r.getData() == null) {
            throw new IllegalStateException("未获取到 OSS 配置：getOssConfigInternal() 返回为空");
        }

        Map<String, OssConfigDTO> ossMap = JSONUtil.toBean(
                JSONUtil.parseObj(r.getData()),
                new TypeReference<Map<String, OssConfigDTO>>() {
                }, false);

        OssConfigDTO cosConfig = ossMap.get(OssProviderEnum.TENCENT.getCode());
        if (cosConfig == null) {
            throw new IllegalStateException("未找到腾讯云 COS 配置，key=" + OssProviderEnum.TENCENT.getCode());
        }
        if (StrUtil.hasBlank(
                cosConfig.getRegion(), cosConfig.getOssKeyId(),
                cosConfig.getOssKeySecret(), cosConfig.getBucketName())) {
            throw new IllegalStateException("COS 配置不完整，请检查 region/accessKeyId/secretKey/bucketName");
        }

        COSCredentials cred = new BasicCOSCredentials(cosConfig.getOssKeyId(), cosConfig.getOssKeySecret());
        ClientConfig clientConfig = new ClientConfig(new Region(cosConfig.getRegion()));
        COSClient cosClient = new COSClient(cred, clientConfig);

        try {
            String key = FileUploadUtils.extractFilename(file);
            log.info("cos bucket={}, region={}, key={}", cosConfig.getBucketName(), cosConfig.getRegion(), key);
//		if (cn.hutool.core.util.StrUtil.isNotBlank(cosConfig.getPathPrefix())) {
//			key = cosConfig.getPathPrefix().replaceAll("/+$", "") + "/" + key;
//		}

            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(file.getSize());
            meta.setContentType(file.getContentType());

            // 使用输入流上传
            try (InputStream in = file.getInputStream()) {
                PutObjectRequest req = new PutObjectRequest(cosConfig.getBucketName(), key, in, meta);
                cosClient.putObject(req);
            }

            // 优先使用配置的域名，否则拼内置默认域名
            String base = StrUtil.blankToDefault(
                    cosConfig.getDomain(),
                    "https://" + cosConfig.getBucketName() + ".cos." + cosConfig.getRegion() + ".myqcloud.com");

            // 如桶为私有读，建议返回带签名的临时 URL（否则直链会 403）
//            if (Boolean.TRUE.equals(cosConfig.getPrivateBucket())) {
//                java.util.Date expire = new java.util.Date(System.currentTimeMillis() + 3600_000); // 1小时
//                com.qcloud.cos.model.GeneratePresignedUrlRequest pre =
//                        new com.qcloud.cos.model.GeneratePresignedUrlRequest(cosConfig.getBucketName(), key)
//                                .withExpiration(expire);
//                java.net.URL signed = cosClient.generatePresignedUrl(pre);
//                return signed.toString();
//            }

            return base.replaceAll("/+$", "") + "/" + key;
        } catch (Exception e) {
            throw new RuntimeException("Tencent COS upload failed", e);
        } finally {
            cosClient.shutdown();
        }
    }

    @Override
    public String uploadFile(File file) {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("文件不存在");
        }

        R<?> r = remoteWebService.getOssConfigInternal();
        if (r == null || r.getData() == null) {
            throw new IllegalStateException("未获取到 OSS 配置：getOssConfigInternal() 返回为空");
        }

        Map<String, OssConfigDTO> ossMap = JSONUtil.toBean(
                JSONUtil.parseObj(r.getData()),
                new TypeReference<Map<String, OssConfigDTO>>() {
                }, false);

        OssConfigDTO cosConfig = ossMap.get(OssProviderEnum.TENCENT.getCode());
        if (cosConfig == null) {
            throw new IllegalStateException("未找到腾讯云 COS 配置，key=" + OssProviderEnum.TENCENT.getCode());
        }
        if (StrUtil.hasBlank(
                cosConfig.getRegion(), cosConfig.getOssKeyId(),
                cosConfig.getOssKeySecret(), cosConfig.getBucketName())) {
            throw new IllegalStateException("COS 配置不完整，请检查 region/accessKeyId/secretKey/bucketName");
        }

        COSCredentials cred = new BasicCOSCredentials(cosConfig.getOssKeyId(), cosConfig.getOssKeySecret());
        ClientConfig clientConfig = new ClientConfig(new Region(cosConfig.getRegion()));
        COSClient cosClient = new COSClient(cred, clientConfig);

        FileInputStream fileInputStream = null;
        try {
            // 生成文件名（格式：日期路径/原文件名_序列号.扩展名）
            String fileName = file.getName();
            String baseName = FilenameUtils.getBaseName(fileName);
            String extension = "";
            int lastDotIndex = fileName.lastIndexOf(".");
            if (lastDotIndex > 0) {
                extension = fileName.substring(lastDotIndex + 1);
            }
            String datePath = DateUtils.datePath();
            String seqId = Seq.getId(Seq.uploadSeqType);
            String key = datePath + "/" + baseName + "_" + seqId + "." + extension;

            log.info("cos bucket={}, region={}, key={}", cosConfig.getBucketName(), cosConfig.getRegion(), key);

            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(file.length());
            // 根据文件扩展名设置ContentType
            String contentType = getContentType(fileName);
            meta.setContentType(contentType);

            // 使用文件输入流上传
            fileInputStream = new FileInputStream(file);
            PutObjectRequest req = new PutObjectRequest(cosConfig.getBucketName(), key, fileInputStream, meta);
            cosClient.putObject(req);

            // 优先使用配置的域名，否则拼内置默认域名
            String base = StrUtil.blankToDefault(
                    cosConfig.getDomain(),
                    "https://" + cosConfig.getBucketName() + ".cos." + cosConfig.getRegion() + ".myqcloud.com");

            return base.replaceAll("/+$", "") + "/" + key;
        } catch (Exception e) {
            throw new RuntimeException("Tencent COS upload failed", e);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (Exception e) {
                    // ignore
                }
            }
            cosClient.shutdown();
        }
    }

    /**
     * 根据文件名获取ContentType
     */
    private String getContentType(String fileName) {
        String extension = "";
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex > 0) {
            extension = fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "webp":
                return "image/webp";
            case "mp4":
                return "video/mp4";
            case "webm":
                return "video/webm";
            default:
                return "application/octet-stream";
        }
    }
}
