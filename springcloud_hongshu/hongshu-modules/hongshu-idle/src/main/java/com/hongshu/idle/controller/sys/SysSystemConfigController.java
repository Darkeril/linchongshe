package com.hongshu.idle.controller.sys;


import com.hongshu.common.core.enums.Result;
import com.hongshu.common.core.validator.myVaildator.noLogin.NoLoginIntercept;
import com.hongshu.common.core.web.controller.BaseController;
import com.hongshu.idle.domain.dto.AlipayConfigDTO;
import com.hongshu.idle.domain.dto.PaymentGlobalConfigDTO;
import com.hongshu.idle.domain.dto.WeChatPayConfigDTO;
import com.hongshu.idle.service.sys.ISysSystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * 系统配置
 *
 * @author: hongshu
 */
@Tag(name = "系统配置相关接口", description = "系统配置相关接口")
@RestController
@RequestMapping("/config")
@Slf4j
public class SysSystemConfigController extends BaseController {

    @Autowired
    private ISysSystemConfigService systemConfigService;


    @NoLoginIntercept
    @Operation(summary = "获取系统配置", description = "获取系统配置")
    @GetMapping("/systemConfig")
    public Result<?> getSystemConfig() {
        return Result.ok(systemConfigService.getSystemConfig());
    }

    @NoLoginIntercept
    @Operation(summary = "获取支付宝配置", description = "获取支付宝配置")
    @GetMapping("/alipayConfig")
    public Result<?> getAlipayConfig() {
        return Result.ok(systemConfigService.getAlipayConfig());
    }

    @Operation(summary = "更新支付宝配置", description = "更新支付宝配置")
    @PostMapping("/alipayConfig")
    public Result<?> updateAlipayConfig(@RequestBody AlipayConfigDTO alipayConfig) {
        systemConfigService.updateAlipayConfig(alipayConfig);
        return Result.ok();
    }

    @NoLoginIntercept
    @Operation(summary = "获取微信支付配置", description = "获取微信支付配置")
    @GetMapping("/wechatPayConfig")
    public Result<?> getWeChatPayConfig() {
        return Result.ok(systemConfigService.getWeChatPayConfig());
    }

    @Operation(summary = "更新微信支付配置", description = "更新微信支付配置")
    @PostMapping("/wechatPayConfig")
    public Result<?> updateWeChatPayConfig(@RequestBody WeChatPayConfigDTO weChatPayConfig) {
        systemConfigService.updateWeChatPayConfig(weChatPayConfig);
        return Result.ok();
    }

    @NoLoginIntercept
    @Operation(summary = "获取支付全局配置", description = "获取支付全局配置（全局支付开关、支付方式选择）")
    @GetMapping("/paymentGlobalConfig")
    public Result<?> getPaymentGlobalConfig() {
        return Result.ok(systemConfigService.getPaymentGlobalConfig());
    }

    @Operation(summary = "更新支付全局配置", description = "更新支付全局配置（全局支付开关、支付方式选择）")
    @PostMapping("/paymentGlobalConfig")
    public Result<?> updatePaymentGlobalConfig(@RequestBody PaymentGlobalConfigDTO paymentGlobalConfig) {
        systemConfigService.updatePaymentGlobalConfig(paymentGlobalConfig);
        return Result.ok();
    }

}

