package com.hongshu.ai.common.annotation;

import com.hongshu.ai.common.constant.RedisConstant;
import com.hongshu.ai.common.enums.LimitTypeEnum;

import java.lang.annotation.*;

/**
 * 限流注解
 *
 * @author: myj
 * @date: 2023/10/20
 * @version: 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiter {

    /**
     * 限流key
     */
    public String key() default RedisConstant.RATE_LIMIT_KEY;

    /**
     * 限流时间,单位秒
     */
    public int time() default 60;

    /**
     * 限流次数
     */
    public int count() default 100;

    /**
     * 限流类型
     */
    public LimitTypeEnum limitType() default LimitTypeEnum.DEFAULT;
}
