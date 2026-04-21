package com.hongshu.file.service.impl;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import com.hongshu.common.core.utils.DateUtils;
import com.hongshu.common.core.utils.uuid.Seq;
import org.apache.commons.io.FilenameUtils;

/**
 * 本地文件存储
 *
 
 */
@Service
public class LocalSysFileServiceImpl implements ISysFileService {

    @Autowired
    private RemoteWebService remoteWebService;


    /**
     * 本地文件上传接口
     *
     * @param file 上传的文件
     * @return 访问地址
     * @throws Exception
     */
    @Override
    public String uploadFile(MultipartFile file) throws Exception {
        R<?> r = remoteWebService.getOssConfigInternal();
        Object data = r.getData();
        Map<String, OssConfigDTO> ossMap =
                JSONUtil.toBean(JSONUtil.parseObj(data),
                        new TypeReference<Map<String, OssConfigDTO>>() {
                        }, false);
        OssConfigDTO ossConfig = ossMap.get(OssProviderEnum.LOCAL.getCode());

        String name = FileUploadUtils.upload(ossConfig.getUrl(), file);
        String localFilePrefix = "/profile"; // 与 ResourcesConfig 保持一致
        return ossConfig.getDomain() + localFilePrefix + name;
    }

    @Override
    public String uploadFile(File file) throws Exception {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("文件不存在");
        }

        R<?> r = remoteWebService.getOssConfigInternal();
        Object data = r.getData();
        Map<String, OssConfigDTO> ossMap =
                JSONUtil.toBean(JSONUtil.parseObj(data),
                        new TypeReference<Map<String, OssConfigDTO>>() {
                        }, false);
        OssConfigDTO ossConfig = ossMap.get(OssProviderEnum.LOCAL.getCode());

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

        // 复制文件到目标目录
        String targetDir = ossConfig.getUrl();
        java.io.File targetFile = new java.io.File(targetDir, newFileName);
        if (!targetFile.getParentFile().exists()) {
            targetFile.getParentFile().mkdirs();
        }
        Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        String localFilePrefix = "/profile"; // 与 ResourcesConfig 保持一致
        return ossConfig.getDomain() + localFilePrefix + "/" + newFileName;
    }
}
