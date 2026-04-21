package com.hongshu.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 管理端「数据监控」iframe 请求 {@code /druid/**}，须由网关转发到系统模块；
 * 若仅在 Nacos 里配了 {@code /system/**} 等路由而没有 {@code /druid/**}，网关会把该路径当成静态资源并报 404。
 */
@Configuration
public class DruidGatewayRouteConfiguration {

    @Bean
    public RouteLocator druidMonitorRoute(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("hongshu-system-druid", r -> r
                        .order(-10)
                        .path("/druid/**")
                        .uri("lb://hongshu-system"))
                .build();
    }
}
