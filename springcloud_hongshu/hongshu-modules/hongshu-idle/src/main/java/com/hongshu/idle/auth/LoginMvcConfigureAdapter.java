package com.hongshu.idle.auth;

import com.hongshu.common.core.config.HongshuConfig;
import com.hongshu.common.core.constant.Constants;
import com.hongshu.common.security.interceptor.UserLoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * @author: hongshu
 */
@Configuration
public class LoginMvcConfigureAdapter extends WebMvcConfigurationSupport {

    /**
     * 拦截器
     * 拦截所有路径，但排除服务间调用和系统路径（这些路径不需要用户登录）
     * 使用common模块的统一拦截器
     * 
     * 说明：
     * 1. hongshu-idle 模块既有用户接口（需要登录），也有服务间调用接口（不需要用户登录）
     * 2. 用户接口使用 @NoLoginIntercept 注解可以跳过登录验证
     * 3. 服务间调用接口建议使用 @NoLoginIntercept 注解，或者排除在拦截路径之外
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserLoginInterceptor())
                // 默认拦截所有路径
                .addPathPatterns("/**")
                // 排除服务间调用和系统路径（不需要用户登录）
                .excludePathPatterns(
                        "/config/**",        // 系统配置接口（服务间调用，如刷新配置）
                        "/actuator/**",      // Spring Boot Actuator监控端点
                        "/swagger-ui/**",    // Swagger UI
                        "/swagger-resources/**", // Swagger资源
                        "/v3/api-docs/**",   // Swagger API文档
                        "/doc.html",         // Knife4j文档
                        "/webjars/**",       // Webjars静态资源
                        "/favicon.ico",      // 图标
                        "/error"             // 错误页面
                );
        super.addInterceptors(registry);
    }

    /**
     * 静态资源访问
     *
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler(UploadFileConstant.OSS + "/**") //虚拟url路径
//                .addResourceLocations("file:" + UploadFileConstant.ADDRESS); //真实本地路径
//        super.addResourceHandlers(registry);

        /** 本地文件上传路径 */
        registry.addResourceHandler(Constants.RESOURCE_PREFIX + "/**")
                .addResourceLocations("file:" + HongshuConfig.getProfile() + "/");
    }
}
