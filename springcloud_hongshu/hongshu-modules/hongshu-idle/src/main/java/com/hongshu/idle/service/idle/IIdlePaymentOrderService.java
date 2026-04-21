package com.hongshu.idle.service.idle;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hongshu.idle.domain.entity.IdlePaymentOrder;

/**
 * 闲宝订单
 *

 */
public interface IIdlePaymentOrderService extends IService<IdlePaymentOrder> {

    /**
     * 创建订单
     *
     * @param productOrderId 订单信息
     * @param payType 支付类型（1-微信支付，2-支付宝支付，默认支付宝）
     */
    String createPaymentOrder(String productOrderId, String payType);

    Object getPaymentOrder(String paymentOrderId);
}
