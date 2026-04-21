package com.hongshu.common.datasource.config;

import com.alibaba.druid.support.jakarta.StatViewServlet;
import com.alibaba.druid.support.jakarta.WebStatFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * druid-spring-boot-3-starter 在配合 dynamic-datasource 等场景时，常常<strong>不会</strong>注册
 * {@link StatViewServlet}；若本类不补注册，访问 {@code /druid/login.html} 会落到 Spring MVC 静态资源处理并出现
 * {@code No static resource druid/login.html}（与是否使用动态数据源无关）。
 * <p>
 * 仅在已开启 {@code spring.datasource.druid.stat-view-servlet.enabled=true}，
 * 且 starter 未注册名为 {@code statViewServletRegistrationBean} 的 Bean 时生效，避免重复注册。
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(StatViewServlet.class)
@ConditionalOnProperty(prefix = "spring.datasource.druid.stat-view-servlet", name = "enabled", havingValue = "true")
public class DruidStatViewServletManualConfiguration {

    private static final String STAT_PREFIX = "spring.datasource.druid.stat-view-servlet.";
    private static final String FILTER_PREFIX = "spring.datasource.druid.web-stat-filter.";

    @Bean
    @ConditionalOnMissingBean(name = "statViewServletRegistrationBean")
    public ServletRegistrationBean<StatViewServlet> hongshuDruidStatViewServletRegistration(Environment env) {
        String pattern = env.getProperty(STAT_PREFIX + "url-pattern", "/druid/*");
        ServletRegistrationBean<StatViewServlet> bean = new ServletRegistrationBean<>(new StatViewServlet(), pattern);
        bean.setName("hongshuDruidStatViewServlet");

        String user = firstNonBlank(
                env.getProperty(STAT_PREFIX + "login-username"),
                env.getProperty(STAT_PREFIX + "loginUsername"),
                "admin");
        String password = firstNonBlank(
                env.getProperty(STAT_PREFIX + "login-password"),
                env.getProperty(STAT_PREFIX + "loginPassword"),
                "admin123");

        bean.addInitParameter("loginUsername", user);
        bean.addInitParameter("loginPassword", password);
        String resetEnable = env.getProperty(STAT_PREFIX + "reset-enable",
                env.getProperty(STAT_PREFIX + "resetEnable", "true"));
        bean.addInitParameter("resetEnable", resetEnable);
        // 仅当显式配置了非空 allow 才限制 IP。starter 在「未配置 allow」时会默认 127.0.0.1；请在 YAML 中显式 allow: "" 覆盖（见 hongshu-system application.yml）。
        String allow = env.getProperty(STAT_PREFIX + "allow");
        if (allow != null && !allow.isBlank()) {
            bean.addInitParameter("allow", allow);
        }
        return bean;
    }

    @Bean
    @ConditionalOnProperty(prefix = FILTER_PREFIX, name = "enabled", havingValue = "true")
    @ConditionalOnMissingBean(name = "webStatFilterRegistrationBean")
    public FilterRegistrationBean<WebStatFilter> hongshuDruidWebStatFilterRegistration(Environment env) {
        FilterRegistrationBean<WebStatFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new WebStatFilter());
        bean.addUrlPatterns("/*");
        String exclusions = env.getProperty(FILTER_PREFIX + "exclusions",
                "*.js,*.gif,*.jpg,*.bmp,*.png,*.css,*.ico,/druid/*");
        bean.addInitParameter("exclusions", exclusions);
        bean.addInitParameter("profileEnable", env.getProperty(FILTER_PREFIX + "profile-enable", "true"));
        bean.setName("hongshuDruidWebStatFilter");
        return bean;
    }

    private static String firstNonBlank(String a, String b, String defaultVal) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        if (b != null && !b.isBlank()) {
            return b;
        }
        return defaultVal;
    }
}
