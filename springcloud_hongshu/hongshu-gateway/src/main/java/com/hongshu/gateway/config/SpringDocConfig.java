package com.hongshu.gateway.config;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Configuration;
import com.alibaba.nacos.client.naming.event.InstancesChangeEvent;
import com.alibaba.nacos.common.notify.Event;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.notify.listener.Subscriber;
import com.hongshu.common.core.utils.StringUtils;

/**
 * SpringDoc配置类
 *

 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "springdoc.api-docs.enabled", matchIfMissing = true)
public class SpringDocConfig implements InitializingBean
{
    @Autowired
    private SwaggerUiConfigProperties swaggerUiConfigProperties;

    @Autowired
    private DiscoveryClient discoveryClient;

    /**
     * 在初始化后调用的方法
     */
    @Override
    public void afterPropertiesSet()
    {
        SwaggerDocRegister subscriber = new SwaggerDocRegister(swaggerUiConfigProperties, discoveryClient);
        NotifyCenter.registerSubscriber(subscriber);
        // 仅依赖 Nacos InstancesChangeEvent 时，启动阶段可能尚未触发事件，urls 为空会导致 Swagger UI 退回 Petstore 示例
        subscriber.refreshSwaggerUrls();
    }
}

/**
 * Swagger文档注册器
 */
class SwaggerDocRegister extends Subscriber<InstancesChangeEvent>
{
    private final SwaggerUiConfigProperties swaggerUiConfigProperties;

    private final DiscoveryClient discoveryClient;

    /** 网关自身、认证、监控等不纳入聚合文档；文件服务已接入 springdoc，需展示请勿在此排除 */
    private static final String[] EXCLUDE_ROUTES =
        new String[] {"hongshu-gateway", "hongshu-auth", "hongshu-monitor"};

    public SwaggerDocRegister(SwaggerUiConfigProperties swaggerUiConfigProperties, DiscoveryClient discoveryClient)
    {
        this.swaggerUiConfigProperties = swaggerUiConfigProperties;
        this.discoveryClient = discoveryClient;
    }

    /**
     * 按服务名聚合（多实例只保留一条），网关 Swagger 下拉框展示各微服务 /{serviceId}/v3/api-docs
     */
    void refreshSwaggerUrls()
    {
        Set<AbstractSwaggerUiConfigProperties.SwaggerUrl> swaggerUrlSet =
            discoveryClient.getServices().stream()
                .filter(serviceId -> !StringUtils.equalsAnyIgnoreCase(serviceId, EXCLUDE_ROUTES))
                .map(serviceId -> {
                    AbstractSwaggerUiConfigProperties.SwaggerUrl swaggerUrl =
                        new AbstractSwaggerUiConfigProperties.SwaggerUrl();
                    swaggerUrl.setName(serviceId);
                    swaggerUrl.setUrl(String.format("/%s/v3/api-docs", serviceId));
                    return swaggerUrl;
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));

        swaggerUiConfigProperties.setUrls(swaggerUrlSet);
    }

    /**
     * 事件回调方法，处理InstancesChangeEvent事件
     * @param event 事件对象
     */
    @Override
    public void onEvent(InstancesChangeEvent event)
    {
        refreshSwaggerUrls();
    }

    /**
     * 订阅类型方法，返回订阅的事件类型
     * @return 订阅的事件类型
     */
    @Override
    public Class<? extends Event> subscribeType()
    {
        return InstancesChangeEvent.class;
    }
}
