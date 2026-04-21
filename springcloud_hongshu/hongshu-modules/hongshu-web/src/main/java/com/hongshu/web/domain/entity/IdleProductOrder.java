package com.hongshu.web.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hongshu.web.domain.HongshuBaseEntity;
import lombok.Data;

/**

 */
@Data
@TableName("idle_product_order")
public class IdleProductOrder extends HongshuBaseEntity {

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
    private long productPostPrice;

    /**
     * 发货方式
     */
    private Integer productPostType;

    /**
     * 商品价格
     */
    private Integer productMoney;

    /**
     * 应支付金额
     */
    private Integer answerBuyMoney;

    /**
     * 实际支付金额
     */
    private Integer actualBuyMoney;

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


    @TableField(exist = false)  // 表示该字段不在数据库表中
    private String productTitle;  // 商品标题

    @TableField(exist = false)
    private String productCover;  // 商品封面
}
