package com.hongshu.web.controller.sys;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.hongshu.common.audit.dto.BaiduQianfanConfigDTO;
import com.hongshu.common.core.enums.Result;
import com.hongshu.common.core.validator.myVaildator.noLogin.NoLoginIntercept;
import com.hongshu.common.core.web.controller.BaseController;
import com.hongshu.web.domain.dto.*;
import com.hongshu.web.service.sys.ISysSystemConfigService;
import com.hongshu.web.service.web.impl.IpProtectionService;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

/**
 * 系统配置
 *
 * @author: hongshu
 */
@Tag(name = "系统配置相关接口", description = "系统配置相关接口")
@RestController
@RequestMapping("/system/config")
@Slf4j
public class SysSystemConfigController extends BaseController {

    @Autowired
    private ISysSystemConfigService systemConfigService;

    @Autowired
    private IpProtectionService ipProtectionService;


    @Operation(summary = "获取所有配置", description = "获取所有配置")
    @GetMapping("/allConfig")
    public Result<?> getAllConfig() {
        return Result.ok(systemConfigService.getAllConfigData());
    }

    @NoLoginIntercept
    @Operation(summary = "获取网站配置", description = "获取网站配置")
    @GetMapping("/websiteConfig")
    public Result<?> getWebsiteConfig() {
        return Result.ok(systemConfigService.getWebsiteConfig());
    }

    @NoLoginIntercept
    @Operation(summary = "获取系统配置", description = "获取系统配置")
    @GetMapping("/systemConfig")
    public Result<?> getSystemConfig() {
        return Result.ok(systemConfigService.getSystemConfig());
    }

    @NoLoginIntercept
    @Operation(summary = "获取本地存储配置", description = "获取本地存储配置")
    @GetMapping("/localConfig")
    public Result<?> getLocalConfig() {
        return Result.ok(systemConfigService.getLocalConfig());
    }

    @NoLoginIntercept
    @Operation(summary = "获取oss配置", description = "获取oss配置")
    @GetMapping("/ossConfig")
    public Result<?> getOssConfig() {
        return Result.ok(systemConfigService.getOssConfig());
    }

    @NoLoginIntercept
    @Operation(summary = "获取oss配置（内部使用，不脱敏）", description = "获取oss配置（内部使用，不脱敏）")
    @GetMapping("/ossConfigInternal")
    public Result<?> getOssConfigInternal() {
        return Result.ok(systemConfigService.getOssConfigInternal());
    }

    @NoLoginIntercept
    @Operation(summary = "获取短信配置", description = "获取短信配置")
    @GetMapping("/smsConfig")
    public Result<?> getSmsConfig() {
        return Result.ok(systemConfigService.getSmsConfig());
    }

    @NoLoginIntercept
    @Operation(summary = "获取短信配置（内部使用，不脱敏）", description = "获取短信配置（内部使用，不脱敏）")
    @GetMapping("/smsConfigInternal")
    public Result<?> getSmsConfigInternal() {
        return Result.ok(systemConfigService.getSmsConfigInternal());
    }

    @NoLoginIntercept
    @Operation(summary = "获取支付宝配置", description = "获取支付宝配置")
    @GetMapping("/alipayConfig")
    public Result<?> getAlipayConfig() {
        return Result.ok(systemConfigService.getAlipayConfig());
    }

    @NoLoginIntercept
    @Operation(summary = "获取微信支付配置", description = "获取微信支付配置")
    @GetMapping("/wechatPay")
    public Result<?> getWeChatPayConfig() {
        return Result.ok(systemConfigService.getWeChatPayConfig());
    }

    @NoLoginIntercept
    @Operation(summary = "获取验证码配置", description = "获取验证码配置")
    @GetMapping("/captchaConfig")
    public Result<?> getCaptchaConfig() {
        return Result.ok(systemConfigService.getCaptchaConfig());
    }

    @NoLoginIntercept
    @Operation(summary = "获取高德地图配置", description = "获取高德地图配置")
    @GetMapping("/amapConfig")
    public Result<?> getAmapConfig() {
        return Result.ok(systemConfigService.getAmapConfig());
    }

    @NoLoginIntercept
    @Operation(summary = "获取演示账号配置", description = "获取演示账号配置")
    @GetMapping("/demoAccountConfig")
    public Result<?> getDemoAccountConfig() {
        return Result.ok(systemConfigService.getDemoAccountConfig());
    }

    @NoLoginIntercept
    @Operation(summary = "获取演示站配置", description = "获取演示站配置")
    @GetMapping("/demoSiteConfig")
    public Result<?> getDemoSiteConfig() {
        return Result.ok(systemConfigService.getDemoSiteConfig());
    }

    @NoLoginIntercept
    @Operation(summary = "获取黑名单列表", description = "获取黑名单列表")
    @GetMapping("/blacklist")
    public Result<?> getBlacklistConfig() {
        return Result.ok(systemConfigService.getBlacklistConfig());
    }

