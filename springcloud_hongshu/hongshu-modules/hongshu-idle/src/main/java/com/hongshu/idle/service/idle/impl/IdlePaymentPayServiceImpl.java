package com.hongshu.idle.service.idle.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import com.alibaba.fastjson2.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongshu.common.core.constant.NoteConstant;
import com.hongshu.common.core.enums.PayStatusEnum;
import com.hongshu.common.core.enums.PayTypeEnum;
import com.hongshu.common.core.enums.ProductTypeEnum;
import com.hongshu.common.core.enums.ResponseEnum;
import com.hongshu.common.core.exception.HongshuException;
import com.hongshu.common.core.context.AuthContextHolder;
import com.hongshu.idle.config.AliPayConfig;
import com.hongshu.idle.config.WeChatPayConfig;
import com.hongshu.idle.domain.dto.PaymentGlobalConfigDTO;
import com.hongshu.idle.domain.entity.IdlePaymentOrder;
import com.hongshu.idle.domain.entity.IdlePaymentPay;
import com.hongshu.idle.domain.entity.IdleProduct;
import com.hongshu.idle.domain.entity.IdleProductOrder;
import com.hongshu.idle.domain.vo.IdleProductVO;
import com.hongshu.idle.service.sys.ISysSystemConfigService;
import com.hongshu.idle.mapper.idle.IdlePaymentOrderMapper;
import com.hongshu.idle.mapper.idle.IdlePaymentPayMapper;
import com.hongshu.idle.mapper.idle.IdleProductMapper;
import com.hongshu.idle.mapper.idle.IdleProductOrderMapper;
import com.hongshu.idle.service.idle.IIdlePaymentPayService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 商品
 *
 * @author: hongshu
 */
@Slf4j
@Service
public class IdlePaymentPayServiceImpl extends ServiceImpl<IdlePaymentPayMapper, IdlePaymentPay> implements IIdlePaymentPayService {

    @Autowired
    private IdlePaymentOrderMapper paymentOrderMapper;
    @Autowired
    private IdlePaymentPayMapper paymentPayMapper;
    @Autowired
    private IdleProductMapper productMapper;
    @Autowired
    private IdleProductOrderMapper productOrderMapper;
    @Autowired
    private AliPayConfig aliPayConfig;
    @Autowired
    private WeChatPayConfig weChatPayConfig;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ElasticsearchClient elasticsearchClient;
    @Autowired
    private ISysSystemConfigService systemConfigService;

    @Value("${payment.timeout.minutes:15}")
    private int paymentTimeoutMinutes;


