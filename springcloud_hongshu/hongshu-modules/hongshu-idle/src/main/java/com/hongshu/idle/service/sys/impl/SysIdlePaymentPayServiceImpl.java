package com.hongshu.idle.service.sys.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hongshu.common.core.constant.NoteConstant;
import com.hongshu.common.core.enums.PayStatusEnum;
import com.hongshu.common.core.enums.PayTypeEnum;
import com.hongshu.common.core.enums.ProductTypeEnum;
import com.hongshu.common.core.utils.DateUtils;
import com.hongshu.idle.config.AliPayConfig;
import com.hongshu.idle.config.WeChatPayConfig;
import com.alibaba.fastjson2.JSONObject;
import com.hongshu.idle.util.WeChatPaySignUtil;
import okhttp3.OkHttpClient;
import com.hongshu.idle.domain.entity.IdlePaymentOrder;
import com.hongshu.idle.domain.entity.IdlePaymentPay;
import com.hongshu.idle.domain.entity.IdleProduct;
import com.hongshu.idle.domain.entity.IdleProductOrder;
import com.hongshu.idle.domain.vo.IdleProductVO;
import com.hongshu.idle.mapper.idle.IdlePaymentOrderMapper;
import com.hongshu.idle.mapper.idle.IdlePaymentPayMapper;
import com.hongshu.idle.mapper.idle.IdleProductMapper;
import com.hongshu.idle.mapper.idle.IdleProductOrderMapper;
import com.hongshu.idle.service.idle.IIdlePaymentPayService;
import com.hongshu.idle.service.sys.ISysIdlePaymentPayService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 闲宝信息 服务层处理
 *

 */
@Slf4j
@Service
public class SysIdlePaymentPayServiceImpl implements ISysIdlePaymentPayService {

    @Autowired
    private IdleProductMapper productMapper;
    @Autowired
    private IdleProductOrderMapper productOrderMapper;
    @Autowired
    private IdlePaymentOrderMapper paymentOrderMapper;
    @Autowired
    private IdlePaymentPayMapper paymentPayMapper;
    @Autowired
    private ElasticsearchClient elasticsearchClient;
    @Autowired
    private AliPayConfig aliPayConfig;
    @Autowired
    private WeChatPayConfig weChatPayConfig;
    @Autowired
    private IIdlePaymentPayService idlePaymentPayService;

    @Value("${payment.timeout.minutes:15}")
    private int paymentTimeoutMinutes;

    /**
     * 更新超时订单状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateExpireStatus() {
        try {
            // 查询待支付且已超时的订单
            List<IdlePaymentPay> paymentPayList = paymentPayMapper
                    .selectList(new QueryWrapper<IdlePaymentPay>()
                            .eq("payment_status", PayStatusEnum.PEND_PAY.getCode())
                            .lt("payment_time_expire", new Date()));
            log.info("找到{}个超时待支付订单", paymentPayList.size());

            for (IdlePaymentPay pay : paymentPayList) {
                try {
                    this.handleOrder(pay, "expire");
                } catch (Exception e) {
                    log.error("处理超时订单失败, orderId: {}", pay.getOrderId(), e);
                }
            }

            releaseStaleIdleOrders();
        } catch (Exception e) {
            log.error("更新超时订单状态失败", e);
            throw new RuntimeException("更新超时订单状态失败", e);
        }
    }

    /**
     * 释放长期占库存场景：仅下单未建支付单；或已建支付单但从未生成支付记录
     */
    private void releaseStaleIdleOrders() {
        Date cutoff = DateUtils.addMinutes(new Date(), -paymentTimeoutMinutes);

        List<IdleProductOrder> orphans = productOrderMapper.selectList(
                new QueryWrapper<IdleProductOrder>()
                        .eq("order_status", PayStatusEnum.UN_PAY.getCode())
                        .and(w -> w.isNull("payment_order_id").or().eq("payment_order_id", ""))
                        .lt("create_time", cutoff));
        log.info("找到{}个超时未关联支付单的商品订单", orphans.size());
        for (IdleProductOrder po : orphans) {
            try {
                releaseOrphanLockedProductOrder(po);
            } catch (Exception e) {
                log.error("释放超时未建支付单订单失败: productOrderId={}", po.getId(), e);
            }
        }

        List<IdlePaymentOrder> stalePaymentOrders = paymentOrderMapper.selectList(
                new QueryWrapper<IdlePaymentOrder>()
                        .eq("order_status", PayStatusEnum.UN_PAY.getCode())
                        .and(w -> w.isNull("payment_pay_id").or().eq("payment_pay_id", ""))
                        .lt("create_time", cutoff));
        log.info("找到{}个超时未发起支付记录的支付单", stalePaymentOrders.size());
        for (IdlePaymentOrder o : stalePaymentOrders) {
            try {
                idlePaymentPayService.updateOrderStatus(o.getId());
            } catch (Exception e) {
                log.error("释放超时未发起支付记录失败: paymentOrderId={}", o.getId(), e);
            }
        }
    }