    @NoLoginIntercept
    @Operation(summary = "获取市集轮播图配置", description = "用于 uniapp 市集页轮播图")
    @GetMapping("/carousel")
    public Result<?> getCarouselConfig() {
        return Result.ok(systemConfigService.getCarouselConfig());
    }

    @Operation(summary = "更新市集轮播图配置", description = "更新市集页轮播图列表")
    @PostMapping("/carousel")
    public Result<?> updateCarouselConfig(@RequestBody List<CarouselItemDTO> list) {
        systemConfigService.updateCarouselConfig(list);
        return Result.ok();
    }

    @Operation(summary = "获取管理端工作台轮播图配置", description = "Arco 工作台右侧轮播，结构与市集轮播单项一致")
    @GetMapping("/carousel/workbench")
    public Result<?> getWorkbenchCarouselConfig() {
        return Result.ok(systemConfigService.getWorkbenchCarouselConfig());
    }

    @Operation(summary = "更新管理端工作台轮播图配置", description = "更新工作台右侧轮播图列表")
    @PostMapping("/carousel/workbench")
    public Result<?> updateWorkbenchCarouselConfig(@RequestBody List<CarouselItemDTO> list) {
        systemConfigService.updateWorkbenchCarouselConfig(list);
        return Result.ok();
    }

    @NoLoginIntercept
    @Operation(summary = "获取管理端登录页圆形展示区配置", description = "Arco 管理端登录页左侧圆框视频/图片")
    @GetMapping("/adminLoginPromo")
    public Result<?> getAdminLoginPromoConfig() {
        return Result.ok(systemConfigService.getAdminLoginPromoConfig());
    }

    @Operation(summary = "更新管理端登录页圆形展示区配置", description = "固定 7 个槽位，与前端默认布局顺序一致")
    @PostMapping("/adminLoginPromo")
    public Result<?> updateAdminLoginPromoConfig(@RequestBody List<AdminLoginPromoCircleDTO> list) {
        systemConfigService.updateAdminLoginPromoConfig(list);
        return Result.ok();
    }

    @NoLoginIntercept
    @Operation(summary = "获取自动审核配置（百度千帆）", description = "获取自动审核配置（百度千帆）")
    @GetMapping("/autoAuditConfig")
    public Result<?> getAutoAuditConfig() {
        return Result.ok(systemConfigService.getBaiduQianfanConfig());
    }

    @Operation(summary = "更新自动审核配置（百度千帆）", description = "更新自动审核配置（百度千帆）")
    @PostMapping("/autoAudit")
    public Result<?> updateAutoAuditConfig(@RequestBody BaiduQianfanConfigDTO baiduQianfanConfig) {
        systemConfigService.updateBaiduQianfanConfig(baiduQianfanConfig);
        return Result.ok();
    }

    @Operation(summary = "更新网站配置", description = "更新网站配置")
    @PostMapping("/website")
    public Result<?> updateWebsiteConfig(@RequestBody WebsiteConfigDTO websiteConfig) {
        systemConfigService.updateWebsiteConfig(websiteConfig);
        return Result.ok();
    }

    @Operation(summary = "更新系统配置", description = "更新系统配置")
    @PostMapping("/system")
    public Result<?> updateSystemConfig(@RequestBody SystemConfigDTO systemConfig) {
        systemConfigService.updateSystemConfig(systemConfig);
        return Result.ok();
    }

    @Operation(summary = "更新本地存储配置", description = "更新本地存储配置")
    @PostMapping("/local")
    public Result<?> updateLocalConfig(@RequestBody LocalConfigDTO localConfig) {
        systemConfigService.updateLocalConfig(localConfig);
        return Result.ok();
    }

    @Operation(summary = "更新oss配置", description = "更新oss配置")
    @PostMapping("/oss")
    public Result<?> updateOssConfig(@RequestBody Map<String, Object> request) {
        String provider = (String) request.get("provider");
        OssConfigDTO ossConfig = new ObjectMapper().convertValue(request.get("config"), OssConfigDTO.class);
        systemConfigService.updateOssConfig(provider, ossConfig);
        return Result.ok();
    }

    @Operation(summary = "更新短信配置", description = "更新短信配置")
    @PostMapping("/sms")
    public Result<?> updateSmsConfig(@RequestBody Map<String, Object> request) {
        String provider = (String) request.get("provider");
        SmsConfigDTO smsConfig = new ObjectMapper().convertValue(request.get("config"), SmsConfigDTO.class);
        systemConfigService.updateSmsConfig(provider, smsConfig);
        return Result.ok();
    }

    @Operation(summary = "更新支付宝配置", description = "更新支付宝配置")
    @PostMapping("/alipay")
    public Result<?> updateAlipayConfig(@RequestBody AlipayConfigDTO alipayConfig) {
        systemConfigService.updateAlipayConfig(alipayConfig);
        return Result.ok();
    }

