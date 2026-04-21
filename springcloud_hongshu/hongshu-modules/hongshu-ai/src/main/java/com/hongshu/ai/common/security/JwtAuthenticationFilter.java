package com.hongshu.ai.common.security;

import com.alibaba.fastjson.JSON;
import com.hongshu.ai.common.constant.AuthConstant;
import com.hongshu.ai.common.constant.RedisConstants;
import com.hongshu.ai.common.utils.RedisUtilss;
import com.hongshu.common.core.constant.HttpConstant;
import com.hongshu.common.core.constant.StringPoolConstant;
import com.hongshu.common.core.enums.ResponseInfo;
import com.hongshu.common.core.exception.ValidateException;
import com.hongshu.common.core.utils.ApplicationContextUtil;
import com.hongshu.common.core.validator.ValidatorUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 登录认证过滤器
 *
 * @author: myj
 * @date: 2024/4/1
 * @version: 1.0.0
 */
public class JwtAuthenticationFilter extends BasicAuthenticationFilter {

    @Autowired
    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, MediaType.ALL.getType());
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, MediaType.ALL.getType());
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, MediaType.ALL.getType());
        response.setHeader(HttpHeaders.CACHE_CONTROL, HttpConstant.NO_CACHE);
        RedisUtilss redisUtil = ApplicationContextUtil.getBean(RedisUtilss.class);
        String uri = request.getRequestURI();
        // 对于本就 permitAll 的接口（统计、GPT 等），直接放行，避免因无效 token 导致 401
        if (uri.startsWith("/statistics")
                || uri.startsWith("/gpt/")
                || uri.startsWith("/common/")
                || uri.startsWith("/files/")
                || uri.startsWith("/app/chat/websocket")
                || uri.startsWith("/v1/chat/websocket")
                || uri.startsWith("/api/")
                || uri.startsWith("/app/api/")
        ) {
            chain.doFilter(request, response);
            return;
        }
        // 从Header中拿到token
        // 优先使用标准 Authorization 头；如果为空，则兼容 accessToken 头（web/uniapp 的用法）
        String token = request.getHeader(AuthConstant.JWT_TOKEN_HEADER);
        if (token == null || token.trim().length() == 0) {
            token = request.getHeader("accessToken");
        }
        // 如果未携带 token，直接放行（交由后续安全策略或 permitAll 处理）
        if (token == null || token.trim().isEmpty()) {
            chain.doFilter(request, response);
            return;
        }
        // 获取令牌并根据令牌获取登录认证信息
        Authentication authentication = null;
        Boolean flag = false;
        ResponseInfo responseInfo = ResponseInfo.error();
        try {
            authentication = JwtTokenUtils.getAuthenticationeFromToken(token);
        } catch (ValidateException e) {
            responseInfo = ResponseInfo.unauthorized(e.getMsg());
        } catch (UnsupportedJwtException e) {
            responseInfo = ResponseInfo.unauthorized("不支持的JWT格式");
        } catch (MalformedJwtException e) {
            responseInfo = ResponseInfo.unauthorized("非JWT格式");
        } catch (SignatureException e) {
            responseInfo = ResponseInfo.unauthorized("签名错误");
        } catch (IllegalArgumentException e) {
            responseInfo = ResponseInfo.unauthorized();
        } catch (ExpiredJwtException e) {
            responseInfo = ResponseInfo.unauthorized("token已过期");
        } catch (Exception e) {
            e.printStackTrace();
            responseInfo = ResponseInfo.unauthorized("解析token失败");
        }
        UserDetail userDetail = JwtTokenUtils.getUserDetail(authentication);

        // 判断redis中是否存在该token
        // 说明：
        //  - 对于 PC 后台签发的 token（包含 sessionId），仍然通过 Redis 判断是否失效；
        //  - 对于 web/uniapp 的 accessToken（只包含 userId，sessionId 为空），不再做 Redis 校验，避免误判为“已失效”。
        if (ValidatorUtil.isNotNull(authentication)
                && userDetail != null
                && ValidatorUtil.isNotNull(userDetail.getSessionId())
                && ValidatorUtil.isNull(redisUtil.get(RedisConstants.LOGIN_TOKEN_KEY + userDetail.getId() + StringPoolConstant.COLON + userDetail.getSessionId()))) {
            flag = true;
            responseInfo = ResponseInfo.unauthorized("token已失效，请重新登录");
        }
        if (ValidatorUtil.isNull(authentication) || flag) {
            // 生成并返回token给客户端，后续访问携带此token
            response.getWriter().print(JSON.toJSONString(responseInfo));
            response.getWriter().flush();
            response.getWriter().close();
            return;
        }
        // 设置登录认证信息到上下文
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }

}
