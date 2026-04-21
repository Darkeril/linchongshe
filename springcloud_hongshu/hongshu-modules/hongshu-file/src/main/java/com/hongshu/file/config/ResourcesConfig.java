package com.hongshu.file.config;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.hongshu.common.core.domain.R;
import com.hongshu.common.core.enums.OssProviderEnum;
import com.hongshu.file.domain.OssConfigDTO;
import com.hongshu.system.api.RemoteWebService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.util.Map;

/**
 * 通用映射配置
 *

 */
@Configuration
public class ResourcesConfig implements WebMvcConfigurer {

    @Autowired
    private ObjectProvider<RemoteWebService> remoteWebServiceProvider;

    @Value("${file.path:/tmp/uploads}")
    private String localFilePath;

    @Value("${file.prefix:/profile}")
    private String localFilePrefix;


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String resolvedPath = localFilePath;
        try {
            RemoteWebService remoteWebService = remoteWebServiceProvider.getIfAvailable();
            if (remoteWebService != null) {
                R<?> r = remoteWebService.getOssConfig();
                if (r != null && r.getData() != null) {
                    Map<String, OssConfigDTO> ossMap = JSONUtil.toBean(
                            JSONUtil.parseObj(r.getData()),
                            new TypeReference<Map<String, OssConfigDTO>>() {
                            }, false);
                    OssConfigDTO local = ossMap.get(OssProviderEnum.LOCAL.getCode());
                    if (local != null && local.getUrl() != null && !local.getUrl().isEmpty()) {
                        resolvedPath = local.getUrl();
                    }
                }
            }
        } catch (Exception ignore) {
            // 失败时使用默认/配置文件值
        }
        registry.addResourceHandler(localFilePrefix + "/**")
                .addResourceLocations("file:" + resolvedPath + File.separator);
    }

    /**
     * 开启跨域
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 设置允许跨域的路由
        registry.addMapping(localFilePrefix + "/**")
                // 设置允许跨域请求的域名
                .allowedOrigins("*")
                // 设置允许的方法
                .allowedMethods("GET");
    }
}