    @Operation(summary = "更新微信支付配置", description = "更新微信支付配置")
    @PostMapping("/wechatPay")
    public Result<?> updateWeChatPayConfig(@RequestBody WeChatPayConfigDTO weChatPayConfig) {
        systemConfigService.updateWeChatPayConfig(weChatPayConfig);
        return Result.ok();
    }

    @Operation(summary = "更新微信登录配置", description = "更新微信登录配置")
    @PostMapping("/wechatLogin")
    public Result<?> updateWeChatLoginConfig(@RequestBody WeChatLoginConfigDTO weChatLoginConfig) {
        systemConfigService.updateWeChatLoginConfig(weChatLoginConfig);
        return Result.ok();
    }

    @NoLoginIntercept
    @Operation(summary = "获取微信登录配置", description = "获取微信登录配置")
    @GetMapping("/wechatLogin")
    public Result<?> getWeChatLoginConfig() {
        WeChatLoginConfigDTO config = systemConfigService.getWeChatLoginConfig();
        return Result.ok(config);
    }

    @NoLoginIntercept
    @Operation(summary = "获取IM微信登录配置", description = "获取IM微信小程序登录配置（内部调用）")
    @GetMapping("/imWechatLogin")
    public Result<?> getImWeChatLoginConfig() {
        WeChatLoginConfigDTO config = systemConfigService.getImWeChatLoginConfig();
        return Result.ok(config);
    }

    @Operation(summary = "更新IM微信登录配置", description = "更新IM微信小程序登录配置")
    @PostMapping("/imWechatLogin")
    public Result<?> updateImWeChatLoginConfig(@RequestBody WeChatLoginConfigDTO config) {
        systemConfigService.updateImWeChatLoginConfig(config);
        return Result.ok();
    }

    @Operation(summary = "更新验证码配置", description = "更新验证码配置")
    @PostMapping("/captcha")
    public Result<?> updateCaptchaConfig(@RequestBody CaptchaConfigDTO captchaConfig) {
        systemConfigService.updateCaptchaConfig(captchaConfig);
        return Result.ok();
    }

    @Operation(summary = "更新高德地图配置", description = "更新高德地图配置")
    @PostMapping("/amap")
    public Result<?> updateAmapConfig(@RequestBody AmapConfigDTO amapConfig) {
        systemConfigService.updateAmapConfig(amapConfig);
        return Result.ok();
    }

    @Operation(summary = "更新演示账号配置", description = "更新演示账号配置")
    @PostMapping("/demoAccount")
    public Result<?> updateDemoAccount(@RequestBody DemoAccountConfigDTO demoAccountConfig) {
        systemConfigService.updateDemoAccount(demoAccountConfig);
        return Result.ok();
    }

    @Operation(summary = "更新演示站配置", description = "更新演示站配置")
    @PostMapping("/demoSite")
    public Result<?> updateDemoSite(@RequestBody DemoSiteConfigDTO demoSiteConfig) {
        systemConfigService.updateDemoSite(demoSiteConfig);
        return Result.ok();
    }

    @Operation(summary = "更新黑名单启用状态", description = "更新黑名单功能启用状态")
    @PostMapping("/blacklist/enabled")
    public Result<?> updateBlacklistEnabled(@RequestBody Map<String, Boolean> request) {
        Boolean enabled = request.get("enabled");
        systemConfigService.updateBlacklistEnabled(enabled);
        return Result.ok();
    }

    @Operation(summary = "添加黑名单项", description = "添加IP黑名单")
    @PostMapping("/blacklist/add")
    public Result<?> addBlacklistItem(@RequestBody @Valid BlacklistItemDTO blacklistItemDTO) {
        systemConfigService.addBlacklistItem(blacklistItemDTO);
        return Result.ok();
    }

    @Operation(summary = "解封黑名单项", description = "解封指定IP")
    @PostMapping("/blacklist/{id}/unban")
    public Result<?> unbanBlacklistItem(@PathVariable Long id) {
        systemConfigService.unbanBlacklistItem(id);
        return Result.ok();
    }

    @Operation(summary = "删除黑名单项", description = "删除指定黑名单项")
    @DeleteMapping("/blacklist/{id}")
    public Result<?> deleteBlacklistItem(@PathVariable Long id) {
        systemConfigService.deleteBlacklistItem(id);
        return Result.ok();
    }

    @Operation(summary = "获取自动封禁配置", description = "获取IP自动封禁相关配置")
    @GetMapping("/blacklist/autoban")
    public Result<?> getAutobanConfig() {
        return Result.ok(ipProtectionService.getAutobanConfig());
    }

    @Operation(summary = "更新自动封禁配置", description = "更新IP自动封禁相关配置")
    @PostMapping("/blacklist/autoban")
    public Result<?> updateAutobanConfig(@RequestBody AutobanConfigDTO autobanConfig) {
        ipProtectionService.updateAutobanConfig(autobanConfig);
        return Result.ok();
    }
}

