package com.hongshu.web.domain.dto;

import lombok.Data;

/**
 * 自动封禁配置DTO
 */
@Data
public class AutobanConfigDTO {

    private Boolean enabled;

    private Integer failThreshold;

    private Integer successThreshold;

    private Integer timeWindowMinutes;

    private Integer banHours;
}
