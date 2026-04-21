package com.hongshu.file.service.impl;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.hongshu.common.core.domain.R;
import com.hongshu.common.core.enums.OssProviderEnum;
import com.hongshu.file.domain.OssConfigDTO;
import com.hongshu.file.service.ISysFileService;
import com.hongshu.file.utils.FileUploadUtils;
import com.hongshu.system.api.RemoteWebService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

import com.hongshu.common.core.utils.DateUtils;
import com.hongshu.common.core.utils.uuid.Seq;
import org.apache.commons.io.FilenameUtils;

@Service
public class AliyunSysFileServiceImpl implements ISysFileService {

    @Autowired
    private RemoteWebService remoteWebService;


    @Override
    public String uploadFile(MultipartFile file) {
        R<?> r = remoteWebService.getOssConfigInternal();
        if (r == null || r.getData() == null) {
            throw new IllegalStateException("未获取到 OSS 配置：remoteWebService.getOssConfigInternal() 返回为空");
        }
        Map<String, OssConfigDTO> ossMap = JSONUtil.toBean(
                JSONUtil.parseObj(r.getData()),
                new TypeReference<Map<String, OssConfigDTO>>() {
                }, false);
        OssConfigDTO ossConfig = ossMap.get(OssProviderEnum.ALIYUN.getCode());
        if (ossConfig == null) {
            throw new IllegalStateException("未找到阿里云 OSS 配置，key=" + OssProviderEnum.ALIYUN.getCode());
        }
        if (StrUtil.hasBlank(
                ossConfig.getEndpoint(), ossConfig.getAccessKeyId(),
                ossConfig.getAccessKeySecret(), ossConfig.getBucketName())) {
            throw new IllegalStateException("阿里云 OSS 配置不完整，请检查 endpoint/accessKeyId/accessKeySecret/bucketName");
        }

        OSS client = new OSSClientBuilder().build(
                ossConfig.getEndpoint(),
                ossConfig.getAccessKeyId(),
                ossConfig.getAccessKeySecret()
        );
        try {
            String key = FileUploadUtils.extractFilename(file);
//            if (StringUtils.isNotEmpty(ossConfig.getPathPrefix())) {
//                key = ossConfig.getPathPrefix().replaceAll("/+$", "") + "/" + key;
//            }
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(file.getSize());
            meta.setContentType(file.getContentType());
            client.putObject(ossConfig.getBucketName(), key, file.getInputStream(), meta);
            String base = StrUtil.blankToDefault(
                    ossConfig.getDomain(), "https://" + ossConfig.getBucketName() + "." + ossConfig.getEndpoint());
            return base.replaceAll("/+$", "") + "/" + key;
        } catch (Exception e) {
            throw new RuntimeException("Aliyun OSS upload failed", e);
        } finally {
            client.shutdown();
        }
    }

    @Override
    public String uploadFile(File file) {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("文件不存在");
        }

        R<?> r = remoteWebService.getOssConfigInternal();
        if (r == null || r.getData() == null) {
            throw new IllegalStateException("未获取到 OSS 配置：remoteWebService.getOssConfigInternal() 返回为空");
        }
        Map<String, OssConfigDTO> ossMap = JSONUtil.toBean(
                JSONUtil.parseObj(r.getData()),
                new TypeReference<Map<String, OssConfigDTO>>() {
                }, false);
        OssConfigDTO ossConfig = ossMap.get(OssProviderEnum.ALIYUN.getCode());
        if (ossConfig == null) {
            throw new IllegalStateException("未找到阿里云 OSS 配置，key=" + OssProviderEnum.ALIYUN.getCode());
        }
        if (StrUtil.hasBlank(
                ossConfig.getEndpoint(), ossConfig.getAccessKeyId(),
                ossConfig.getAccessKeySecret(), ossConfig.getBucketName())) {
            throw new IllegalStateException("阿里云 OSS 配置不完整，请检查 endpoint/accessKeyId/accessKeySecret/bucketName");
        }

        OSS client = new OSSClientBuilder().build(
                ossConfig.getEndpoint(),
                ossConfig.getAccessKeyId(),
                ossConfig.getAccessKeySecret()
        );
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

            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(file.length());
            // 根据文件扩展名设置ContentType
            String contentType = getContentType(fileName);
            meta.setContentType(contentType);

            fileInputStream = new FileInputStream(file);
            client.putObject(ossConfig.getBucketName(), key, fileInputStream, meta);
            String base = StrUtil.blankToDefault(
                    ossConfig.getDomain(), "https://" + ossConfig.getBucketName() + "." + ossConfig.getEndpoint());
            return base.replaceAll("/+$", "") + "/" + key;
        } catch (Exception e) {
            throw new RuntimeException("Aliyun OSS upload failed", e);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (Exception e) {
                    // ignore
                }
            }
            client.shutdown();
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
