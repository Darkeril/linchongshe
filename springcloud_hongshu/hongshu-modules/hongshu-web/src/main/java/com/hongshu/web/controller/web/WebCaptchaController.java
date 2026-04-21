package com.hongshu.web.controller.web;

import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.properties.AjCaptchaProperties;
import com.anji.captcha.service.CaptchaService;
import com.anji.captcha.util.StringUtils;
import com.hongshu.common.core.enums.Result;
import com.hongshu.common.core.validator.myVaildator.noLogin.NoLoginIntercept;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/captcha")
public class WebCaptchaController {

    @Autowired(required = false)
    private CaptchaService captchaService;
    @Autowired(required = false)
    private AjCaptchaProperties ajCaptchaProperties;


    /**
     * 获取验证码类型
     */
    @NoLoginIntercept
    @GetMapping("/type")
    public Result<?> captchaType() {
        if (ajCaptchaProperties == null) {
            return Result.fail("验证码服务未启用");
        }
        String captchaType = ajCaptchaProperties.getType().getCodeValue();
        return Result.ok(captchaType);
    }

    @NoLoginIntercept
    @PostMapping("/get")
    public ResponseModel get(@RequestBody CaptchaVO data, HttpServletRequest request) {
        if (captchaService == null) {
            return ResponseModel.errorMsg("验证码服务未启用");
        }
        assert request.getRemoteHost() != null;
        data.setBrowserInfo(getRemoteId(request));
        return captchaService.get(data);
    }

    @NoLoginIntercept
    @PostMapping("/check")
    public ResponseModel check(@RequestBody CaptchaVO data, HttpServletRequest request) {
        if (captchaService == null) {
            return ResponseModel.errorMsg("验证码服务未启用");
        }
        data.setBrowserInfo(getRemoteId(request));
        return captchaService.check(data);
    }

    /***
     * 服务端验证接口，独立部署的场景使用，集成部署的场景：服务内部调用，不需要调用此接口，可注释掉
     * @param data
     * @param request
     * @return
     */
    @PostMapping("/verify")
    public ResponseModel verify(@RequestBody CaptchaVO data, HttpServletRequest request) {
        if (captchaService == null) {
            return ResponseModel.errorMsg("验证码服务未启用");
        }
        return captchaService.verification(data);
    }

    public static final String getRemoteId(HttpServletRequest request) {
        String xfwd = request.getHeader("X-Forwarded-For");
        String ip = getRemoteIpFromXfwd(xfwd);
        String ua = request.getHeader("user-agent");
        if (StringUtils.isNotBlank(ip)) {
            return ip + ua;
        }
        return request.getRemoteAddr() + ua;
    }

    private static String getRemoteIpFromXfwd(String xfwd) {
        if (StringUtils.isNotBlank(xfwd)) {
            String[] ipList = xfwd.split(",");
            return StringUtils.trim(ipList[0]);
        }
        return null;
    }

}
