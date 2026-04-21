package com.hongshu.web.config;

import com.anji.captcha.config.AjCaptchaAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 显式导入 Anji Captcha 的自动配置，避免某些环境下自动配置未被加载导致
 * {@code CaptchaService}、{@code AjCaptchaProperties} 注入为空。
 */
@Configuration
@Import(AjCaptchaAutoConfiguration.class)
public class CaptchaAutoImportConfig {
}




