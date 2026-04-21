package com.hongshu.web.service.sms.impl;

import com.hongshu.common.core.utils.StringUtils;
import com.hongshu.web.service.sms.ISmsService;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import com.tencentcloudapi.sms.v20210111.models.SendStatus;
import lombok.extern.slf4j.Slf4j;


/**
 * 短信服务
 *
 * @author: hongshu
 */
@Slf4j
public class TencentSmsServiceImpl implements ISmsService {

    private final SmsClient client;
    private final String sdkAppId;
    private final String signName;
    private final String templateId;

    public TencentSmsServiceImpl(String secretId, String secretKey, String sdkAppId,
                                 String signName, String templateId, String region) {
        try {
            Credential cred = new Credential(secretId, secretKey);
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setSignMethod(ClientProfile.SIGN_TC3_256);
            // 默认使用广州区域，可通过配置修改
            this.client = new SmsClient(cred, region != null ? region : "ap-guangzhou", clientProfile);
            this.sdkAppId = sdkAppId;
            this.signName = signName;
            this.templateId = templateId;
        } catch (Exception e) {
            log.error("初始化腾讯云短信客户端失败", e);
            throw new RuntimeException("初始化腾讯云短信客户端失败", e);
        }
    }

    @Override
    public boolean sendVerificationCode(String phone, String code) {
        if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(code)) {
            log.error("手机号或验证码为空: phone={}, code={}", phone, code);
            return false;
        }

        try {
            // 构建请求
            SendSmsRequest req = new SendSmsRequest();
            // 处理手机号格式
            req.setPhoneNumberSet(new String[]{formatPhoneNumber(phone)});
            req.setSmsSdkAppId(sdkAppId);
            req.setSignName(signName);
            req.setTemplateId(templateId);
            req.setTemplateParamSet(new String[]{code, "5"}); // 验证码和有效期（分钟）

            // 发送短信
            SendSmsResponse resp = client.SendSms(req);

            // 处理响应
            if (resp != null && resp.getSendStatusSet() != null && resp.getSendStatusSet().length > 0) {
                SendStatus sendStatus = resp.getSendStatusSet()[0];
                String responseCode = sendStatus.getCode();
                String message = sendStatus.getMessage();
                String requestId = resp.getRequestId();

                log.info("腾讯云短信发送结果: phone={}, code={}, message={}, requestId={}",
                        phone, responseCode, message, requestId);

                if ("Ok".equalsIgnoreCase(responseCode)) {
                    return true;
                } else {
                    log.error("腾讯云短信发送失败: phone={}, code={}, message={}",
                            phone, responseCode, message);
                }
            }
            return false;
        } catch (TencentCloudSDKException e) {
            log.error("腾讯云短信发送异常: phone={}, error={}", phone, e.getMessage(), e);
            throw new RuntimeException("短信发送失败", e);
        }
    }

    /**
     * 格式化手机号（添加国家代码）
     */
    private String formatPhoneNumber(String phone) {
        if (phone.startsWith("+")) {
            return phone;
        }
        return "+86" + phone; // 默认中国大陆号码
    }
}
