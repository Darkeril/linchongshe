package com.hongshu.web.config;

import com.hongshu.web.domain.dto.OssConfigDTO;
import com.hongshu.web.service.sys.ISysSystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OssConfigService {

    @Autowired
    private ISysSystemConfigService systemConfigService;


    public OssConfigDTO getOssConfig(String provider) {
        try {
            // 从数据库获取OSS配置
            return systemConfigService.getOssConfig(provider);
        } catch (Exception e) {
            log.error("获取OSS配置失败: " + provider, e);
            return null;
        }
    }
}
