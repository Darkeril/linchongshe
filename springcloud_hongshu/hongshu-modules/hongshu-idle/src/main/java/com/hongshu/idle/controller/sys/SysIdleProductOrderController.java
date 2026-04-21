package com.hongshu.idle.controller.sys;

import com.hongshu.common.core.enums.Result;
import com.hongshu.common.core.utils.poi.ExcelUtil;
import com.hongshu.common.core.validator.myVaildator.noLogin.NoLoginIntercept;
import com.hongshu.common.core.web.controller.BaseController;
import com.hongshu.common.core.web.domain.AjaxResult;
import com.hongshu.common.core.web.page.TableDataInfo;
import com.hongshu.common.log.annotation.Log;
import com.hongshu.common.log.enums.BusinessType;
import com.hongshu.idle.domain.Query;
import com.hongshu.idle.domain.entity.IdleProductOrder;
import com.hongshu.idle.service.sys.ISysIdleProductOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


/**
 * 闲宝信息
 *

 */
@RestController
@RequestMapping("/order")
public class SysIdleProductOrderController extends BaseController {

    @Autowired
    private ISysIdleProductOrderService orderService;


    /**
     * 获取订单列表
     */
    @PreAuthorize("@ss.hasPermi('web:order:list')")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam Map map) {
        this.startPage();
        List<IdleProductOrder> orderList = orderService.selectOrderList(new Query(map));
        return getDataTable(orderList);
    }

    /**
     * 根据笔记编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('web:order:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(orderService.selectOrderById(id));
    }

    /**
     * 删除笔记
     */
    @PreAuthorize("@ss.hasPermi('web:order:remove')")
    @Log(title = "笔记管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(orderService.deleteOrderByIds(ids));
    }

    /**
     * 导出笔记列表
     */
    @Log(title = "笔记管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('web:order:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, Query query) {
        List<IdleProductOrder> orderList = orderService.selectOrderList(query);
        ExcelUtil<IdleProductOrder> util = new ExcelUtil<>(IdleProductOrder.class);
        util.exportExcel(response, orderList, "笔记数据");
    }

    @GetMapping("/orderDataByWeek")
    @NoLoginIntercept
    public Result<?> getOrderDataByWeek(@RequestParam(defaultValue = "7") int days) {
        return Result.ok(orderService.getOrderDataByDays(days));
    }

}
