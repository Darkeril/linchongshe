package com.hongshu.idle.service.idle;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hongshu.idle.domain.entity.IdlePaymentPay;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 闲宝订单
 *

 */
public interface IIdlePaymentPayService extends IService<IdlePaymentPay> {

    /**
     * 创建订单
     *
     * @param paymentOrderId 订单信息
     */
    Object createPaymentPay(String paymentOrderId);

    /**
     * 支付
     * @param paymentPayId 支付记录ID
     * @param paymentMethod 支付方式（可选）："qrCode"=扫码支付，"miniprogram"=小程序支付，"form"=表单跳转（默认）
     * @param openid 小程序用户openid（小程序支付时必传）
     */
    Map<String, Object> finishPay(String paymentPayId, String paymentMethod, String openid);

    /**
     * 取消支付
     */
    void cancelPay(String paymentPayId);

    /**
     * 查询支付结果
     */
    Object payStatus(String paymentPayId);

    /**
     * 更新支付状态
     */
    void updateOrderStatus(String paymentOrderId);

    /**
     * 支付宝支付结果回调
     */
    String handleAlipayNotify(HttpServletRequest request);

    /**
     * 微信支付结果回调
     */
    String handleWeChatPayNotify(HttpServletRequest request);
}
