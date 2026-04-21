package com.hongshu.ai.controller.app;

import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import com.hongshu.ai.common.ResponseInfo;
import com.hongshu.ai.common.constant.RedisConstants;
import com.hongshu.ai.common.constant.SysConfigConstants;
import com.hongshu.ai.common.enums.StatusEnum;
import com.hongshu.ai.common.enums.UserTypeEnum;
import com.hongshu.ai.common.security.JwtTokenUtils;
import com.hongshu.ai.common.security.Oauth2Token;
import com.hongshu.ai.common.security.UserDetail;
import com.hongshu.ai.common.third.WxAppConstant;
import com.hongshu.ai.common.third.WxServiceHandler;
import com.hongshu.ai.common.utils.RedisUtilss;
import com.hongshu.ai.domain.command.LoginCommand;
import com.hongshu.ai.domain.vo.UserVO;
import com.hongshu.ai.service.IGptLoginLogService;
import com.hongshu.ai.service.IUserService;
import com.hongshu.common.core.constant.StringPoolConstant;
import com.hongshu.common.core.enums.ResponseEnum;
import com.hongshu.common.core.validator.ValidatorUtil;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.Set;

/**
 * 登录登出接口
 *
 * @author: myj
 * @date: 2023/5/4
 * @version: 1.0.0
 */
