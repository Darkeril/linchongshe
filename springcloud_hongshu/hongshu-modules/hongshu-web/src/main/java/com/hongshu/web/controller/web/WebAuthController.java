package com.hongshu.web.controller.web;

import com.hongshu.common.core.enums.Result;
import com.hongshu.common.core.enums.ResultCodeEnum;
import com.hongshu.common.core.utils.JwtUtilss;
import com.hongshu.common.core.utils.RedisUtils;
import com.hongshu.common.core.validator.myVaildator.noLogin.NoLoginIntercept;
import com.hongshu.web.domain.dto.AuthUserDTO;
import com.hongshu.web.domain.entity.WebUser;
import com.hongshu.web.domain.vo.QrCodeConfirmVO;
import com.hongshu.web.service.web.IWebAuthUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 权限
 *
 * @author: hongshu
 */
@Tag(name = "权限模块")
@RestController
@RequestMapping("/auth")
@Slf4j
public class WebAuthController {

    @Autowired
    private IWebAuthUserService authUserService;

    @Autowired
    private RedisUtils redisUtils;


    /**
     * 用户登录
     *
     * @param authUserDTO 用户
     */
    @Operation(summary = "用户登录")
    @PostMapping("login")
    @NoLoginIntercept
    public Result<?> login(@RequestBody AuthUserDTO authUserDTO) {
        authUserDTO.setTerminal("web");
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
        authUserDTO.setTerminal("web");
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
        authUserDTO.setTerminal("web");
        Map<String, Object> map = authUserService.demoLogin(authUserDTO);
        return Result.ok(map);
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
        authUserDTO.setTerminal("web");
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
        String terminal = "web" ;
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
     * 生成二维码
     */
    @GetMapping("/qrcode")
    @NoLoginIntercept
    public Result<?> getQrCode() {
        return Result.ok(authUserService.getQrCode());
    }

    /**
     * 移动端扫描二维码
     */
    @PostMapping("/qrcode/scan")
    @NoLoginIntercept
    public Result<?> scanQrCode(@RequestParam String loginToken) {
        return Result.ok(authUserService.scanQrCode(loginToken));
    }

    /**
     * 移动端确认登录
     */
    @PostMapping("/qrcode/confirm")
    @NoLoginIntercept
    public Result<?> confirmLogin(@RequestBody QrCodeConfirmVO vo) {
        return Result.ok(authUserService.confirmQrCodeLogin(vo));
    }

    /**
     * Web端轮询二维码状态
     */
    @GetMapping("/qrcode/status")
    @NoLoginIntercept
    public Result<?> checkQrCodeStatus(@RequestParam String loginToken) {
        return Result.ok(authUserService.checkQrCodeStatus(loginToken));
    }

    /**
     * 二维码登录成功，获取用户信息和token
     */
    @PostMapping("/qrcode/login")
    @NoLoginIntercept
    public Result<?> qrCodeLogin(@RequestParam String userId) {
        return Result.ok(authUserService.qrCodeLogin(userId));
    }

    /**
     * 生成微信网页授权URL
     *
     * @param redirectUri 回调地址（可选，如果不传则使用默认回调地址）
     */
    @Operation(summary = "生成微信网页授权URL")
    @GetMapping("/wechat/authUrl")
    @NoLoginIntercept
    public Result<?> generateWeChatAuthUrl(@RequestParam(required = false) String redirectUri,
                                           HttpServletRequest httpRequest) {
        // 优先从配置中获取回调地址
        String configCallback = authUserService.getWeChatCallbackUrl();
        if (StringUtils.isNotBlank(configCallback)) {
            redirectUri = configCallback;
            log.info("使用配置的回调地址: {}", redirectUri);
        }
        
        // 如果配置的回调地址或传入的回调地址不完整（只有域名），补充完整路径
        if (StringUtils.isNotBlank(redirectUri) && !redirectUri.contains("/web/auth/wechat/callback")) {
            // 如果只是域名，补充完整路径
            if (!redirectUri.endsWith("/")) {
                redirectUri = redirectUri + "/web/auth/wechat/callback";
            } else {
                redirectUri = redirectUri + "web/auth/wechat/callback";
            }
            log.info("补充完整回调地址路径: {}", redirectUri);
        } else if (StringUtils.isBlank(redirectUri) && httpRequest != null) {
            // 如果没有配置且没有传入回调地址，使用默认回调地址
            String scheme = httpRequest.getScheme(); // http 或 https
            String serverName = httpRequest.getServerName(); // 域名
            int serverPort = httpRequest.getServerPort(); // 端口
            String contextPath = httpRequest.getContextPath(); // 上下文路径
            
            // 构建默认回调地址
            if (serverPort == 80 || serverPort == 443) {
                redirectUri = String.format("%s://%s%s/web/auth/wechat/callback", 
                        scheme, serverName, contextPath);
            } else {
                redirectUri = String.format("%s://%s:%d%s/web/auth/wechat/callback", 
                        scheme, serverName, serverPort, contextPath);
            }
            
            // 如果是localhost，给出警告
            if (redirectUri.contains("localhost") || redirectUri.contains("127.0.0.1")) {
                log.warn("回调地址包含localhost，可能导致微信授权失败。请在系统配置中设置 wechat.web.callback.url");
            }
        }
        
        String authUrl = authUserService.generateWeChatAuthUrl(redirectUri);
        return Result.ok(authUrl);
    }

    /**
     * 微信网页登录回调
     *
     * @param code 微信授权码
     * @param state 状态参数
     */
    @Operation(summary = "微信网页登录回调")
    @GetMapping("/wechat/callback")
    @NoLoginIntercept
    public void wechatCallback(@RequestParam String code, 
                               @RequestParam(required = false) String state,
                               HttpServletRequest request,
                               HttpServletResponse response) {
        try {
            Map<String, Object> result = authUserService.wechatWebLogin(code);
            
            // 生成临时token，存到Redis（有效期5分钟）
            String tempToken = java.util.UUID.randomUUID().toString().replace("-", "");
            String tokenKey = "wechat:login:temp:" + tempToken;
            
            // 将登录结果存到Redis（JSON格式）
            String loginData = com.alibaba.fastjson2.JSON.toJSONString(result);
            redisUtils.setEx(tokenKey, loginData, 5, TimeUnit.MINUTES);
            
            // 构建前端页面URL
            String scheme = request.getScheme();
            String serverName = request.getServerName();
            int serverPort = request.getServerPort();
            
            // 从请求头获取前端URL，或使用默认值
            String referer = request.getHeader("Referer");
            String frontendUrl = referer != null ? referer.substring(0, referer.lastIndexOf("/")) : 
                (serverPort == 80 || serverPort == 443 ? 
                    String.format("%s://%s", scheme, serverName) : 
                    String.format("%s://%s:%d", scheme, serverName, serverPort));
            
            // 重定向到前端页面，带上临时token
            String redirectUrl = frontendUrl + "?wechat_login_token=" + tempToken;
            response.sendRedirect(redirectUrl);
            
        } catch (Exception e) {
            log.error("微信登录回调处理失败: code={}, state={}", code, state, e);
            try {
                // 返回错误页面
                String scheme = request.getScheme();
                String serverName = request.getServerName();
                int serverPort = request.getServerPort();
                String frontendUrl = serverPort == 80 || serverPort == 443 ? 
                    String.format("%s://%s", scheme, serverName) : 
                    String.format("%s://%s:%d", scheme, serverName, serverPort);
                response.sendRedirect(frontendUrl + "?wechat_login_error=1&message=" + 
                    java.net.URLEncoder.encode("微信登录失败: " + e.getMessage(), "UTF-8"));
            } catch (Exception ex) {
                log.error("重定向失败", ex);
            }
        }
    }
    
    /**
     * 通过临时token获取登录信息
     */
    @Operation(summary = "通过临时token获取微信登录信息")
    @GetMapping("/wechat/getLoginInfo")
    @NoLoginIntercept
    public Result<?> getWeChatLoginInfo(@RequestParam String tempToken) {
        try {
            String tokenKey = "wechat:login:temp:" + tempToken;
            String loginData = redisUtils.get(tokenKey);
            
            if (StringUtils.isBlank(loginData)) {
                return Result.build(400, "临时token无效或已过期");
            }
            
            // 删除临时token（一次性使用）
            redisUtils.delete(tokenKey);
            
            // 解析并返回登录信息
            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) com.alibaba.fastjson2.JSON.parseObject(loginData, Map.class);
            return Result.okAuthBody(result);
            
        } catch (Exception e) {
            log.error("获取微信登录信息失败: tempToken={}", tempToken, e);
            return Result.build(500, "获取登录信息失败: " + e.getMessage());
        }
    }

}
