package com.hongshu.ai.controller.base;

import com.hongshu.ai.common.constant.AuthConstant;
import com.hongshu.ai.common.exception.ProhibitVisitException;
import com.hongshu.ai.common.security.JwtTokenUtils;
import com.hongshu.ai.common.security.UserDetail;
import com.hongshu.common.core.constant.StringPoolConstant;
import com.hongshu.common.core.enums.ResponseInfo;
import com.hongshu.common.core.utils.ip.IPUtil;
import com.hongshu.common.core.validator.ValidatorUtil;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 基础接口抽象类
 *
 * @author: myj
 * @date: 2024/4/1
 * @version: 1.0.0
 */
public abstract class BaseController extends ResponseInfo {

    @Autowired
    private HttpServletRequest request;
//    @Autowired
//    private ISysUserService sysUserService;


    /**
     * 获取请求ip地址
     *
     * @return ip地址
     */
    public String getIp() {
        return IPUtil.getIpAddr(request);
    }

    /**
     * 获取请求头中token信息
     *
     * @return token
     */
    public String getToken() {
        String token = request.getHeader(AuthConstant.JWT_TOKEN_HEADER);
        return ValidatorUtil.isNotNull(token) ? token.replace(AuthConstant.JWT_TOKEN_PREFIX, StringPoolConstant.EMPTY) : null;
    }

    /**
     * 获取登录用户信息
     */
    public UserDetail getLoginUser() {
        try {
            UserDetail userDetail = JwtTokenUtils.getLoginUser();
            if (ValidatorUtil.isNull(userDetail)) {
                throw new ProhibitVisitException();
            }
            return userDetail;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ProhibitVisitException();
        }
    }

    /**
     * 获取登录用户id
     */
    public Long getSysUserId() {
        return getLoginUser().getId();
    }

    /**
     * 获取登录用户标识
     */
    public String getUid() {
        return getLoginUser().getUid();
    }

    /**
     * 获取登录用户名称
     */
    public String getSysUserName() {
        return getLoginUser().getUsername();
    }

    /**
     * 获取登录账号信息
     *
     * @return
     */
//    public SysUser getSysUser() {
//        return sysUserService.getById(getLoginUser().getId());
//    }

}
