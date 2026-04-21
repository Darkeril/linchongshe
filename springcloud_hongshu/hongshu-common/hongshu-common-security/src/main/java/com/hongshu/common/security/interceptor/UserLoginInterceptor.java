package com.hongshu.common.security.interceptor;

import com.hongshu.common.core.constant.TokenConstant;
import com.hongshu.common.core.constant.UserConstant;
import com.hongshu.common.core.context.AuthContextHolder;
import com.hongshu.common.core.enums.ResultCodeEnum;
import com.hongshu.common.core.exception.ServiceException;
import com.hongshu.common.core.utils.JwtUtilss;
import com.hongshu.common.core.utils.WebUtils;
import com.hongshu.common.core.validator.myVaildator.noLogin.NoLoginIntercept;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 用户登录拦截器
 * 用于验证用户登录token，并将用户ID存储到线程变量中
 *

 */
@Slf4j
public class UserLoginInterceptor implements HandlerInterceptor {

    /**
     * token拦截验证
     *
     * @param request  current HTTP request
     * @param response current HTTP response
     * @param handler  chosen handler to execute, for type and/or instance evaluation
     * @return true继续执行，false中断执行
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        // 如果不是HandlerMethod，直接放行（如静态资源等）
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 获取请求路径
        String requestURI = request.getRequestURI();

        // 排除二维码轮询接口的日志输出（避免刷屏）
        boolean isQrCodePolling = requestURI.contains("/qrcode/status");

        String accessToken = request.getHeader(TokenConstant.ACCESS_TOKEN);

        // 非轮询接口才记录日志
        if (!isQrCodePolling) {
            log.debug("accessToken:{},{}", accessToken, WebUtils.getRequestHeader(UserConstant.USER_ID));
        }

        // 获取方法处理器
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        NoLoginIntercept noLoginIntercept = handlerMethod.getMethod()
                .getAnnotation(NoLoginIntercept.class);

        // 如果方法上标注了@NoLoginIntercept注解，则跳过登录验证
        if (noLoginIntercept != null) {
            // 即使不需要登录，如果提供了token，也解析并设置用户ID（用于可选登录的场景）
            if (!StringUtils.isEmpty(accessToken)) {
                setLocalUser(accessToken);
            }
            return true;
        }

        // 判断token不为空
        if (!StringUtils.isEmpty(accessToken)) {
            setLocalUser(accessToken);
            return true;
        }

        // token为空且需要登录，抛出异常
        throw new ServiceException(ResultCodeEnum.TOKEN_FAIL.getMessage(), ResultCodeEnum.TOKEN_FAIL.getCode());
    }

    /**
     * 设置当前线程的用户信息
     *
     * @param accessToken 访问令牌
     */
    private void setLocalUser(String accessToken) {
        boolean flag = JwtUtilss.checkToken(accessToken);
        if (!flag) {
            throw new ServiceException(ResultCodeEnum.TOKEN_EXIST.getMessage(), ResultCodeEnum.TOKEN_EXIST.getCode());
        }
        String userId = JwtUtilss.getUserId(accessToken);
        AuthContextHolder.setUserId(userId);
    }

    /**
     * 请求处理完成后清除线程变量
     *
     * @param request  current HTTP request
     * @param response current HTTP response
     * @param handler  chosen handler to execute
     * @param ex       异常信息
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 清除当前线程的用户ID，防止内存泄漏
        AuthContextHolder.removeUserId();
    }
}









