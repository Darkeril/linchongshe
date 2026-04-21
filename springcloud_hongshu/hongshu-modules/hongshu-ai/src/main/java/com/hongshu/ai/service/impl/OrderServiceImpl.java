package com.hongshu.ai.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongshu.ai.common.IPageInfo;
import com.hongshu.ai.common.Query;
import com.hongshu.ai.common.exception.ErrorException;
import com.hongshu.ai.domain.command.OrderCommand;
import com.hongshu.ai.domain.entity.Order;
import com.hongshu.ai.domain.vo.OrderVO;
import com.hongshu.ai.mapper.OrderMapper;
import com.hongshu.ai.service.IOrderService;
import com.hongshu.common.core.enums.ResponseInfo;
import com.hongshu.common.core.utils.DozerUtil;
import com.hongshu.common.core.validator.ValidatorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单 服务实现类
 *
 * @author: myj
 * @date: 2023-04-28
 * @version: 1.0.0
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    @Autowired
    private OrderMapper orderMapper;


    /**
     * 根据id获取订单信息
     *
     * @param id 订单id
     * @return
     */
    private Order getOrder(Long id) {
        Order order = orderMapper.selectById(id);
        if (ValidatorUtil.isNull(order)) {
            throw new ErrorException("订单信息不存在，无法操作");
        }
        return order;
    }

    @Override
    public ResponseInfo<IPageInfo<OrderVO>> pageOrder(Query query) {
        IPage<OrderVO> iPage = orderMapper.pageOrder(new Page<>(query.getCurrent(), query.getSize()), query);
        return ResponseInfo.success(new IPageInfo(iPage.getCurrent(), iPage.getSize(), iPage.getTotal(), iPage.getRecords()));
    }

    @Override
    public ResponseInfo<List<OrderVO>> listOrder(Query query) {
        return ResponseInfo.success(orderMapper.listOrder(query));
    }

    @Override
    public ResponseInfo<OrderVO> getOrderById(Long id) {
        return ResponseInfo.success(DozerUtil.convertor(getOrder(id), OrderVO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "masterTransactionManager")
    public ResponseInfo saveOrder(OrderCommand command) {
        Order order = DozerUtil.convertor(command, Order.class);
        order.setCreateUser(command.getOperater());
        orderMapper.insert(order);
        return ResponseInfo.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "masterTransactionManager")
    public ResponseInfo updateOrder(OrderCommand command) {
        Order order = getOrder(command.getId());
        DozerUtil.convertor(command, order);
        order.setUpdateUser(command.getOperater());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        return ResponseInfo.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "masterTransactionManager")
    public ResponseInfo removeOrderByIds(List<Long> ids) {
        orderMapper.deleteBatchIds(ids);
        return ResponseInfo.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "masterTransactionManager")
    public ResponseInfo removeOrderById(Long id) {
        orderMapper.deleteById(id);
        return ResponseInfo.success();
    }

}
