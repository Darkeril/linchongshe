package com.hongshu.web.service.sms.impl;

import com.hongshu.web.service.sms.ISmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


/**
 * 短信服务
 *
 * @author: hongshu
 */
@Service
@Slf4j
public class FallbackSmsServiceImpl implements ISmsService {

    private final ISmsService primaryService;  // 主用服务（阿里云）
    private final ISmsService secondaryService; // 备用服务（云片）


    public FallbackSmsServiceImpl(ISmsService primary, ISmsService secondary) {
        this.primaryService = primary;
        this.secondaryService = secondary;
    }

    @Override
    public boolean sendVerificationCode(String phone, String code) {
        // 优先尝试主用服务
        try {
            if (primaryService.sendVerificationCode(phone, code)) {
                log.info("主用短信服务发送成功");
                return true;
            }
        } catch (Exception e) {
            log.warn("主用服务异常，触发降级", e);
        }
        // 主用失败时尝试备用服务
        if (secondaryService.sendVerificationCode(phone, code)) {
            log.info("备用短信服务发送成功");
            return true;
        }
        // 全部失败
        log.error("所有短信服务均失败！phone={}", phone);
        return false;
    }
}
