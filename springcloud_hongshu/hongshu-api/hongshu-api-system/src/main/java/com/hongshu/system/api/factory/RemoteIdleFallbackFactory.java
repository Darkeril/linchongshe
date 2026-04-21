package com.hongshu.system.api.factory;

import com.hongshu.common.core.domain.R;
import com.hongshu.system.api.RemoteIdleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 闲宝服务降级处理
 */
@Component
public class RemoteIdleFallbackFactory implements FallbackFactory<RemoteIdleService> {

    private static final Logger log = LoggerFactory.getLogger(RemoteIdleFallbackFactory.class);

    @Override
    public RemoteIdleService create(Throwable throwable) {
        log.error("闲宝服务调用失败:{}", throwable.getMessage());

        return new RemoteIdleService() {
            @Override
            public R<?> getProductCount() {
                return R.fail("获取商品数量失败：" + throwable.getMessage());
            }

            @Override
            public R<?> getProductCountByType() {
                return R.fail("根据类型查询商品数量参数值失败：" + throwable.getMessage());
            }

            @Override
            public R<?> getOrderDataByWeek(int days) {
                return R.fail("查询周订单数量失败：" + throwable.getMessage());
            }

            @Override
            public R<?> getProductCountByCategory() {
                return R.fail("根据分类查询商品数量失败：" + throwable.getMessage());
            }

            @Override
            public R<?> getProductContributeCount() {
                return R.fail("获取商品贡献度失败：" + throwable.getMessage());
            }

            @Override
            public R<?> updateExpireStatus() {
                return R.fail("更新超时订单状态失败：" + throwable.getMessage());
            }

            @Override
            public R<?> updatePayStatus() {
                return R.fail("定时查询并更新已支付订单失败：" + throwable.getMessage());
            }

            @Override
            public R<?> refreshProductData() {
                return R.fail("定时同步mysql数据失败：" + throwable.getMessage());
            }

            @Override
            public R<?> refreshPayConfig() {
                return R.fail("刷新支付配置失败：" + throwable.getMessage());
            }

            @Override
            public R<?> refreshAlipayConfig() {
                return R.fail("刷新支付宝配置失败：" + throwable.getMessage());
            }

            @Override
            public R<?> refreshWeChatPayConfig() {
                return R.fail("刷新微信支付配置失败：" + throwable.getMessage());
            }
        };
    }
}