    private void releaseOrphanLockedProductOrder(IdleProductOrder productOrder) {
        productOrder.setOrderStatus("5");
        productOrder.setDealStatus("9");
        productOrder.setUpdateTime(new Date());
        productOrderMapper.updateById(productOrder);
        IdleProduct product = productMapper.selectById(productOrder.getProductId());
        if (product != null) {
            product.setBuyUid("");
            product.setStatus(0);
            product.setUpdateTime(new Date());
            productMapper.updateById(product);
            this.updateEsProduct(product.getId(), 0);
        }
        log.info("超时释放未关联支付单的商品订单: productOrderId={}", productOrder.getId());
    }

    /**
     * 定时查询并更新已支付订单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePayStatus() {
        // 1. 只查询最近24小时的订单（沙箱订单有效期）
        Date expireTime = DateUtils.addHours(new Date(), -24);
        List<IdlePaymentPay> paymentPayList = paymentPayMapper.selectList(
                new QueryWrapper<IdlePaymentPay>()
                        .eq("payment_status", PayStatusEnum.PEND_PAY.getCode())
                        .ge("create_time", expireTime) // 避免查询过期订单
        );
        log.info("找到{}个已支付订单", paymentPayList.size());
        if (paymentPayList.size() <= 0) {
            return;
        }

        // 2. 分批处理（防止大事务）
        for (IdlePaymentPay pay : paymentPayList) {
            try {
                this.checkSingleOrder(pay);
            } catch (Exception e) {
                log.error("订单状态检查失败: orderId={}", pay.getOrderId(), e);
                // 继续处理下一个订单（不因单个失败中断整个任务）
            }
        }
    }

    /**
     * 分批处理订单
     */
    @SneakyThrows
    private void checkSingleOrder(IdlePaymentPay pay) {
        IdlePaymentOrder order = paymentOrderMapper.selectById(pay.getOrderId());
        if (order == null) {
            log.warn("支付订单不存在: orderId={}", pay.getOrderId());
            return;
        }
        
        // 根据支付类型调用不同的查询接口
        String payType = order.getPayType();
        boolean isPaid = false;
        
        if (PayTypeEnum.ALI_PAY.getCode().equals(payType) || "2".equals(payType)) {
            // 支付宝支付：调用支付宝查询接口
            isPaid = checkAlipayOrderStatus(order);
        } else if (PayTypeEnum.WX_PAY.getCode().equals(payType) || "1".equals(payType)) {
            // 微信支付：调用微信支付查询接口
            isPaid = checkWeChatPayOrderStatus(order);
        } else {
            log.warn("未知的支付类型: payType={}, orderId={}", payType, order.getId());
            return;
        }
        
        // 如果支付成功，更新订单状态
        if (isPaid) {
            try {
                // 再次确认状态（防止处理期间状态变化）
                IdlePaymentPay paymentPay = paymentPayMapper.selectById(pay.getId());
                if (paymentPay == null) {
                    log.warn("支付记录不存在: payId={}", pay.getId());
                    return;
                }
                if (PayStatusEnum.PEND_PAY.getCode().equals(paymentPay.getPaymentStatus())) {
                    // 更新状态
                    this.handleOrder(paymentPay, "pay");
                    log.info("订单支付状态更新成功: orderId={}, payType={}", order.getId(), payType);
                }
            } catch (Exception e) {
                log.error("更新订单状态失败: orderId={}", pay.getOrderId(), e);
            }
        }
    }
    