    /**
     * 创建订单
     *
     * @param paymentOrderId 订单信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createPaymentPay(String paymentOrderId) {
        // 1. 获取并验证订单
        IdlePaymentOrder order = this.validateOrder(paymentOrderId);
        // 2. 创建支付记录
        IdlePaymentPay pay = this.createPayRecord(order);
        // 3. 更新订单状态
        this.updateOrderStatus(order, pay);

        log.info("创建支付记录成功: orderId={}, payId={}, amount={}",
                order.getId(), pay.getId(), order.getPayPrice());

        return pay.getId();
    }

    private IdlePaymentOrder validateOrder(String paymentOrderId) {
        // 获取订单
        IdlePaymentOrder order = paymentOrderMapper.selectById(paymentOrderId);
        if (order == null) {
            throw new HongshuException("订单不存在", ResponseEnum.ERROR.getCode());
        }
        // 检查订单状态
        if (!PayStatusEnum.UN_PAY.getCode().equals(order.getOrderStatus())) {
            throw new HongshuException("订单状态异常", ResponseEnum.ERROR.getCode());
        }
        // 检查订单金额
        if (order.getPayPrice() == null || order.getPayPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new HongshuException("订单金额必须大于0", ResponseEnum.ERROR.getCode());
        }

        // 检查订单是否超时
        LocalDateTime orderCreateTime = order.getCreateTime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        if (orderCreateTime.isBefore(LocalDateTime.now().minusMinutes(paymentTimeoutMinutes))) {
            log.warn("订单已超时: orderId={}, createTime={}", order.getId(), orderCreateTime);
            throw new HongshuException(String.format("订单已超时（%d分钟），请重新下单", paymentTimeoutMinutes), ResponseEnum.ERROR.getCode());
        }
        return order;
    }

    private IdlePaymentPay createPayRecord(IdlePaymentOrder order) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = now.plusMinutes(paymentTimeoutMinutes);

        IdlePaymentPay pay = new IdlePaymentPay();
        pay.setOrderId(order.getId());
        pay.setUid(order.getUid());
        // paymentPrice也是BigDecimal类型，以元存储，直接使用
        pay.setPaymentPrice(order.getPayPrice());
        // 使用订单的支付类型，而不是硬编码支付宝
        pay.setPaymentType(order.getPayType());
        pay.setPaymentStatus(PayStatusEnum.UN_PAY.getCode());

        // 设置支付时间和超时时间
        pay.setPaymentTimeStart(now);
        pay.setPaymentTimeExpire(expireTime);

        // 设置创建和更新时间
        Date currentDate = new Date();
        pay.setCreateTime(currentDate);
        pay.setUpdateTime(currentDate);

        // 保存支付记录
        try {
            paymentPayMapper.insert(pay);
        } catch (Exception e) {
            log.error("创建支付记录失败: orderId={}", order.getId(), e);
            throw new HongshuException("创建支付记录失败", ResponseEnum.ERROR.getCode());
        }
        return pay;
    }

    private void updateOrderStatus(IdlePaymentOrder order, IdlePaymentPay pay) {
        order.setPaymentPayId(pay.getId());
        // 不要覆盖订单的支付类型，订单的支付类型在创建时已经确定
        // order.setPayType(pay.getPaymentType());
        order.setOrderStatus(PayStatusEnum.PEND_PAY.getCode());  // 更新为支付中状态
        order.setUpdateTime(new Date());

        try {
            paymentOrderMapper.updateById(order);
        } catch (Exception e) {
            log.error("更新订单状态失败: orderId={}, payId={}", order.getId(), pay.getId(), e);
            throw new HongshuException("更新订单状态失败", ResponseEnum.ERROR.getCode());
        }
    }


    @SneakyThrows
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> finishPay(String paymentPayId, String paymentMethod, String openid) {
        // 0. 获取支付全局配置，检查支付开关
        PaymentGlobalConfigDTO paymentGlobalConfig = systemConfigService.getPaymentGlobalConfig();
        
        // 检查全局支付开关
        if (paymentGlobalConfig.getPaymentEnabled() == null || !paymentGlobalConfig.getPaymentEnabled()) {
            throw new HongshuException("支付功能已关闭，请联系管理员", ResponseEnum.ERROR.getCode());
        }
        
        // 1. 数据验证
        IdlePaymentPay paymentPay = this.validateAndGetPaymentPay(paymentPayId);
        IdlePaymentOrder paymentOrder = this.validateAndGetPaymentOrder(paymentPay.getOrderId());

        // 2. 验证用户身份
//        String userId = WebUtils.getRequestHeader(UserConstant.USER_ID);
        String userId = AuthContextHolder.getUserId();
        if (!paymentOrder.getUid().equals(userId)) {
            throw new HongshuException("无权操作此订单", ResponseEnum.ERROR.getCode());
        }

        String lockKey = "payment:lock:" + paymentPayId;
        try {
            // 3. 使用分布式锁防止并发支付
            if (!redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 30, TimeUnit.SECONDS)) {
                throw new HongshuException("订单正在处理中", ResponseEnum.ERROR.getCode());
            }

            // 4. 检查支付记录状态：已支付则不可再次发起；仅允许未支付/待支付/支付中
            String payStatus = paymentPay.getPaymentStatus();
            if (PayStatusEnum.PAY.getCode().equals(payStatus)
                    || PayStatusEnum.PAY_COMPLETE.getCode().equals(payStatus)) {
                throw new HongshuException("订单已支付", ResponseEnum.ERROR.getCode());
            }
            if (!PayStatusEnum.UN_PAY.getCode().equals(payStatus)
                    && !PayStatusEnum.PEND_PAY.getCode().equals(payStatus)
                    && !PayStatusEnum.PAYING.getCode().equals(payStatus)) {
                throw new HongshuException("订单状态异常", ResponseEnum.ERROR.getCode());
            }

            // 5. 检查订单是否过期
            Date createTime = paymentPay.getCreateTime();
            Date now = new Date();
            long diffInMillies = now.getTime() - createTime.getTime();
            long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillies);

            if (diffInMinutes >= paymentTimeoutMinutes) {
                throw new HongshuException("订单已过期，请重新下单", ResponseEnum.ERROR.getCode());
            }

            // 5. 根据支付类型生成对应的支付表单/二维码
            Map<String, Object> result;
            String payType = paymentOrder.getPayType();
            log.info("订单支付类型: payType={}, WX_PAY_CODE={}, ALI_PAY_CODE={}", 
                    payType, PayTypeEnum.WX_PAY.getCode(), PayTypeEnum.ALI_PAY.getCode());
            
            if (PayTypeEnum.WX_PAY.getCode().equals(payType) || "1".equals(payType)) {
                // 微信支付
                // 检查微信支付开关
                if (paymentGlobalConfig.getWechatQrcodeEnabled() == null || !paymentGlobalConfig.getWechatQrcodeEnabled()) {
                    throw new HongshuException("微信支付已关闭，请选择其他支付方式", ResponseEnum.ERROR.getCode());
                }
                
                // 判断是否为小程序支付
                boolean isMiniprogram = "miniprogram".equals(paymentMethod);
                if (isMiniprogram) {
                    // 检查小程序支付开关
                    if (paymentGlobalConfig.getWechatMiniprogramEnabled() == null || !paymentGlobalConfig.getWechatMiniprogramEnabled()) {
                        log.warn("小程序支付未启用，使用二维码支付");
                        // 小程序支付未启用，使用二维码支付
                        result = this.generateWeChatPayQRCode(paymentOrder);
                    } else {
                        // 小程序支付
                        if (openid == null || openid.isEmpty()) {
                            throw new HongshuException("小程序支付需要提供openid", ResponseEnum.ERROR.getCode());
                        }
                        log.info("使用微信小程序支付");
                        result = this.generateWeChatPayMiniprogram(paymentOrder, openid);
                    }
                } else {
                    // 微信扫码支付
                    log.info("使用微信支付生成二维码");
                    result = this.generateWeChatPayQRCode(paymentOrder);
                }
            } else {
                // 支付宝支付（默认）
                // 检查是否使用扫码支付（通过请求参数传递，默认为null/false使用沙箱跳转）
                boolean useQrCode = "qrCode".equals(paymentMethod);
                
                if (useQrCode) {
                    // 支付宝扫码支付
                    // 检查支付宝扫码支付开关
                    if (paymentGlobalConfig.getAlipayQrcodeEnabled() == null || !paymentGlobalConfig.getAlipayQrcodeEnabled()) {
                        throw new HongshuException("支付宝扫码支付已关闭，请选择其他支付方式", ResponseEnum.ERROR.getCode());
                    }
                    log.info("使用支付宝扫码支付生成二维码");
                    result = this.generateAlipayQRCode(paymentOrder);
                } else {
                    // 支付宝沙箱跳转支付（默认）
                    // 检查支付宝沙箱支付开关
                    if (paymentGlobalConfig.getAlipaySandboxEnabled() == null || !paymentGlobalConfig.getAlipaySandboxEnabled()) {
                        throw new HongshuException("支付宝沙箱支付已关闭，请选择其他支付方式", ResponseEnum.ERROR.getCode());
                    }
                    log.info("使用支付宝沙箱跳转支付生成表单");
                    result = this.generateAlipayForm(paymentOrder);
                }
            }

            // 6. 更新订单状态为处理中
            paymentPay.setPaymentStatus(PayStatusEnum.PEND_PAY.getCode());
            paymentPay.setUpdateTime(new DateTime());
            paymentPayMapper.updateById(paymentPay);

            // 7. 返回支付信息
            log.info("支付订单生成成功: {}", result);
            return result;

        } catch (HongshuException e) {
            // 业务异常直接抛出，保留原始错误信息
            log.error("支付处理失败: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            // 系统异常包装后抛出，保留详细错误信息
            log.error("支付处理失败", e);
            String errorMsg = "支付处理失败: " + e.getMessage();
            if (e.getCause() != null) {
                errorMsg += " (" + e.getCause().getMessage() + ")";
            }
            throw new HongshuException(errorMsg, ResponseEnum.ERROR.getCode());
        } finally {
            // 确保锁一定会被释放
            redisTemplate.delete(lockKey);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelPay(String paymentPayId) {
        // 1. 数据验证
        IdlePaymentPay paymentPay = this.validateAndGetPaymentPay(paymentPayId);
        IdlePaymentOrder paymentOrder = this.validateAndGetPaymentOrder(paymentPay.getOrderId());

        // 2. 验证用户身份
        String userId = AuthContextHolder.getUserId();
        if (!paymentOrder.getUid().equals(userId)) {
            throw new HongshuException("无权操作此订单", ResponseEnum.ERROR.getCode());
        }

        String ps = paymentPay.getPaymentStatus();
        if (PayStatusEnum.PAY.getCode().equals(ps) || PayStatusEnum.PAY_COMPLETE.getCode().equals(ps)) {
            throw new HongshuException("订单已支付，无法取消", ResponseEnum.ERROR.getCode());
        }

        // 与超时释放一致：同步商品订单、支付单、支付记录与 ES
        this.updateOrderStatus(paymentOrder.getId());
    }

    @Override
    public Object payStatus(String paymentPayId) {
        IdlePaymentPay paymentPay = this.validateAndGetPaymentPay(paymentPayId);
        return paymentPay.getPaymentStatus();
    }

    @Override
    public String handleAlipayNotify(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        try {
            // 1. 获取所有请求参数
            for (String name : requestParams.keySet()) {
                String[] values = requestParams.get(name);
                String valueStr = "";
                for (int i = 0; i < values.length; i++) {
                    valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
                }
                params.put(name, valueStr);
            }
            // 2. 验证交易状态
            String tradeStatus = params.get("trade_status");
            if (!"TRADE_SUCCESS".equals(tradeStatus)) {
                log.warn("收到非成功交易的支付宝回调: {}", tradeStatus);
                return "fail";
            }
            // 3. 验证签名
            String sign = params.get("sign");
            String content = AlipaySignature.getSignCheckContentV1(params);
            boolean checkSignature = AlipaySignature.rsa256CheckContent(content, sign,
                    aliPayConfig.getAlipayPublicKey(), "UTF-8");
            if (!checkSignature) {
                log.error("支付宝回调签名验证失败");
                return "fail";
            }
            // 4. 获取关键参数
            String outTradeNo = params.get("out_trade_no");
            String tradeNo = params.get("trade_no");
            String totalAmount = params.get("total_amount");
            String gmtPayment = params.get("gmt_payment");
            String sellerId = params.get("seller_id");
            // 5. 验证商家ID
            if (!aliPayConfig.getPid().equals(sellerId)) {
                log.error("收到非本商家的支付宝回调");
                return "fail";
            }
            // 6. 处理订单
            try {
                // 使用分布式锁防止重复处理
                String lockKey = "payment:notify:lock:" + outTradeNo;
                if (!redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 30, TimeUnit.SECONDS)) {
                    log.warn("订单正在处理中: {}", outTradeNo);
                    return "success";
                }
                try {
                    // 验证订单金额
                    IdlePaymentOrder order = paymentOrderMapper
                            .selectOne(new QueryWrapper<IdlePaymentOrder>().eq("order_number", outTradeNo));
                    if (order == null) {
                        log.error("未找到对应的订单: {}", outTradeNo);
                        return "fail";
                    }
                    // 验证订单金额（注意精度）
                    // 支付宝回调的totalAmount是"元"（字符串，如"0.01"）
                    // payPrice是BigDecimal类型，以元存储，直接比较
                    BigDecimal notifyAmount = new BigDecimal(totalAmount);
                    BigDecimal orderAmount = order.getPayPrice().setScale(2, RoundingMode.HALF_UP);
                    if (notifyAmount.compareTo(orderAmount) != 0) {
                        log.error("订单金额不匹配: 回调金额={}, 订单金额={}", notifyAmount, orderAmount);
                        return "fail";
                    }
                    // 🔧 更新所有相关表的状态
                    Date updateTime = new Date();
                    
                    // 1. 更新支付表状态
                    IdlePaymentPay pay = paymentPayMapper.selectById(order.getPaymentPayId());
                    pay.setPaymentStatus(PayStatusEnum.PAY.getCode());
                    pay.setFinishTime(updateTime);
                    pay.setUpdateTime(updateTime);
                    paymentPayMapper.updateById(pay);
                    
                    // 2. 更新支付订单表状态
                    order.setOrderStatus(PayStatusEnum.PAY.getCode());
                    order.setUpdateTime(updateTime);
                    paymentOrderMapper.updateById(order);
                    
                    // 3. 更新产品订单表状态
                    IdleProductOrder productOrder = productOrderMapper.selectOne(
                            new QueryWrapper<IdleProductOrder>().eq("payment_order_id", order.getId()));
                    if (productOrder != null) {
                        productOrder.setOrderStatus(PayStatusEnum.PAY.getCode());
                        productOrder.setUpdateTime(updateTime);
                        productOrderMapper.updateById(productOrder);
                        
                        // 4. 更新产品状态
                        IdleProduct product = productMapper.selectById(order.getProductId());
                        if (product != null) {
                            product.setType(ProductTypeEnum.SELL_OUT.getCode());
                            product.setStatus(Integer.valueOf(PayStatusEnum.PAY.getCode()));
                            product.setUpdateTime(updateTime);
                            productMapper.updateById(product);
                            
                            // 5. 更新ES数据
                            this.updateEsProduct(product.getId(), Integer.parseInt(PayStatusEnum.PAY.getCode()));
                        }
                    }
                    
                    log.info("支付宝回调处理成功: 订单号={}, 交易号={}, 金额={}", outTradeNo, tradeNo, totalAmount);
                    return "success";
                } finally {
                    redisTemplate.delete(lockKey);
                }
            } catch (Exception e) {
                log.error("处理支付宝回调异常", e);
                return "fail";
            }
        } catch (Exception e) {
            log.error("支付宝回调处理异常", e);
            return "fail";
        }
    }

    /**
     * 更新支付状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatus(String paymentOrderId) {
        IdlePaymentOrder order = paymentOrderMapper.selectById(paymentOrderId);
        if (order == null) {
            log.warn("尝试更新不存在的支付订单状态: paymentOrderId={}", paymentOrderId);
            return;
        }
        
        // 1. 更新支付订单状态为未支付
        order.setOrderStatus(PayStatusEnum.UN_PAY.getCode());
        order.setUpdateTime(new Date());
        paymentOrderMapper.updateById(order);
        log.info("支付订单状态更新为未支付: paymentOrderId={}", paymentOrderId);

        // 2. 更新商品订单状态为已取消（这样就不会在"待付款"列表中显示了）
        IdleProductOrder productOrder = productOrderMapper.selectOne(
                new QueryWrapper<IdleProductOrder>().eq("payment_order_id", paymentOrderId));
        if (productOrder != null) {
            productOrder.setOrderStatus("5"); // 已取消
            productOrder.setDealStatus("9"); // 交易状态也标记为已取消
            productOrder.setUpdateTime(new Date());
            productOrderMapper.updateById(productOrder);
            log.info("商品订单状态更新为已取消: productOrderId={}", productOrder.getId());
        } else {
            log.warn("未找到关联的商品订单: paymentOrderId={}", paymentOrderId);
        }

        // 3. 释放商品（将商品状态改回可售）
        IdleProduct product = productMapper.selectById(order.getProductId());
        if (product != null) {
            product.setBuyUid("");
            product.setStatus(0); // 0-可售
            product.setUpdateTime(new Date());
            productMapper.updateById(product);
            // 更新ES数据
            this.updateEsProduct(product.getId(), 0);
            log.info("商品已释放: productId={}", product.getId());
        }

        // 4. 更新支付记录状态为未支付（如果存在）
        if (StringUtils.isNotBlank(order.getPaymentPayId())) {
            IdlePaymentPay pay = paymentPayMapper.selectById(order.getPaymentPayId());
            if (pay != null) {
                pay.setPaymentStatus(PayStatusEnum.UN_PAY.getCode());
                pay.setUpdateTime(new Date());
                paymentPayMapper.updateById(pay);
                log.info("支付记录状态更新为未支付: paymentPayId={}", pay.getId());
            } else {
                log.warn("支付记录不存在: paymentPayId={}", order.getPaymentPayId());
            }
        } else {
            log.warn("支付订单没有关联的支付记录ID: paymentOrderId={}", paymentOrderId);
        }
        
        log.info("订单过期处理完成: paymentOrderId={}", paymentOrderId);
    }

    private void updateEsProduct(String productId, int status) {
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

    // 拆分验证逻辑
    private IdlePaymentPay validateAndGetPaymentPay(String paymentPayId) {
        IdlePaymentPay paymentPay = paymentPayMapper.selectById(paymentPayId);
        if (BeanUtil.isEmpty(paymentPay)) {
            throw new HongshuException(ResponseEnum.ERROR.getMsg());
        }
        return paymentPay;
    }

    private IdlePaymentOrder validateAndGetPaymentOrder(String orderId) {
        IdlePaymentOrder paymentOrder = paymentOrderMapper.selectById(orderId);
        if (BeanUtil.isEmpty(paymentOrder)) {
            throw new HongshuException(ResponseEnum.ERROR.getMsg());
        }
        return paymentOrder;
    }

    // 生成支付宝表单
    private Map<String, Object> generateAlipayForm(IdlePaymentOrder paymentOrder) throws AlipayApiException {
        // 使用AliPayConfig.getAlipayClient()获取最新实例，确保配置刷新后能获取到最新的客户端
        AlipayClient alipayClient = aliPayConfig.getAlipayClient();

        // 创建支付请求
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();

        // 构建业务参数
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", paymentOrder.getOrderNumber());
        // 价格处理：payPrice是BigDecimal类型，以元存储（DECIMAL类型）
        // 支付宝需要"元"格式，直接使用
        BigDecimal amount = paymentOrder.getPayPrice().setScale(2, RoundingMode.HALF_UP);
        bizContent.put("total_amount", amount.toString());
        bizContent.put("subject", String.format("订单支付-%s", paymentOrder.getOrderNumber()));
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        bizContent.put("timeout_express", "15m");

        // 设置请求参数
        alipayRequest.setBizContent(bizContent.toString());
        alipayRequest.setReturnUrl(aliPayConfig.getReturnUrl());
        alipayRequest.setNotifyUrl(aliPayConfig.getNotifyUrl());

        // 执行请求
        AlipayTradePagePayResponse response = alipayClient.pageExecute(alipayRequest);
        if (response.isSuccess()) {
            return buildSuccessResult(paymentOrder.getOrderNumber(), response.getBody());
        } else {
            log.error("创建支付宝订单失败: {}, 错误码: {}, 错误信息: {}",
                    response.getMsg(), response.getSubCode(), response.getSubMsg());
            throw new HongshuException("创建支付订单失败", ResponseEnum.ERROR.getCode());
        }
    }

    // 提取构建返回结果的方法
    private Map<String, Object> buildSuccessResult(String orderNumber, String formHtml) {
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderNumber);
        result.put("paymentFormHtml", formHtml);
        result.put("payType", "2"); // 支付宝支付类型
        return result;
    }

    /**
     * 生成支付宝扫码支付二维码
     */
    private Map<String, Object> generateAlipayQRCode(IdlePaymentOrder paymentOrder) throws AlipayApiException {
        // 使用AliPayConfig.getAlipayClient()获取最新实例
        AlipayClient alipayClient = aliPayConfig.getAlipayClient();

        // 创建预创建订单请求（用于生成二维码）
        AlipayTradePrecreateRequest alipayRequest = new AlipayTradePrecreateRequest();

        // 构建业务参数
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", paymentOrder.getOrderNumber());
        // 价格处理：payPrice是BigDecimal类型，以元存储
        BigDecimal amount = paymentOrder.getPayPrice().setScale(2, RoundingMode.HALF_UP);
        bizContent.put("total_amount", amount.toString());
        bizContent.put("subject", String.format("订单支付-%s", paymentOrder.getOrderNumber()));
        bizContent.put("timeout_express", "15m");

        // 设置请求参数
        alipayRequest.setBizContent(bizContent.toString());
        alipayRequest.setNotifyUrl(aliPayConfig.getNotifyUrl());

        // 执行请求
        AlipayTradePrecreateResponse response = alipayClient.execute(alipayRequest);
        if (response.isSuccess()) {
            String qrCode = response.getQrCode();
            if (qrCode != null && !qrCode.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("orderId", paymentOrder.getOrderNumber());
                result.put("codeUrl", qrCode);
                result.put("qrCodeUrl", qrCode); // 二维码URL，前端可用此URL生成二维码
                result.put("payType", "2"); // 支付宝支付类型
                log.info("支付宝扫码支付二维码生成成功: orderId={}, qrCode={}", paymentOrder.getOrderNumber(), qrCode);
                return result;
            } else {
                log.error("支付宝扫码支付二维码为空");
                throw new HongshuException("获取支付二维码失败", ResponseEnum.ERROR.getCode());
            }
        } else {
            log.error("创建支付宝扫码支付订单失败: {}, 错误码: {}, 错误信息: {}",
                    response.getMsg(), response.getSubCode(), response.getSubMsg());
            throw new HongshuException("创建支付订单失败: " + response.getSubMsg(), ResponseEnum.ERROR.getCode());
        }
    }

