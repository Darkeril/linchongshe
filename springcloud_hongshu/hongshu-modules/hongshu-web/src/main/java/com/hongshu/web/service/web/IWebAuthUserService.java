package com.hongshu.web.service.web;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hongshu.web.domain.dto.AuthUserDTO;
import com.hongshu.web.domain.dto.WeChatLoginDTO;
import com.hongshu.web.domain.entity.WebUser;
import com.hongshu.web.domain.vo.QrCodeConfirmVO;

import java.util.Map;
public interface IWebAuthUserService extends IService<WebUser> {

    /**
     * 用户登录
     *
     * @param authUserDTO 用户
     */
    Map<String, Object> login(AuthUserDTO authUserDTO);

    /**
     * 验证码登录
     *
     * @param authUserDTO 用户
     */
    Map<String, Object> loginByCode(AuthUserDTO authUserDTO);

    /**
     * 发送验证码
     *
     * @param phone 手机号
     */
    boolean sendVerifyCode(String phone, String captchaVerification);

    /**
     * 演示账户登录
     *
     * @param authUserDTO 用户
     */
    Map<String, Object> demoLogin(AuthUserDTO authUserDTO);

    /**
     * 根据token获取当前用户
     *
     * @param accessToken accessToken
     */
    WebUser getUserInfoByToken(String accessToken);

    /**
     * 用户注册
     *
     * @param authUserDTO 前台传递用户信息
     */
    WebUser register(AuthUserDTO authUserDTO);

    /**
     * 用户是否注册
     *
     * @param authUserDTO 用户
     */
    boolean isRegister(AuthUserDTO authUserDTO);

    /**
     * 退出登录
     *
     * @param userId 用户ID
     */
    void loginOut(String userId, String terminal);

    /**
     * 修改密码
     *
     * @param authUserDTO 用户
     */
    boolean updatePassword(AuthUserDTO authUserDTO);

//    /**
//     * 第三方登录
//     *
//     * @param userOtherLoginRelationDTO
//     */
//     @Deprecated
//     Map<String, Object> otherLogin(UserOtherLoginRelationDTO userOtherLoginRelationDTO);

    /**
     * 刷新token
     *
     * @param refreshToken refreshToken
     */
    Map<String, Object> refreshToken(String refreshToken);

    /**
     * 生成二维码
     */
    Map<String, Object> getQrCode();

    /**
     * 移动端扫描二维码
     *
     * @param loginToken 登录token
     */
    Map<String, Object> scanQrCode(String loginToken);

    /**
     * 移动端确认二维码登录
     *
     * @param vo 二维码确认信息
     */
    Map<String, Object> confirmQrCodeLogin(QrCodeConfirmVO vo);

    /**
     * 检查二维码状态
     *
     * @param loginToken 登录token
     */
    Map<String, Object> checkQrCodeStatus(String loginToken);

    /**
     * 二维码登录成功，获取用户信息和token
     *
     * @param userId 用户ID
     */
    Map<String, Object> qrCodeLogin(String userId);

    /**
     * 微信小程序登录
     *
     * @param weChatLoginDTO 微信登录信息
     */
    Map<String, Object> wechatLogin(WeChatLoginDTO weChatLoginDTO);

    /**
     * 生成微信网页授权URL
     *
     * @param redirectUri 回调地址
     * @return 授权URL
     */
    String generateWeChatAuthUrl(String redirectUri);

    /**
     * 微信网页登录（通过code）
     *
     * @param code 微信授权码
     * @return 登录结果（包含token和用户信息）
     */
    Map<String, Object> wechatWebLogin(String code);

    /**
     * 获取微信回调地址（从配置中读取）
     *
     * @return 回调地址
     */
    String getWeChatCallbackUrl();
}
