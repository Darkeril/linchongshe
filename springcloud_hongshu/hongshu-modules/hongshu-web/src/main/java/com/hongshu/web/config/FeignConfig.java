package com.hongshu.web.config;

import feign.Request;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign客户端配置
 * 用于优化文件上传性能
 */
@Configuration
public class FeignConfig {

    /**
     * 配置请求超时时间
     * connectTimeout: 连接超时时间（毫秒）
     * readTimeout: 读取超时时间（毫秒）
     */
    @Bean
    public Request.Options requestOptions() {
        // 连接超时：10秒（10000毫秒）
        // 读取超时：300秒（300000毫秒，文件上传可能需要较长时间）
        // 注意：Request.Options 构造函数参数单位是毫秒
        return new Request.Options(10000, 300000);
    }

    /**
     * 配置重试策略
     * period: 重试间隔（毫秒）
     * maxPeriod: 最大重试间隔（毫秒）
     * maxAttempts: 最大重试次数
     */
    @Bean
    public Retryer retryer() {
        // 不重试，避免重复上传
        return Retryer.NEVER_RETRY;
    }
}

