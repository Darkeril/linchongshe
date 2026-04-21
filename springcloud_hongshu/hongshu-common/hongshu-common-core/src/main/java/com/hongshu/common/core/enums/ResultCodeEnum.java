package com.hongshu.common.core.enums;

import lombok.Getter;

/**
 * 统一返回结果状态信息类
 *
 * @author: hongshu
 */
@Getter
public enum ResultCodeEnum {

    SUCCESS(200, "成功"),
    FAIL(201, "失败"),
    TOKEN_FAIL(401, "token异常"),
    TOKEN_EXIST(501, "token过期"),
    ERROR_PASSWORD(502, "密码有误，请检查重新输入"),
    ERROR_ACCOUNT_SUSPENDED(503, "账户异常已被封禁，请联系管理员处理"),
    ERROR_IP_BANNED(504, "IP地址异常已禁止登录，请联系管理员处理"),
    ERROR_PHONE(505, "手机号不能为空"),
    ERROR_CODE(506, "验证码不能为空"),
    ERROR_BLOCKPUZZLE(507, "滑块验证未通过"),
    ERROR_IP_COUNT(508, "当前IP发送过于频繁，请稍后再试"),
    ERROR_CODE_COUNT(509, "当前号码发送验证码过于频繁，请稍后再试"),
    ERROR_CODE_NULL(510, "验证码不存在或已过期，请重新获取"),
    ERROR_CODE_ERROR(511, "验证码有误，请检查充新输入"),
    /** C 端账号待管理员审核，禁止登录与调用业务接口（已注册用户再次登录） */
    ERROR_ACCOUNT_PENDING_AUDIT(516, "账号待审核，请等待管理员通过后再登录"),
    /** 首次自动注册成功，当前为待审核状态，不发放 token */
    REGISTER_PENDING_AUDIT(517, "注册成功，请等待管理员审核通过后再登录"),
    ERROR_2(512, ""),
    ERROR_3(513, ""),
    ERROR_4(514, ""),
    ERROR_5(515, ""),


    NOT_NULL(10001, "为空");

    private final Integer code;

    private final String message;

    private ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
