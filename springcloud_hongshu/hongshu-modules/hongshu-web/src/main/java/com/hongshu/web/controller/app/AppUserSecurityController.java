package com.hongshu.web.controller.app;

import com.hongshu.common.core.enums.Result;
import com.hongshu.common.core.validator.myVaildator.noLogin.NoLoginIntercept;
import com.hongshu.web.domain.dto.AuthUserDTO;
import com.hongshu.web.domain.dto.UserPasswordOldDTO;
import com.hongshu.web.service.web.IWebUserSecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 兼容前端历史 /user/* 接口（账号与安全）
 */
@Tag(name = "用户安全（兼容接口）")
@RestController
@RequestMapping("/app/user")
@RequiredArgsConstructor
public class AppUserSecurityController {

    private final IWebUserSecurityService securityService;


    @Operation(summary = "获取第三方绑定状态")
    @GetMapping("/getUserIsBindThird")
    public Result<?> getUserIsBindThird() {
        return Result.ok(securityService.getThirdBindStatus());
    }

    @Operation(summary = "更新绑定手机号")
    @PostMapping("/updatePhoneNumber")
    public Result<?> updatePhoneNumber(@RequestParam String phoneNumber, @RequestParam String smsCode) {
        return Result.ok(securityService.updatePhoneNumber(phoneNumber, smsCode));
    }

    /**
     * 忘记密码：通过手机号 + 短信验证码重置密码
     */
    @Operation(summary = "手机号重置密码")
    @PostMapping("resetPasswordByPhone")
    @NoLoginIntercept
    public Result<?> resetPasswordByPhone(@RequestBody AuthUserDTO authUserDTO) {
        return Result.ok(securityService.resetPasswordByPhone(authUserDTO));
    }


    @Operation(summary = "通过原密码修改密码")
    @PostMapping("/resetPasswordByOld")
    public Result<?> resetPasswordByOld(@RequestBody UserPasswordOldDTO passwordVO) {
        return Result.ok(securityService.resetPasswordByOld(passwordVO));
    }

    @Operation(summary = "注销账号（软禁用）")
    @PostMapping("/cancelAccount")
    public Result<?> cancelAccount() {
        return Result.ok(securityService.cancelAccount());
    }
}

