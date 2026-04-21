package com.hongshu.web.controller.app;

import com.hongshu.common.core.domain.R;
import com.hongshu.common.core.enums.Result;
import com.hongshu.system.api.RemoteNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 通知公告（App端）
 *
 * @author: hongshu
 */
@RequestMapping("/app/notice")
@RestController
public class AppNoticeController {

    @Autowired
    private RemoteNoticeService remoteNoticeService;

    /**
     * 获取通知公告列表（分页）
     *
     * @param currentPage 当前页
     * @param pageSize    每页数量
     */
    @GetMapping("list/{currentPage}/{pageSize}")
    public Result<?> getNoticeList(@PathVariable long currentPage, @PathVariable long pageSize) {
        R<?> result = remoteNoticeService.getNoticeList(currentPage, pageSize);
        if (result.getCode() == R.SUCCESS) {
            return Result.ok(result.getData());
        } else {
            return Result.fail(result.getMsg());
        }
    }

    /**
     * 根据通知公告ID获取详细信息
     *
     * @param noticeId 公告ID
     */
    @GetMapping("{noticeId}")
    public Result<?> getNoticeDetail(@PathVariable Long noticeId) {
        R<?> result = remoteNoticeService.getNoticeDetail(noticeId);
        if (result.getCode() == R.SUCCESS) {
            return Result.ok(result.getData());
        } else {
            return Result.fail(result.getMsg());
        }
    }
}

