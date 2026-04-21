package com.hongshu.idle.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hongshu.idle.domain.HongshuBaseEntity;
import lombok.Data;

import java.math.BigDecimal;

/**

 */
@Data
@TableName("idle_payment_order")
public class IdlePaymentOrder extends HongshuBaseEntity {

    /**
     * 订单编号
     */
    private String orderNumber;

    /**
     * 支付ID
     */
    private String paymentPayId;

    /**
     * 商品ID
     */
    private String productId;

    /**
     * 用户ID
     */
    private String uid;

    /**
     * 支付类型
     */
    private String payType;

    /**
     * 支付标题
     */
    private String payTypeName;

    /**
     * 总价（单位：元，DECIMAL类型）
     */
    private BigDecimal payPrice;

    /**
     * 订单状态
     */
    private String orderStatus;
}
