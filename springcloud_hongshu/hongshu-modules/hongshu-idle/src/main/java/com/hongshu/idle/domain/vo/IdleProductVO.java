package com.hongshu.idle.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 商品
 *

 */
@Data
@Accessors(chain = true)
public class IdleProductVO implements Serializable {

    private String id;

    private String paymentOrderId;

    private String productOrderNumber;

    /**
     * 用户ID
     */
    private String uid;

    /**
     * 用户ID
     */
    private String buyUid;

    /**
     * 标题
     */
    private String title;

    /**
     * 描述
     */
    private String description;

    /**
     * 封面图片
     */
    private String cover;

    /**
     * 图片集合
     */
    private String urls;

    /**
     * 类型
     */
    private Integer type;

    /**
     * 价格
     */
    private String price;

    /**
     * 原始价格
     */
    private String originalPrice;

    /**
     * 二级分类ID
     */
    private String cid;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 父级ID
     */
    private String cpid;

    /**
     * 父级分类名称
     */
    private String categoryParentName;

    /**
     * 商品内容类型（图片、视频、动图）
     */
    private Integer productType;

    /**
     * 发货方式
     */
    private Integer postType;

    /**
     * 想要的数量
     */
    private Long likeCount;


    private Boolean isCollection;

    /**
     * 地址代码
     */
    private String addCode;

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
     * 状态
     */
    private Integer status;

    private String dealStatus;

    /**
     * 审核状态
     */
    private String auditStatus;

    /**
     * 时间
     */
    private long time;

    private Date createTime;

    private Date updateTime;

    /**
     * 用户名
     */
    private String username;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 自提码
     */
    private String postSelfCode;
}
