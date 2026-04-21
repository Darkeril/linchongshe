package com.hongshu.idle.service.idle.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.RandomUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongshu.common.core.constant.NoteConstant;
import com.hongshu.common.core.context.AuthContextHolder;
import com.hongshu.common.core.enums.DealStatusEnum;
import com.hongshu.common.core.enums.PayStatusEnum;
import com.hongshu.common.core.enums.ResponseEnum;
import com.hongshu.common.core.exception.HongshuException;
import com.hongshu.idle.domain.dto.ProductOrderDTO;
import com.hongshu.idle.domain.entity.*;
import com.hongshu.idle.domain.vo.IdleProductOrderVO;
import com.hongshu.idle.domain.vo.IdleProductVO;
import com.hongshu.idle.mapper.idle.*;
import com.hongshu.idle.service.idle.IIdleProductOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品
 *
 * @author: hongshu
 */
@Slf4j
@Service
public class IdleProductOrderServiceImpl extends ServiceImpl<IdleProductOrderMapper, IdleProductOrder> implements IIdleProductOrderService {

    @Autowired
    private IdleUserMapper userMapper;
    @Autowired
    private IdleProductMapper productMapper;
    @Autowired
    private IdleProductOrderMapper orderMapper;
    @Autowired
    private IdlePaymentOrderMapper paymentOrderMapper;
    @Autowired
    private IdlePaymentPayMapper paymentPayMapper;
    @Autowired
    private ElasticsearchClient elasticsearchClient;


    /**
     * 创建订单
     *
     * @param productOrderDTO 订单信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(ProductOrderDTO productOrderDTO) {
        IdleProductOrder productOrder = new IdleProductOrder();

//        String userId = WebUtils.getRequestHeader(UserConstant.USER_ID);
        String userId = AuthContextHolder.getUserId();
        IdleUser user = userMapper.selectById(userId);
        if (BeanUtil.isEmpty(user)) {
            throw new HongshuException("用户不存在", ResponseEnum.ERROR.getCode());
        }
        IdleProduct product = productMapper.selectById(productOrderDTO.getProductId());
        if (BeanUtil.isEmpty(product)) {
            throw new HongshuException("商品不存在", ResponseEnum.ERROR.getCode());
        }
        if (product.getStatus() == 1) {
            throw new HongshuException("商品已售出", ResponseEnum.ERROR.getCode());
        }
        if (userId.equals(product.getUid())) {
            throw new HongshuException("不能购买自己的商品", ResponseEnum.ERROR.getCode());
        }
        Integer postType = product.getPostType();
        String postMode;
        if (0 == postType) {
            postMode = "物流发货";
        } else {
            postMode = "用户自提";
        }
        if (postMode.equals("物流发货")) {
            productOrder.setPostUsername(productOrderDTO.getUsername());
            productOrder.setPostAddress(productOrderDTO.getAddress());
            productOrder.setPostPhone(productOrderDTO.getPhone());
        } else {
            productOrder.setPostUsername(productOrderDTO.getUsername());
            productOrder.setPostPhone(productOrderDTO.getPhone());
        }
        // 价格存储：price是String类型（如"0.01"），answerBuyMoney以元存储
        // 注意：answerBuyMoney是Integer类型，无法存储小数，所以仍然转换为分存储
        // 但在创建支付订单时，会将分转换为元存储到payPrice（BigDecimal类型）
        try {
            double priceInYuan = Double.parseDouble(product.getPrice());
            int priceInFen = (int) Math.round(priceInYuan * 100); // 转换为分存储（因为Integer类型无法存储小数）
            productOrder.setAnswerBuyMoney(priceInFen);
        } catch (NumberFormatException e) {
            log.error("价格格式错误: price={}", product.getPrice(), e);
            throw new HongshuException("商品价格格式错误", ResponseEnum.ERROR.getCode());
        }

        if (postMode.equals("用户自提")) {
            String selfCode = (int) ((Math.random() * 9 + 1) * 100000) + "";
            productOrder.setPostSelfCode(selfCode);
        }
        // 新增闲宝订单数据
        productOrder.setPostType(postMode);
        productOrder.setOrderNumber(RandomUtil.randomNumbers(12));
        productOrder.setProductId(product.getId());
        productOrder.setProductUid(product.getUid());
        productOrder.setBuyUid(userId);
        productOrder.setProductNum(1);
        productOrder.setProductPostPrice(0L);
        productOrder.setProductPostType(product.getPostType());
        productOrder.setProductMoney(productOrder.getProductNum() * productOrder.getAnswerBuyMoney());
        productOrder.setActualBuyMoney(productOrder.getAnswerBuyMoney());
        productOrder.setOrderStatus(PayStatusEnum.UN_PAY.getCode());
        productOrder.setDealStatus(DealStatusEnum.TEMPORARY.getCode());
        productOrder.setCreateTime(new DateTime());
        productOrder.setUpdateTime(new DateTime());
        orderMapper.insert(productOrder);

        // 更新闲宝商品商品数据
        product.setStatus(1);
        product.setBuyUid(userId);
        product.setUpdateTime(new Date());
        productMapper.updateById(product);
        // 更新ES闲宝数据
        this.updateEsProduct(product.getId(), 1);

        return productOrder.getId();
    }

    /**
     * 更新ES闲宝数据
     */
    private void updateEsProduct(String productId, Integer status) {
        try {
            // Step 1: 获取现有的数据
            GetResponse<IdleProductVO> getResponse = elasticsearchClient.get(g -> g.index(NoteConstant.PRODUCT_INDEX).id(productId), IdleProductVO.class);
            // 检查是否获取到了数据
            if (getResponse.found()) {
                IdleProductVO productSearchVO = getResponse.source();
                // Step 2: 更新 status 字段
                productSearchVO.setStatus(status);
                // Step 3: 将更新后的数据保存回 Elasticsearch，使用refresh=wait_for确保立即生效
                elasticsearchClient.update(u -> u
                        .index(NoteConstant.PRODUCT_INDEX)
                        .id(productId)
                        .doc(productSearchVO)
                        .refresh(co.elastic.clients.elasticsearch._types.Refresh.WaitFor), IdleProductVO.class);
                log.info("ES商品状态更新成功: productId={}, status={}", productId, status);
            } else {
                log.warn("ES中未找到商品数据: productId={}", productId);
            }
        } catch (Exception e) {
            log.error("更新ES商品状态失败: productId={}, status={}", productId, status, e);
            e.printStackTrace();
        }
    }

