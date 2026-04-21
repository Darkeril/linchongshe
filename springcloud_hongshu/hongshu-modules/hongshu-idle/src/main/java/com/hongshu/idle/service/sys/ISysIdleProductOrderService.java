package com.hongshu.idle.service.sys;

import com.hongshu.idle.domain.Query;
import com.hongshu.idle.domain.entity.IdleProductOrder;

import java.util.List;
import java.util.Map;

/**
 * 闲宝信息 服务层
 *

 */
public interface ISysIdleProductOrderService {

    List<IdleProductOrder> selectOrderList(Query query);

    IdleProductOrder selectOrderById(Long id);

    int deleteOrderByIds(Long[] ids);

    Map<String, Object> getOrderDataByWeek();

    /**
     * 按自然日统计订单趋势，days 仅支持 7 或 30（含首尾共 days 天）
     */
    Map<String, Object> getOrderDataByDays(int days);

}
