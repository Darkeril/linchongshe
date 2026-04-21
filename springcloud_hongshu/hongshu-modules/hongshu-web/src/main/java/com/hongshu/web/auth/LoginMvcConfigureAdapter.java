package com.hongshu.web.auth;

import com.hongshu.common.core.config.HongshuConfig;
import com.hongshu.common.core.constant.Constants;
import com.hongshu.common.security.interceptor.UserLoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
public class LoginMvcConfigureAdapter extends WebMvcConfigurationSupport {

    @Autowired
    private WebUserStatusInterceptor webUserStatusInterceptor;

    /**
     * 拦截器
     * 使用common模块的统一拦截器
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserLoginInterceptor())
                .order(0)
                .addPathPatterns("/**")
                // 与 hongshu-idle 一致：Swagger 拉 /v3/api-docs 不带业务 Token，不排除会返回 AjaxResult 非 OpenAPI，UI 报「无 openapi 版本」
                .excludePathPatterns(
                        "/config/**",
                        "/actuator/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/swagger-resources/**",
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/doc.html",
                        "/webjars/**",
                        "/favicon.ico",
                        "/error");
        registry.addInterceptor(webUserStatusInterceptor)
                .order(1)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/config/**",
                        "/actuator/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/swagger-resources/**",
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/doc.html",
                        "/webjars/**",
                        "/favicon.ico",
                        "/error");
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
