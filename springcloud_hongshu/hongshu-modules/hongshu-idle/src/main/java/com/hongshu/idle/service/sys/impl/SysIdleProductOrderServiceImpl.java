package com.hongshu.idle.service.sys.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hongshu.common.core.validator.ValidatorUtil;
import com.hongshu.idle.domain.Query;
import com.hongshu.idle.domain.entity.IdleCategory;
import com.hongshu.idle.domain.entity.IdleProduct;
import com.hongshu.idle.domain.entity.IdleProductOrder;
import com.hongshu.idle.domain.entity.IdlePaymentOrder;
import com.hongshu.idle.domain.entity.WebUser;
import com.hongshu.idle.mapper.idle.IdleCategoryMapper;
import com.hongshu.idle.mapper.idle.IdleProductMapper;
import com.hongshu.idle.mapper.idle.IdleProductOrderMapper;
import com.hongshu.idle.mapper.idle.IdlePaymentOrderMapper;
import com.hongshu.idle.mapper.web.WebUserMapper;
import com.hongshu.idle.service.sys.ISysIdleProductOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 闲宝信息 服务层处理
 *

 */
@Slf4j
@Service
public class SysIdleProductOrderServiceImpl implements ISysIdleProductOrderService {

    @Autowired
    private IdleCategoryMapper categoryMapper;
    @Autowired
    private IdleProductMapper productMapper;
    @Autowired
    private IdleProductOrderMapper orderMapper;
    @Autowired
    private IdlePaymentOrderMapper paymentOrderMapper;
    @Autowired
    private WebUserMapper userMapper;


    @Override
    public Map<String, Object> getOrderDataByWeek() {
        return getOrderDataByDays(7);
    }

    @Override
    public Map<String, Object> getOrderDataByDays(int days) {
        if (days != 7 && days != 30) {
            days = 7;
        }
        Map<String, Object> result = new HashMap<>();
        Map<String, List<Integer>> categoryData = new HashMap<>();      // 分类名称 → 数据列表
        Map<String, String> categoryIdMap = new HashMap<>();            // 分类名称 → id（辅助映射）

        // 1. 获取最近 days 天的日期和所有分类
        List<String> dateList = getLastNDays(days);
        List<IdleCategory> categoryList = categoryMapper.selectList(new QueryWrapper<>());

        // 2. 初始化数据结构
        for (IdleCategory category : categoryList) {
            String categoryName = category.getTitle();
            categoryData.put(categoryName, new ArrayList<>());
            categoryIdMap.put(categoryName, category.getId());  // 记录名称对应的id
        }

        // 3. 填充每日数据
        for (String date : dateList) {
            for (IdleCategory category : categoryList) {
                String categoryName = category.getTitle();
                String categoryId = category.getId();
                int count = orderMapper.countOrdersByCategoryAndDate(date, categoryId);
                categoryData.get(categoryName).add(count);
            }
        }

        // 4. 返回结果
        result.put("dates", dateList);
        result.put("categoryData", categoryData);
        result.put("categoryIdMap", categoryIdMap);  // 可选：提供分类名称到id的映射
        return result;
    }

