package com.hongshu.job.task;

import com.hongshu.common.core.utils.SpringUtils;
import com.hongshu.system.api.RemoteIdleService;
import com.hongshu.system.api.RemoteWebService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * ES同步Mysql数据
 *
 */
@Slf4j
@Component("esTask")
public class EsTask {

    /**
     * 定时同步mysql数据o
     */
    public void synchronizeData() {
        // 同步笔记数据
        RemoteWebService webService = SpringUtils.getBean(RemoteWebService.class);
        webService.refreshNoteData();
        // 同步闲宝数据
        RemoteIdleService idleService = SpringUtils.getBean(RemoteIdleService.class);
        idleService.refreshProductData();
    }

}
