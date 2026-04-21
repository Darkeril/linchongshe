package com.hongshu.idle.controller.app;

import com.hongshu.common.core.enums.Result;
import com.hongshu.idle.service.idle.IIdlePaymentOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * UniApp支付订单控制器
 *
 * @author: hongshu
 */
@Slf4j
@RequestMapping("/app/payment/order")
@RestController
public class AppPaymentOrderController {

    @Autowired
    private IIdlePaymentOrderService paymentService;

    /**
     * 创建支付订单
     *
     * @param productOrderId 商品订单ID
     * @param payType        支付类型（1-微信支付，2-支付宝支付，默认支付宝）
     */
    @GetMapping("createPayment")
    public Result<?> createPaymentOrder(@RequestParam String productOrderId, 
                                        @RequestParam(required = false, defaultValue = "2") String payType) {
        log.info("UniApp创建支付订单: productOrderId={}, payType={}", productOrderId, payType);
        return Result.ok(paymentService.createPaymentOrder(productOrderId, payType));
    }

    /**
     * 获取支付订单信息
     *
     * @param paymentOrderId 支付订单ID
     */
    @GetMapping("getPaymentOrder")
    public Result<?> getPaymentOrder(@RequestParam String paymentOrderId) {
        log.info("UniApp获取支付订单: paymentOrderId={}", paymentOrderId);
        return Result.ok(paymentService.getPaymentOrder(paymentOrderId));
    }
}



