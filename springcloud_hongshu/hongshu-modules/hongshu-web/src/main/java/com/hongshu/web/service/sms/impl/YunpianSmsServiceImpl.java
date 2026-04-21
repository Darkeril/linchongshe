package com.hongshu.web.service.sms.impl;

import com.hongshu.common.core.utils.StringUtils;
import com.hongshu.web.service.sms.ISmsService;
import com.yunpian.sdk.YunpianClient;
import com.yunpian.sdk.model.Result;
import com.yunpian.sdk.model.SmsSingleSend;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;


/**
 * 短信服务
 *
 * @author: hongshu
 */
@Slf4j
public class YunpianSmsServiceImpl implements ISmsService {

    private final YunpianClient client;
    private final String signName;
    private static final int MAX_RETRY_TIMES = 2;
    private static final long RETRY_INTERVAL_MS = 1000;

    // 定义错误码
    private static final int SUCCESS_CODE = 0;
    private static final int MOBILE_ERROR = 1;
    private static final int RATE_LIMIT_ERROR = 11;
    private static final int BALANCE_ERROR = 12;

    public YunpianSmsServiceImpl(String apiKey, String signName) {
        this.signName = signName;
        try {
            this.client = new YunpianClient(apiKey).init();
        } catch (Exception e) {
            log.error("初始化云片客户端失败", e);
            throw new RuntimeException("初始化云片客户端失败", e);
        }
    }

    @Override
    public boolean sendVerificationCode(String phone, String code) {
        // 参数校验
        this.validateParams(phone, code);

        int retryCount = 0;
        while (retryCount < MAX_RETRY_TIMES) {
            try {
                Map<String, String> params = new HashMap<>();
                params.put("mobile", phone);
                params.put("text", String.format("【%s】您的验证码是%s，有效期为5分钟。", signName, code));

                Result<SmsSingleSend> result = client.sms().single_send(params);

                if (result != null) {
                    log.info("云片短信发送结果: phone={}, code={}, message={}", phone, result.getCode(), result.getMsg());

                    if (result.isSucc()) {
                        return true;
                    }

                    // 处理特定错误码
                    switch (result.getCode()) {
                        case MOBILE_ERROR:
                            throw new IllegalArgumentException("手机号格式不正确");
                        case RATE_LIMIT_ERROR:
                            log.warn("短信发送频率超限: phone={}", phone);
                            Thread.sleep(RETRY_INTERVAL_MS);
                            break;
                        case BALANCE_ERROR:
                            log.error("账户余额不足");
                            return false;
                        default:
                            log.error("云片短信发送失败: phone={}, code={}, message={}", phone, result.getCode(), result.getMsg());
                    }
                }
            } catch (IllegalArgumentException e) {
                log.error("参数验证失败: {}", e.getMessage());
                return false;
            } catch (Exception e) {
                log.error("云片短信发送异常: phone={}, retry={}, error={}", phone, retryCount, e.getMessage());

                if (retryCount == MAX_RETRY_TIMES - 1) {
                    return false;
                }

                try {
                    Thread.sleep(RETRY_INTERVAL_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("重试等待被中断", ie);
                    return false;
                }
            }
            retryCount++;
        }
        return false;
    }

    private void validateParams(String phone, String code) {
        if (StringUtils.isEmpty(phone)) {
            throw new IllegalArgumentException("手机号不能为空");
        }
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            throw new IllegalArgumentException("手机号格式不正确");
        }
        if (StringUtils.isEmpty(code)) {
            throw new IllegalArgumentException("验证码不能为空");
        }
        if (!code.matches("^\\d{4,6}$")) {
            throw new IllegalArgumentException("验证码格式不正确");
        }
    }

    @PreDestroy
    public void destroy() {
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                log.error("关闭云片客户端失败", e);
            }
        }
    }
}
