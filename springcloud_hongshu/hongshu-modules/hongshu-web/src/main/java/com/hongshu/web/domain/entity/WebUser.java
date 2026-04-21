package com.hongshu.web.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hongshu.web.domain.HongshuBaseEntity;
import lombok.Data;

import java.util.Date;

/**
 * 用户
 *
 * @author: hongshu
 */
@Data
@TableName("web_user")
public class WebUser extends HongshuBaseEntity {

    /**
     * 来因ID
     */
    private Long hsId;

    /**
     * 用户来源
     */
    private Integer fromType;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 性别
     */
    private String gender;

    /**
     * 电话
     */
    private String phone;

    /**
     * email
     */
    private String email;

    /**
     * 描述
     */
    private String description;

    /**
     * 用户状态：0 正常，1 封禁，2 待审核（开启用户审核开关时新注册用户）
     */
    private String status;

    /**
     * 生日
     */
    private String birthday;

    /**
     * 省
     */
    private String province;

    /**
     * 市
     */
    private String city;

    /**
     * 区
     */
    private String district;

    /**
     * 地址
     */
    private String address;

    /**
     * 用户封面
     */
    private String userCover;

    /**
     * 用户标签
     */
    private String tags;

    /**
     * 笔记数量
     */
    private Long noteCount;

    /**
     * 闲宝数量
     */
    private Long productCount;

    /**
     * 关注数量
     */
    private Long followerCount;

    /**
     * 粉丝数量
     */
    private Long fanCount;

    /**
     * 最后登录IP
     */
    private String loginIp;

    /**
     * 最后登录时间
     */
    private Date loginDate;

    /**
     * 备注
     */
    private String remark;

    /**
     * 微信openid
     */
    @TableField("openid")
    private String openId;

    /**
     * 删除标识（0：正常 1：删除）
     */
    private Integer deleted;
}
