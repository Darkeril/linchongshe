package com.hongshu.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import com.hongshu.common.core.constant.CacheConstants;
import com.hongshu.common.core.constant.HttpStatus;
import com.hongshu.common.core.constant.SecurityConstants;
import com.hongshu.common.core.constant.TokenConstants;
import com.hongshu.common.core.utils.JwtUtils;
import com.hongshu.common.core.utils.ServletUtils;
import com.hongshu.common.core.utils.StringUtils;
import com.hongshu.common.redis.service.RedisService;
import com.hongshu.gateway.config.properties.IgnoreWhiteProperties;
import io.jsonwebtoken.Claims;
import reactor.core.publisher.Mono;

/**
 * 网关鉴权
 *

 */
@Component
public class AuthFilter implements GlobalFilter, Ordered
{
    private static final Logger log = LoggerFactory.getLogger(AuthFilter.class);

    // 排除过滤的 uri 地址，nacos自行添加
    @Autowired
    private IgnoreWhiteProperties ignoreWhite;

    @Autowired
    private RedisService redisService;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain)
    {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpRequest.Builder mutate = request.mutate();

        String url = request.getURI().getPath();
        // Druid 监控页由 iframe 加载，一般不携带管理端 JWT；放行后由系统服务上的 Druid 控制台账号校验
        if (StringUtils.isNotEmpty(url) && url.startsWith("/druid")) {
            return chain.filter(exchange);
        }
        // 管理端登录页拉取的公开配置（无 token），需与 websiteConfig / carousel 一样匿名可达
        if (StringUtils.isNotEmpty(url) && url.contains("/system/config/adminLoginPromo")) {
            return chain.filter(exchange);
        }
        // 跳过不需要验证的路径
        if (StringUtils.matches(url, ignoreWhite.getWhites()))
        {
            return chain.filter(exchange);
        }
        String token = getToken(request);
        if (StringUtils.isEmpty(token))
        {
            return unauthorizedResponse(exchange, "令牌不能为空");
        }
        Claims claims = JwtUtils.parseToken(token);
        if (claims == null)
        {
            return unauthorizedResponse(exchange, "令牌已过期或验证不正确！");
        }
        
        // 尝试获取userKey（系统模块的token会有这个字段）
        String userkey = JwtUtils.getUserKey(claims);
        String userid = null;
        String username = null;
        
        // 如果userKey为空，说明是web模块的token（只包含userId）
        if (StringUtils.isEmpty(userkey))
        {
            // web模块的token，直接从claims中获取userId
            Object userIdObj = claims.get("userId");
            if (userIdObj != null)
            {
                userid = userIdObj.toString();
                // 对于web模块的token，使用userId作为userKey
                userkey = userid;
                // web模块的token不依赖Redis，直接验证JWT即可
                // 不需要从Redis查找，因为web模块的token是自包含的
            }
            else
            {
                return unauthorizedResponse(exchange, "令牌验证失败：无法获取用户ID");
            }
        }
        else
        {
            // 系统模块的token，需要从Redis验证
            boolean islogin = redisService.hasKey(getTokenKey(userkey));
            if (!islogin)
            {
                return unauthorizedResponse(exchange, "登录状态已过期");
            }
            userid = JwtUtils.getUserId(claims);
            username = JwtUtils.getUserName(claims);
            if (StringUtils.isEmpty(userid) || StringUtils.isEmpty(username))
            {
                return unauthorizedResponse(exchange, "令牌验证失败");
            }
        }

        // 设置用户信息到请求
        addHeader(mutate, SecurityConstants.USER_KEY, userkey);
        if (StringUtils.isNotEmpty(userid))
        {
            addHeader(mutate, SecurityConstants.DETAILS_USER_ID, userid);
        }
        if (StringUtils.isNotEmpty(username))
        {
            addHeader(mutate, SecurityConstants.DETAILS_USERNAME, username);
        }
        // 内部请求来源参数清除
        removeHeader(mutate, SecurityConstants.FROM_SOURCE);
        return chain.filter(exchange.mutate().request(mutate.build()).build());
    }

    private void addHeader(ServerHttpRequest.Builder mutate, String name, Object value)
    {
        if (value == null)
        {
            return;
        }
        String valueStr = value.toString();
        String valueEncode = ServletUtils.urlEncode(valueStr);
        mutate.header(name, valueEncode);
    }

    private void removeHeader(ServerHttpRequest.Builder mutate, String name)
    {
        mutate.headers(httpHeaders -> httpHeaders.remove(name)).build();
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String msg)
    {
        log.error("[鉴权异常处理]请求路径:{},错误信息:{}", exchange.getRequest().getPath(), msg);
        return ServletUtils.webFluxResponseWriter(exchange.getResponse(), msg, HttpStatus.UNAUTHORIZED);
    }

    /**
     * 获取缓存key
     */
    private String getTokenKey(String token)
    {
        return CacheConstants.LOGIN_TOKEN_KEY + token;
    }

    /**
     * 获取请求token
     */
    private String getToken(ServerHttpRequest request)
    {
        String token = request.getHeaders().getFirst(SecurityConstants.AUTHORIZATION_HEADER);
        // 如果前端设置了令牌前缀，则裁剪掉前缀
        if (StringUtils.isNotEmpty(token) && token.startsWith(TokenConstants.PREFIX))
        {
            token = token.replaceFirst(TokenConstants.PREFIX, StringUtils.EMPTY);
        }
        return token;
    }

    @Override
    public int getOrder()
    {
        return -200;
    }
}