    @Override
    public List<IdleProductOrder> selectOrderList(Query query) {
        // 1. 查询订单列表
        QueryWrapper<IdleProductOrder> qw = new QueryWrapper<>();

        // 订单号筛选
        String orderNumber = (String) query.get("orderNumber");
        if (ValidatorUtil.isNotNull(orderNumber)) {
            qw.lambda().like(IdleProductOrder::getOrderNumber, orderNumber);
        }

        // 交易状态筛选
        Object dealStatus = query.get("dealStatus");
        if (ValidatorUtil.isNotNull(dealStatus)) {
            qw.lambda().eq(IdleProductOrder::getDealStatus, dealStatus);
        }

        // 支付状态筛选
        Object orderStatus = query.get("orderStatus");
        if (ValidatorUtil.isNotNull(orderStatus)) {
            qw.lambda().eq(IdleProductOrder::getOrderStatus, orderStatus);
        }

        // 支付方式筛选（通过支付订单表查询）
        Object payTypeObj = query.get("payType");
        if (ValidatorUtil.isNotNull(payTypeObj)) {
            String payType = String.valueOf(payTypeObj);
            log.debug("支付方式筛选条件: payType={}", payType);
            QueryWrapper<IdlePaymentOrder> paymentQw = new QueryWrapper<>();
            paymentQw.lambda().eq(IdlePaymentOrder::getPayType, payType);
            List<IdlePaymentOrder> paymentOrders = paymentOrderMapper.selectList(paymentQw);
            log.debug("查询到匹配的支付订单数量: {}", paymentOrders.size());
            if (paymentOrders.isEmpty()) {
                // 如果没有匹配的支付订单，返回空列表
                log.debug("没有匹配的支付订单，返回空列表");
                return new ArrayList<>();
            }
            List<String> paymentOrderIds = paymentOrders.stream()
                    .map(IdlePaymentOrder::getId)
                    .collect(Collectors.toList());
            log.debug("支付订单ID列表: {}", paymentOrderIds);
            qw.lambda().in(IdleProductOrder::getPaymentOrderId, paymentOrderIds);
        }

        // 买家姓名筛选（通过用户表查询）
        String buyerName = (String) query.get("buyerName");
        if (StringUtils.isNotBlank(buyerName)) {
            QueryWrapper<WebUser> buyerQw = new QueryWrapper<>();
            buyerQw.lambda().like(WebUser::getUsername, buyerName);
            List<WebUser> buyers = userMapper.selectList(buyerQw);
            if (buyers.isEmpty()) {
                return new ArrayList<>();
            }
            List<String> buyerIds = buyers.stream()
                    .map(WebUser::getId)
                    .collect(Collectors.toList());
            qw.lambda().in(IdleProductOrder::getBuyUid, buyerIds);
        }

        // 卖家姓名筛选（通过用户表查询）
        String sellerName = (String) query.get("sellerName");
        if (StringUtils.isNotBlank(sellerName)) {
            QueryWrapper<WebUser> sellerQw = new QueryWrapper<>();
            sellerQw.lambda().like(WebUser::getUsername, sellerName);
            List<WebUser> sellers = userMapper.selectList(sellerQw);
            if (sellers.isEmpty()) {
                return new ArrayList<>();
            }
            List<String> sellerIds = sellers.stream()
                    .map(WebUser::getId)
                    .collect(Collectors.toList());
            qw.lambda().in(IdleProductOrder::getProductUid, sellerIds);
        }

        // 时间范围查询
        String beginTimeStr = (String) query.get("params[beginTime]");
        String endTimeStr = (String) query.get("params[endTime]");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        if (ValidatorUtil.isNotNull(beginTimeStr)) {
            try {
                Date beginTime = sdf.parse(beginTimeStr);
                qw.lambda().ge(IdleProductOrder::getCreateTime, beginTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (ValidatorUtil.isNotNull(endTimeStr)) {
            try {
                Date endTime = sdf.parse(endTimeStr);
                endTime = new Date(endTime.getTime() + 1000 * 60 * 60 * 24 - 1);
                qw.lambda().le(IdleProductOrder::getCreateTime, endTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 排序
        String orderByColumn = (String) query.get("orderByColumn");
        String isAsc = (String) query.get("isAsc");
        if (StringUtils.isNotBlank(orderByColumn)) {
            if ("createTime".equals(orderByColumn)) {
                if ("asc".equals(isAsc)) {
                    qw.lambda().orderByAsc(IdleProductOrder::getCreateTime);
                } else {
                    qw.lambda().orderByDesc(IdleProductOrder::getCreateTime);
                }
            } else if ("updateTime".equals(orderByColumn)) {
                if ("asc".equals(isAsc)) {
                    qw.lambda().orderByAsc(IdleProductOrder::getUpdateTime);
                } else {
                    qw.lambda().orderByDesc(IdleProductOrder::getUpdateTime);
                }
            } else {
                qw.lambda().orderByDesc(IdleProductOrder::getUpdateTime);
            }
        } else {
            qw.lambda().orderByDesc(IdleProductOrder::getUpdateTime);
        }

        // 执行查询
        List<IdleProductOrder> orderList = orderMapper.selectList(qw);

        // 2. 提取所有 productId
        Set<String> productIds = orderList.stream()
                .map(IdleProductOrder::getProductId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        // 3. 批量查询商品信息
        if (!productIds.isEmpty()) {
            List<IdleProduct> productList = productMapper.selectBatchIds(productIds);
            Map<String, IdleProduct> productMap = productList.stream()
                    .collect(Collectors.toMap(IdleProduct::getId, p -> p));

            // 4. 填充 productTitle 和 productCover
            orderList.forEach(order -> {
                IdleProduct product = productMap.get(order.getProductId());
                if (product != null) {
                    order.setProductTitle(product.getTitle());
                    order.setProductCover(product.getCover());
                }
            });
        }

        // 5. 提取所有买家ID和卖家ID
        Set<String> buyerIds = orderList.stream()
                .map(IdleProductOrder::getBuyUid)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> sellerIds = orderList.stream()
                .map(IdleProductOrder::getProductUid)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        // 6. 批量查询用户信息
        Map<String, String> buyerNameMap;
        Map<String, String> sellerNameMap;

        if (!buyerIds.isEmpty()) {
            List<WebUser> buyers = userMapper.selectBatchIds(buyerIds);
            buyerNameMap = buyers.stream()
                    .collect(Collectors.toMap(WebUser::getId, WebUser::getUsername, (k1, k2) -> k1));
        } else {
            buyerNameMap = new HashMap<>();
        }

        if (!sellerIds.isEmpty()) {
            List<WebUser> sellers = userMapper.selectBatchIds(sellerIds);
            sellerNameMap = sellers.stream()
                    .collect(Collectors.toMap(WebUser::getId, WebUser::getUsername, (k1, k2) -> k1));
        } else {
            sellerNameMap = new HashMap<>();
        }

        // 7. 提取所有支付订单ID
        Set<String> paymentOrderIds = orderList.stream()
                .map(IdleProductOrder::getPaymentOrderId)
                .filter(java.util.Objects::nonNull)
                .filter(id -> !id.isEmpty())
                .collect(Collectors.toSet());

        // 8. 批量查询支付订单信息（获取支付方式）
        Map<String, IdlePaymentOrder> paymentOrderMap = new HashMap<>();
        if (!paymentOrderIds.isEmpty()) {
            List<IdlePaymentOrder> paymentOrders = paymentOrderMapper.selectBatchIds(paymentOrderIds);
            paymentOrderMap = paymentOrders.stream()
                    .collect(Collectors.toMap(IdlePaymentOrder::getId, p -> p, (k1, k2) -> k1));
        }

        // 9. 填充买家姓名、卖家姓名和支付方式
        final Map<String, String> finalBuyerNameMap = buyerNameMap;
        final Map<String, String> finalSellerNameMap = sellerNameMap;
        final Map<String, IdlePaymentOrder> finalPaymentOrderMap = paymentOrderMap;
        orderList.forEach(order -> {
            if (order.getBuyUid() != null && finalBuyerNameMap.containsKey(order.getBuyUid())) {
                order.setBuyerName(finalBuyerNameMap.get(order.getBuyUid()));
            }
            if (order.getProductUid() != null && finalSellerNameMap.containsKey(order.getProductUid())) {
                order.setSellerName(finalSellerNameMap.get(order.getProductUid()));
            }
            // 填充支付方式
            if (order.getPaymentOrderId() != null && finalPaymentOrderMap.containsKey(order.getPaymentOrderId())) {
                IdlePaymentOrder paymentOrder = finalPaymentOrderMap.get(order.getPaymentOrderId());
                order.setPayType(paymentOrder.getPayType());
                order.setPayTypeName(paymentOrder.getPayTypeName());
            }
        });

        return orderList;
    }

    @Override
    public IdleProductOrder selectOrderById(Long id) {
        IdleProductOrder idleProductOrder = orderMapper.selectById(id);

        // 填充商品信息
        if (idleProductOrder.getProductId() != null) {
            IdleProduct idleProduct = productMapper.selectById(idleProductOrder.getProductId());
            if (idleProduct != null) {
                idleProductOrder.setProductCover(idleProduct.getCover());
                idleProductOrder.setProductTitle(idleProduct.getTitle());
            }
        }

        // 填充买家姓名
        if (idleProductOrder.getBuyUid() != null) {
            WebUser buyer = userMapper.selectById(idleProductOrder.getBuyUid());
            if (buyer != null) {
                idleProductOrder.setBuyerName(buyer.getUsername());
            }
        }

        // 填充卖家姓名
        if (idleProductOrder.getProductUid() != null) {
            WebUser seller = userMapper.selectById(idleProductOrder.getProductUid());
            if (seller != null) {
                idleProductOrder.setSellerName(seller.getUsername());
            }
        }

        return idleProductOrder;
    }

    @Override
    public int deleteOrderByIds(Long[] ids) {
        List<Long> longs = Arrays.asList(ids);
        for (Long id : ids) {
            IdleProductOrder order = orderMapper.selectById(id);
            if (ValidatorUtil.isNull(order)) {
                log.info("订单不存在:{}", id);
                longs.remove(id);
            }
        }
        return orderMapper.deleteBatchIds(longs);
    }

    /**
     * 获取含今天在内共 n 天的日期字符串（升序）
     */
    private List<String> getLastNDays(int n) {
        if (n < 1) {
            n = 7;
        }
        List<String> dateList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        for (int i = n - 1; i >= 0; i--) {
            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_MONTH, -i);
            dateList.add(sdf.format(calendar.getTime()));
        }
        return dateList;
    }
}
