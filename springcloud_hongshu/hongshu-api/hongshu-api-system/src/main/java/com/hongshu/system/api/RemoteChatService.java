package com.hongshu.system.api;

import com.hongshu.common.core.constant.ServiceNameConstants;
import com.hongshu.common.core.domain.R;
import com.hongshu.system.api.domain.LoginCommand;
import com.hongshu.system.api.factory.RemoteChatFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 闲宝服务
 */
@FeignClient(contextId = "remoteChatService", value = ServiceNameConstants.CHAT_SERVICE, fallbackFactory = RemoteChatFallbackFactory.class)
public interface RemoteChatService {

    @GetMapping(value = "/statistics/index/gptModel")
    R<?> getGptModelCount();

    @PostMapping(value = "/app/api/oauth/token")
    R<?> registerChatUser(@RequestBody LoginCommand loginCommand);
}
