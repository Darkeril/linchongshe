package com.hongshu.web.service.sms;

/**
 * 短信服务
 *
 * @author: hongshu
 * <p>
 * 主用阿里云：默认优先调用阿里云短信API。
 * 异常降级：捕获阿里云发送失败异常（如网络超时、余额不足、频控等），自动切换至云片。
 * 失败兜底：记录发送日志，若全部失败可触发邮件/钉钉告警。
 */
public interface ISmsService {

    /**
     * 发送短信验证码
     *
     * @param phone 手机号
     * @param code  验证码
     * @return true表示成功
     */
    boolean sendVerificationCode(String phone, String code);
}