    /**
     * 查看订单
     *
     * @param orderNumber 商品订单ID
     */
    @Override
    public Map getOrderDetail(String orderNumber) {
        HashMap<Object, Object> map = new HashMap<>();
        IdleProductOrder productOrder = orderMapper.selectOne(
                new QueryWrapper<IdleProductOrder>().eq("order_number", orderNumber));
        if (BeanUtil.isEmpty(productOrder)) {
            throw new HongshuException(ResponseEnum.ERROR.getMsg());
        }
        map.put("productOrder", productOrder);
        IdleProduct productInfo = productMapper.selectById(productOrder.getProductId());
        map.put("productInfo", productInfo);
        IdlePaymentOrder paymentOrder = null;
        IdlePaymentPay paymentPay = null;
        if (StringUtils.isNotBlank(productOrder.getPaymentOrderId())) {
            paymentOrder = paymentOrderMapper.selectById(productOrder.getPaymentOrderId());
            if (paymentOrder != null && StringUtils.isNotBlank(paymentOrder.getPaymentPayId())) {
                paymentPay = paymentPayMapper.selectById(paymentOrder.getPaymentPayId());
            }
        }
        map.put("paymentOrder", paymentOrder);
        map.put("paymentPay", paymentPay);
        return map;
    }

    /**
     * 通过订单ID获取完整订单详情（包括商品和支付信息）
     *
     * @param orderId 订单ID
     */
    @Override
    public Map getOrderDetailById(String orderId) {
        HashMap<Object, Object> map = new HashMap<>();
        IdleProductOrder productOrder = orderMapper.selectById(orderId);
        if (BeanUtil.isEmpty(productOrder)) {
            throw new HongshuException("订单不存在", ResponseEnum.ERROR.getCode());
        }

        // 订单基本信息
        map.put("id", productOrder.getId());
        map.put("orderNumber", productOrder.getOrderNumber());
        map.put("orderStatus", productOrder.getOrderStatus());
        map.put("orderStatusText", getOrderStatusText(productOrder.getOrderStatus()));
        map.put("actualBuyMoney", productOrder.getActualBuyMoney());
        map.put("answerBuyMoney", productOrder.getAnswerBuyMoney());
        map.put("productMoney", productOrder.getProductMoney());
        map.put("postUsername", productOrder.getPostUsername());
        map.put("postPhone", productOrder.getPostPhone());
        map.put("postAddress", productOrder.getPostAddress());
        map.put("createTime", productOrder.getCreateTime());

        // 商品信息
        IdleProduct productInfo = productMapper.selectById(productOrder.getProductId());
        if (BeanUtil.isNotEmpty(productInfo)) {
            map.put("productTitle", productInfo.getTitle());
            map.put("productCover", productInfo.getCover());
            map.put("productPrice", productInfo.getPrice());
        }

        // 支付信息
        if (StringUtils.isNotBlank(productOrder.getPaymentOrderId())) {
            IdlePaymentOrder paymentOrder = paymentOrderMapper.selectById(productOrder.getPaymentOrderId());
            if (BeanUtil.isNotEmpty(paymentOrder)) {
                map.put("payType", paymentOrder.getPayType());
                map.put("payTypeName", paymentOrder.getPayTypeName());
                map.put("payPrice", paymentOrder.getPayPrice());
            }
        }

        return map;
    }

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderId) {
        // 1. 验证订单是否存在
        IdleProductOrder productOrder = orderMapper.selectById(orderId);
        if (BeanUtil.isEmpty(productOrder)) {
            throw new HongshuException("订单不存在", ResponseEnum.ERROR.getCode());
        }

        // 2. 验证用户身份
        String userId = AuthContextHolder.getUserId();
        if (!productOrder.getBuyUid().equals(userId)) {
            throw new HongshuException("无权操作此订单", ResponseEnum.ERROR.getCode());
        }

        // 3. 验证订单状态（只有未支付或待支付的订单才能取消）
        String orderStatus = productOrder.getOrderStatus();
        if (!"0".equals(orderStatus) && !"1".equals(orderStatus)) {
            throw new HongshuException("只有未支付或待支付的订单才能取消", ResponseEnum.ERROR.getCode());
        }

        // 4. 更新订单状态为已取消
        productOrder.setOrderStatus("5"); // 已取消
        // deal_status 字段在数据库中是整数类型，使用 "9" 表示已取消（DealStatusEnum 最大是 "8"）
        productOrder.setDealStatus("9");
        productOrder.setUpdateTime(new DateTime());
        orderMapper.updateById(productOrder);

        // 5. 如果有关联的支付订单，也需要取消支付订单
        if (StringUtils.isNotBlank(productOrder.getPaymentOrderId())) {
            IdlePaymentOrder paymentOrder = paymentOrderMapper.selectById(productOrder.getPaymentOrderId());
            if (BeanUtil.isNotEmpty(paymentOrder)) {
                // 更新支付订单状态
                paymentOrder.setOrderStatus(PayStatusEnum.UN_PAY.getCode());
                paymentOrder.setUpdateTime(new Date());
                paymentOrderMapper.updateById(paymentOrder);

                // 如果有关联的支付记录，也取消支付记录
                if (StringUtils.isNotBlank(paymentOrder.getPaymentPayId())) {
                    IdlePaymentPay paymentPay = paymentPayMapper.selectById(paymentOrder.getPaymentPayId());
                    if (BeanUtil.isNotEmpty(paymentPay)) {
                        paymentPay.setPaymentStatus(PayStatusEnum.UN_PAY.getCode());
                        paymentPay.setUpdateTime(new DateTime());
                        paymentPayMapper.updateById(paymentPay);
                    }
                }
            }
        }

        // 6. 释放商品（将商品状态改回可售）
        IdleProduct product = productMapper.selectById(productOrder.getProductId());
        if (BeanUtil.isNotEmpty(product)) {
            product.setBuyUid("");
            product.setStatus(0); // 0-可售
            product.setUpdateTime(new Date());
            productMapper.updateById(product);

            // 7. 更新ES数据
            this.updateEsProduct(product.getId(), 0);
        }

        log.info("订单取消成功: orderId={}, userId={}", orderId, userId);
    }

    /**
     * 获取订单状态文本
     */
    private String getOrderStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0:
                return "未支付";
            case 1:
                return "待支付";
            case 2:
                return "支付中";
            case 3:
                return "已支付";
            case 4:
                return "交易完成";
            case 5:
                return "已取消";
            default:
                return "未知";
        }
    }

    /**
     * 订单详情
     *
     * @param productOrderId 商品订单ID
     */
    @Override
    public Object getProductOrderInfo(String productOrderId) {
        IdleProductOrder productOrder = orderMapper.selectById(productOrderId);
        if (BeanUtil.isEmpty(productOrder)) {
            throw new HongshuException(ResponseEnum.ERROR.getMsg());
        }
        return productOrder;
    }

