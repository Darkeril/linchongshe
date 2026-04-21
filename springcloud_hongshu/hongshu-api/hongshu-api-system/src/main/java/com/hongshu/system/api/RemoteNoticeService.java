package com.hongshu.system.api;

import com.hongshu.common.core.constant.ServiceNameConstants;
import com.hongshu.common.core.domain.R;
import com.hongshu.system.api.factory.RemoteNoticeFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 通知公告服务
 */
@FeignClient(contextId = "remoteNoticeService", value = ServiceNameConstants.SYSTEM_SERVICE, fallbackFactory = RemoteNoticeFallbackFactory.class)
public interface RemoteNoticeService {

    /**
     * 获取通知公告列表（分页）
     *
     * @param currentPage 当前页
     * @param pageSize    每页数量
     * @return 结果
     */
    @GetMapping(value = "/app/notice/list/{currentPage}/{pageSize}")
    R<?> getNoticeList(@PathVariable("currentPage") long currentPage, @PathVariable("pageSize") long pageSize);

    /**
     * 根据通知公告ID获取详细信息
     *
     * @param noticeId 公告ID
     * @return 结果
     */
    @GetMapping(value = "/app/notice/{noticeId}")
    R<?> getNoticeDetail(@PathVariable("noticeId") Long noticeId);

    /**
     * 内部服务调用：新增通知公告
     *
     * @param title   标题
     * @param type    类型（1通知 2公告）
     * @param content 内容
     * @return 结果
     */
    @PostMapping(value = "/notice/inner")
    R<?> addNotice(@RequestParam("title") String title,
                   @RequestParam("type") String type,
                   @RequestParam("content") String content);
}





