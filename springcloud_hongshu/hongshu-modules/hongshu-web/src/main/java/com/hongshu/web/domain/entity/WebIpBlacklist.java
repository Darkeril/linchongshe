package com.hongshu.web.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * IP黑名单
 *
 * @author: hongshu
 */
@Data
@TableName("web_ip_blacklist")
public class WebIpBlacklist {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 被封禁的IP
     */
    private String ipAddress;

    /**
     * 封禁原因
     */
    private String reason;

    /**
     * 是否有效
     */
    private String status;

    /**
     * 封禁时间
     */
    private Date banTime;

    /**
     * 解封时间
     */
    private LocalDateTime expireTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
