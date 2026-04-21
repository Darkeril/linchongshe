package com.hongshu.idle.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hongshu.idle.domain.HongshuBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@TableName("idle_payment_pay")
public class IdlePaymentPay extends HongshuBaseEntity {

    /**
     * 订单ID
     */
    @Schema(description = "订单ID")
    private String orderId;

    /**
     * 用户id
     */
    @Schema(description = "用户id")
    private String uid;

    /**
     * 支付金额（单位：元，DECIMAL类型）
     */
    @Schema(description = "支付金额", example = "0")
    private BigDecimal paymentPrice;

    /**
     * 支付类型
     */
    @Schema(description = "支付类型")
    private String paymentType;

    /**
     * 支付返回数据
     */
    @Schema(description = "支付返回数据")
    private String paymentResultData;

    /**
     * 交易起始时间
     */
    @Schema(description = "交易起始时间")
    private LocalDateTime paymentTimeStart;

    /**
     * 交易结束时间
     */
    @Schema(description = "交易结束时间")
    private LocalDateTime paymentTimeExpire;

    /**
     * 支付状态，0待支付，1支付中，2支付失败,8已退款，9支付成功
     */
    @Schema(description = "支付状态，0待支付，1支付中，2支付失败,8已退款，9支付成功", example = "0")
    private String paymentStatus;

    /**
     * 支付状态处理、0不处理，1未处理、9已处理
     */
    @Schema(description = "支付状态处理、0不处理，1未处理、9已处理", example = "0")
    private Integer processStatus;

    /**
     * 完成时间
     */
    @Schema(description = "完成时间")
    private Date finishTime;
}
