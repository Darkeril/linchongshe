package com.hongshu.web.service.web.impl;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hongshu.common.core.constant.AuthConstant;
import com.hongshu.common.core.context.AuthContextHolder;
import com.hongshu.common.core.exception.HongshuException;
import com.hongshu.common.core.utils.RedisUtils;
import com.hongshu.web.domain.dto.AuthUserDTO;
import com.hongshu.web.domain.dto.UserPasswordOldDTO;
import com.hongshu.web.domain.entity.WebUser;
import com.hongshu.web.domain.vo.ThirdBindStatusVO;
import com.hongshu.web.mapper.web.WebUserMapper;
import com.hongshu.web.service.web.IWebUserSecurityService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WebUserSecurityServiceImpl implements IWebUserSecurityService {

    private final WebUserMapper userMapper;
    private final RedisUtils redisUtils;

    // 与短信发送存储保持一致
    private static final String VERIFY_CODE_PREFIX = "sms:verify:";

    @Override
    public ThirdBindStatusVO getThirdBindStatus() {
        String userId = AuthContextHolder.getUserId();
        // 目前项目未接入真正第三方绑定/解绑，这里先按 openid 是否存在做占位
        WebUser user = userMapper.selectById(userId);
        ThirdBindStatusVO vo = new ThirdBindStatusVO();
        vo.setWechatBind(user != null && StringUtils.isNotBlank(user.getOpenId()));
        vo.setQqBind(false);
        vo.setFacebookBind(false);
        return vo;
    }

    @Override
    @Transactional
    public WebUser updatePhoneNumber(String phoneNumber, String smsCode) {
        String userId = AuthContextHolder.getUserId();
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(phoneNumber)) {
            throw new HongshuException("参数不能为空");
        }
        if (StringUtils.isBlank(smsCode)) {
            throw new HongshuException("验证码不能为空");
        }
        if (!phoneNumber.matches("^1[3-9]\\d{9}$")) {
            throw new HongshuException("手机号格式不正确");
        }
        // 校验验证码（校验通过会自动删除）
        this.verifySmsCode(phoneNumber, smsCode);
        // 检查手机号是否已被占用
        long exists = userMapper.selectCount(new LambdaQueryWrapper<WebUser>()
                .eq(WebUser::getPhone, phoneNumber)
                .ne(WebUser::getId, userId));
        if (exists > 0) {
            throw new HongshuException("手机号已被绑定");
        }
        WebUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new HongshuException("用户不存在");
        }
        user.setPhone(phoneNumber);
        user.setUpdateTime(new Date());
        userMapper.updateById(user);
        return user;
    }

    /**
     * 通过手机号 + 短信验证码重置密码（忘记密码）
     */
    @Override
    @Transactional
    public boolean resetPasswordByPhone(AuthUserDTO authUserDTO) {
        if (authUserDTO == null) {
            throw new HongshuException("参数不能为空");
        }
        String phone = authUserDTO.getPhone();
        String password = authUserDTO.getPassword();
        String checkPassword = authUserDTO.getCheckPassword();
        String code = authUserDTO.getCode();

        if (StringUtils.isAnyBlank(phone, password, checkPassword, code)) {
            throw new HongshuException("参数不能为空");
        }
        if (!password.equals(checkPassword)) {
            throw new HongshuException("两次输入的密码不一致");
        }

        // 校验短信验证码
        verifySmsCode(phone, code);

        // 根据手机号查用户
        WebUser user = userMapper.selectOne(
                new LambdaQueryWrapper<WebUser>().eq(WebUser::getPhone, phone)
        );
        if (user == null) {
            throw new HongshuException("用户不存在");
        }

        user.setPassword(SecureUtil.md5(password));
        user.setUpdateTime(new Date());
        return userMapper.updateById(user) > 0;
    }

    /**
     * 校验短信验证码（不做区分 bind/reset，当前实现共用一套 key）
     */
    private void verifySmsCode(String phoneNumber, String smsCode) {
        String codeKey = VERIFY_CODE_PREFIX + "code:" + phoneNumber;
        String saved = redisUtils.get(codeKey);
        if (StringUtils.isBlank(saved)) {
            throw new HongshuException("验证码不存在或已过期");
        }
        if (!smsCode.equals(saved)) {
            throw new HongshuException("验证码错误");
        }
        redisUtils.delete(codeKey);
    }

    @Override
    @Transactional
    public boolean resetPasswordByOld(UserPasswordOldDTO dto) {
        String userId = AuthContextHolder.getUserId();
        if (StringUtils.isBlank(userId) || dto == null) {
            throw new HongshuException("参数不能为空");
        }
        if (StringUtils.isBlank(dto.getOldPassword()) || StringUtils.isBlank(dto.getNewPassword())) {
            throw new HongshuException("密码不能为空");
        }
        // 规则与前端一致：6-16位，数字+字母
        if (!dto.getNewPassword().matches("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$")) {
            throw new HongshuException("密码6-16位，必须包含数字和字母");
        }
        WebUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new HongshuException("用户不存在");
        }
        String oldMd5 = SecureUtil.md5(dto.getOldPassword());
        if (!oldMd5.equals(user.getPassword())) {
            throw new HongshuException("原密码错误");
        }
        user.setPassword(SecureUtil.md5(dto.getNewPassword()));
        user.setUpdateTime(new Date());
        return userMapper.updateById(user) > 0;
    }

    @Override
    @Transactional
    public boolean cancelAccount() {
        String userId = AuthContextHolder.getUserId();
        if (StringUtils.isBlank(userId)) {
            throw new HongshuException("参数不能为空");
        }
        WebUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new HongshuException("用户不存在");
        }
        // 约定：status=1 为封禁/不可用（项目登录处用 "1" 判定封禁）
        user.setStatus("1");
        user.setUpdateTime(new Date());
        int updated = userMapper.updateById(user);

        // 清理登录态缓存（让用户立即下线）
        try {
            String userKey = AuthConstant.USER_KEY + userId;
            String refreshTokenStartTimeKey = AuthConstant.REFRESH_TOKEN_START_TIME + userId;
            redisUtils.delete(List.of(userKey, refreshTokenStartTimeKey));
        } catch (Exception ignore) {
        }
        return updated > 0;
    }
}

