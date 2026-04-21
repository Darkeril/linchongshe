package com.hongshu.ai.common.annotation;

import com.hongshu.ai.common.enums.BusinessTypeEnum;
import com.hongshu.ai.common.enums.OperatorTypeEnum;
import com.hongshu.common.core.constant.StringPoolConstant;

import java.lang.annotation.*;

/**
 * 系统日志 @SysLog(type = "1",value = "操作内容")
 *
 * @author: myj
 * @date: 2020/3/4
 * @version: 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    /**
     * 操作类型
     */
    public BusinessTypeEnum businessType() default BusinessTypeEnum.OTHER;

    /**
     * 操作人类别
     */
    public OperatorTypeEnum operatorType() default OperatorTypeEnum.MANAGE;

    /**
     * 模块名
     */
    String type() default StringPoolConstant.EMPTY;

    /**
     * 操作内容
     */
    String value() default StringPoolConstant.EMPTY;

    /**
     * 是否保存请求的参数
     */
    public boolean isSaveRequestData() default true;

    /**
     * 是否保存响应的参数
     */
    public boolean isSaveResponseData() default true;

}