    /**
     * 生成微信支付二维码
     */
    private Map<String, Object> generateWeChatPayQRCode(IdlePaymentOrder paymentOrder) {
        try {
            log.info("开始生成微信支付二维码: orderNumber={}", paymentOrder.getOrderNumber());
            
            // 检查微信支付是否启用
            if (weChatPayConfig.getIsEnabled() == null || !weChatPayConfig.getIsEnabled()) {
                log.error("微信支付未启用");
                throw new HongshuException("微信支付未启用，请联系管理员", ResponseEnum.ERROR.getCode());
            }
            
            // 获取微信支付HTTP客户端
            OkHttpClient httpClient;
            try {
                httpClient = weChatPayConfig.getWeChatPayHttpClient();
            } catch (Exception e) {
                log.error("获取微信支付HTTP客户端失败", e);
                throw new HongshuException("微信支付客户端初始化失败: " + e.getMessage(), ResponseEnum.ERROR.getCode());
            }
            
            // 根据实际场景选择AppID：PC端Native支付优先使用pcAppId
            String appId = weChatPayConfig.getPcAppId();
            if (appId == null || appId.isEmpty()) {
                appId = weChatPayConfig.getAppId(); // 如果pcAppId为空，使用appId
            }
            if (appId == null || appId.isEmpty()) {
                appId = weChatPayConfig.getAppAppId(); // 如果appId也为空，使用appAppId
            }
            
            String mchId = weChatPayConfig.getMchId();
            String notifyUrl = weChatPayConfig.getNotifyUrl();
            String privateKeyPath = weChatPayConfig.getPrivateKeyPath();
            String certSerialNo = weChatPayConfig.getCertSerialNo();

            // 验证必要配置
            if (appId == null || appId.isEmpty()) {
                log.error("微信支付配置不完整: pcAppId、appId、appAppId都为空");
                throw new HongshuException("微信支付配置不完整：AppID未配置（请配置PC端AppID、AppID或APP应用AppID），请联系管理员", ResponseEnum.ERROR.getCode());
            }
            if (mchId == null || mchId.isEmpty()) {
                log.error("微信支付配置不完整: mchId为空");
                throw new HongshuException("微信支付配置不完整：商户号未配置，请联系管理员", ResponseEnum.ERROR.getCode());
            }
            if (privateKeyPath == null || privateKeyPath.isEmpty()) {
                log.error("微信支付配置不完整: privateKeyPath为空");
                throw new HongshuException("微信支付配置不完整：商户私钥未配置，请联系管理员", ResponseEnum.ERROR.getCode());
            }
            if (certSerialNo == null || certSerialNo.isEmpty()) {
                log.error("微信支付配置不完整: certSerialNo为空");
                throw new HongshuException("微信支付配置不完整：证书序列号未配置，请联系管理员", ResponseEnum.ERROR.getCode());
            }
            
            log.info("微信支付配置验证通过: appId={}, mchId={}, certSerialNo={}", appId, mchId, certSerialNo);

            // 构建请求URL（Native支付）
            String requestUrl = "/v3/pay/transactions/native";
            String fullUrl = "https://api.mch.weixin.qq.com" + requestUrl;
            
            // 构建请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("appid", appId);
            requestBody.put("mchid", mchId);
            requestBody.put("description", String.format("订单支付-%s", paymentOrder.getOrderNumber()));
            requestBody.put("out_trade_no", paymentOrder.getOrderNumber());
            requestBody.put("notify_url", notifyUrl);
            
            // 金额信息（单位：分）
            // 微信支付API需要"分"作为单位
            // payPrice是BigDecimal类型，以元存储，需要乘以100转换为分
            long totalAmount = paymentOrder.getPayPrice()
                    .multiply(new BigDecimal(100))
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValue();
            
            JSONObject amount = new JSONObject();
            amount.put("total", totalAmount);
            amount.put("currency", "CNY");
            requestBody.put("amount", amount);

            String bodyJson = requestBody.toJSONString();
            log.debug("微信支付请求体: {}", bodyJson);
            
            // 生成签名
            long timestamp = com.hongshu.idle.util.WeChatPaySignUtil.getTimestamp();
            String nonce = com.hongshu.idle.util.WeChatPaySignUtil.generateNonce();
            log.debug("生成签名参数: timestamp={}, nonce={}", timestamp, nonce);
            
            String authorization;
            try {
                authorization = com.hongshu.idle.util.WeChatPaySignUtil.generateAuthorization(
                        "POST", requestUrl, timestamp, nonce, bodyJson, privateKeyPath, certSerialNo, mchId);
                log.debug("签名生成成功");
            } catch (Exception e) {
                log.error("生成微信支付签名失败", e);
                throw new HongshuException("生成支付签名失败: " + e.getMessage(), ResponseEnum.ERROR.getCode());
            }

            // 创建请求
            okhttp3.RequestBody body = okhttp3.RequestBody.create(
                    bodyJson,
                    okhttp3.MediaType.parse("application/json; charset=utf-8"));
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(fullUrl)
                    .post(body)
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", authorization)
                    .addHeader("Wechatpay-Serial", certSerialNo)
                    .build();

            log.info("微信支付请求: url={}, body={}", fullUrl, bodyJson);
            
            // 执行请求
            okhttp3.Response response = httpClient.newCall(request).execute();
            String responseBody = response.body().string();
            
            log.info("微信支付响应: status={}, body={}", response.code(), responseBody);
            
            if (response.isSuccessful()) {
                JSONObject resultJson = JSONObject.parseObject(responseBody);
                String codeUrl = resultJson.getString("code_url");
                
                if (codeUrl == null || codeUrl.isEmpty()) {
                    log.error("微信支付返回的code_url为空: {}", responseBody);
                    throw new HongshuException("微信支付返回数据异常，请联系管理员", ResponseEnum.ERROR.getCode());
                }
                
                // 构建返回结果
                Map<String, Object> result = new HashMap<>();
                result.put("orderId", paymentOrder.getOrderNumber());
                result.put("payType", "wechat");
                result.put("codeUrl", codeUrl);
                result.put("qrCodeUrl", codeUrl); // 二维码URL，前端可用此URL生成二维码
                
                log.info("微信支付二维码生成成功: orderId={}, codeUrl={}", paymentOrder.getOrderNumber(), codeUrl);
                return result;
            } else {
                log.error("微信支付二维码生成失败: status={}, response={}", response.code(), responseBody);
                // 尝试解析错误信息
                String errorMsg = "生成微信支付二维码失败";
                try {
                    JSONObject errorJson = JSONObject.parseObject(responseBody);
                    if (errorJson.containsKey("message")) {
                        errorMsg = errorJson.getString("message");
                    } else if (errorJson.containsKey("detail")) {
                        errorMsg = errorJson.getString("detail");
                    }
                } catch (Exception e) {
                    // 忽略解析错误
                }
                throw new HongshuException(errorMsg, ResponseEnum.ERROR.getCode());
            }
        } catch (HongshuException e) {
            throw e;
        } catch (Exception e) {
            log.error("生成微信支付二维码异常", e);
            throw new HongshuException("生成微信支付二维码失败: " + e.getMessage(), ResponseEnum.ERROR.getCode());
        }
    }

