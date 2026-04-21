package com.hongshu.file.service.impl;

import cn.hutool.json.JSONUtil;
import com.hongshu.common.core.domain.R;
import com.hongshu.common.core.enums.OssProviderEnum;
import com.hongshu.file.domain.SystemConfigDTO;
import com.hongshu.file.service.ISysFileService;
import com.hongshu.system.api.RemoteWebService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传服务实现 - 根据系统配置动态选择上传策略
 *
 
 */
@Primary
@Slf4j
@Service
public class SysFileServiceImpl implements ISysFileService {

    @Autowired
    private RemoteWebService remoteWebService;
    @Autowired
    private LocalSysFileServiceImpl localSysFileService;
    @Autowired
    private AliyunSysFileServiceImpl aliyunSysFileService;
    @Autowired
    private TencentSysFileServiceImpl tencentSysFileService;
    @Autowired
    private QiNiuYunSysFileServiceImpl qiNiuYunSysFileService;
    @Autowired
    private MinioSysFileServiceImpl minioSysFileService;
//    @Autowired
//    private FastDfsSysFileServiceImpl fastDfsSysFileService;


    private final Map<String, ISysFileService> uploadStrategies = new HashMap<>();
    private SystemConfigDTO cachedSystemConfig;
    private long lastConfigFetchTime = 0;
    private static final long CONFIG_CACHE_TIME = 5 * 60 * 1000; // 5分钟缓存


    /**
     * 初始化上传策略映射
     */
    @PostConstruct
    public void initStrategies() {
        uploadStrategies.put(OssProviderEnum.LOCAL.getCode(), localSysFileService);
        uploadStrategies.put(OssProviderEnum.ALIYUN.getCode(), aliyunSysFileService);
        uploadStrategies.put(OssProviderEnum.TENCENT.getCode(), tencentSysFileService);
        uploadStrategies.put(OssProviderEnum.QINIUYUN.getCode(), qiNiuYunSysFileService);
        uploadStrategies.put(OssProviderEnum.MINIO.getCode(), minioSysFileService);
//        uploadStrategies.put(OssProviderEnum.FASTDFS.getCode(), fastDfsSysFileService);
    }

    @Override
    public String uploadFile(MultipartFile file) throws Exception {
        ISysFileService uploadStrategy = this.getUploadStrategy();
        try {
            return uploadStrategy.uploadFile(file);
        } catch (Exception e) {
            log.error("文件上传失败，尝试使用本地存储作为备用方案", e);
            return localSysFileService.uploadFile(file);
        }
    }

    @Override
    public String uploadFile(File file) throws Exception {
        ISysFileService uploadStrategy = this.getUploadStrategy();
        try {
            return uploadStrategy.uploadFile(file);
        } catch (Exception e) {
            log.error("文件上传失败，尝试使用本地存储作为备用方案", e);
            return localSysFileService.uploadFile(file);
        }
    }

    /**
     * 获取当前的上传策略
     */
    private ISysFileService getUploadStrategy() {
        SystemConfigDTO systemConfig = getSystemConfigWithCache();
        if (systemConfig == null || systemConfig.getOssType() == null) {
            log.warn("系统配置为空，使用默认本地存储");
            return localSysFileService;
        }
        // 获取数值类型的ossType
        String ossTypeCode = systemConfig.getOssType();
        log.info("当前使用的OSS类型代码: {}", ossTypeCode);
        // 根据数值代码转换为对应的枚举代码
        String ossType = this.convertOssTypeCodeToEnumCode(ossTypeCode);
        if (ossType == null) {
            log.warn("未找到对应的OSS类型代码: {}，使用默认本地存储", ossTypeCode);
            return localSysFileService;
        }
        ISysFileService uploadStrategy = uploadStrategies.get(ossType);
        if (uploadStrategy == null) {
            log.warn("未找到对应的上传策略: {}，使用默认本地存储", ossType);
            return localSysFileService;
        }
        return uploadStrategy;
    }

    // 建议放到类的静态常量处
    private static final Map<String, OssProviderEnum> TYPE_CODE_TO_ENUM = new HashMap<String, OssProviderEnum>() {{
        put("0", OssProviderEnum.LOCAL);
        put("1", OssProviderEnum.QINIUYUN);
        put("2", OssProviderEnum.MINIO);
        put("3", OssProviderEnum.ALIYUN);
        put("4", OssProviderEnum.TENCENT);
    }};

    /**
     * 将数值类型的OSS类型代码转换为枚举代码
     */
    private String convertOssTypeCodeToEnumCode(String ossTypeCode) {
        if (ossTypeCode == null) {
            return null;
        }
        OssProviderEnum provider = TYPE_CODE_TO_ENUM.get(ossTypeCode);
        if (provider == null) {
            return null;
        }
        log.info("当前使用的OSS类型: {}", provider.getName());
        return provider.getCode();
    }

    /**
     * 获取带缓存的系统配置
     */
    private SystemConfigDTO getSystemConfigWithCache() {
        long currentTime = System.currentTimeMillis();
        // 如果缓存有效且未过期，直接返回缓存
        if (cachedSystemConfig != null &&
                (currentTime - lastConfigFetchTime) < CONFIG_CACHE_TIME) {
            return cachedSystemConfig;
        }
        try {
            R<?> r = remoteWebService.getSystemConfig();
            if (r == null || r.getCode() != 200) {
                log.warn("获取系统配置失败，响应: {}", r);
                return cachedSystemConfig; // 返回旧的缓存配置
            }
            Object data = r.getData();
            if (data == null) {
                log.warn("系统配置数据为空");
                return cachedSystemConfig;
            }
            SystemConfigDTO systemConfig = JSONUtil.toBean(JSONUtil.parseObj(data), SystemConfigDTO.class);
            // 更新缓存
            cachedSystemConfig = systemConfig;
            lastConfigFetchTime = currentTime;
            log.info("系统配置已更新: {}", systemConfig.getOssType());
            return systemConfig;
        } catch (Exception e) {
            log.warn("获取系统配置失败，使用缓存配置", e);
            return cachedSystemConfig; // 返回旧的缓存配置
        }
    }
}
