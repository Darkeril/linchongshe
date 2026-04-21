package com.hongshu.web.domain.vo;

import lombok.Data;

/**
 * 工作台右侧栏：待处理 + 今日核心（与登录地点统计配套）
 */
@Data
public class WorkbenchSidebarSummaryVO {

    /** 待审核笔记数 */
    private long pendingNotes;

    /** 待审核商品数 */
    private long pendingProducts;

    /** 今日新注册会员 */
    private long todayRegistrations;

    /** 今日新发笔记（创建日=今天，未删除） */
    private long todayNotes;

    /** 今日新发商品 */
    private long todayNewProducts;

    /** 今日新建订单（下单记录条数） */
    private long todayOrders;
}
