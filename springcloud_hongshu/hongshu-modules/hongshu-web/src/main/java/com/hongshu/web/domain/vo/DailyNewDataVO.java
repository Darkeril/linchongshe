package com.hongshu.web.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 *
 */
@Data
@Builder
public class DailyNewDataVO {

    private Integer todayNewUsers;
    private Integer todayNewNotes;
    private Integer todayNewProducts;
    /** 今日新建订单（idle_product_order，创建日=今天） */
    private Integer todayNewOrders;
    private List<String> dates;
    private List<Integer> users;
    private List<Integer> notes;
    private List<Integer> products;
}
