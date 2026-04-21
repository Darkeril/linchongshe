package com.hongshu.idle.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 订单VO
 *
 
 */
@Data
@Accessors(chain = true)
public class IdleProductOrderVO implements Serializable {

    private String id;

    /**
     * 订单编号
     */
    private String orderNumber;

    /**
     * 支付订单ID
     */
    private String paymentOrderId;

    /**
     * 商品ID
     */
    private String productId;

    /**
     * 商品发布用户ID
     */
    private String productUid;

    /**
     * 购买用户ID
     */
    private String buyUid;

    /**
     * 购买数量
     */
    private Integer productNum;

    /**
     * 快递费用
     */
    private Long productPostPrice;

    /**
     * 发货方式
     */
    private Integer productPostType;

    /**
     * 商品价格
     */
    private Long productMoney;

    /**
     * 应支付金额
     */
    private Long answerBuyMoney;

    /**
     * 实际支付金额
     */
    private Long actualBuyMoney;

    /**
     * 订单状态
     */
    private String orderStatus;

    /**
     * 交易状态
     */
    private String dealStatus;

    /**
     * 发货方式
     */
    private String postType;

    /**
     * 自提码
     */
    private String postSelfCode;

    /**
     * 收货人姓名
     */
    private String postUsername;

    /**
     * 收货人联系方式
     */
    private String postPhone;

    /**
     * 收货地址
     */
    private String postAddress;

    /**
     * 发货人姓名
     */
    private String shipUsername;

    /**
     * 发货人联系方式
     */
    private String shipPhone;

    /**
     * 发货地址
     */
    private String shipAddress;

    /**
     * 物流订单号
     */
    private String shipNum;

    /**
     * 物流公司
     */
    private String shipCompany;

    /**
     * 发货时间
     */
    private String shipTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    // 商品信息
    /**
     * 商品标题
     */
    private String productTitle;

    /**
     * 商品封面
     */
    private String productCover;

    /**
     * 商品描述
     */
    private String productDescription;

    // 用户信息
    /**
     * 发布者用户名
     */
    private String productUsername;

    /**
     * 发布者头像
     */
    private String productUserAvatar;

    /**
     * 购买者用户名
     */
    private String buyUsername;

    /**
     * 购买者头像
     */
    private String buyUserAvatar;

    // 状态显示文本
    /**
     * 订单状态文本
     */
    private String orderStatusText;

    /**
     * 交易状态文本
     */
    private String dealStatusText;
}
