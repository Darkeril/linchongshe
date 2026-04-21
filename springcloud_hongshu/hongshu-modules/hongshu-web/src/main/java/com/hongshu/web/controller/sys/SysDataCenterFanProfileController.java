package com.hongshu.web.controller.sys;

import com.hongshu.common.core.web.controller.BaseController;
import com.hongshu.common.core.web.domain.AjaxResult;
import com.hongshu.web.domain.vo.FanProfileDistributionVo;
import com.hongshu.web.domain.vo.FanProfileOverviewVo;
import com.hongshu.web.service.sys.IFanProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 数据中心-粉丝画像
 */
@RestController
@RequestMapping("/dataCenter/fanProfile")
public class SysDataCenterFanProfileController extends BaseController {

    @Autowired
    private IFanProfileService fanProfileService;


    /**
     * 粉丝画像概览与趋势（总粉丝、新增、流失、按日趋势）
     * 参数：uid（可选，创作者ID）、days（7 或 30）
     */
    @PreAuthorize("@ss.hasPermi('web:note:list')")
    @GetMapping("/overview")
    public AjaxResult overview(
            @RequestParam(required = false) String uid,
            @RequestParam(defaultValue = "7") Integer days) {
        if (days == null || (days != 7 && days != 30)) {
            days = 7;
        }
        FanProfileOverviewVo vo = fanProfileService.getOverview(uid, days);
        return success(vo);
    }

    /**
     * 粉丝画像分布（性别、年龄、地域、兴趣）
     * 参数：uid（可选，创作者ID）
     */
    @PreAuthorize("@ss.hasPermi('web:note:list')")
    @GetMapping("/distribution")
    public AjaxResult distribution(@RequestParam(required = false) String uid) {
        FanProfileDistributionVo vo = fanProfileService.getDistribution(uid);
        return success(vo);
    }
}
