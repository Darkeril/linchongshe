package com.hongshu.ai.service.impl;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongshu.ai.common.IPageInfo;
import com.hongshu.ai.common.Query;
import com.hongshu.ai.common.ResponseInfo;
import com.hongshu.ai.common.constant.BaseConfigConstant;
import com.hongshu.ai.common.enums.IntegerEnum;
import com.hongshu.ai.common.enums.StatusEnum;
import com.hongshu.ai.common.enums.UserTypeEnum;
import com.hongshu.ai.common.exception.ErrorException;
import com.hongshu.ai.common.exception.ProhibitVisitException;
import com.hongshu.ai.common.security.JWTPasswordEncoder;
import com.hongshu.ai.common.utils.CommonUtil;
import com.hongshu.ai.domain.command.SysUserShareCommand;
import com.hongshu.ai.domain.command.UserCommand;
import com.hongshu.ai.domain.command.UserPasswordCommand;
import com.hongshu.ai.domain.dto.config.AppInfoDTO;
import com.hongshu.ai.domain.entity.User;
import com.hongshu.ai.domain.vo.UserVO;
import com.hongshu.ai.mapper.UserMapper;
import com.hongshu.ai.service.BaseConfigService;
import com.hongshu.ai.service.IUserService;
import com.hongshu.common.core.constant.AuthConstant;
import com.hongshu.common.core.constant.StringPoolConstant;
import com.hongshu.common.core.enums.ResponseEnum;
import com.hongshu.common.core.utils.DozerUtil;
import com.hongshu.common.core.utils.RandomUtil;
import com.hongshu.common.core.validator.ValidatorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会员用户 服务实现类
 *
 * @author: Yang
 * @date: 2023-04-28
 * @version: 1.0.0
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private BaseConfigService baseConfigService;


    /**
     * 根据id获取会员用户信息
     *
     * @param id 会员用户id
     * @return
     */
    private User getUser(Long id) {
        User user = userMapper.selectById(id);
        if (ValidatorUtil.isNull(user)) {
            throw new ErrorException("会员用户信息不存在，无法操作");
        }
        return user;
    }

    @Override
    public ResponseInfo<IPageInfo<UserVO>> pageUser(Query query) {
        IPage<UserVO> iPage = userMapper.pageUser(new Page<>(query.getCurrent(), query.getSize()), query);
        iPage.getRecords().stream().forEach(v -> v.setTel(CommonUtil.mobileEncrypt(v.getTel())));
        return ResponseInfo.success(new IPageInfo(iPage.getCurrent(), iPage.getSize(), iPage.getTotal(), iPage.getRecords()));
    }

    @Override
    public ResponseInfo<List<UserVO>> listUser(Query query) {
        List<UserVO> userVOS = userMapper.listUser(query);
        userVOS.stream().forEach(v -> v.setTel(CommonUtil.mobileEncrypt(v.getTel())));
        return ResponseInfo.success(userVOS);
    }

    @Override
    public ResponseInfo<UserVO> getUserById(Long id) {
        UserVO userVO = DozerUtil.convertor(getUser(id), UserVO.class);
        if (ValidatorUtil.isNull(userVO)) {
            throw new ProhibitVisitException("会员用户信息不存在，无法操作");
        }
        userVO.setTel(CommonUtil.mobileEncrypt(userVO.getTel()));
        return ResponseInfo.success(userVO);
    }

    @Override
    public ResponseInfo<UserVO> getLoginUserById(Long id) {
        User user = userMapper.selectById(id);
        if (ValidatorUtil.isNull(user)) {
            throw new ProhibitVisitException("会员用户信息不存在，无法操作");
        }
        UserVO userVO = DozerUtil.convertor(user, UserVO.class);
        userVO.setTel(CommonUtil.mobileEncrypt(userVO.getTel()));
        return ResponseInfo.success(userVO);
    }

    @Override
    public ResponseInfo<UserVO> getUserByUserName(String username) {
        User user = null;
        if (username.startsWith(UserTypeEnum.TEL.getPrefix())) {
            user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getTel, username.replace(UserTypeEnum.TEL.getPrefix(), StringPoolConstant.EMPTY)));
        } else if (username.startsWith(UserTypeEnum.USERNAME.getPrefix())) {
            user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getTel, username.replace(UserTypeEnum.USERNAME.getPrefix(), StringPoolConstant.EMPTY)));
        }
        return ResponseInfo.success(DozerUtil.convertor(user, UserVO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "masterTransactionManager")
    public ResponseInfo<UserVO> loginByTel(String tel, String password, String shareCode) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getTel, tel));
        if (ValidatorUtil.isNotNull(user)) {
            return ResponseInfo.success(DozerUtil.convertor(user, UserVO.class));
        }
        String name = "用户" + RandomUtil.randomString(8);
        user = User.builder()
                .loginTime(LocalDateTime.now())
                .uid(UUID.randomUUID().toString())
                .name(name).nickName(name)
                .avatar(AuthConstant.DEFAULT_AVATAR)
                .tel(tel).num(IntegerEnum.ZERO.getValue())
                .type(UserTypeEnum.TEL.getValue())
                .status(StatusEnum.ENABLED.getValue())
                .build();
        if (ValidatorUtil.isNotNull(password)) {
            user.setPassword(JWTPasswordEncoder.bcryptEncode(password));
        } else {
            user.setPassword(JWTPasswordEncoder.bcryptEncode("123456a"));
        }
        // 是否开启注册赠送
        AppInfoDTO appInfo = baseConfigService.getBaseConfigByName(BaseConfigConstant.APP_INFO, AppInfoDTO.class);
        if (ValidatorUtil.isNotNull(appInfo) && ValidatorUtil.isNotNull(appInfo.getFreeNum())) {
            user.setNum(appInfo.getFreeNum());
        }
        userMapper.insert(user);
        return ResponseInfo.success(DozerUtil.convertor(user, UserVO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "masterTransactionManager")
    public ResponseInfo<UserVO> autoLogin(String tel) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getTel, tel));
        if (ValidatorUtil.isNull(user)) {
            return ResponseInfo.businessFail("该用户未开通AI功能，请联系管理员开通");
        }
        return ResponseInfo.success(DozerUtil.convertor(user, UserVO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "masterTransactionManager")
    public ResponseInfo saveUser(UserCommand command) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getTel, command.getTel()));
        if (ValidatorUtil.isNotNull(user)) {
            return ResponseInfo.customizeError(ResponseEnum.TEL_IS_EXIST);
        }
        user = DozerUtil.convertor(command, User.class);
        user.setCreateUser(command.getOperater());
        String name = "手机用户" + RandomUtil.randomString(6);
        user.setUid(UUID.fastUUID().toString());
        user.setName(name);
        user.setNickName(name);
        user.setType(UserTypeEnum.TEL.getValue());
        userMapper.insert(user);
        return ResponseInfo.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "masterTransactionManager")
    public ResponseInfo updateUser(UserCommand command) {
        User user = getUser(command.getId());
        user.setNickName(command.getNickName());
        user.setUpdateUser(command.getOperater());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return ResponseInfo.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseInfo updateUserContext(UserCommand command) {
        User user = getUser(command.getOperaterId());
        user.setContext(command.getContext());
        userMapper.updateById(user);
        return ResponseInfo.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseInfo updateUserAvatar(Long id, String avatar) {
        User user = getUser(id);
        user.setAvatar(avatar);
        userMapper.updateById(user);
        return ResponseInfo.success(avatar);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseInfo updatePassword(UserPasswordCommand command) {
        User user = getUser(command.getOperaterId());
        boolean isBcrypt = JWTPasswordEncoder.matchesBcrypt(user.getPassword());
        if (isBcrypt) {
            if (!JWTPasswordEncoder.matchesBcrypt(command.getOldPassword(), user.getPassword())) {
                return ResponseInfo.businessFail("旧密码错误，您还有5次机会或者联系管理员重置密码");
            }
            if (JWTPasswordEncoder.matchesBcrypt(command.getNewPassword(), user.getPassword())) {
                return ResponseInfo.businessFail("新密码与旧密码相同，无需重置");
            }
        } else {
            if (!command.getOldPassword().equals(user.getPassword())) {
                return ResponseInfo.businessFail("旧密码错误，您还有5次机会或者联系管理员重置密码");
            }
            if (command.getNewPassword().equals(user.getPassword())) {
                return ResponseInfo.businessFail("新密码与旧密码相同，无需重置");
            }
        }
        user.setPassword(JWTPasswordEncoder.bcryptEncode(command.getNewPassword()));
        userMapper.updateById(user);
        return ResponseInfo.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "masterTransactionManager")
    public ResponseInfo removeUserByIds(List<Long> ids) {
        userMapper.deleteBatchIds(ids);
        return ResponseInfo.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "masterTransactionManager")
    public ResponseInfo removeUserById(Long id) {
        userMapper.deleteById(id);
        return ResponseInfo.success();
    }

    @Override
    public ResponseInfo shareAccount(SysUserShareCommand command) {
        return null;
    }

}
