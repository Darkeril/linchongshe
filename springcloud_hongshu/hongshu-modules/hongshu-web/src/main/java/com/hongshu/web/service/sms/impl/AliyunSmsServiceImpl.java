package com.hongshu.web.service.sms.impl;

import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.hongshu.common.core.utils.StringUtils;
import com.hongshu.web.service.sms.ISmsService;
import lombok.extern.slf4j.Slf4j;


/**
 * 短信服务
 *
 * @author: hongshu
 */
@Slf4j
public class AliyunSmsServiceImpl implements ISmsService {

    private final com.aliyun.dysmsapi20170525.Client client;
    private final String signName;
    private final String templateCode;

    // 重试次数和间隔时间
    private static final int MAX_RETRY_TIMES = 3;
    private static final long RETRY_INTERVAL_MS = 1000;

    public AliyunSmsServiceImpl(String accessKeyId, String accessKeySecret, String signName, String templateCode) {
        this.client = createClient(accessKeyId, accessKeySecret);
        this.signName = signName;
        this.templateCode = templateCode;
    }

    private com.aliyun.dysmsapi20170525.Client createClient(String accessKeyId, String accessKeySecret) {
        try {
            com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                    .setAccessKeyId(accessKeyId)
                    .setAccessKeySecret(accessKeySecret)
                    // 访问的域名
                    .setEndpoint("dysmsapi.aliyuncs.com");
            return new com.aliyun.dysmsapi20170525.Client(config);
        } catch (Exception e) {
            log.error("创建阿里云短信客户端失败", e);
            throw new RuntimeException("初始化阿里云短信客户端失败", e);
        }
    }

    @Override
    public boolean sendVerificationCode(String phone, String code) {
        // 参数校验
        this.validateParams(phone, code);

        int retryCount = 0;
        while (retryCount < MAX_RETRY_TIMES) {
            try {
                SendSmsRequest request = new SendSmsRequest()
                        .setPhoneNumbers(phone)
                        .setSignName(signName)
                        .setTemplateCode(templateCode)
                        .setTemplateParam("{\"code\":\"" + code + "\"}");

                SendSmsResponse response = client.sendSms(request);

                // 详细的响应处理
                if (response != null && response.getBody() != null) {
                    String responseCode = response.getBody().getCode();
                    String message = response.getBody().getMessage();
                    String requestId = response.getBody().getRequestId();

                    log.info("阿里云短信发送结果: phone={}, code={}, message={}, requestId={}",
                            phone, responseCode, message, requestId);

                    if ("OK".equals(responseCode)) {
                        return true;
                    } else {
                        // 处理特定的错误码
                        switch (responseCode) {
                            case "isv.MOBILE_NUMBER_ILLEGAL":
                                throw new IllegalArgumentException("手机号格式不正确");
                            case "isv.BUSINESS_LIMIT_CONTROL":
                                throw new RuntimeException("发送频率超限");
                            case "isv.OUT_OF_SERVICE":
                                throw new RuntimeException("业务停机");
                            default:
                                log.error("阿里云短信发送失败: phone={}, code={}, message={}",
                                        phone, responseCode, message);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("阿里云短信发送异常: phone={}, retry={}, error={}",
                        phone, retryCount, e.getMessage());

                // 如果是参数异常，直接抛出不重试
                if (e instanceof IllegalArgumentException) {
                    try {
                        throw e;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                // 最后一次重试失败，抛出异常触发降级
                if (retryCount == MAX_RETRY_TIMES - 1) {
                    throw new RuntimeException("阿里云短信服务异常", e);
                }

                // 重试等待
                try {
                    Thread.sleep(RETRY_INTERVAL_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("重试被中断", ie);
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
}
