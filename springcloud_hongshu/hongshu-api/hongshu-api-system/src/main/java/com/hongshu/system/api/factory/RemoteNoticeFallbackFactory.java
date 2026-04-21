package com.hongshu.system.api.factory;

import com.hongshu.common.core.domain.R;
import com.hongshu.system.api.RemoteNoticeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 通知公告服务降级处理
 */
@Component
public class RemoteNoticeFallbackFactory implements FallbackFactory<RemoteNoticeService> {
    private static final Logger log = LoggerFactory.getLogger(RemoteNoticeFallbackFactory.class);

    @Override
    public RemoteNoticeService create(Throwable throwable) {
        log.error("通知公告服务调用失败:{}", throwable.getMessage());

        return new RemoteNoticeService() {
            @Override
            public R<?> getNoticeList(long currentPage, long pageSize) {
                return R.fail("获取通知公告列表失败：" + throwable.getMessage());
            }

            @Override
            public R<?> getNoticeDetail(Long noticeId) {
                return R.fail("获取通知公告详情失败：" + throwable.getMessage());
            }

            @Override
            public R<?> addNotice(String title, String type, String content) {
                return R.fail("新增通知公告失败：" + throwable.getMessage());
            }
        };
    }
}





