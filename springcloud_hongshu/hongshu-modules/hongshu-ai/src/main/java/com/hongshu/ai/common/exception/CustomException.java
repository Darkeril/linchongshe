package com.hongshu.ai.common.exception;

import com.hongshu.common.core.enums.ResponseEnum;
import lombok.Data;

/**
 * 自定义抛出异常
 *
 * @author: myj
 * @date: 2020/3/4
 * @version: 1.0.0
 */
@Data
public class CustomException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private Integer code;
    private String msg;

    public CustomException() {
        this.code = ResponseEnum.ERROR.getCode();
        this.msg = ResponseEnum.ERROR.getMsg();
    }

    public CustomException(Integer code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public CustomException(ResponseEnum responseEnum) {
        super(responseEnum.getMsg());
        this.code = responseEnum.getCode();
        this.msg = responseEnum.getMsg();
    }

}
