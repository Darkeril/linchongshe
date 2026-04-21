package com.hongshu.web.async;

import com.hongshu.common.core.utils.RedisUtils;
import com.hongshu.web.service.sms.ISmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Slf4j
@Service
public class SmsAsyncService {

    @Resource
    private RedisUtils redisUtils;
    @Autowired
    private ISmsService smsService;


    // Redis中验证码的key前缀
    private static final String VERIFY_CODE_PREFIX = "sms:verify:";
    // 验证码有效期（分钟）
    private static final long VERIFY_CODE_EXPIRE_MINUTES = 5;
    // 同一手机号发送验证码的间隔时间（秒）
    private static final long SEND_CODE_INTERVAL_SECONDS = 60;


    @Async
    public void sendVerificationCodeAsync(String phone, String intervalKey, String code) {
        //  发送验证码
        if (smsService.sendVerificationCode(phone, code)) {
            // 发送成功，将验证码存入Redis
            String codeKey = VERIFY_CODE_PREFIX + "code:" + phone;
            redisUtils.set(codeKey, code, VERIFY_CODE_EXPIRE_MINUTES * 60); // 转换为秒
            log.info("验证码发送成功: phone={},code={}", phone, code);
        }
        log.error("验证码发送失败: phone={}", phone);
    }
}
