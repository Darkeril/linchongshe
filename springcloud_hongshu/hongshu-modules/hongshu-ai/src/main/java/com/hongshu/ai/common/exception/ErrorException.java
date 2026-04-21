package com.hongshu.ai.common.exception;

import com.hongshu.common.core.enums.ResponseEnum;
import lombok.Data;

/**
 * 系统异常
 *
 * @author: myj
 * @date: 2020/3/4
 * @version: 1.0.0
 */
@Data
public class ErrorException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private Integer code;
    private String msg;

    public ErrorException() {
        this.code = ResponseEnum.ERROR.getCode();
        this.msg = ResponseEnum.ERROR.getMsg();
    }

    public ErrorException(String msg) {
        super(msg);
        this.code = ResponseEnum.ERROR.getCode();
        this.msg = msg;
    }

}
