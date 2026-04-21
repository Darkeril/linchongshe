package com.hongshu.web.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 网站配置表
 *
 * @author: hongshu
 */
@Data
@TableName("web_website_config")
public class WebWebsiteConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 网站logo
     */
    private String logo;

    /**
     * 网站名称
     */
    private String name;

    /**
     * 网站作者
     */
    private String author;

    /**
     * 网站标题
     */
    private String title;

    /**
     * 网站描述
     */
    private String description;

    /**
     * 版权申明
     */
    private String copyright;

    /**
     * 微信二维码
     */
    private String wechatQrCode;

    /**
     * 关注我们
     */
    private Boolean followUs;

    /**
     * 微信收款二维码
     */
    private Boolean wechatRewardQrCode;

    /**
     * 支付宝收款二维码
     */
    private Boolean alipayRewardQrCode;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
