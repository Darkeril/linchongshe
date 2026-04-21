package com.hongshu.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hongshu.ai.common.IPageInfo;
import com.hongshu.ai.common.Query;
import com.hongshu.ai.domain.command.OrderCommand;
import com.hongshu.ai.domain.entity.Order;
import com.hongshu.ai.domain.vo.OrderVO;
import com.hongshu.common.core.enums.ResponseInfo;

import java.util.List;

/**
 * 订单 服务类
 *
 * @author: myj
 * @date: 2023-04-28
 * @version: 1.0.0
 */
public interface IOrderService extends IService<Order> {

    /**
     * 查询订单分页列表
     *
     * @param query 查询条件
     * @return 订单集合
     */
    ResponseInfo<IPageInfo<OrderVO>> pageOrder(Query query);

    /**
     * 查询订单列表
     *
     * @param query 查询条件
     * @return 订单集合
     */
    ResponseInfo<List<OrderVO>> listOrder(Query query);

    /**
     * 根据主键查询订单
     *
     * @param id 订单主键
     * @return 订单
     */
    ResponseInfo<OrderVO> getOrderById(Long id);

    /**
     * 新增订单
     *
     * @param command 订单
     * @return 结果
     */
    ResponseInfo saveOrder(OrderCommand command);

    /**
     * 修改订单
     *
     * @param command 订单
     * @return 结果
     */
    ResponseInfo updateOrder(OrderCommand command);

    /**
     * 批量删除订单
     *
     * @param ids 需要删除的订单主键集合
     * @return 结果
     */
    ResponseInfo removeOrderByIds(List<Long> ids);

    /**
     * 删除订单信息
     *
     * @param id 订单主键
     * @return 结果
     */
    ResponseInfo removeOrderById(Long id);

}
