package com.hongshu.common.audit.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 审核模块自动配置
 *
 * @author hongshu
 */
@Configuration
@ComponentScan(basePackages = "com.hongshu.common.audit")
public class AuditAutoConfiguration {
    // 自动扫描 com.hongshu.common.audit 包下的所有组件
}
