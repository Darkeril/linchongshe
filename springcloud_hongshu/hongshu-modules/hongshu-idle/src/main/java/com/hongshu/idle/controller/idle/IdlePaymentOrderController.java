package com.hongshu.idle.controller.idle;

import com.hongshu.common.core.enums.Result;
import com.hongshu.idle.service.idle.IIdlePaymentOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 闲宝订单
 *
 * @author: hongshu
 */
@RequestMapping("/payment/order")
@RestController
public class IdlePaymentOrderController {

    @Autowired
    private IIdlePaymentOrderService paymentService;


    /**
     * 创建订单
     *
     * @param productOrderId 订单信息
     * @param payType        支付类型（1-微信支付，2-支付宝支付，默认支付宝）
     */
    @GetMapping("createPayment")
    public Result<?> createOrder(String productOrderId, @RequestParam(required = false, defaultValue = "2") String payType) {
        return Result.ok(paymentService.createPaymentOrder(productOrderId, payType));
    }

    @GetMapping("getPaymentOrder")
    public Result<?> getPaymentOrder(String paymentOrderId) {
        return Result.ok(paymentService.getPaymentOrder(paymentOrderId));
    }
}
