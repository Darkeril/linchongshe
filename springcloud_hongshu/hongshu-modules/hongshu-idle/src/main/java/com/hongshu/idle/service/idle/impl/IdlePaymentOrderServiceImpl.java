package com.hongshu.idle.service.idle.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hongshu.common.core.enums.DealStatusEnum;
import com.hongshu.common.core.enums.PayStatusEnum;
import com.hongshu.common.core.enums.PayTypeEnum;
import com.hongshu.common.core.enums.ResultCodeEnum;
import com.hongshu.common.core.enums.ResponseEnum;
import com.hongshu.common.core.exception.HongshuException;
import com.hongshu.common.core.context.AuthContextHolder;
import com.hongshu.idle.domain.entity.IdlePaymentOrder;
import com.hongshu.idle.domain.entity.IdleProductOrder;
import com.hongshu.idle.domain.entity.IdleUser;
import com.hongshu.idle.mapper.idle.IdlePaymentOrderMapper;
import com.hongshu.idle.mapper.idle.IdleProductOrderMapper;
import com.hongshu.idle.mapper.idle.IdleProductMapper;
import com.hongshu.idle.domain.entity.IdleProduct;
import com.hongshu.idle.mapper.idle.IdleUserMapper;
import com.hongshu.idle.service.idle.IIdlePaymentOrderService;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 商品
 *
 * @author: hongshu
 */
@Slf4j
@Service
public class IdlePaymentOrderServiceImpl extends ServiceImpl<IdlePaymentOrderMapper, IdlePaymentOrder> implements IIdlePaymentOrderService {

    @Autowired
    private IdleUserMapper userMapper;
    @Autowired
    private IdleProductOrderMapper orderMapper;
    @Autowired
    private IdlePaymentOrderMapper paymentMapper;
    @Autowired
    private IdleProductMapper productMapper;


    /**
     * 创建订单
     *
     * @param productOrderId 商品订单ID
     * @param payType 支付类型（1-微信支付，2-支付宝支付，默认支付宝）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createPaymentOrder(String productOrderId, String payType) {
        log.info("创建支付订单: productOrderId={}, payType={}", productOrderId, payType);
        
//        String userId = WebUtils.getRequestHeader(UserConstant.USER_ID);
        String userId = AuthContextHolder.getUserId();
        IdleUser user = userMapper.selectById(userId);

        IdleProductOrder productOrder = orderMapper.selectById(productOrderId);
        if (BeanUtil.isEmpty(productOrder)) {
            log.error("商品订单不存在: productOrderId={}", productOrderId);
            throw new HongshuException(ResponseEnum.ERROR.getCode(), "商品订单不存在");
        }
        if (!DealStatusEnum.TEMPORARY.getCode().equals(productOrder.getDealStatus())) {
            log.error("商品订单状态不正确: productOrderId={}, dealStatus={}, 期望状态={}", 
                    productOrderId, productOrder.getDealStatus(), DealStatusEnum.TEMPORARY.getCode());
            throw new HongshuException(ResponseEnum.ERROR.getCode(), "订单状态不正确，无法创建支付订单");
        }
        
        // 确定支付类型，默认为支付宝
        PayTypeEnum payTypeEnum;
        if ("1".equals(payType)) {
            payTypeEnum = PayTypeEnum.WX_PAY;
            log.info("选择微信支付");
        } else {
            payTypeEnum = PayTypeEnum.ALI_PAY;
            log.info("选择支付宝支付");
        }
        
        IdlePaymentOrder paymentOrder = new IdlePaymentOrder();
        paymentOrder.setOrderNumber(RandomUtil.randomNumbers(12));
        paymentOrder.setProductId(productOrder.getProductId());
        paymentOrder.setUid(user.getId());
        paymentOrder.setPayType(payTypeEnum.getCode());
        paymentOrder.setPayTypeName(payTypeEnum.getDesc());
        // 价格处理：answerBuyMoney是Integer类型（存储分，如1分=0.01元）
        // payPrice字段是BigDecimal类型，以元存储（DECIMAL类型）
        // 需要将分转换为元：分 / 100.0 = 元
        BigDecimal payPriceInYuan = new BigDecimal(productOrder.getAnswerBuyMoney())
                .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
        paymentOrder.setPayPrice(payPriceInYuan);
        paymentOrder.setOrderStatus(PayStatusEnum.UN_PAY.getCode());
        paymentOrder.setCreateTime(new DateTime());
        paymentOrder.setUpdateTime(new DateTime());
        paymentMapper.insert(paymentOrder);
        
        log.info("支付订单创建成功: orderId={}, payType={}, payTypeCode={}", 
                paymentOrder.getId(), payTypeEnum.getDesc(), paymentOrder.getPayType());

        // 更新产品订单
        productOrder.setPaymentOrderId(paymentOrder.getId());
        productOrder.setOrderStatus(PayStatusEnum.PEND_PAY.getCode());
        orderMapper.updateById(productOrder);
        return paymentOrder.getId();
    }

    /**
     * 获取支付订单
     *
     * @param paymentOrderId 支付订单ID
     */
    @Override
    public Object getPaymentOrder(String paymentOrderId) {
        IdlePaymentOrder paymentOrder = paymentMapper.selectById(paymentOrderId);
        if (ObjectUtils.isEmpty(paymentOrder)) {
            throw new HongshuException(ResultCodeEnum.FAIL);
        }
        // 补充商品及订单信息，便于前端展示
        IdleProductOrder productOrder = orderMapper.selectOne(
                new QueryWrapper<IdleProductOrder>().eq("payment_order_id", paymentOrderId));
        IdleProduct product = null;
        if (productOrder != null && ObjectUtils.isNotEmpty(productOrder.getProductId())) {
            product = productMapper.selectById(productOrder.getProductId());
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("id", paymentOrder.getId());
        resp.put("orderNumber", paymentOrder.getOrderNumber());
        resp.put("payPrice", paymentOrder.getPayPrice() != null
                ? paymentOrder.getPayPrice().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        resp.put("payType", paymentOrder.getPayType());
        resp.put("payTypeName", paymentOrder.getPayTypeName());
        resp.put("orderStatus", paymentOrder.getOrderStatus());
        resp.put("productId", paymentOrder.getProductId());
        resp.put("paymentPayId", paymentOrder.getPaymentPayId());
        // 添加创建时间，用于计算倒计时
        if (paymentOrder.getCreateTime() != null) {
            resp.put("createTime", paymentOrder.getCreateTime());
        }

        // 商品/订单附加信息
        if (productOrder != null) {
            resp.put("productOrderId", productOrder.getId());
            resp.put("productTitle", productOrder.getProductTitle());
        }
        if (product != null) {
            resp.put("productTitle", product.getTitle());
            resp.put("productCover", product.getCover());
        }

        return resp;
    }
}
