package com.hongshu.system.api;

import com.hongshu.common.core.constant.ServiceNameConstants;
import com.hongshu.common.core.domain.R;
import com.hongshu.system.api.factory.RemoteSystemFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 文件服务
 */
@FeignClient(contextId = "remoteSystemService", value = ServiceNameConstants.SYSTEM_SERVICE, fallbackFactory = RemoteSystemFallbackFactory.class)
public interface RemoteSystemService {

    @GetMapping(value = "/config/configKey/{configKey}")
    R<String> selectConfigByKey(@PathVariable(value = "configKey") String configId);

}
