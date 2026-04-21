package com.hongshu.web.controller.app;

import com.hongshu.common.core.domain.R;
import com.hongshu.common.core.enums.Result;
import com.hongshu.common.core.utils.RedisUtils;
import com.hongshu.common.core.validator.myVaildator.noLogin.NoLoginIntercept;
import com.hongshu.system.api.RemoteFileService;
import com.hongshu.system.api.domain.SysFile;
import com.hongshu.web.async.SmsAsyncService;
import com.hongshu.web.utils.WeChatUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.util.Random;
import java.util.Map;

/**
 * 第三方服务控制器
 *
 * @author: hongshu
 */
@Tag(name = "第三方服务模块")
@RestController
@RequestMapping({"/web/app/third", "/app/third"})
@Slf4j
public class AppThirdController {

    @Autowired
    private WeChatUtil weChatUtil;
    @Resource
    private RedisUtils redisUtils;
    @Autowired
    private SmsAsyncService smsAsyncService;
    @Autowired
    private RemoteFileService remoteFileService;

    // Redis Key 前缀（与 SmsAsyncService 中保存验证码保持一致）
    private static final String VERIFY_CODE_PREFIX = "sms:verify:";
    // 发送频率限制（秒）
    private static final long SEND_CODE_INTERVAL_SECONDS = 60;


    /**
     * 获取微信JS-SDK签名配置
     *
     * @param url 当前页面的完整URL（去掉hash部分）
     * @return 微信JS-SDK配置
     */
    @Operation(summary = "获取微信JS-SDK签名配置")
    @GetMapping("/getWeChatJSSDKConfig")
    @NoLoginIntercept
    public Result<?> getWeChatJSSDKConfig(@RequestParam(required = false) String url) {
        try {
            // 如果URL为空，返回错误
            if (StringUtils.isBlank(url)) {
                log.warn("获取微信JS-SDK配置失败: URL参数为空");
                return Result.build(400, "URL参数不能为空");
            }

            // 调用工具类获取配置
            Map<String, String> config = weChatUtil.getWeChatJSSDKConfig(url);

            log.info("成功获取微信JS-SDK配置: url={}", url);
            return Result.ok(config);

        } catch (Exception e) {
            log.error("获取微信JS-SDK配置失败: url={}", url, e);
            return Result.build(500, "获取微信JS-SDK配置失败: " + e.getMessage());
        }
    }

    @Operation(summary = "发送绑定手机号验证码")
    @GetMapping("/sendBindPhoneSms")
    public Result<?> sendBindPhoneSms(@RequestParam String phoneNumber) {
        return this.sendSmsCode(phoneNumber, "bind");
    }

    @Operation(summary = "发送重置手机号验证码（用于验证旧手机号）")
    @GetMapping("/sendResetPhoneSms")
    public Result<?> sendResetPhoneSms(@RequestParam String phoneNumber) {
        return this.sendSmsCode(phoneNumber, "reset");
    }

    @Operation(summary = "校验绑定手机号验证码")
    @PostMapping("/checkBindSmsCode")
    public Result<?> checkBindSmsCode(@RequestParam String phoneNumber, @RequestParam String smsCode) {
        return this.checkSmsCode(phoneNumber, smsCode);
    }

    @Operation(summary = "校验重置手机号验证码")
    @PostMapping("/checkResetSmsCode")
    public Result<?> checkResetSmsCode(@RequestParam String phoneNumber, @RequestParam String smsCode) {
        return this.checkSmsCode(phoneNumber, smsCode);
    }

    @Operation(summary = "上传语音文件")
    @PostMapping("/uploadAudio")
    public Result<?> uploadAudio(@RequestPart("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.build(400, "上传文件不能为空");
        }
        try {
            R<SysFile> uploadResult = remoteFileService.upload(file);
            if (uploadResult == null || uploadResult.getCode() != 200 || uploadResult.getData() == null) {
                log.error("语音文件上传失败，文件名: {}，结果: {}", file.getOriginalFilename(), uploadResult);
                return Result.build(500, "语音上传失败，请稍后重试");
            }
            String url = uploadResult.getData().getUrl();
            if (StringUtils.isBlank(url)) {
                log.error("语音文件上传成功但返回URL为空，文件名: {}", file.getOriginalFilename());
                return Result.build(500, "语音上传失败，文件URL为空");
            }
            return Result.ok(url);
        } catch (Exception e) {
            log.error("语音文件上传异常，文件名: {}", file != null ? file.getOriginalFilename() : "null", e);
            return Result.build(500, "语音上传失败：" + e.getMessage());
        }
    }

    private Result<?> sendSmsCode(String phoneNumber, String biz) {
        if (StringUtils.isBlank(phoneNumber) || !phoneNumber.matches("^1[3-9]\\d{9}$")) {
            return Result.build(400, "手机号格式不正确");
        }
        String intervalKey = VERIFY_CODE_PREFIX + "interval:" + phoneNumber;
        if (!redisUtils.setIfAbsent(intervalKey, "1", SEND_CODE_INTERVAL_SECONDS)) {
            return Result.build(429, "发送验证码太频繁，请稍后再试");
        }
        String code = String.format("%06d", new Random().nextInt(1000000));
        smsAsyncService.sendVerificationCodeAsync(phoneNumber, intervalKey, code);
        // 前端历史代码期望 20010 代表发送成功，这里兼容返回
        return Result.build(20010, "发送成功");
    }

    private Result<?> checkSmsCode(String phoneNumber, String smsCode) {
        if (StringUtils.isBlank(phoneNumber) || StringUtils.isBlank(smsCode)) {
            return Result.build(400, "参数不能为空");
        }
        String codeKey = VERIFY_CODE_PREFIX + "code:" + phoneNumber;
        String saved = redisUtils.get(codeKey);
        if (StringUtils.isBlank(saved)) {
            return Result.build(400, "验证码不存在或已过期");
        }
        if (!smsCode.equals(saved)) {
            return Result.build(400, "验证码错误");
        }
        // 通过后删除验证码
        redisUtils.delete(codeKey);
        // 前端历史代码期望 20020 代表校验成功，这里兼容返回
        return Result.build(20020, "校验成功");
    }
}


