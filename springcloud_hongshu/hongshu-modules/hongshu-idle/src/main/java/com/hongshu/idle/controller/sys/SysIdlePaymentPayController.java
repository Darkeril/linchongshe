package com.hongshu.idle.controller.sys;

import com.hongshu.common.core.enums.Result;
import com.hongshu.common.core.validator.myVaildator.noLogin.NoLoginIntercept;
import com.hongshu.common.core.web.controller.BaseController;
import com.hongshu.idle.service.sys.ISysIdlePaymentPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 闲宝信息
 *
 */
@RestController
@RequestMapping("/payment")
public class SysIdlePaymentPayController extends BaseController {

    @Autowired
    private ISysIdlePaymentPayService payService;


    /**
     * 更新超时订单状态
     */
    @NoLoginIntercept
    @PostMapping("/updateExpireStatus")
    public Result<?> updateExpireStatus() {
        payService.updateExpireStatus();
        return Result.ok();
    }

    /**
     * 定时查询并更新已支付订单
     */
    @NoLoginIntercept
    @PostMapping("/updatePayStatus")
    public Result<?> updatePayStatus() {
        payService.updatePayStatus();
        return Result.ok();
    }
}
