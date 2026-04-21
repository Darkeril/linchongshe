package com.hongshu.system.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.hongshu.common.core.domain.R;
import com.hongshu.common.core.web.controller.BaseController;
import com.hongshu.common.core.web.domain.AjaxResult;
import com.hongshu.common.core.web.page.TableDataInfo;
import com.hongshu.common.log.annotation.Log;
import com.hongshu.common.log.enums.BusinessType;
import com.hongshu.common.security.annotation.InnerAuth;
import com.hongshu.common.security.annotation.RequiresPermissions;
import com.hongshu.common.security.utils.SecurityUtils;
import com.hongshu.system.domain.SysNotice;
import com.hongshu.system.mapper.SysNoticeMapper;
import com.hongshu.system.service.ISysNoticeService;

/**
 * 公告 信息操作处理
 *
 
 */
@RestController
@RequestMapping("/notice")
public class SysNoticeController extends BaseController
{
    /**
     * listTop 查询结果中 isRead 可能为 Integer/Long/BigDecimal 等，不能用 Integer.equals 与 Long 比较。
     */
    private static boolean isNoticeUnreadRow(Map<String, Object> row)
    {
        Object v = row.get("isRead");
        if (v == null)
        {
            return true;
        }
        if (v instanceof Number)
        {
            return ((Number) v).intValue() == 0;
        }
        if (v instanceof Boolean)
        {
            return !((Boolean) v);
        }
        return true;
    }

    @Autowired
    private ISysNoticeService noticeService;

    @Autowired
    private SysNoticeMapper noticeMapper;

    /**
     * 内部服务调用：新增通知公告（跳过权限校验）
     */
    @InnerAuth
    @PostMapping("/inner")
    public R<?> addInner(@RequestParam("title") String title,
                         @RequestParam("type") String type,
                         @RequestParam("content") String content)
    {
        SysNotice notice = new SysNotice();
        notice.setNoticeTitle(title);
        notice.setNoticeType(type);
        notice.setNoticeContent(content);
        notice.setStatus("0");
        notice.setCreateBy("system");
        return R.ok(noticeService.insertNotice(notice));
    }

    /**
     * 获取通知公告列表
     */
    @RequiresPermissions("system:notice:list")
    @GetMapping("/list")
    public TableDataInfo list(SysNotice notice)
    {
        startPage();
        List<SysNotice> list = noticeService.selectNoticeList(notice);
        return getDataTable(list);
    }

    /**
     * 顶部通知公告列表（最新5条正常状态的公告，带当前用户已读标记）
     */
    @GetMapping("/listTop")
    public AjaxResult listTop()
    {
        Long userId = SecurityUtils.getUserId();
        List<Map<String, Object>> list = noticeMapper.selectNoticeTopWithRead(userId, 5);
        long unread = list.stream().filter(SysNoticeController::isNoticeUnreadRow).count();
        Map<String, Object> result = new HashMap<>();
        result.put("data", list);
        result.put("unreadCount", unread);
        return success(result);
    }

    /**
     * 标记单条公告已读
     */
    @PostMapping("/markRead")
    public AjaxResult markRead(Long noticeId)
    {
        Long userId = SecurityUtils.getUserId();
        if (noticeId != null && noticeMapper.countNoticeRead(userId, noticeId) == 0)
        {
            noticeMapper.insertNoticeRead(userId, noticeId);
        }
        return success();
    }

    /**
     * 批量标记公告已读
     */
    @PostMapping("/markReadAll")
    public AjaxResult markReadAll(String ids)
    {
        Long userId = SecurityUtils.getUserId();
        if (ids != null && !ids.isEmpty())
        {
            for (String id : ids.split(","))
            {
                Long noticeId = Long.valueOf(id.trim());
                if (noticeMapper.countNoticeRead(userId, noticeId) == 0)
                {
                    noticeMapper.insertNoticeRead(userId, noticeId);
                }
            }
        }
        return success();
    }

    /**
     * 根据通知公告编号获取详细信息
     */
    @RequiresPermissions("system:notice:query")
    @GetMapping(value = "/{noticeId}")
    public AjaxResult getInfo(@PathVariable Long noticeId)
    {
        return success(noticeService.selectNoticeById(noticeId));
    }

    /**
     * 新增通知公告
     */
    @RequiresPermissions("system:notice:add")
    @Log(title = "通知公告", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysNotice notice)
    {
        notice.setCreateBy(SecurityUtils.getUsername());
        return toAjax(noticeService.insertNotice(notice));
    }

    /**
     * 修改通知公告
     */
    @RequiresPermissions("system:notice:edit")
    @Log(title = "通知公告", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysNotice notice)
    {
        notice.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(noticeService.updateNotice(notice));
    }

    /**
     * 删除通知公告
     */
    @RequiresPermissions("system:notice:remove")
    @Log(title = "通知公告", businessType = BusinessType.DELETE)
    @DeleteMapping("/{noticeIds}")
    public AjaxResult remove(@PathVariable Long[] noticeIds)
    {
        return toAjax(noticeService.deleteNoticeByIds(noticeIds));
    }
}
