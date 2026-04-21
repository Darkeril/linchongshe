package com.hongshu.web.controller.sys;

import com.hongshu.common.core.web.controller.BaseController;
import com.hongshu.common.core.web.domain.AjaxResult;
import com.hongshu.web.domain.vo.AnalyticsDataVo;
import com.hongshu.web.service.sys.IAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据中心-数据分析（观看、互动、涨粉、发布）
 */
@RestController
@RequestMapping("/dataCenter/analytics")
public class SysDataCenterAnalyticsController extends BaseController {

    @Autowired
    private IAnalyticsService analyticsService;


    /**
     * 获取数据分析聚合数据
     * 参数：uid（可选，创作者ID）、days（7 或 30，默认 7）
     */
    @PreAuthorize("@ss.hasPermi('web:note:list')")
    @GetMapping
    public AjaxResult getAnalytics(
            @RequestParam(required = false) String uid,
            @RequestParam(defaultValue = "7") Integer days) {
        if (days == null || (days != 7 && days != 30)) {
            days = 7;
        }
        AnalyticsDataVo vo = analyticsService.getAnalytics(uid, days);
        return success(vo);
    }
}