    /**
     * 查询支付宝订单状态
     */
    private boolean checkAlipayOrderStatus(IdlePaymentOrder order) {
        try {
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            request.setBizContent("{\"out_trade_no\":\"" + order.getOrderNumber() + "\"}");
            AlipayClient alipayClient = aliPayConfig.getAlipayClient();
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            
            if (response.isSuccess()) {
                String tradeStatus = response.getTradeStatus();
                if ("TRADE_SUCCESS".equals(tradeStatus)) {
                    log.info("支付宝订单支付成功: orderNumber={}", order.getOrderNumber());
                    return true;
                } else if ("TRADE_CLOSED".equals(tradeStatus)) {
                    log.info("支付宝订单已关闭: orderNumber={}", order.getOrderNumber());
                }
            } else {
                log.warn("支付宝查询订单失败: orderNumber={}, error={}", order.getOrderNumber(), response.getSubMsg());
            }
        } catch (Exception e) {
            log.error("查询支付宝订单状态异常: orderNumber={}", order.getOrderNumber(), e);
        }
        return false;
    }
    
    /**
     * 查询微信支付订单状态
     */
    private boolean checkWeChatPayOrderStatus(IdlePaymentOrder order) {
        try {
            // 微信支付查询订单API: GET /v3/pay/transactions/out-trade-no/{out_trade_no}
            String outTradeNo = order.getOrderNumber();
            String requestUrl = "/v3/pay/transactions/out-trade-no/" + outTradeNo + "?mchid=" + weChatPayConfig.getMchId();
            String fullUrl = "https://api.mch.weixin.qq.com" + requestUrl;
            
            // 获取配置
            String mchId = weChatPayConfig.getMchId();
            String certSerialNo = weChatPayConfig.getCertSerialNo();
            String privateKeyPath = weChatPayConfig.getPrivateKeyPath();
            
            if (mchId == null || certSerialNo == null || privateKeyPath == null) {
                log.error("微信支付配置不完整，无法查询订单状态");
                return false;
            }
            
            // 生成签名
            long timestamp = WeChatPaySignUtil.getTimestamp();
            String nonce = WeChatPaySignUtil.generateNonce();
            String authorization = WeChatPaySignUtil.generateAuthorization(
                    "GET", requestUrl, timestamp, nonce, "", privateKeyPath, certSerialNo, mchId);
            
            // 创建HTTP请求
            OkHttpClient httpClient = weChatPayConfig.getWeChatPayHttpClient();
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(fullUrl)
                    .get()
                    .addHeader("Accept", "application/json")
                    .addHeader("Authorization", authorization)
                    .addHeader("Wechatpay-Serial", certSerialNo)
                    .build();
            
            log.debug("微信支付查询订单: url={}", fullUrl);
            
            // 执行请求
            okhttp3.Response response = httpClient.newCall(request).execute();
            String responseBody = response.body().string();
            
            log.debug("微信支付查询订单响应: status={}, body={}", response.code(), responseBody);
            
            if (response.isSuccessful()) {
                JSONObject resultJson = JSONObject.parseObject(responseBody);
                String tradeState = resultJson.getString("trade_state");
                
                if ("SUCCESS".equals(tradeState)) {
                    log.info("微信支付订单支付成功: orderNumber={}", order.getOrderNumber());
                    return true;
                } else {
                    log.debug("微信支付订单状态: orderNumber={}, tradeState={}", order.getOrderNumber(), tradeState);
                }
            } else {
                log.warn("微信支付查询订单失败: orderNumber={}, status={}, body={}", 
                        order.getOrderNumber(), response.code(), responseBody);
            }
        } catch (Exception e) {
            log.error("查询微信支付订单状态异常: orderNumber={}", order.getOrderNumber(), e);
        }
        return false;
    }

    /**
     * 更新状态
     */
    private void handleOrder(IdlePaymentPay pay, String type) {
        Date updateTime = new Date();

        // 更新支付表状态
        this.updatePaymentPay(pay, type, updateTime);

        // 更新支付订单表状态
        IdlePaymentOrder paymentOrder = this.updatePaymentOrder(pay, type, updateTime);

        // 更新产品订单表状态
        IdleProductOrder productOrder = this.updateProductOrder(paymentOrder, type, updateTime);
        if (productOrder == null) {
            log.warn("未找到关联商品订单，跳过商品与ES更新: payOrderId={}", pay.getOrderId());
            return;
        }

        // 更新产品状态
        this.updateProduct(productOrder, type, updateTime);
    }

