package com.hongshu.idle.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 系统配置表
 *
 * @author: hongshu
 */
@Data
@TableName("web_system_config")
public class WebSystemConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 配置键
     */
    private String configKey;

    /**
     * 配置值
     */
    private String configValue;

    /**
     * 配置分组
     */
    private String configGroup;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否敏感字段
     */
    private Boolean isSensitive;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
