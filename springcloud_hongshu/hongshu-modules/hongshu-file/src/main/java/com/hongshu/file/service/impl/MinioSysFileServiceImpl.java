package com.hongshu.file.service.impl;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.common.utils.IoUtils;
import com.hongshu.common.core.domain.R;
import com.hongshu.common.core.enums.OssProviderEnum;
import com.hongshu.file.domain.OssConfigDTO;
import com.hongshu.file.service.ISysFileService;
import com.hongshu.file.utils.FileUploadUtils;
import com.hongshu.system.api.RemoteWebService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
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

/**
 * Minio 文件存储
 *

 */
@Service
public class MinioSysFileServiceImpl implements ISysFileService {

    @Autowired
    private RemoteWebService remoteWebService;


    /**
     * Minio文件上传接口
     *
     * @param file 上传的文件
     * @return 访问地址
     */
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
        OssConfigDTO ossConfig = ossMap.get(OssProviderEnum.MINIO.getCode());
        if (ossConfig == null) {
            throw new IllegalStateException("未找到Minio OSS 配置，key=" + OssProviderEnum.MINIO.getCode());
        }
        // endpoint 兼容：优先 domain（MinIO 场景），其次 endpoint
        String endpoint = StrUtil.blankToDefault(ossConfig.getDomain(), ossConfig.getEndpoint());
        if (StrUtil.hasBlank(ossConfig.getDomain(), ossConfig.getAccessKey(), ossConfig.getSecretKey(), ossConfig.getBucketName())) {
            throw new IllegalStateException("Minio OSS 配置不完整，请检查 url/endpoint、accessKey、secretKey、bucketName");
        }

        // 构建临时 MinioClient
        MinioClient client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(ossConfig.getAccessKey(), ossConfig.getSecretKey())
                .build();

        InputStream inputStream = null;
        try {
            String fileName = FileUploadUtils.extractFilename(file);
            inputStream = file.getInputStream();
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(ossConfig.getBucketName())
                    .object(fileName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(StrUtil.blankToDefault(file.getContentType(), "application/octet-stream"))
                    .build();
            client.putObject(args);

            // 拼接访问地址：优先 domain，其次 endpoint/url
            String base = StrUtil.emptyToDefault(ossConfig.getDomain(), endpoint);
            base = base.replaceAll("/+$", "");
            if (!base.startsWith("http://") && !base.startsWith("https://")) {
                base = "http://" + base;
            }
            return base + "/" + ossConfig.getBucketName() + "/" + fileName;
        } catch (Exception e) {
            throw new RuntimeException("Minio Failed to upload file", e);
        } finally {
            IoUtils.closeQuietly(inputStream);
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
        OssConfigDTO ossConfig = ossMap.get(OssProviderEnum.MINIO.getCode());
        if (ossConfig == null) {
            throw new IllegalStateException("未找到Minio OSS 配置，key=" + OssProviderEnum.MINIO.getCode());
        }
        // endpoint 兼容：优先 domain（MinIO 场景），其次 endpoint
        String endpoint = StrUtil.blankToDefault(ossConfig.getDomain(), ossConfig.getEndpoint());
        if (StrUtil.hasBlank(ossConfig.getDomain(), ossConfig.getAccessKey(), ossConfig.getSecretKey(), ossConfig.getBucketName())) {
            throw new IllegalStateException("Minio OSS 配置不完整，请检查 url/endpoint、accessKey、secretKey、bucketName");
        }

        // 构建临时 MinioClient
        MinioClient client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(ossConfig.getAccessKey(), ossConfig.getSecretKey())
                .build();

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
            String newFileName = datePath + "/" + baseName + "_" + seqId + "." + extension;

            fileInputStream = new FileInputStream(file);
            // 根据文件扩展名设置ContentType
            String contentType = getContentType(fileName);
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(ossConfig.getBucketName())
                    .object(newFileName)
                    .stream(fileInputStream, file.length(), -1)
                    .contentType(StrUtil.blankToDefault(contentType, "application/octet-stream"))
                    .build();
            client.putObject(args);

            // 拼接访问地址：优先 domain，其次 endpoint/url
            String base = StrUtil.emptyToDefault(ossConfig.getDomain(), endpoint);
            base = base.replaceAll("/+$", "");
            if (!base.startsWith("http://") && !base.startsWith("https://")) {
                base = "http://" + base;
            }
            return base + "/" + ossConfig.getBucketName() + "/" + newFileName;
        } catch (Exception e) {
            throw new RuntimeException("Minio Failed to upload file", e);
        } finally {
            IoUtils.closeQuietly(fileInputStream);
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
