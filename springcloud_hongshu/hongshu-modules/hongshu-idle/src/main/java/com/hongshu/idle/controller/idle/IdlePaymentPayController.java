package com.hongshu.idle.controller.idle;

import com.hongshu.common.core.enums.Result;
import com.hongshu.common.core.validator.myVaildator.noLogin.NoLoginIntercept;
import com.hongshu.idle.service.idle.IIdlePaymentPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 闲宝订单
 *
 * @author: hongshu
 */
@Slf4j
@RequestMapping("/payment/pay")
@RestController
public class IdlePaymentPayController {

    @Autowired
    private IIdlePaymentPayService paymentPayService;


    /**
     * 创建订单
     *
     * @param paymentOrderId 订单信息
     */
    @GetMapping("createPaymentPay")
    public Result<?> createPaymentPay(String paymentOrderId) {
        return Result.ok(paymentPayService.createPaymentPay(paymentOrderId));
    }

    /**
     * 支付
     * @param paymentPayId 支付记录ID
     * @param paymentMethod 支付方式（可选）："qrCode"=扫码支付，"miniprogram"=小程序支付，"form"或不传=表单跳转（默认）
     * @param openid 小程序用户openid（小程序支付时必传）
     */
    @PostMapping("/finish")
    public Result<?> finishPay(@RequestParam String paymentPayId, 
                                @RequestParam(required = false) String paymentMethod,
                                @RequestParam(required = false) String openid) {
        try {
            log.info("收到支付请求: paymentPayId={}, paymentMethod={}, openid={}", paymentPayId, paymentMethod, openid);
            Map<String, Object> result = paymentPayService.finishPay(paymentPayId, paymentMethod, openid);
            log.info("支付请求处理成功: paymentPayId={}", paymentPayId);
            return Result.ok(result);
        } catch (Exception e) {
            log.error("支付请求处理失败: paymentPayId={}", paymentPayId, e);
            // 异常会被全局异常处理器捕获，这里只记录日志
            throw e;
        }
    }

    /**
     * 取消支付
     */
    @PostMapping("/cancel")
    public Result<?> cancelPay(String paymentPayId) {
        paymentPayService.cancelPay(paymentPayId);
        return Result.ok();
    }

    /**
     * 查询支付结果
     */
    @GetMapping("/status")
    public Result<?> payStatus(String paymentPayId) {
        return Result.ok(paymentPayService.payStatus(paymentPayId));
    }

    /**
     * 更新支付状态
     */
    @PostMapping("/updateOrderStatus")
    public Result<?> updateOrderStatus(String paymentOrderId) {
        paymentPayService.updateOrderStatus(paymentOrderId);
        return Result.ok();
    }

    /**
     * 支付宝支付结果回调
     */
    @NoLoginIntercept
    @PostMapping("/alipay/notify")
    public String handleAlipayNotify(HttpServletRequest request) {
        log.info("收到支付宝回调");
        return paymentPayService.handleAlipayNotify(request);
    }

    /**
     * 微信支付结果回调
     */
    @NoLoginIntercept
    @PostMapping("/wechat/notify")
    public String handleWeChatPayNotify(HttpServletRequest request) {
        log.info("收到微信支付回调");
        return paymentPayService.handleWeChatPayNotify(request);
    }
}