    /**
     * 更新支付表状态
     */
    private void updatePaymentPay(IdlePaymentPay pay, String type, Date updateTime) {
        if ("expire".equals(type)) {
            pay.setPaymentStatus(PayStatusEnum.UN_PAY.getCode());
        }
        if ("pay".equals(type)) {
            pay.setPaymentStatus(PayStatusEnum.PAY.getCode());
        }
        pay.setFinishTime(updateTime);
        pay.setUpdateTime(updateTime);
        paymentPayMapper.updateById(pay);
    }

    /**
     * 更新支付订单表状态
     */
    private IdlePaymentOrder updatePaymentOrder(IdlePaymentPay pay, String type, Date updateTime) {
        IdlePaymentOrder paymentOrder = paymentOrderMapper
                .selectOne(new QueryWrapper<IdlePaymentOrder>()
                        .eq("id", pay.getOrderId()));
        if (paymentOrder == null) {
            log.warn("支付订单不存在: orderId={}", pay.getOrderId());
            return null;
        }
        if ("expire".equals(type)) {
            paymentOrder.setOrderStatus(PayStatusEnum.UN_PAY.getCode());
        }
        if ("pay".equals(type)) {
            paymentOrder.setOrderStatus(PayStatusEnum.PAY.getCode());
        }
        paymentOrder.setUpdateTime(updateTime);
        paymentOrderMapper.updateById(paymentOrder);
        return paymentOrder;
    }

    /**
     * 更新产品订单表状态
     */
    private IdleProductOrder updateProductOrder(IdlePaymentOrder paymentOrder, String type, Date updateTime) {
        if (paymentOrder == null) {
            return null;
        }
        IdleProductOrder productOrder = productOrderMapper
                .selectOne(new QueryWrapper<IdleProductOrder>()
                        .eq("payment_order_id", paymentOrder.getId()));
        if (productOrder == null) {
            log.warn("未找到关联商品订单: paymentOrderId={}", paymentOrder.getId());
            return null;
        }
        if ("expire".equals(type)) {
            productOrder.setOrderStatus(PayStatusEnum.UN_PAY.getCode());
        }
        if ("pay".equals(type)) {
            productOrder.setOrderStatus(PayStatusEnum.PAY.getCode());
        }
        productOrder.setUpdateTime(updateTime);
        productOrderMapper.updateById(productOrder);
        return productOrder;
    }

    /**
     * 更新产品状态
     */
    private void updateProduct(IdleProductOrder productOrder, String type, Date updateTime) {
        IdleProduct product = productMapper.selectById(productOrder.getProductId());
        if (product == null) {
            log.warn("商品不存在，跳过商品与ES更新: productId={}", productOrder.getProductId());
            return;
        }
        if ("expire".equals(type)) {
            product.setBuyUid("");
            product.setStatus(Integer.valueOf(PayStatusEnum.UN_PAY.getCode()));
        }
        if ("pay".equals(type)) {
            product.setType(ProductTypeEnum.SELL_OUT.getCode());
            product.setStatus(Integer.valueOf(PayStatusEnum.PAY.getCode()));
        }
        product.setUpdateTime(updateTime);
        productMapper.updateById(product);

        int esStatus = product.getStatus() != null ? product.getStatus() : 0;
        this.updateEsProduct(product.getId(), esStatus);
    }

    /**
     * 更新ES数据
     */
    private void updateEsProduct(String productId, int status) {
        try {
            GetResponse<IdleProductVO> getResponse = elasticsearchClient.get(g -> g
                    .index(NoteConstant.PRODUCT_INDEX)
                    .id(productId), IdleProductVO.class);

            if (getResponse.found()) {
                IdleProductVO productSearchVO = getResponse.source();
                productSearchVO.setStatus(status);
                // 使用refresh=wait_for确保立即生效
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
            log.error("更新ES产品数据失败, productId: {}", productId, e);
        }
    }
}
