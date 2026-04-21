package com.hongshu.job.task;

import com.hongshu.common.core.utils.SpringUtils;
import com.hongshu.system.api.RemoteIdleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 闲宝信息 服务层处理
 *

 */
@Slf4j
@Component("payTask")
public class PayTask {

    /**
     * 定时查询并关闭已过期支付订单
     */
    public void expireStatusTask() {
        RemoteIdleService remoteIdleService = SpringUtils.getBean(RemoteIdleService.class);
        remoteIdleService.updateExpireStatus();
    }

    /**
     * 定时查询并更新已支付订单
     */
    public void queryOrderStatus() {
        RemoteIdleService remoteIdleService = SpringUtils.getBean(RemoteIdleService.class);
        remoteIdleService.updatePayStatus();
    }
}
