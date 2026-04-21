package com.hongshu.web.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 演示账号配置DTO
 */
@Data
public class DemoAccountConfigDTO {
    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 演示账号用户名
     */
    private String username;

    /**
     * 演示账号密码
     */
    private String password;

    /**
     * 演示账号描述
     */
    private String description;

    /**
     * 权限范围
     */
    private List<String> permissions;

    /**
     * 有效期
     */
    private String expireTime;
}

