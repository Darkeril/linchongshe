package com.hongshu.ai.common.security;

import com.hongshu.ai.domain.vo.UserVO;
import com.hongshu.ai.service.IUserService;
import com.hongshu.common.core.enums.ResponseEnum;
import com.hongshu.common.core.validator.ValidatorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * 用户登录认证信息查询
 *
 * @author: myj
 * @date: 2024/4/1
 * @version: 1.0.0
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private IUserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) {
        UserDetail userDetail;
        Set<String> permissions = new HashSet<>();

        UserVO user = Optional.ofNullable(userService.getUserByUserName(username).getData()).orElse(null);
        if (ValidatorUtil.isNull(user)) {
            throw new BadCredentialsException(ResponseEnum.ACCOUNT_NOT_EXIST.getMsg());
        }

        userDetail = new UserDetail(user, permissions);

        if (!userDetail.isEnabled()) {
            throw new DisabledException(ResponseEnum.ACCOUNT_IS_DISABLED.getMsg());
        }

        return userDetail;
    }
}

