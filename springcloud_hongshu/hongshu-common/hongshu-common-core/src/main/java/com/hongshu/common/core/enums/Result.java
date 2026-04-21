package com.hongshu.common.core.enums;

import com.hongshu.common.core.constant.AuthConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * @author: hongshu
 */
@Schema(name = "统一返回结果")
@Data
public class Result<T> {

    @Schema(description = "状态码")
    private Integer code;

    @Schema(description = "信息")
    private String message;

    @Schema(description = "数据")
    private T data;

    //构造私有化
    private Result() {
    }

    //设置数据,返回对象的方法
    public static <T> Result<T> build(T data, Integer code, String message) {
        //创建Resullt对象，设置值，返回对象
        Result<T> result = new Result<>();
        //判断返回结果中是否需要数据
        if (data != null) {
            //设置数据到result对象
            result.setData(data);
        }
        //设置其他值
        result.setCode(code);
        result.setMessage(message);
        //返回设置值之后的对象
        return result;
    }


    //设置数据,返回对象的方法
    public static <T> Result<T> build(T data, ResultCodeEnum resultCodeEnum) {
        //创建Resullt对象，设置值，返回对象
        Result<T> result = new Result<>();
        //判断返回结果中是否需要数据
        if (data != null) {
            //设置数据到result对象
            result.setData(data);
        }
        //设置其他值
        result.setCode(resultCodeEnum.getCode());
        result.setMessage(resultCodeEnum.getMessage());
        //返回设置值之后的对象
        return result;
    }

    //成功的方法
    public static <T> Result<T> ok(T data) {
        return build(data, ResultCodeEnum.SUCCESS);
    }

    /**
     * 登录类接口：首次注册待审核时 data 中带 {@link AuthConstant#REGISTER_PENDING_AUDIT_FLAG}，返回码 517
     */
    @SuppressWarnings("unchecked")
    public static <T> Result<T> okAuthBody(T data) {
        if (data instanceof Map<?, ?> raw) {
            if (Boolean.TRUE.equals(raw.get(AuthConstant.REGISTER_PENDING_AUDIT_FLAG))) {
                return (Result<T>) build((Map<String, Object>) raw, ResultCodeEnum.REGISTER_PENDING_AUDIT);
            }
        }
        return ok(data);
    }

    public static <T> Result<T> ok() {
        return build(null, ResultCodeEnum.SUCCESS);
    }

    public static <T> Result<T> build(Integer code, String message) {
        //创建Resullt对象，设置值，返回对象
        Result<T> result = new Result<>();
        //设置其他值
        result.setCode(code);
        result.setMessage(message);
        //返回设置值之后的对象
        return result;
    }

    //失败的方法
    public static <T> Result<T> fail(T data) {
        return build(data, ResultCodeEnum.FAIL);
    }
}
