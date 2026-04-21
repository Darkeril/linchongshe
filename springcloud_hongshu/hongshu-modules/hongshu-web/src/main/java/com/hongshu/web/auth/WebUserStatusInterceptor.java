package com.hongshu.web.auth;

import com.hongshu.common.core.context.AuthContextHolder;
import com.hongshu.common.core.enums.ResultCodeEnum;
import com.hongshu.common.core.exception.ServiceException;
import com.hongshu.common.core.validator.myVaildator.noLogin.NoLoginIntercept;
import com.hongshu.web.domain.entity.WebUser;
import com.hongshu.web.mapper.web.WebUserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 校验 Web 端用户状态：封禁、待审核时拒绝访问业务接口（管理端 /member 等排除）
 */
@Component
@RequiredArgsConstructor
public class WebUserStatusInterceptor implements HandlerInterceptor {

    private final WebUserMapper webUserMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        String uri = request.getRequestURI();
        if (shouldSkip(uri)) {
            return true;
        }
        HandlerMethod hm = (HandlerMethod) handler;
        if (hm.hasMethodAnnotation(NoLoginIntercept.class)) {
            return true;
        }
        String userId = AuthContextHolder.getUserId();
        if (StringUtils.isBlank(userId)) {
            return true;
        }
        WebUser user = webUserMapper.selectById(userId);
        if (user == null) {
            return true;
        }
        if (user.getDeleted() != null && user.getDeleted() != 0) {
            return true;
        }
        if ("1".equals(user.getStatus())) {
            throw new ServiceException(ResultCodeEnum.ERROR_ACCOUNT_SUSPENDED.getMessage(),
                    ResultCodeEnum.ERROR_ACCOUNT_SUSPENDED.getCode());
        }
        if ("2".equals(user.getStatus())) {
            throw new ServiceException(ResultCodeEnum.ERROR_ACCOUNT_PENDING_AUDIT.getMessage(),
                    ResultCodeEnum.ERROR_ACCOUNT_PENDING_AUDIT.getCode());
        }
        return true;
    }

    private boolean shouldSkip(String uri) {
        if (uri == null) {
            return true;
        }
        if (uri.contains("/member")) {
            return true;
        }
        if (uri.contains("/auth/") || uri.contains("/app/auth/")) {
            return true;
        }
        if (uri.contains("/config")) {
            return true;
        }
        if (uri.contains("/swagger") || uri.contains("/v3/api-docs") || uri.contains("/doc.html") || uri.contains("/webjars/")) {
            return true;
        }
        if (uri.contains("/actuator") || uri.contains("/error") || uri.contains("/favicon.ico")) {
            return true;
        }
        return false;
    }
}
