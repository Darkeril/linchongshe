package com.hongshu.web.domain.dto;

import lombok.Data;

/**
 * 演示站配置DTO
 */
@Data
public class DemoSiteConfigDTO {
    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 移动端地址
     */
    private String mobileUrl;

    /**
     * Web端地址
     */
    private String webUrl;

    /**
     * 管理端地址
     */
    private String adminUrl;

    /**
     * Arco管理端地址
     */
    private String arcoAdminUrl;

    /**
     * Gitee地址
     */
    private String giteeUrl;

    /**
     * GitHub地址
     */
    private String githubUrl;

    /**
     * 文档地址
     */
    private String docUrl;

    /**
     * 演示站描述
     */
    private String description;

    /**
     * 是否启用访问统计
     */
    private Boolean enableStats;
}
