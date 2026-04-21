package com.hongshu.idle.controller.app;

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
 * UniApp支付控制器
 *
 * @author: hongshu
 */
@Slf4j
@RequestMapping("/app/payment/pay")
@RestController
public class AppPaymentPayController {

    @Autowired
    private IIdlePaymentPayService paymentPayService;

    /**
     * 创建支付记录
     *
     * @param paymentOrderId 支付订单ID
     */
    @GetMapping("createPaymentPay")
    public Result<?> createPaymentPay(@RequestParam String paymentOrderId) {
        log.info("UniApp创建支付记录: paymentOrderId={}", paymentOrderId);
        return Result.ok(paymentPayService.createPaymentPay(paymentOrderId));
    }

    /**
     * 完成支付（生成支付二维码或表单）
     *
     * @param paymentPayId 支付记录ID
     * @param paymentMethod 支付方式（可选）："qrCode"=扫码支付，"miniprogram"=小程序支付，"form"或不传=表单跳转（默认）
     * @param openid 小程序用户openid（小程序支付时必传）
     */
    @PostMapping("finish")
    public Result<?> finishPay(@RequestParam String paymentPayId,
                                @RequestParam(required = false) String paymentMethod,
                                @RequestParam(required = false) String openid) {
        try {
            log.info("UniApp收到支付请求: paymentPayId={}, paymentMethod={}, openid={}", paymentPayId, paymentMethod, openid);
            Map<String, Object> result = paymentPayService.finishPay(paymentPayId, paymentMethod, openid);
            log.info("UniApp支付请求处理成功: paymentPayId={}", paymentPayId);
            return Result.ok(result);
        } catch (Exception e) {
            log.error("UniApp支付请求处理失败: paymentPayId={}", paymentPayId, e);
            throw e;
        }
    }

    /**
     * 取消支付
     *
     * @param paymentPayId 支付记录ID
     */
    @PostMapping("cancel")
    public Result<?> cancelPay(@RequestParam String paymentPayId) {
        log.info("UniApp取消支付: paymentPayId={}", paymentPayId);
        paymentPayService.cancelPay(paymentPayId);
        return Result.ok();
    }

    /**
     * 查询支付状态
     *
     * @param paymentPayId 支付记录ID
     */
    @GetMapping("status")
    public Result<?> payStatus(@RequestParam String paymentPayId) {
        log.info("UniApp查询支付状态: paymentPayId={}", paymentPayId);
        return Result.ok(paymentPayService.payStatus(paymentPayId));
    }

    /**
     * 更新支付状态
     *
     * @param paymentOrderId 支付订单ID
     */
    @PostMapping("updateOrderStatus")
    public Result<?> updateOrderStatus(@RequestParam String paymentOrderId) {
        log.info("UniApp更新支付状态: paymentOrderId={}", paymentOrderId);
        paymentPayService.updateOrderStatus(paymentOrderId);
        return Result.ok();
    }

    /**
     * 支付宝支付结果回调
     */
    @NoLoginIntercept
    @PostMapping("alipay/notify")
    public String handleAlipayNotify(HttpServletRequest request) {
        log.info("UniApp收到支付宝回调");
        return paymentPayService.handleAlipayNotify(request);
    }

    /**
     * 微信支付结果回调
     */
    @NoLoginIntercept
    @PostMapping("wechat/notify")
    public String handleWeChatPayNotify(HttpServletRequest request) {
        log.info("UniApp收到微信支付回调");
        return paymentPayService.handleWeChatPayNotify(request);
    }
}