//    @Override
//    public void evaluate(ProductOrderEvaluateDTO evaluateDTO) {
//        productOrderService.lambdaUpdate().eq(IdleProductOrder::getId, evaluateDTO.getId())
//                .eq(IdleProductOrder::getUid, StpUtil.getLoginIdAsString())
//                .eq(IdleProductOrder::getDealStatus, 9)
//                .set(IdleProductOrder::getEvaScore, evaluateDTO.getScore())
//                .set(IdleProductOrder::getEvaContent, evaluateDTO.getContent())
//                .set(IdleProductOrder::getDealStatus, 11)
//                .set(IdleProductOrder::getUpdateTime, new Date()).update();
//    }

    @Override
    public Page<IdleProductOrderVO> getOrdersByUser(long currentPage, long pageSize, String userId, String orderStatus) {
        // 构建查询条件
        QueryWrapper<IdleProductOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("buy_uid", userId) // 查询购买者的订单
                .orderByDesc("create_time");

        // 根据订单状态筛选
        // 状态值：0-未支付，1-待支付，2-支付中，3-已支付，4-交易完成，5-已取消
        // 前端状态映射：unpaid(待付款) -> 0/1/2, pending(待发货) -> 3, shipped(待收货) -> deal_status=4, received(已签收) -> 4
        if (orderStatus != null && !orderStatus.isEmpty()) {
            switch (orderStatus) {
                case "unpaid":
                    // 待付款：未支付(0)、待支付(1)、支付中(2)
                    queryWrapper.in("order_status", "0", "1", "2");
                    break;
                case "pending":
                    // 待发货：已支付(3)但未发货（deal_status不是4或5）
                    queryWrapper.eq("order_status", "3")
                            .and(wrapper -> wrapper.ne("deal_status", "4").ne("deal_status", "5").or().isNull("deal_status"));
                    break;
                case "paying":
                    queryWrapper.eq("order_status", "2"); // 支付中
                    break;
                case "paid":
                    queryWrapper.eq("order_status", "3"); // 已支付
                    break;
                case "shipped":
                    // 待收货：已支付(3)且deal_status=4(待确认/待收货)
                    queryWrapper.eq("order_status", "3")
                            .eq("deal_status", "4");
                    break;
                case "received":
                    // 已签收：交易完成(4)
                    queryWrapper.eq("order_status", "4");
                    break;
                case "completed":
                    queryWrapper.eq("order_status", "4"); // 交易完成
                    break;
                case "cancelled":
                    queryWrapper.eq("order_status", "5"); // 已取消
                    break;
                default:
                    // 全部订单，排除已取消的订单
                    queryWrapper.ne("order_status", "5"); // 不显示已取消的订单
                    break;
            }
        } else {
            // 如果没有指定状态，默认排除已取消的订单
            queryWrapper.ne("order_status", "5"); // 不显示已取消的订单
        }

        // 分页查询订单
        Page<IdleProductOrder> orderPage = orderMapper.selectPage(new Page<>(currentPage, pageSize), queryWrapper);

        // 转换为VO
        Page<IdleProductOrderVO> voPage = new Page<>();
        voPage.setCurrent(orderPage.getCurrent());
        voPage.setSize(orderPage.getSize());
        voPage.setTotal(orderPage.getTotal());
        voPage.setPages(orderPage.getPages());

        // 转换订单数据
        List<IdleProductOrderVO> voList = orderPage.getRecords().stream().map(order -> {
            IdleProductOrderVO vo = new IdleProductOrderVO();
            vo.setId(order.getId())
                    .setOrderNumber(order.getOrderNumber())
                    .setPaymentOrderId(order.getPaymentOrderId())
                    .setProductId(order.getProductId())
                    .setProductUid(order.getProductUid())
                    .setBuyUid(order.getBuyUid())
                    .setProductNum(order.getProductNum())
                    .setProductPostPrice(order.getProductPostPrice())
                    .setProductPostType(order.getProductPostType())
                    .setProductMoney(Long.valueOf(order.getProductMoney()))
                    .setAnswerBuyMoney(Long.valueOf(order.getAnswerBuyMoney()))
                    .setActualBuyMoney(Long.valueOf(order.getActualBuyMoney()))
                    .setOrderStatus(order.getOrderStatus())
                    .setDealStatus(order.getDealStatus())
                    .setPostType(order.getPostType())
                    .setPostSelfCode(order.getPostSelfCode())
                    .setPostUsername(order.getPostUsername())
                    .setPostPhone(order.getPostPhone())
                    .setPostAddress(order.getPostAddress())
                    .setShipUsername(order.getShipUsername())
                    .setShipPhone(order.getShipPhone())
                    .setShipAddress(order.getShipAddress())
                    .setShipNum(order.getShipNum())
                    .setShipCompany(order.getShipCompany())
                    .setShipTime(order.getShipTime())
                    .setRemark(order.getRemark())
                    .setCreateTime(order.getCreateTime())
                    .setUpdateTime(order.getUpdateTime());

            // 设置状态文本
            vo.setOrderStatusText(getOrderStatusText(order.getOrderStatus()));
            vo.setDealStatusText(getDealStatusText(order.getDealStatus()));

            // 查询商品信息
            if (order.getProductId() != null && !order.getProductId().isEmpty()) {
                IdleProduct product = productMapper.selectById(order.getProductId());
                if (product != null) {
                    vo.setProductTitle(product.getTitle())
                            .setProductCover(product.getCover())
                            .setProductDescription(product.getDescription());
                }
            }

            // 查询发布者用户信息
            if (order.getProductUid() != null && !order.getProductUid().isEmpty()) {
                IdleUser productUser = userMapper.selectById(order.getProductUid());
                if (productUser != null) {
                    vo.setProductUsername(productUser.getUsername())
                            .setProductUserAvatar(productUser.getAvatar());
                }
            }

            // 查询购买者用户信息
            if (order.getBuyUid() != null && !order.getBuyUid().isEmpty()) {
                IdleUser buyUser = userMapper.selectById(order.getBuyUid());
                if (buyUser != null) {
                    vo.setBuyUsername(buyUser.getUsername())
                            .setBuyUserAvatar(buyUser.getAvatar());
                }
            }

            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 获取订单状态文本
     * 状态值：0-未支付，1-待支付，2-支付中，3-已支付，4-交易完成
     */
    private String getOrderStatusText(String orderStatus) {
        if (orderStatus == null) return "未知";
        switch (orderStatus) {
            case "0":
                return "未支付";
            case "1":
                return "待支付";
            case "2":
                return "支付中";
            case "3":
                return "已支付";
            case "4":
                return "交易完成";
            case "5":
                return "已取消";
            default:
                return "未知";
        }
    }

    /**
     * 获取交易状态文本
     */
    private String getDealStatusText(String dealStatus) {
        if (dealStatus == null) return "未知";
        switch (dealStatus) {
            case "0":
                return DealStatusEnum.TEMPORARY.getDesc(); // 临时
            case "1":
                return DealStatusEnum.PAYING.getDesc(); // 付款中
            case "2":
                return DealStatusEnum.PAY_COMPLETE.getDesc(); // 交易完成
            case "3":
                return DealStatusEnum.PAY_FAILED.getDesc(); // 交易失败
            case "4":
                return DealStatusEnum.PEND_SHIP.getDesc(); // 待发货
            case "5":
                return DealStatusEnum.PEND_CONFIRM.getDesc(); // 待确认
            case "6":
                return DealStatusEnum.RETURN_GOODS.getDesc(); // 退货中
            case "7":
                return DealStatusEnum.PEND_EVALUATE.getDesc(); // 待评价
            case "8":
                return DealStatusEnum.EVALUATE_COMPLETE.getDesc(); // 评价完成
            case "9":
                return "已取消";
            case "pending":
                return "待处理";
            case "processing":
                return "处理中";
            case "completed":
                return "已完成";
            case "cancelled":
                return "已取消";
            default:
                return "未知";
        }
    }
}
