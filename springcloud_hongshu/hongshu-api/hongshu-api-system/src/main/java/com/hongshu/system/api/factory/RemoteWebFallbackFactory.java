package com.hongshu.system.api.factory;

import com.hongshu.common.core.domain.R;
import com.hongshu.system.api.RemoteWebService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 闲宝服务降级处理
 */
@Component
public class RemoteWebFallbackFactory implements FallbackFactory<RemoteWebService> {

    private static final Logger log = LoggerFactory.getLogger(RemoteWebFallbackFactory.class);


    @Override
    public RemoteWebService create(Throwable throwable) {
        log.error("web服务调用失败:{}", throwable.getMessage());

        return new RemoteWebService() {

            @Override
            public R<?> refreshNoteData() {
                return R.fail("定时同步mysql数据失败：" + throwable.getMessage());
            }

            @Override
            public R<?> getSystemConfig() {
                return R.fail("获取系统配置失败：" + throwable.getMessage());
            }

            @Override
            public R<?> getOssConfig() {
                return R.fail("获取OSS配置失败：" + throwable.getMessage());
            }

            @Override
            public R<?> getOssConfigInternal() {
                return R.fail("获取OSS配置（内部）失败：" + throwable.getMessage());
            }

            @Override
            public R<?> getSmsConfigInternal() {
                return R.fail("获取短信配置（内部）失败：" + throwable.getMessage());
            }

            @Override
            public R<?> getWeChatLoginConfig() {
                return R.fail("获取微信登录配置失败：" + throwable.getMessage());
            }

            @Override
            public R<?> getImWeChatLoginConfig() {
                return R.fail("获取IM微信登录配置失败：" + throwable.getMessage());
            }
        };
    }
}
