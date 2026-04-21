package com.hongshu.system.api.factory;

import com.hongshu.common.core.domain.R;
import com.hongshu.system.api.RemoteSystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 文件服务降级处理
 */
@Component
public class RemoteSystemFallbackFactory implements FallbackFactory<RemoteSystemService> {
    private static final Logger log = LoggerFactory.getLogger(RemoteSystemFallbackFactory.class);

    @Override
    public RemoteSystemService create(Throwable throwable) {
        log.error("系统服务调用失败:{}", throwable.getMessage());

        return new RemoteSystemService() {
            @Override
            public R<String> selectConfigByKey(String configKey) {
                return R.fail("根据参数键名查询参数值失败：" + throwable.getMessage());
            }
        };
    }
}
