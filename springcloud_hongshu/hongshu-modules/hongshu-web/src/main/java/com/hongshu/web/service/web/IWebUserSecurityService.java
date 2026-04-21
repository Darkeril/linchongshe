package com.hongshu.web.service.web;

import com.hongshu.web.domain.dto.AuthUserDTO;
import com.hongshu.web.domain.dto.UserPasswordOldDTO;
import com.hongshu.web.domain.entity.WebUser;
import com.hongshu.web.domain.vo.ThirdBindStatusVO;

/**
 * App 端用户安全相关服务
 */
public interface IWebUserSecurityService {

    /**
     * 获取第三方绑定状态（占位实现）
     */
    ThirdBindStatusVO getThirdBindStatus();

    /**
     * 绑定新手机号（需先通过短信验证码校验）
     */
    WebUser updatePhoneNumber(String phoneNumber, String smsCode);

    /**
     * 通过手机号 + 短信验证码重置密码（忘记密码）
     */
    boolean resetPasswordByPhone(AuthUserDTO authUserDTO);

    /**
     * 通过原密码修改密码
     */
    boolean resetPasswordByOld(UserPasswordOldDTO dto);

    /**
     * 注销账号（软禁用）
     */
    boolean cancelAccount();

}

