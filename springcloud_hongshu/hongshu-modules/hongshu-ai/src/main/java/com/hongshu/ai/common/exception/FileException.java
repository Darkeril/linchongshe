package com.hongshu.ai.common.exception;

import com.hongshu.common.core.enums.ResponseEnum;
import lombok.Data;

/**
 * 文件处理异常
 *
 * @author: myj
 * @date: 2020/3/4
 * @version: 3.0.0
 */
@Data
public class FileException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private Integer code;
    private String msg;

    public FileException() {
        this.code = ResponseEnum.FILE_ERROR.getCode();
        this.msg = ResponseEnum.FILE_ERROR.getMsg();
    }

    public FileException(String msg) {
        super(msg);
        this.code = ResponseEnum.FILE_ERROR.getCode();
        this.msg = msg;
    }

}