@RestController(value = "appLoginController")
@RequestMapping("/app")
public class LoginController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private WxServiceHandler wxServiceHandler;
    @Autowired
    private IUserService userService;
    @Autowired
    private RedisUtilss redisUtils;
    @Autowired
    private IGptLoginLogService gptLoginLogService;


    /**
     * 登录
     *
     * @author: myj
     * @date: 2023/1/9
     * @version: 1.0.0
     */
    @PostMapping("/api/oauth/token")
    public ResponseInfo<Oauth2Token> login(@RequestBody LoginCommand command) {
        // 自动登录特殊处理：跳过密码验证
        if (UserTypeEnum.AUTO_LOGIN.getValue().equals(command.getLoginType())) {
            return handleAutoLogin(command);
        }
        
        UsernamePasswordAuthenticationToken authenticationToken;
        if (UserTypeEnum.WXAPP.getValue().equals(command.getLoginType())) {
            WxMaPhoneNumberInfo wxInfo;
            try {
                wxInfo = wxServiceHandler.getMaService(WxAppConstant.APP_ID).getUserService().getNewPhoneNoInfo(command.getCode());
            } catch (WxErrorException e) {
                return ResponseInfo.error("获取小程序用户信息失败");
            }
            authenticationToken = new UsernamePasswordAuthenticationToken(wxInfo.getPurePhoneNumber(), StringPoolConstant.EMPTY);
        } else if (UserTypeEnum.WXMP.getValue().equals(command.getLoginType())) {
            WxMaPhoneNumberInfo wxInfo;
            try {
                wxInfo = wxServiceHandler.getMaService(WxAppConstant.APP_ID).getUserService().getNewPhoneNoInfo(command.getCode());
            } catch (WxErrorException e) {
                return ResponseInfo.error("获取小程序用户信息失败");
            }
            authenticationToken = new UsernamePasswordAuthenticationToken(wxInfo.getPurePhoneNumber(), StringPoolConstant.EMPTY);
        } else if (UserTypeEnum.TEL.getValue().equals(command.getLoginType())) {
            UserVO user = userService.loginByTel(command.getTel(), command.getPassword(), command.getCode()).getData();
            authenticationToken = new UsernamePasswordAuthenticationToken(UserTypeEnum.TEL.getPrefix() + user.getTel(), command.getPassword());
        } else if (UserTypeEnum.USERNAME.getValue().equals(command.getLoginType())) {
            // 账户密码登录：使用手机号作为用户名，添加tel:前缀
            authenticationToken = new UsernamePasswordAuthenticationToken(UserTypeEnum.TEL.getPrefix() + command.getTel(), command.getPassword());
        } else {
            return ResponseInfo.validateFail("未知的登录方式，请确认登录方式");
        }
        Authentication auth = null;
        ResponseInfo<Oauth2Token> response = ResponseInfo.unauthorized();
        try {
            auth = authenticationManager.authenticate(authenticationToken);
        } catch (AuthenticationException e) {
            e.printStackTrace();
            if (ResponseEnum.ACCOUNT_NOT_EXIST.getMsg().equals(e.getMessage())) {
                response = ResponseInfo.accountNotExist();
            } else if (ResponseEnum.ACCOUNT_IS_DISABLED.getMsg().equals(e.getMessage())) {
                response = ResponseInfo.accountDisabled();
            } else if (ResponseEnum.PASSWORD_ERROR.getMsg().equals(e.getMessage())) {
                response = ResponseInfo.passwordError();
            } else if (e instanceof BadCredentialsException) {
                response = ResponseInfo.passwordError();
            } else if (e instanceof CredentialsExpiredException) {
                response = ResponseInfo.unauthorized();
            } else {
                response = ResponseInfo.error(Optional.ofNullable(e.getMessage()).orElse("登录失败"));
            }
        }
        //如果认证没通过给出相应的提示
        if (ValidatorUtil.isNull(auth)) {
            return response;
        }
        // 存储登录认证信息到上下文
        SecurityContextHolder.getContext().setAuthentication(auth);
        Oauth2Token token = Oauth2Token.builder().token(JwtTokenUtils.generateToken(auth)).expiresIn(JwtTokenUtils.EXPIRE_TIME / 1000L).build();
        // 添加登录日志
        this.saveLoginLog(token.getToken());
        return ResponseInfo.success(token);
    }

    /**
     * 自动登录处理 - 跳过密码验证
     *
     * @param command 登录命令
     * @return 登录结果
     */
    private ResponseInfo<Oauth2Token> handleAutoLogin(LoginCommand command) {
        try {
            // 验证用户是否存在
            ResponseInfo<UserVO> userResponse = userService.autoLogin(command.getTel());
            if (userResponse.getCode() != 200) {
                return ResponseInfo.error(userResponse.getMsg());
            }
            
            UserVO user = userResponse.getData();
            if (ValidatorUtil.isNull(user)) {
                return ResponseInfo.accountNotExist();
            }
            
            // 构造UserDetail对象
            UserDetail userDetail = new UserDetail(user, new java.util.HashSet<>());
            if (!userDetail.isEnabled()) {
                return ResponseInfo.error("账户已被禁用");
            }
            
            // 创建已认证的Authentication对象（跳过密码验证）
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userDetail, null, userDetail.getAuthorities());
            authentication.setDetails(userDetail);
            
            // 存储登录认证信息到上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 生成Token
            Oauth2Token token = Oauth2Token.builder()
                .token(JwtTokenUtils.generateToken(authentication))
                .expiresIn(JwtTokenUtils.EXPIRE_TIME / 1000L)
                .build();
            
            // 添加登录日志
            this.saveLoginLog(token.getToken());
            
            return ResponseInfo.success(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseInfo.error("自动登录失败：" + e.getMessage());
        }
    }

    /**
     * 添加登录日志
     *
     * @param token 用户token
     */
    private void saveLoginLog(String token) {
        UserDetail userDetail = JwtTokenUtils.getLoginUser();
        String allLogin = redisUtils.get(RedisConstants.SYS_CONFIG_KEY + SysConfigConstants.ALL_LOGIN);
        String key = RedisConstants.LOGIN_TOKEN_KEY + userDetail.getId() + StringPoolConstant.COLON;
        if (StringPoolConstant.FALSE.equalsIgnoreCase(allLogin)) {
            Set<String> keys = redisUtils.getKeys(key + StringPoolConstant.ASTERISK);
            redisUtils.del(keys);
        }
        redisUtils.set(key + userDetail.getSessionId(), token, JwtTokenUtils.EXPIRE_TIME / 1000L);
        gptLoginLogService.saveLoginLog(userDetail.getId(), userDetail.getSessionId(),
                userDetail.getUsername(), StatusEnum.ENABLED.getValue(), token, "登录成功");
    }

}
