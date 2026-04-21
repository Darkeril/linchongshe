package com.hongshu.idle.controller.idle;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hongshu.common.core.enums.Result;
import com.hongshu.idle.domain.dto.ProductOrderDTO;
import com.hongshu.idle.domain.vo.IdleProductOrderVO;
import com.hongshu.idle.service.idle.IIdleProductOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 闲宝订单
 *
 * @author: hongshu
 */
@RequestMapping("/product/order")
@RestController
public class IdleProductOrderController {

    @Autowired
    private IIdleProductOrderService orderService;


    /**
     * 创建订单
     *
     * @param productOrderDTO 订单信息
     */
    @PostMapping("createOrder")
    public Result<?> createOrder(@RequestBody ProductOrderDTO productOrderDTO) {
        return Result.ok(orderService.createOrder(productOrderDTO));
    }

    /**
     * 查看订单
     *
     * @param orderNumber 订单ID
     */
    @GetMapping("detail")
    public Result<?> getOrderDetail(@RequestParam String orderNumber) {
        Map map = orderService.getOrderDetail(orderNumber);
        return Result.ok(map);
    }

    @GetMapping
    public Result<?> getProductOrderInfo(String productOrderId) {
        return Result.ok(orderService.getProductOrderInfo(productOrderId));
    }

    /**
     * 通过订单ID获取完整订单详情（UniApp专用）
     *
     * @param orderId 订单ID
     */
    @GetMapping("getOrderDetailById/{orderId}")
    public Result<?> getOrderDetailById(@PathVariable String orderId) {
        Map map = orderService.getOrderDetailById(orderId);
        return Result.ok(map);
    }

    /**
     * 获取用户订单列表
     *
     * @param currentPage 当前页
     * @param pageSize    每页数量
     * @param userId      用户ID
     * @param orderStatus 订单状态（可选）
     */
    @GetMapping("getOrdersByUser/{currentPage}/{pageSize}")
    public Result<?> getOrdersByUser(@PathVariable long currentPage,
                                     @PathVariable long pageSize,
                                     String userId,
                                     @RequestParam(required = false) String orderStatus) {
        Page<IdleProductOrderVO> pageInfo = orderService.getOrdersByUser(currentPage, pageSize, userId, orderStatus);
        return Result.ok(pageInfo);
    }

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     */
    @PostMapping("cancelOrder/{orderId}")
    public Result<?> cancelOrder(@PathVariable String orderId) {
        orderService.cancelOrder(orderId);
        return Result.ok();
    }

//    @PutMapping("/evaluate")
//    public Result<?> evaluate(@RequestBody ProductOrderEvaluateDTO evaluateDTO) {
//        orderService.evaluate(evaluateDTO);
//        return Result.ok();
//    }
}
