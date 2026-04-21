package com.hongshu.system.api;

import com.hongshu.common.core.constant.ServiceNameConstants;
import com.hongshu.common.core.domain.R;
import com.hongshu.system.api.factory.RemoteIdleFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 闲宝服务
 */
@FeignClient(contextId = "remoteIdleService", value = ServiceNameConstants.IDLE_SERVICE, fallbackFactory = RemoteIdleFallbackFactory.class)
public interface RemoteIdleService {

    @GetMapping(value = "/product/productCount")
    R<?> getProductCount();

    @GetMapping(value = "/product/productCountByType")
    R<?> getProductCountByType();

    @GetMapping(value = "/order/orderDataByWeek")
    R<?> getOrderDataByWeek(@RequestParam(value = "days", defaultValue = "7") int days);

    @GetMapping(value = "/product/productCountByCategory")
    R<?> getProductCountByCategory();

    @GetMapping(value = "/product/productContributeCount")
    R<?> getProductContributeCount();

    @PostMapping(value = "/payment/updateExpireStatus")
    R<?> updateExpireStatus();

    @PostMapping(value = "/payment/updatePayStatus")
    R<?> updatePayStatus();

    @PostMapping(value = "/product/refreshProductData")
    R<?> refreshProductData();

    /**
     * 刷新支付配置（支付宝和微信支付）
     */
    @PostMapping(value = "/config/refreshPayConfig")
    R<?> refreshPayConfig();

    /**
     * 刷新支付宝配置
     */
    @PostMapping(value = "/config/refreshAlipayConfig")
    R<?> refreshAlipayConfig();

    /**
     * 刷新微信支付配置
     */
    @PostMapping(value = "/config/refreshWeChatPayConfig")
    R<?> refreshWeChatPayConfig();
}
