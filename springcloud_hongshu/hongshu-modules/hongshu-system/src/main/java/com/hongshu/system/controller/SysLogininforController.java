package com.hongshu.system.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.hongshu.common.core.domain.LoginLocationStatVO;

import com.hongshu.common.core.constant.CacheConstants;
import com.hongshu.common.core.utils.poi.ExcelUtil;
import com.hongshu.common.core.web.controller.BaseController;
import com.hongshu.common.core.web.domain.AjaxResult;
import com.hongshu.common.core.web.page.TableDataInfo;
import com.hongshu.common.log.annotation.Log;
import com.hongshu.common.log.enums.BusinessType;
import com.hongshu.common.redis.service.RedisService;
import com.hongshu.common.security.annotation.InnerAuth;
import com.hongshu.common.security.annotation.RequiresPermissions;
import com.hongshu.system.api.domain.SysLogininfor;
import com.hongshu.system.service.ISysLogininforService;

/**
 * 系统访问记录
 *
 
 */
@RestController
@RequestMapping("/logininfor")
public class SysLogininforController extends BaseController
{
    @Autowired
    private ISysLogininforService logininforService;

    @Autowired
    private RedisService redisService;

    @RequiresPermissions("system:logininfor:list")
    @GetMapping("/list")
    public TableDataInfo list(SysLogininfor logininfor)
    {
        startPage();
        List<SysLogininfor> list = logininforService.selectLogininforList(logininfor);
        return getDataTable(list);
    }

    @Log(title = "登录日志", businessType = BusinessType.EXPORT)
    @RequiresPermissions("system:logininfor:export")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysLogininfor logininfor)
    {
        List<SysLogininfor> list = logininforService.selectLogininforList(logininfor);
        ExcelUtil<SysLogininfor> util = new ExcelUtil<SysLogininfor>(SysLogininfor.class);
        util.exportExcel(response, list, "登录日志");
    }

    @RequiresPermissions("system:logininfor:remove")
    @Log(title = "登录日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{infoIds}")
    public AjaxResult remove(@PathVariable Long[] infoIds)
    {
        return toAjax(logininforService.deleteLogininforByIds(infoIds));
    }

    @RequiresPermissions("system:logininfor:remove")
    @Log(title = "登录日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/clean")
    public AjaxResult clean()
    {
        logininforService.cleanLogininfor();
        return success();
    }

    @RequiresPermissions("system:logininfor:unlock")
    @Log(title = "账户解锁", businessType = BusinessType.OTHER)
    @GetMapping("/unlock/{userName}")
    public AjaxResult unlock(@PathVariable("userName") String userName)
    {
        redisService.deleteObject(CacheConstants.PWD_ERR_CNT_KEY + userName);
        return success();
    }

    @RequiresPermissions("system:logininfor:list")
    @GetMapping("/locationStats")
    public AjaxResult locationStats(
            @RequestParam(defaultValue = "1") int days,
            @RequestParam(defaultValue = "8") int limit)
    {
        if (days != 1 && days != 7 && days != 30)
        {
            days = 1;
        }
        List<LoginLocationStatVO> stats = logininforService.selectLoginLocationStats(days, limit);
        return success(stats);
    }

    @InnerAuth
    @PostMapping
    public AjaxResult add(@RequestBody SysLogininfor logininfor)
    {
        return toAjax(logininforService.insertLogininfor(logininfor));
    }
}
