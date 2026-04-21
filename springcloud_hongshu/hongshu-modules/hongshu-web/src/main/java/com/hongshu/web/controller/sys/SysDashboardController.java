package com.hongshu.web.controller.sys;

import com.hongshu.common.core.web.controller.BaseController;
import com.hongshu.common.core.web.domain.AjaxResult;
import com.hongshu.web.service.sys.ISysDashboardService;
import com.hongshu.web.rocketmq.StatisticsServiceV2;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页RestApi
 *
 * @author: hongshu
 * @date 2018年10月22日下午3:27:24
 */
@RestController
@RequestMapping("/index")
@Tag(name = "首页相关接口", description = "首页相关接口")
@Slf4j
public class SysDashboardController extends BaseController {

    @Autowired
    private ISysDashboardService dashboardService;

    @Autowired
    private StatisticsServiceV2 statisticsServiceV2;


    @Operation(summary = "首页初始化数据（优化版）", description = "首页初始化数据")
    @RequestMapping(value = "/init", method = RequestMethod.GET)
    public AjaxResult init() {
        // 使用优化版服务
        return success(statisticsServiceV2.getBasicStatistics());
    }

    @Operation(summary = "获取最近一周用户独立IP数和访问量（优化版）", description = "获取最近一周用户独立IP数和访问量")
    @RequestMapping(value = "/getVisitByWeek", method = RequestMethod.GET)
    public AjaxResult getVisitByWeek() {
        // 使用优化版服务
        return success(statisticsServiceV2.getWeekVisit());
    }

    @Operation(summary = "获取每个标签下文章数目", description = "获取每个标签下文章数目")
    @RequestMapping(value = "/getNoteCountByType", method = RequestMethod.GET)
    public AjaxResult getBlogCountByTag() {
        return success(dashboardService.getNoteCountByType());
    }

    @Operation(summary = "获取每个类型下商品数目", description = "获取每个类型下商品数目")
    @RequestMapping(value = "/getProductCountByType", method = RequestMethod.GET)
    public AjaxResult getProductCountByType() {
        return success(dashboardService.getProductCountByType());
    }

    @Operation(summary = "获取订单数据", description = "近7日或30日按分类的订单销售趋势，days 仅 7 或 30")
    @RequestMapping("/getOrderData")
    public AjaxResult getOrderData(@RequestParam(defaultValue = "7") int days) {
        if (days != 7 && days != 30) {
            days = 7;
        }
        return success(dashboardService.getOrderData(days));
    }

    @Operation(summary = "获取每日新增数据（优化版）", description = "近7日或30日每日新增趋势，days 仅 7 或 30")
    @RequestMapping("/getDailyNewData")
    public AjaxResult getDailyNewData(@RequestParam(defaultValue = "7") int days) {
        if (days != 7 && days != 30) {
            days = 7;
        }
        return success(statisticsServiceV2.getDailyNewData(days));
    }

    @Operation(summary = "工作台-笔记互动效率", description = "近7/30日新发笔记的互动汇总、趋势与分类占比Top3")
    @RequestMapping(value = "/getNoteInteractionEfficiency", method = RequestMethod.GET)
    public AjaxResult getNoteInteractionEfficiency(@RequestParam(defaultValue = "7") int days) {
        return success(statisticsServiceV2.getNoteInteractionEfficiency(days));
    }

    @Operation(summary = "获取每个分类下文章数目（优化版）", description = "获取每个分类下文章数目")
    @RequestMapping(value = "/getBlogCountByBlogSort", method = RequestMethod.GET)
    public AjaxResult getBlogCountByBlogSort() {
        // 使用优化版服务
        return success(statisticsServiceV2.getCategoryStatistics("note"));
    }

    @Operation(summary = "获取每个分类下商品数目（优化版）", description = "获取每个分类下商品数目")
    @RequestMapping(value = "/getProductByBlogSort", method = RequestMethod.GET)
    public AjaxResult getProductByBlogSort() {
        // 使用优化版服务
        return success(statisticsServiceV2.getCategoryStatistics("product"));
    }

    @Operation(summary = "获取一年内的文章贡献数", description = "获取一年内的文章贡献度")
    @RequestMapping(value = "/getBlogContributeCount", method = RequestMethod.GET)
    public AjaxResult getBlogContributeCount() {
        return success(dashboardService.getNoteContributeCount());
    }

    @Operation(summary = "获取热门内容（优化版）", description = "获取热门内容")
    @RequestMapping(value = "/getHotNote", method = RequestMethod.GET)
    public AjaxResult getHotNote(String noteType) {
        // 使用优化版服务
        return success(statisticsServiceV2.getHotNotes(noteType));
    }

    @Operation(summary = "获取用户地理位置分布", description = "获取用户地理位置分布")
    @RequestMapping(value = "/getUserLocations", method = RequestMethod.GET)
    public AjaxResult getUserLocations() {
        return success(dashboardService.getUserLocations());
    }

    @Operation(summary = "Web端登录地点统计", description = "当日或近7/30日 web_login_info 按登录地点聚合 TopN，days 为 1/7/30")
    @RequestMapping(value = "/getWebLoginLocationStats", method = RequestMethod.GET)
    public AjaxResult getWebLoginLocationStats(
            @RequestParam(defaultValue = "1") int days,
            @RequestParam(defaultValue = "8") int limit) {
        if (days != 1 && days != 7 && days != 30) {
            days = 1;
        }
        return success(dashboardService.getWebLoginLocationStats(days, limit));
    }

    @Operation(summary = "会员移动端登录地点统计", description = "当日或近7/30日 web_login_info 中 App/小程序 按登录地点聚合 TopN，days 为 1/7/30")
    @RequestMapping(value = "/getMobileLoginLocationStats", method = RequestMethod.GET)
    public AjaxResult getMobileLoginLocationStats(
            @RequestParam(defaultValue = "1") int days,
            @RequestParam(defaultValue = "8") int limit) {
        if (days != 1 && days != 7 && days != 30) {
            days = 1;
        }
        return success(dashboardService.getMobileLoginLocationStats(days, limit));
    }

    @Operation(summary = "工作台右侧待处理与今日核心", description = "待审笔记/商品、今日注册/发帖/上新/订单")
    @RequestMapping(value = "/getWorkbenchSidebarSummary", method = RequestMethod.GET)
    public AjaxResult getWorkbenchSidebarSummary() {
        return success(statisticsServiceV2.getWorkbenchSidebarSummary());
    }

    @Operation(summary = "高频登录 IP（会员端）", description = "web_login_info 全渠道按 IP 聚合次数 TopN，days 为 1/7/30")
    @RequestMapping(value = "/getIpLoginFrequencyStats", method = RequestMethod.GET)
    public AjaxResult getIpLoginFrequencyStats(
            @RequestParam(defaultValue = "1") int days,
            @RequestParam(defaultValue = "3") int limit) {
        if (days != 1 && days != 7 && days != 30) {
            days = 1;
        }
        return success(dashboardService.getIpLoginFrequencyStats(days, limit));
    }
}
