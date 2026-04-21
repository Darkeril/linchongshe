package com.hongshu.idle.service.sys;

/**
 * 闲宝信息 服务层
 *

 */
public interface ISysIdlePaymentPayService {

    /**
     * 更新超时订单状态
     */
    void updateExpireStatus();

    /**
     * 定时查询并更新已支付订单
     */
    void updatePayStatus();
}