    /**
     * 生成微信小程序JSAPI支付参数
     */
    private Map<String, Object> generateWeChatPayMiniprogram(IdlePaymentOrder paymentOrder, String openid) {
        try {
            log.info("开始生成微信小程序支付参数: orderNumber={}, openid={}", paymentOrder.getOrderNumber(), openid);
            
            // 检查微信支付是否启用
            if (weChatPayConfig.getIsEnabled() == null || !weChatPayConfig.getIsEnabled()) {
                log.error("微信支付未启用");
                throw new HongshuException("微信支付未启用，请联系管理员", ResponseEnum.ERROR.getCode());
            }
            
            // 获取微信支付HTTP客户端
            OkHttpClient httpClient;
            try {
                httpClient = weChatPayConfig.getWeChatPayHttpClient();
            } catch (Exception e) {
                log.error("获取微信支付HTTP客户端失败", e);
                throw new HongshuException("微信支付客户端初始化失败: " + e.getMessage(), ResponseEnum.ERROR.getCode());
            }
            
            // 小程序支付必须使用appId（小程序APPID）
            String appId = weChatPayConfig.getAppId();
            if (appId == null || appId.isEmpty()) {
                log.error("微信支付配置不完整: appId（小程序APPID）为空");
                throw new HongshuException("微信支付配置不完整：小程序AppID未配置，请联系管理员", ResponseEnum.ERROR.getCode());
            }
            
            String mchId = weChatPayConfig.getMchId();
            String notifyUrl = weChatPayConfig.getNotifyUrl();
            String privateKeyPath = weChatPayConfig.getPrivateKeyPath();
            String certSerialNo = weChatPayConfig.getCertSerialNo();
            String apiV3Key = weChatPayConfig.getApiV3Key();

            // 验证必要配置
            if (mchId == null || mchId.isEmpty()) {
                log.error("微信支付配置不完整: mchId为空");
                throw new HongshuException("微信支付配置不完整：商户号未配置，请联系管理员", ResponseEnum.ERROR.getCode());
            }
            if (privateKeyPath == null || privateKeyPath.isEmpty()) {
                log.error("微信支付配置不完整: privateKeyPath为空");
                throw new HongshuException("微信支付配置不完整：商户私钥未配置，请联系管理员", ResponseEnum.ERROR.getCode());
            }
            if (certSerialNo == null || certSerialNo.isEmpty()) {
                log.error("微信支付配置不完整: certSerialNo为空");
                throw new HongshuException("微信支付配置不完整：证书序列号未配置，请联系管理员", ResponseEnum.ERROR.getCode());
            }
            if (apiV3Key == null || apiV3Key.isEmpty()) {
                log.error("微信支付配置不完整: apiV3Key为空");
                throw new HongshuException("微信支付配置不完整：APIv3密钥未配置，请联系管理员", ResponseEnum.ERROR.getCode());
            }
            
            log.info("微信小程序支付配置验证通过: appId={}, mchId={}, certSerialNo={}", appId, mchId, certSerialNo);

            // 构建请求URL（JSAPI支付）
            String requestUrl = "/v3/pay/transactions/jsapi";
            String fullUrl = "https://api.mch.weixin.qq.com" + requestUrl;
            
            // 构建请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("appid", appId);
            requestBody.put("mchid", mchId);
            requestBody.put("description", String.format("订单支付-%s", paymentOrder.getOrderNumber()));
            requestBody.put("out_trade_no", paymentOrder.getOrderNumber());
            requestBody.put("notify_url", notifyUrl);
            
            // 小程序支付需要传入payer信息（openid）
            JSONObject payer = new JSONObject();
            payer.put("openid", openid);
            requestBody.put("payer", payer);
            
            // 金额信息（单位：分）
            long totalAmount = paymentOrder.getPayPrice()
                    .multiply(new BigDecimal(100))
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValue();
            
            JSONObject amount = new JSONObject();
            amount.put("total", totalAmount);
            amount.put("currency", "CNY");
            requestBody.put("amount", amount);

            String bodyJson = requestBody.toJSONString();
            log.debug("微信小程序支付请求体: {}", bodyJson);
            
            // 生成签名
            long timestamp = com.hongshu.idle.util.WeChatPaySignUtil.getTimestamp();
            String nonce = com.hongshu.idle.util.WeChatPaySignUtil.generateNonce();
            log.debug("生成签名参数: timestamp={}, nonce={}", timestamp, nonce);
            
            String authorization;
            try {
                authorization = com.hongshu.idle.util.WeChatPaySignUtil.generateAuthorization(
                        "POST", requestUrl, timestamp, nonce, bodyJson, privateKeyPath, certSerialNo, mchId);
                log.debug("签名生成成功");
            } catch (Exception e) {
                log.error("生成微信支付签名失败", e);
                throw new HongshuException("生成支付签名失败: " + e.getMessage(), ResponseEnum.ERROR.getCode());
            }

            // 创建请求
            okhttp3.RequestBody body = okhttp3.RequestBody.create(
                    bodyJson,
                    okhttp3.MediaType.parse("application/json; charset=utf-8"));
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(fullUrl)
                    .post(body)
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", authorization)
                    .addHeader("Wechatpay-Serial", certSerialNo)
                    .build();

            log.info("微信小程序支付请求: url={}, body={}", fullUrl, bodyJson);
            
            // 执行请求
            okhttp3.Response response = httpClient.newCall(request).execute();
            String responseBody = response.body().string();
            
            log.info("微信小程序支付响应: status={}, body={}", response.code(), responseBody);
            
            if (response.isSuccessful()) {
                JSONObject resultJson = JSONObject.parseObject(responseBody);
                String prepayId = resultJson.getString("prepay_id");
                
                if (prepayId == null || prepayId.isEmpty()) {
                    log.error("微信小程序支付返回的prepay_id为空: {}", responseBody);
                    throw new HongshuException("微信支付返回数据异常，请联系管理员", ResponseEnum.ERROR.getCode());
                }
                
                // 生成小程序支付参数
                String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
                String nonceStr = com.hongshu.idle.util.WeChatPaySignUtil.generateNonce();
                String packageValue = "prepay_id=" + prepayId;
                String signType = "RSA";
                
                // 生成paySign签名
                // 签名字符串格式：appId\n时间戳\n随机字符串\nprepay_id\n
                String signStr = appId + "\n" + timeStamp + "\n" + nonceStr + "\n" + prepayId + "\n";
                String paySign = generatePaySign(signStr, privateKeyPath);
                
                // 构建返回结果
                Map<String, Object> result = new HashMap<>();
                result.put("orderId", paymentOrder.getOrderNumber());
                result.put("payType", "wechat");
                result.put("paymentMethod", "miniprogram");
                result.put("timeStamp", timeStamp);
                result.put("nonceStr", nonceStr);
                result.put("package", packageValue);
                result.put("signType", signType);
                result.put("paySign", paySign);
                
                log.info("微信小程序支付参数生成成功: orderId={}, prepayId={}", paymentOrder.getOrderNumber(), prepayId);
                return result;
            } else {
                log.error("微信小程序支付参数生成失败: status={}, response={}", response.code(), responseBody);
                // 尝试解析错误信息
                String errorMsg = "生成微信小程序支付参数失败";
                try {
                    JSONObject errorJson = JSONObject.parseObject(responseBody);
                    if (errorJson.containsKey("message")) {
                        errorMsg = errorJson.getString("message");
                    } else if (errorJson.containsKey("detail")) {
                        errorMsg = errorJson.getString("detail");
                    }
                } catch (Exception e) {
                    // 忽略解析错误
                }
                throw new HongshuException(errorMsg, ResponseEnum.ERROR.getCode());
            }
        } catch (HongshuException e) {
            throw e;
        } catch (Exception e) {
            log.error("生成微信小程序支付参数异常", e);
            throw new HongshuException("生成微信小程序支付参数失败: " + e.getMessage(), ResponseEnum.ERROR.getCode());
        }
    }

