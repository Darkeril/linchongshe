package com.hongshu.idle.service.idle;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hongshu.idle.domain.dto.ProductOrderDTO;
import com.hongshu.idle.domain.entity.IdleProductOrder;
import com.hongshu.idle.domain.vo.IdleProductOrderVO;

import java.util.Map;

/**
 * 闲宝订单
 *

 */
public interface IIdleProductOrderService extends IService<IdleProductOrder> {

    /**
     * 创建订单
     *
     * @param productOrderDTO 订单信息
     */
    String createOrder(ProductOrderDTO productOrderDTO);

    /**
     * 查看订单
     *
     * @param orderNumber 商品订单ID
     */
    Map getOrderDetail(String orderNumber);

    /**
     * 通过订单ID获取完整订单详情（包括商品和支付信息）
     *
     * @param orderId 订单ID
     */
    Map getOrderDetailById(String orderId);

    Object getProductOrderInfo(String productOrderId);

    /**
     * 获取用户订单列表
     *
     * @param currentPage 当前页
     * @param pageSize    每页数量
     * @param userId      用户ID
     * @param orderStatus 订单状态（可选）
     */
    Page<IdleProductOrderVO> getOrdersByUser(long currentPage, long pageSize, String userId, String orderStatus);

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     */
    void cancelOrder(String orderId);

//    void evaluate(ProductOrderEvaluateDTO evaluateDTO);
}
