package com.hongshu.system.api.factory;

import com.hongshu.common.core.domain.R;
import com.hongshu.system.api.RemoteChatService;
import com.hongshu.system.api.domain.LoginCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * chat服务降级处理
 */
@Component
public class RemoteChatFallbackFactory implements FallbackFactory<RemoteChatService> {

    private static final Logger log = LoggerFactory.getLogger(RemoteChatFallbackFactory.class);

    @Override
    public RemoteChatService create(Throwable throwable) {
        log.error("chat服务调用失败:{}", throwable.getMessage());

        return new RemoteChatService() {
            @Override
            public R<?> getGptModelCount() {
                return R.fail("获取模型数量失败：" + throwable.getMessage());
            }

            @Override
            public R<?> registerChatUser(LoginCommand loginCommand) {
                return R.fail("注册用户失败：" + throwable.getMessage());
            }

        };
    }
}