    /**
     * 生成paySign签名（使用RSA私钥签名）
     */
    private String generatePaySign(String signStr, String privateKeyPath) {
        try {
            // 使用WeChatPaySignUtil的私钥签名方法
            return com.hongshu.idle.util.WeChatPaySignUtil.signWithPrivateKey(signStr, privateKeyPath);
        } catch (Exception e) {
            log.error("生成paySign签名失败", e);
            throw new HongshuException("生成paySign签名失败: " + e.getMessage(), ResponseEnum.ERROR.getCode());
        }
    }

    /**
     * 微信支付回调处理
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handleWeChatPayNotify(HttpServletRequest request) {
        try {
            // 读取请求体
            java.io.BufferedReader reader = request.getReader();
            StringBuilder requestBody = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
            String body = requestBody.toString();
            
            log.info("收到微信支付回调: {}", body);

            // 解析回调数据
            JSONObject notifyData = JSONObject.parseObject(body);
            String eventType = notifyData.getString("event_type");
            
            // 只处理支付成功事件
            if (!"TRANSACTION.SUCCESS".equals(eventType)) {
                log.warn("收到非支付成功的微信支付回调: {}", eventType);
                return "success";
            }

            // 获取加密的订单信息
            JSONObject resource = notifyData.getJSONObject("resource");
            if (resource == null) {
                log.error("微信支付回调数据中缺少resource字段");
                return "fail";
            }
            
            String ciphertext = resource.getString("ciphertext");
            String nonce = resource.getString("nonce");
            String associatedData = resource.getString("associated_data");
            
            // 解密数据（使用AES-GCM解密）
            // 注意：这里需要apiV3Key（APIv3密钥）来解密
            String apiV3Key = weChatPayConfig.getApiV3Key();
            if (apiV3Key == null || apiV3Key.isEmpty()) {
                log.error("微信支付APIv3密钥未配置，无法解密回调数据");
                return "fail";
            }
            
            // 使用AES-GCM解密
            String decryptedData = decryptWeChatPayData(ciphertext, nonce, associatedData, apiV3Key);
            if (decryptedData == null || decryptedData.isEmpty()) {
                log.error("微信支付回调数据解密失败");
                return "fail";
            }
            
            log.info("微信支付回调解密后的数据: {}", decryptedData);
            
            // 解析解密后的订单信息
            JSONObject transactionData = JSONObject.parseObject(decryptedData);
            String outTradeNo = transactionData.getString("out_trade_no");
            String transactionId = transactionData.getString("transaction_id");
            String tradeState = transactionData.getString("trade_state");
            JSONObject amountObj = transactionData.getJSONObject("amount");
            Long totalAmount = amountObj != null ? amountObj.getLong("total") : null; // 单位：分
            
            if (outTradeNo == null || outTradeNo.isEmpty()) {
                log.error("微信支付回调数据中缺少订单号");
                return "fail";
            }
            
            // 验证支付状态
            if (!"SUCCESS".equals(tradeState)) {
                log.warn("微信支付回调状态不是SUCCESS: {}", tradeState);
                return "success";
            }
            
            // 使用分布式锁防止重复处理
            String lockKey = "payment:notify:wechat:lock:" + outTradeNo;
            if (!redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 30, TimeUnit.SECONDS)) {
                log.warn("微信支付回调正在处理中: {}", outTradeNo);
                return "success";
            }
            
            try {
                // 查询支付订单
                IdlePaymentOrder order = paymentOrderMapper
                        .selectOne(new QueryWrapper<IdlePaymentOrder>().eq("order_number", outTradeNo));
                if (order == null) {
                    log.error("未找到对应的支付订单: {}", outTradeNo);
                    return "fail";
                }
                
                // 验证订单金额
                // 微信支付回调的totalAmount单位是"分"（Long类型）
                // payPrice是BigDecimal类型，以元存储，需要转换为分来比较
                if (totalAmount != null) {
                    long orderAmountInFen = order.getPayPrice()
                            .multiply(new BigDecimal(100))
                            .setScale(0, RoundingMode.HALF_UP)
                            .longValue();
                    if (!totalAmount.equals(orderAmountInFen)) {
                        log.error("微信支付订单金额不匹配: 回调金额={}分, 订单金额={}元({}分)", 
                                totalAmount, order.getPayPrice(), orderAmountInFen);
                        return "fail";
                    }
                }
                
                // 更新所有相关表的状态
                Date updateTime = new Date();
                
                // 1. 更新支付表状态
                IdlePaymentPay pay = paymentPayMapper.selectById(order.getPaymentPayId());
                if (pay != null) {
                    pay.setPaymentStatus(PayStatusEnum.PAY.getCode());
                    pay.setFinishTime(updateTime);
                    pay.setUpdateTime(updateTime);
                    paymentPayMapper.updateById(pay);
                }
                
                // 2. 更新支付订单表状态
                order.setOrderStatus(PayStatusEnum.PAY.getCode());
                order.setUpdateTime(updateTime);
                paymentOrderMapper.updateById(order);
                
                // 3. 更新产品订单表状态
                IdleProductOrder productOrder = productOrderMapper.selectOne(
                        new QueryWrapper<IdleProductOrder>().eq("payment_order_id", order.getId()));
                if (productOrder != null) {
                    productOrder.setOrderStatus(PayStatusEnum.PAY.getCode());
                    productOrder.setUpdateTime(updateTime);
                    productOrderMapper.updateById(productOrder);
                    
                    // 4. 更新产品状态
                    IdleProduct product = productMapper.selectById(order.getProductId());
                    if (product != null) {
                        product.setType(ProductTypeEnum.SELL_OUT.getCode());
                        product.setStatus(Integer.valueOf(PayStatusEnum.PAY.getCode()));
                        product.setUpdateTime(updateTime);
                        productMapper.updateById(product);
                        
                        // 5. 更新ES数据
                        this.updateEsProduct(product.getId(), Integer.parseInt(PayStatusEnum.PAY.getCode()));
                    }
                }
                
                log.info("微信支付回调处理成功: 订单号={}, 交易号={}, 金额={}", outTradeNo, transactionId, totalAmount);
                return "success";
                
            } finally {
                redisTemplate.delete(lockKey);
            }
        } catch (Exception e) {
            log.error("处理微信支付回调异常", e);
            return "fail";
        }
    }
    
    /**
     * 解密微信支付回调数据（AES-GCM解密）
     */
    private String decryptWeChatPayData(String ciphertext, String nonce, String associatedData, String apiV3Key) {
        try {
            // Base64解码
            byte[] ciphertextBytes = Base64.getDecoder().decode(ciphertext);
            byte[] nonceBytes = Base64.getDecoder().decode(nonce);
            byte[] associatedDataBytes = associatedData != null ? associatedData.getBytes(StandardCharsets.UTF_8) : new byte[0];
            byte[] keyBytes = apiV3Key.getBytes(StandardCharsets.UTF_8);
            
            // 使用AES-GCM解密
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding");
            javax.crypto.spec.GCMParameterSpec gcmSpec = new javax.crypto.spec.GCMParameterSpec(128, nonceBytes);
            javax.crypto.spec.SecretKeySpec keySpec = new javax.crypto.spec.SecretKeySpec(keyBytes, "AES");
            cipher.init(javax.crypto.Cipher.DECRYPT_MODE, keySpec, gcmSpec);
            cipher.updateAAD(associatedDataBytes);
            
            byte[] decryptedBytes = cipher.doFinal(ciphertextBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("解密微信支付回调数据失败", e);
            return null;
        }
    }
}
