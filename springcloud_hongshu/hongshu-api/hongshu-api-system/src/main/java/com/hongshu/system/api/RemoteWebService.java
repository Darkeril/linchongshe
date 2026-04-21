package com.hongshu.system.api;

import com.hongshu.common.core.constant.ServiceNameConstants;
import com.hongshu.common.core.domain.R;
import com.hongshu.system.api.factory.RemoteWebFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 闲宝服务
 */
@FeignClient(contextId = "remoteWebService", value = ServiceNameConstants.WEB_SERVICE, fallbackFactory = RemoteWebFallbackFactory.class)
public interface RemoteWebService {

    @PostMapping(value = "/es/note/refreshNoteData")
    R<?> refreshNoteData();

    @GetMapping(value = "/system/config/systemConfig")
    R<?> getSystemConfig();

    @GetMapping(value = "/system/config/ossConfig")
    R<?> getOssConfig();

    @GetMapping(value = "/system/config/ossConfigInternal")
    R<?> getOssConfigInternal();

    @GetMapping(value = "/system/config/smsConfigInternal")
    R<?> getSmsConfigInternal();

    @GetMapping(value = "/system/config/wechatLogin")
    R<?> getWeChatLoginConfig();

    @GetMapping(value = "/system/config/imWechatLogin")
    R<?> getImWeChatLoginConfig();
}
