package com.hongshu.web.controller.app;

import com.hongshu.common.core.enums.Result;
import com.hongshu.common.core.enums.ResultCodeEnum;
import com.hongshu.common.core.utils.JwtUtilss;
import com.hongshu.common.core.validator.myVaildator.noLogin.NoLoginIntercept;
import com.hongshu.web.domain.dto.AuthUserDTO;
import com.hongshu.web.domain.dto.WeChatLoginDTO;
import com.hongshu.web.domain.entity.WebUser;
import com.hongshu.web.service.web.IWebAuthUserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 权限
 *
 * @author: hongshu
 */
@RestController
@RequestMapping("/app/auth")
@Slf4j
public class AppAuthController {

    @Autowired
    private IWebAuthUserService authUserService;


    /**
     * 用户登录
     *
     * @param authUserDTO 用户
     */
    @Operation(summary = "用户登录")
    @PostMapping("login")
    @NoLoginIntercept
    public Result<?> login(@RequestBody AuthUserDTO authUserDTO) {
        authUserDTO.setTerminal("app");
        Map<String, Object> map = authUserService.login(authUserDTO);
        return Result.okAuthBody(map);
    }

    /**
     * 验证码登录
     *
     * @param authUserDTO 用户
     */
    @Operation(summary = "验证码登录")
    @PostMapping("loginByCode")
    @NoLoginIntercept
    public Result<?> loginByCode(@RequestBody AuthUserDTO authUserDTO) {
        authUserDTO.setTerminal("app");
        Map<String, Object> map = authUserService.loginByCode(authUserDTO);
        return Result.okAuthBody(map);
    }

    /**
     * 发送验证码
     *
     * @param phone 手机号
     */
    @PostMapping("/sendCode")
    @NoLoginIntercept
    public Result<?> sendVerifyCode(@RequestParam String phone, @RequestParam String captchaVerification) {
        return Result.ok(authUserService.sendVerifyCode(phone, captchaVerification));
    }

    /**
     * 演示账户登录
     *
     * @param authUserDTO 用户
     */
    @Operation(summary = "演示账户登录")
    @PostMapping("demoLogin")
    @NoLoginIntercept
    public Result<?> demoLogin(@RequestBody AuthUserDTO authUserDTO) {
        authUserDTO.setTerminal("app");
        Map<String, Object> map = authUserService.demoLogin(authUserDTO);
        return Result.ok(map);
    }

    @GetMapping("/checkToken")
    public Result<?> checkToken() {
        return Result.ok();
    }

    /**
     * 根据token获取当前用户
     *
     * @param accessToken accessToken
     */
    @Operation(summary = "根据token获取当前用户")
    @GetMapping("getUserInfoByToken")
    public Result<?> getUserInfoByToken(String accessToken) {
        boolean checkToken = JwtUtilss.checkToken(accessToken);
        if (!checkToken) {
            // accessToken过期，刷新accessToken
            return Result.build(ResultCodeEnum.TOKEN_EXIST.getCode(), ResultCodeEnum.TOKEN_EXIST.getMessage());
        }
        WebUser user = authUserService.getUserInfoByToken(accessToken);
        return Result.ok(user);
    }

    /**
     * 用户注册
     *
     * @param authUserDTO 前台传递用户信息
     */
    @Operation(summary = "用户注册")
    @PostMapping("register")
    @NoLoginIntercept
    public Result<?> register(@RequestBody AuthUserDTO authUserDTO) {
        authUserDTO.setTerminal("app");
        WebUser user = authUserService.register(authUserDTO);
        return Result.ok(user);
    }

    /**
     * 用户是否注册
     *
     * @param authUserDTO 用户
     */
    @Operation(summary = "用户是否注册")
    @PostMapping("isRegister")
    public Result<?> isRegister(@RequestBody AuthUserDTO authUserDTO) {
        boolean flag = authUserService.isRegister(authUserDTO);
        return Result.ok(flag);
    }

    /**
     * 退出登录
     *
     * @param userId 用户ID
     */
    @Operation(summary = "退出登录")
    @GetMapping("loginOut")
    public Result<?> loginOut(String userId) {
        String terminal = "app";
        authUserService.loginOut(userId, terminal);
        return Result.ok();
    }

    /**
     * 修改密码
     *
     * @param authUserDTO 用户
     */
    @Operation(summary = "修改密码")
    @PostMapping("updatePassword")
    public Result<?> updatePassword(@RequestBody AuthUserDTO authUserDTO) {
        Boolean flag = authUserService.updatePassword(authUserDTO);
        return Result.ok(flag);
    }

    /**
     * 刷新token
     *
     * @param refreshToken refreshToken
     */
    @Operation(summary = "刷新token")
    @GetMapping("refreshToken")
    @NoLoginIntercept
    public Result<?> refreshToken(String refreshToken) {
        boolean checkToken = JwtUtilss.checkToken(refreshToken);
        if (!checkToken) {
            //refreshToken过期，跳转登录界面
            return Result.build(ResultCodeEnum.TOKEN_FAIL.getCode(), ResultCodeEnum.TOKEN_FAIL.getMessage());
        }
        Map<String, Object> map = authUserService.refreshToken(refreshToken);
        return Result.ok(map);
    }

    /**
     * 微信小程序登录
     *
     * @param weChatLoginDTO 微信登录信息
     */
    @Operation(summary = "微信小程序登录")
    @PostMapping("wechatLogin")
    @NoLoginIntercept
    public Result<?> wechatLogin(@RequestBody WeChatLoginDTO weChatLoginDTO) {
        weChatLoginDTO.setTerminal("applet");
        Map<String, Object> map = authUserService.wechatLogin(weChatLoginDTO);
        return Result.okAuthBody(map);
    }
}
