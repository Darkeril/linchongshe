package com.hongshu.ai.domain.vo;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会员用户对象 VO
 *
 * @author: myj
 * @date: 2023-04-28
 * @version: 1.0.0
 */
@Data
public class UserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 创建人
     */
    private String createUser;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;

    /**
     * uuid
     */
    private String uid;

    /**
     * 姓名
     */
    private String name;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 手机号
     */
    private String tel;

    /**
     * 密码
     */
    @JsonIgnore
    private String password;

    /**
     * 头像
     */
    private String avatar;

    /**
     * openid
     */
    private String openid;

    /**
     * 登录ip
     */
    private String ip;

    /**
     * 登录ip
     */
    private String address;

    /**
     * 是否开启上下文
     */
    private Boolean context;

    /**
     * 调用次数
     */
    private Long num;

    /**
     * 邀请人
     */
    private Long shareId;

    /**
     * 用户类型 1 微信小程序 2 公众号 3 手机号
     */
    private Integer type;

    /**
     * 状态 0 禁用 1 启用
     */
    private Integer status;

    /**
     * 会员类型 1 普通 会员 2 尊享会员
     */
    private Integer vipType;

    /**
     * 剩余重置次数
     */
    private Integer resetCount;

    /**
     * 当日限制请求次数
     */
    private Integer limitCount;
}
