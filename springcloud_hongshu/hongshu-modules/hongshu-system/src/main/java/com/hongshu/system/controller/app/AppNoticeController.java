package com.hongshu.system.controller.app;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hongshu.common.core.domain.R;
import com.hongshu.common.core.validator.myVaildator.noLogin.NoLoginIntercept;
import com.hongshu.system.domain.SysNotice;
import com.hongshu.system.service.ISysNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 通知公告（App端）
 *
 * @author: hongshu
 */
@RequestMapping("/app/notice")
@RestController
public class AppNoticeController {

    @Autowired
    private ISysNoticeService noticeService;

    /**
     * 获取通知公告列表（分页）
     *
     * @param currentPage 当前页
     * @param pageSize    每页数量
     */
    @NoLoginIntercept
    @GetMapping("list/{currentPage}/{pageSize}")
    public R<?> getNoticeList(@PathVariable long currentPage, @PathVariable long pageSize) {
        SysNotice notice = new SysNotice();
        notice.setStatus("0");
        
        List<SysNotice> allList = noticeService.selectNoticeList(notice);
        // 过滤掉管理端专属通知（类型3=系统告警），仅返回通知(1)和公告(2)
        allList = allList.stream()
                .filter(n -> !"3".equals(n.getNoticeType()))
                .collect(Collectors.toList());
        
        // 手动分页
        int total = allList.size();
        int start = (int) ((currentPage - 1) * pageSize);
        int end = (int) Math.min(start + pageSize, total);
        
        List<SysNotice> pageList;
        if (start >= total) {
            pageList = new ArrayList<>();
        } else {
            pageList = allList.subList(start, end);
        }
        
        // 构建分页对象
        Page<SysNotice> pageInfo = new Page<>(currentPage, pageSize);
        pageInfo.setRecords(pageList);
        pageInfo.setTotal(total);
        pageInfo.setPages((total + pageSize - 1) / pageSize);
        
        return R.ok(pageInfo);
    }

    /**
     * 根据通知公告ID获取详细信息
     *
     * @param noticeId 公告ID
     */
    @NoLoginIntercept
    @GetMapping("{noticeId}")
    public R<?> getNoticeDetail(@PathVariable Long noticeId) {
        return R.ok(noticeService.selectNoticeById(noticeId));
    }
}

