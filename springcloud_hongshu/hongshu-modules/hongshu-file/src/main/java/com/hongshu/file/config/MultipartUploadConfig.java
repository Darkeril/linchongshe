package com.hongshu.file.config;

import jakarta.servlet.MultipartConfigElement;

import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

/**
 * 上传配置：用于放大文件服务的上传阈值，避免默认 1MB 限制。
 */
@Configuration
public class MultipartUploadConfig {

    private static final DataSize MAX_FILE_SIZE = DataSize.ofMegabytes(200);
    private static final DataSize MAX_REQUEST_SIZE = DataSize.ofMegabytes(500);

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(MAX_FILE_SIZE);
        factory.setMaxRequestSize(MAX_REQUEST_SIZE);
        return factory.createMultipartConfig();
    }

    @Bean
    public TomcatServletWebServerFactory tomcatServletWebServerFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.addConnectorCustomizers((TomcatConnectorCustomizer) connector -> {
            int maxPostSize = (int) MAX_FILE_SIZE.toBytes();
            connector.setMaxPostSize(maxPostSize);
            connector.setMaxSavePostSize(maxPostSize);
            connector.setProperty("maxSwallowSize", String.valueOf(-1));
        });
        return factory;
    }
}

